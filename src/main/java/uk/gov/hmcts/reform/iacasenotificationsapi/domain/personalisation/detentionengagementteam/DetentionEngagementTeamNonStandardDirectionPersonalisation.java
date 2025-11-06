package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
public class DetentionEngagementTeamNonStandardDirectionPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String sendNonStandardDirectionTemplateId;
    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String nonAdaPrefix;
    private final DetentionEmailService detentionEmailService;
    private final DocumentDownloadClient documentDownloadClient;
    private final PersonalisationProvider personalisationProvider;

    public DetentionEngagementTeamNonStandardDirectionPersonalisation(
            @Value("${govnotify.template.nonStandardDirectionInternal.detentionEngagementTeam.email}")
            String sendNonStandardDirectionTemplateId,
            DetentionEmailService detentionEmailService,
            DocumentDownloadClient documentDownloadClient,
            PersonalisationProvider personalisationProvider
    ) {
        this.sendNonStandardDirectionTemplateId = sendNonStandardDirectionTemplateId;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_NON_STANDARD_DIRECTION_DET";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }
        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getTemplateId() {
        return sendNonStandardDirectionTemplateId;
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", nonAdaPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getAppealDecidedLetterJsonObject(asylumCase))
                .build();
    }

    private JSONObject getAppealDecidedLetterJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get internalNonStandardDirectionToAppellantLetter in compatible format", e);
            throw new IllegalStateException("Failed to get internalNonStandardDirectionToAppellantLetter in compatible format");
        }
    }
}
