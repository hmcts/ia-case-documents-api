package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REVIEW_OUTCOME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamUploadAppealResponsePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private static final String HOME_OFFICE_RESPONSE = "Home Office Response";
    private static final String WITHDRAWAL_LETTER = "Withdrawal Letter";
    private static final String WITHDRAW = "decisionWithdrawn";

    private final CustomerServicesProvider customerServicesProvider;
    private final String detentionEngagementTeamUploadAppealResponseTemplateId;
    private final String adaPrefix;
    private final DetEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamUploadAppealResponsePersonalisation(
        @Value("${govnotify.template.homeOfficeResponseUploaded.detentionEngagementTeam.email}") String detentionEngagementTeamUploadAppealResponseTemplateId,
        @Value("${govnotify.emailPrefix.adaInPerson}") String adaPrefix,
        CustomerServicesProvider customerServicesProvider,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamUploadAppealResponseTemplateId = detentionEngagementTeamUploadAppealResponseTemplateId;
        this.adaPrefix = adaPrefix;
        this.customerServicesProvider = customerServicesProvider;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId() {
        return detentionEngagementTeamUploadAppealResponseTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_HO_RESPONSE_DETENTION_ENGAGEMENT_TEAM";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");


        String appealReviewOutcome = getAppealReviewOutcome(asylumCase);
        String documentDownloadTitle = appealReviewOutcome.equals(WITHDRAW) ? WITHDRAWAL_LETTER : HOME_OFFICE_RESPONSE;

        return ImmutableMap
            .<String, Object>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", adaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("documentLink", appealReviewOutcome.equals(WITHDRAW) ? new JSONObject() : getAppealResponseLetter(asylumCase))
            .put("documentName", documentDownloadTitle)
            .build();
    }

    private String getAppealReviewOutcome(AsylumCase asylumCase) {
        AppealReviewOutcome appealReviewOutcome = asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)
            .orElseThrow(() -> new IllegalStateException("Appeal review outcome is not present"));

        return appealReviewOutcome.toString();
    }

    private JSONObject getAppealResponseLetter(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalRespondentDocuments = asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS);
        DocumentWithMetadata document = optionalRespondentDocuments
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getTag() == DocumentTag.UPLOAD_THE_APPEAL_RESPONSE)
            .findFirst().orElseThrow(() -> new IllegalStateException("Appeal response letter not available"));

        try {
            return documentDownloadClient.getJsonObjectFromDocument(document);
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Appeal response letter in compatible format", e);
            throw new IllegalStateException("Failed to get Appeal response letter in compatible format");
        }
    }
}

