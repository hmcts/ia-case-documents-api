package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_CHANGE_HEARING_CENTRE_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

@Slf4j
@Service
public class DetentionEngagementTeamChangeHearingCentrePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamEndAppealTemplateId;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;

    @Value("${govnotify.emailPrefix.adaByPost}")
    private String adaPrefix;

    @Value("${govnotify.emailPrefix.nonAdaByPost}")
    private String nonAdaPrefix;

    public DetentionEngagementTeamChangeHearingCentrePersonalisation(
            @Value("${govnotify.template.changeHearingCentre.detentionEngagementTeam.email}") String detentionEngagementTeamChangeHearingCentreTemplateId,
            DetEmailService detEmailService,
            PersonalisationProvider personalisationProvider, DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamEndAppealTemplateId = detentionEngagementTeamChangeHearingCentreTemplateId;
        this.detEmailService = detEmailService;
        this.personalisationProvider = personalisationProvider;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_CHANGE_HEARING_CENTRE_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamEndAppealTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap.<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getHearingCentreChangedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getHearingCentreChangedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_CHANGE_HEARING_CENTRE_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Change Hearing Centre Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Change Hearing Centre Letter in compatible format");
        }
    }
}
