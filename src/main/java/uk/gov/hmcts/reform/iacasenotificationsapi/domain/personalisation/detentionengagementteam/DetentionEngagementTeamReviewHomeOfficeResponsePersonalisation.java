package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REVIEW_OUTCOME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

import java.io.IOException;
import java.util.*;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealReviewOutcome;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;


@Slf4j
@Service
public class DetentionEngagementTeamReviewHomeOfficeResponsePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String homeOfficeAppealReviewMaintainedDocumentName = "Home Office Response";
    private final String homeOfficeAppealReviewWithdrawnDocumentName = "Withdrawal Letter";
    private final String internalDetainedReviewHomeOfficeResponseTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;

    private String subjectPrefix;

    public DetentionEngagementTeamReviewHomeOfficeResponsePersonalisation(
            @Value("${govnotify.template.reviewHomeOfficeResponse.detentionEngagementTeam.email}") String internalDetainedReviewHomeOfficeResponseTemplateId,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            @Value("${govnotify.emailPrefix.nonAdaByPost}") String subjectPrefix,
            PersonalisationProvider personalisationProvider
    ) {
        this.internalDetainedReviewHomeOfficeResponseTemplateId = internalDetainedReviewHomeOfficeResponseTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.subjectPrefix = subjectPrefix;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return internalDetainedReviewHomeOfficeResponseTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (detentionFacility.isEmpty() || detentionFacility.get().equals("other")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detEmailService.getDetEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_REVIEW_HOME_OFFICE_RESPONSE_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", subjectPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentName", getDocumentName(asylumCase))
                .put("documentLink", getInternalDetainedReviewHomeOfficeResponseLetterInJsonObject(asylumCase))
                .build();
    }

    private String getDocumentName(AsylumCase asylumCase) {
        AppealReviewOutcome appealReviewOutcome =  asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)
                .orElseThrow(() -> new IllegalStateException("Appeal review outcome is not present"));

        return appealReviewOutcome.equals(AppealReviewOutcome.DECISION_MAINTAINED)
                ? homeOfficeAppealReviewMaintainedDocumentName
                : homeOfficeAppealReviewWithdrawnDocumentName;
    }

    private JSONObject getInternalDetainedReviewHomeOfficeResponseLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained review Home Office response letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal detained review Home Office response letter in compatible format");
        }
    }
}
