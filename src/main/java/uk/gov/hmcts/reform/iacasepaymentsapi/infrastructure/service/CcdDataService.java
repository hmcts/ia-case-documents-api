package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.S2STokenValidator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemUserProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State.APPEAL_STARTED_BY_ADMIN;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State.APPEAL_SUBMITTED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State.PENDING_PAYMENT;

@Service
@Slf4j
public class CcdDataService {

    private final CcdDataApi ccdDataApi;
    private final SystemTokenGenerator systemTokenGenerator;
    private final SystemUserProvider systemUserProvider;
    private final AuthTokenGenerator serviceAuthorization;

    private final S2STokenValidator s2sTokenValidator;

    public CcdDataService(CcdDataApi ccdDataApi,
                          SystemTokenGenerator systemTokenGenerator,
                          SystemUserProvider systemUserProvider,
                          AuthTokenGenerator serviceAuthorization,
                          S2STokenValidator s2STokenValidator) {
        this.ccdDataApi = ccdDataApi;
        this.systemTokenGenerator = systemTokenGenerator;
        this.systemUserProvider = systemUserProvider;
        this.serviceAuthorization = serviceAuthorization;
        this.s2sTokenValidator = s2STokenValidator;
    }

    public SubmitEventDetails updatePaymentStatus(CaseMetaData caseMetaData, boolean isWaysToPay, String s2sAuthToken) {
        s2sTokenValidator.checkIfServiceIsAllowed(s2sAuthToken);

        String event = caseMetaData.getEvent().toString();
        String caseId = String.valueOf(caseMetaData.getCaseId());
        String jurisdiction = caseMetaData.getJurisdiction();
        String caseType = caseMetaData.getCaseType();

        String userToken;
        String s2sToken;
        String uid;
        try {
            userToken = "Bearer " + systemTokenGenerator.generate();
            log.info("System user token has been generated for event: {}, caseId: {}.", event, caseId);

            s2sToken = serviceAuthorization.generate();
            log.info("S2S token has been generated for event: {}, caseId: {}.", event, caseId);

            uid = systemUserProvider.getSystemUserId(userToken);
            log.info("System user id has been fetched for event: {}, caseId: {}.", event, caseId);

        } catch (IdentityManagerResponseException ex) {

            log.error("Unauthorized access to getCaseById: " + ex.getMessage());
            throw new IdentityManagerResponseException(ex.getMessage(), ex);
        }

        // Get case details by Idxr
        final StartEventDetails startEventDetails = startEvent(userToken, s2sToken, uid, jurisdiction, caseType, caseId);
        log.info("Case details found for the caseId: {}", caseId);

        handleUpdatePaymentStatusValidation(startEventDetails.getCaseDetails(), caseId);

        if (!isPaymentReferenceExists(startEventDetails.getCaseDetails().getCaseData(),
                                      caseMetaData.getPaymentReference()) && !isWaysToPay) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Payment reference not found for the caseId: " + caseId);
        }

        // Update case payment status
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(PAYMENT_STATUS.value(), caseMetaData.getPaymentStatus());

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", caseMetaData.getEvent().toString());

        SubmitEventDetails submitEventDetails = submitEvent(userToken, s2sToken, caseId, caseData, eventData,
                                                            startEventDetails.getToken());

        log.info("Case payment status updated for the caseId: {}, Status: {}, Message: {}", caseId,
                 submitEventDetails.getCallbackResponseStatusCode(), submitEventDetails.getCallbackResponseStatus());

        return submitEventDetails;
    }

    private StartEventDetails startEvent(
        String userToken, String s2sToken, String uid, String jurisdiction, String caseType, String caseId) {

        return ccdDataApi.startEvent(userToken, s2sToken, uid, jurisdiction, caseType,
                                     caseId, Event.UPDATE_PAYMENT_STATUS.toString());
    }

    private SubmitEventDetails submitEvent(
        String userToken, String s2sToken, String caseId, Map<String, Object> data,
        Map<String, Object> eventData, String eventToken) {
        CaseDataContent request = new CaseDataContent(caseId, data, eventData, eventToken, true);
        return ccdDataApi.submitEvent(userToken, s2sToken, caseId, request);
    }

    private boolean isPaymentReferenceExists(AsylumCase asylumCase, String reference) {

        Optional<String> paymentReference = asylumCase.read(PAYMENT_REFERENCE, String.class);

        return paymentReference.isPresent() && paymentReference.get().equals(reference);
    }

    private void handleUpdatePaymentStatusValidation(CaseDetails<AsylumCase> caseDetails, String caseId) {
        State state = caseDetails.getState();
        AppealType appealType = caseDetails.getCaseData().read(APPEAL_TYPE, AppealType.class).orElseThrow(
            () -> new IllegalStateException("No appeal type in case data for case: " + caseId)
        );
        String paAppealTypePaymentOption = caseDetails.getCaseData().read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");
        String paAppealTypeAipPaymentOption = caseDetails.getCaseData().read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class).orElse("");
        List<State> validNonPaStates = List.of(APPEAL_STARTED, APPEAL_SUBMITTED, APPEAL_STARTED_BY_ADMIN, PENDING_PAYMENT);
        if ((!appealType.equals(AppealType.PA) || !(paAppealTypePaymentOption.equals("payLater") || paAppealTypeAipPaymentOption.equals("payLater")))
            && !validNonPaStates.contains(state)) {
            throw new IllegalStateException(appealType.getValue() + " appeal payment should not be made at " + state.toString() + " state for case: " + caseId);
        }
    }
}
