package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_EDIT_APPEAL_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

@Slf4j
@Service
public class DetentionEngagementTeamEditAppealPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetEditAppealTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private String adaPrefix;
    private String nonAdaPrefix;

    public DetentionEngagementTeamEditAppealPersonalisation(
            @Value("${govnotify.template.editAppeal.detentionEngagementTeam.email}") String internalDetMaintainCaseUnlinkAppealTemplateId,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            PersonalisationProvider personalisationProvider, @Value("${govnotify.emailPrefix.adaByPost}") String adaPrefix,
            @Value("${govnotify.emailPrefix.nonAdaByPost}") String nonAdaPrefix
    ) {
        this.internalDetEditAppealTemplateId = internalDetMaintainCaseUnlinkAppealTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.personalisationProvider = personalisationProvider;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getTemplateId() {
        return internalDetEditAppealTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }

        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_EDIT_APPEAL_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalEditAppealDocumentInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getInternalEditAppealDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_EDIT_APPEAL_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Detained edit appeal document in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Detained edit appeal document in compatible format");
        }
    }
}
