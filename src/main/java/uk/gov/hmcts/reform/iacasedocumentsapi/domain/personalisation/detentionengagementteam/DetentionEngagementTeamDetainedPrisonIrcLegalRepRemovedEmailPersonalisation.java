package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadSystemUserClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.DETAINED_LEGAL_REP_REMOVED_IRC_PRISON_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

@Slf4j
@Service
public class DetentionEngagementTeamDetainedPrisonIrcLegalRepRemovedEmailPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamTemplateId;
    private final String nonAdaPrefix;
    private final DetentionEmailService detentionEmailService;
    private final DocumentDownloadSystemUserClient documentDownloadSystemUserClient;

    public DetentionEngagementTeamDetainedPrisonIrcLegalRepRemovedEmailPersonalisation(
            @Value("${govnotify.template.appealSubmitted.detentionEngagementTeam.email}") String detentionEngagementTeamTemplateId,
            @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
            DetentionEmailService detentionEmailService,
            DocumentDownloadSystemUserClient documentDownloadSystemUserClient
    ) {
        this.detentionEngagementTeamTemplateId = detentionEngagementTeamTemplateId;
        this.nonAdaPrefix = nonAdaPrefix;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadSystemUserClient = documentDownloadSystemUserClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_LEGAL_REP_REMOVED";
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
            return documentDownloadSystemUserClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, DETAINED_LEGAL_REP_REMOVED_IRC_PRISON_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get detainedLegalRepRemovedIrcPrisonLetter in compatible format", e);
            throw new IllegalStateException("Failed to get detainedLegalRepRemovedIrcPrisonLetter in compatible format");
        }
    }
}
