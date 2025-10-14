package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

@Slf4j
@Service
public class DetentionEngagementTeamChangeHoDirectionDueDatePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detentionEngagementTeamChangeHoDirectionDueDateTemplateId;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;


    private final String adaPrefix;


    private final String nonAdaPrefix;

    public DetentionEngagementTeamChangeHoDirectionDueDatePersonalisation(
            @Value("${govnotify.template.changeDirectionDueDateOfHomeOffice.detentionEngagementTeam.email}") String detentionEngagementTeamChangeHoDirectionDueDateTemplateId,
            DetEmailService detEmailService,
            PersonalisationProvider personalisationProvider,
            DocumentDownloadClient documentDownloadClient,
            @Value("${govnotify.emailPrefix.adaByPost}") String adaPrefix,
            @Value("${govnotify.emailPrefix.nonAdaByPost}") String nonAdaPrefix) {
        this.detentionEngagementTeamChangeHoDirectionDueDateTemplateId = detentionEngagementTeamChangeHoDirectionDueDateTemplateId;
        this.detEmailService = detEmailService;
        this.personalisationProvider = personalisationProvider;
        this.documentDownloadClient = documentDownloadClient;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_CHANGE_HO_DIRECTION_DUE_DATE_EMAIL";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return detentionEngagementTeamChangeHoDirectionDueDateTemplateId;
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
                .put("documentLink", getHoDirectionDueDateChangedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getHoDirectionDueDateChangedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, DocumentTag.INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Change HO Direction Due Date Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Change HO Direction Due Date Letter in compatible format");
        }
    }
}
