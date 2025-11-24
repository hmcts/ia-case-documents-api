package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_IRC_PRISON_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

@Slf4j
@Service
public class DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamTemplateId;
    private final String nonAdaPrefix;
    private final DetentionEmailService detentionEmailService;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamInternalCaseDetainedPrisonIrcSubmitAppealOutOfTimeWithRemissionEmailPersonalisation(
            @Value("${govnotify.template.appealSubmitted.detentionEngagementTeam.email}") String detentionEngagementTeamTemplateId,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
            DetentionEmailService detentionEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamTemplateId = detentionEngagementTeamTemplateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_ADA_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_REMISSION";
    }


    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getTemplateId() {
        return detentionEngagementTeamTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", nonAdaPrefix)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("documentLink", getAppealSubmittedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getAppealSubmittedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_IRC_PRISON_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Appeal submission Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Appeal submission Letter in compatible format");
        }
    }
}
