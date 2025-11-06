package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.END_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamEndAppealPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamEndAppealTemplateId;
    private final DetentionEmailService detentionEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;

    @Value("${govnotify.emailPrefix.adaInPerson}")
    private String adaSubjectPrefix;

    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String nonAdaPrefix;

    public DetentionEngagementTeamEndAppealPersonalisation(
            @Value("${govnotify.template.endAppeal.detentionEngagementTeam.email}") String detentionEngagementTeamEndAppealTemplateId,
            DetentionEmailService detentionEmailService,
            PersonalisationProvider personalisationProvider, DocumentDownloadClient documentDownloadClient
    ) {
        this.detentionEngagementTeamEndAppealTemplateId = detentionEngagementTeamEndAppealTemplateId;
        this.detentionEmailService = detentionEmailService;
        this.personalisationProvider = personalisationProvider;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_END_APPEAL_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamEndAppealTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap.<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaSubjectPrefix : nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, END_APPEAL));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal End Appeal decision Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal End Appeal decision Letter in compatible format");
        }
    }
}
