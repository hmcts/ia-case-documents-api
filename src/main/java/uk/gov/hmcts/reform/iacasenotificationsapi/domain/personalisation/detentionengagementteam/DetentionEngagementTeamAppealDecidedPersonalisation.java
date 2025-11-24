package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_ALLOWED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DET_DECISION_AND_REASONS_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamAppealDecidedPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamUploadAppealDecidedDismissedTemplateId;
    private final String detentionEngagementTeamUploadAppealDecidedAllowedTemplateId;
    private final DetentionEmailService detEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    @Value("${govnotify.emailPrefix.adaInPerson}")
    private String adaSubjectPrefix;

    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String iaftSubjectPrefix;

    public DetentionEngagementTeamAppealDecidedPersonalisation(
            @Value("${govnotify.template.appealDecided.detentionTeam.dismissed.email}") String detentionEngagementTeamUploadAppealDecidedDismissedTemplateId,
            @Value("${govnotify.template.appealDecided.detentionTeam.allowed.email}") String detentionEngagementTeamUploadAppealDecidedAllowedTemplateId,
            DetentionEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamUploadAppealDecidedDismissedTemplateId = detentionEngagementTeamUploadAppealDecidedDismissedTemplateId;
        this.detentionEngagementTeamUploadAppealDecidedAllowedTemplateId = detentionEngagementTeamUploadAppealDecidedAllowedTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_APPEAL_DECIDED_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return getAppealDecisionFrom(asylumCase).equals(AppealDecision.DISMISSED)
                ? detentionEngagementTeamUploadAppealDecidedDismissedTemplateId : detentionEngagementTeamUploadAppealDecidedAllowedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }

        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);
        if (detentionFacility.isEmpty() || detentionFacility.get().equals("other")) {
            return Collections.emptySet();
        }

        return Collections.singleton(detEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        ImmutableMap.Builder<String, Object> immutableMap = ImmutableMap.<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaSubjectPrefix : iaftSubjectPrefix)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase));

        if (getAppealDecisionFrom(asylumCase).equals(AppealDecision.DISMISSED)) {
            immutableMap.put("formName", resolveFormNameAndLink(asylumCase).getLeft());
            immutableMap.put("formLinkText", resolveFormNameAndLink(asylumCase).getRight());
        }

        return immutableMap.build();
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DET_DECISION_AND_REASONS_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Appeal decision Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Appeal decision Letter in compatible format");
        }
    }

    private ImmutablePair<String, String> resolveFormNameAndLink(AsylumCase asylumCase) {
        final String adaFormName = "IAFT-ADA5: Ask for permission to appeal to the Upper Tribunal (Immigration and Asylum Chamber) – Accelerated detained appeal (ADA)";
        final String iaftFormName = "IAFT-DE5: Ask for permission to appeal to the Upper Tribunal (Immigration and Asylum Chamber) – Detained appeal";
        final String formLinkPreText = "This form can be found here: ";
        final String adaFormLink = "http://www.gov.uk/government/publications/ask-for-permission-to-appeal-to-the-upper-tribunal-immigration-and-asylum-chamber-accelerated-detained-appeal-form-iaftada5";
        final String iaftFormLink = "https://www.gov.uk/government/publications/ask-for-permission-to-appeal-to-the-upper-tribunal-immigration-and-asylum-chamber-detained-appeal-form-iaft-de5";

        if (isAcceleratedDetainedAppeal(asylumCase)) {
            return new ImmutablePair<>(adaFormName, formLinkPreText + adaFormLink);
        } else {
            return new ImmutablePair<>(iaftFormName, formLinkPreText + iaftFormLink);
        }
    }

    private AppealDecision getAppealDecisionFrom(AsylumCase asylumCase) {
        return asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)
                        .orElseThrow(() -> new RequiredFieldMissingException("Appeal decision is missing."));
    }

}
