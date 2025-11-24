package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DET_MARK_AS_ADA_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
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

@Slf4j
@Service
public class DetentionEngagementTeamMarkAppealAsAdaPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String markAppealAsAdaTemplateId;
    private final String adaPrefix;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;

    public DetentionEngagementTeamMarkAppealAsAdaPersonalisation(
            @Value("${govnotify.template.markAppealAsAda.detentionEngagementTeam.email}") String markAppealAsAdaTemplateId,
            @Value("${govnotify.emailPrefix.adaByPost}") String adaPrefix,
            DetEmailService detEmailService,
            PersonalisationProvider personalisationProvider,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.markAppealAsAdaTemplateId = markAppealAsAdaTemplateId;
        this.adaPrefix = adaPrefix;
        this.detEmailService = detEmailService;
        this.personalisationProvider = personalisationProvider;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_MARK_APPEAL_AS_ADA";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getTemplateId() {
        return markAppealAsAdaTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", adaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getMarkAsAdaLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getMarkAsAdaLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DET_MARK_AS_ADA_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal 'Mark appeal as ADA' Letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal 'Mark appeal as ADA' Letter in compatible format");
        }
    }
}
