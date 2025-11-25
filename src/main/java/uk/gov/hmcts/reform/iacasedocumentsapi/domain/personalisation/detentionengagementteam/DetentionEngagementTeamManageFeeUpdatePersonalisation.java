package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetentionEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamManageFeeUpdatePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetainedManageFeeUpdateTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetentionEmailService detentionEmailService;
    private final PersonalisationProvider personalisationProvider;
    private String adaPrefix;
    private String nonAdaPrefix;


    public DetentionEngagementTeamManageFeeUpdatePersonalisation(
            @Value("${govnotify.template.det-email-template}") String internalDetainedManageFeeUpdateTemplateId,
            DetentionEmailService detentionEmailService,
            DocumentDownloadClient documentDownloadClient,
            PersonalisationProvider personalisationProvider
    ) {
        this.internalDetainedManageFeeUpdateTemplateId = internalDetainedManageFeeUpdateTemplateId;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return internalDetainedManageFeeUpdateTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        if (!isAppellantInDetention(asylumCase)) {
            return Collections.emptySet();
        }

        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_MANAGE_FEE_UPDATE_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", "IAFT - SERVE IN PERSON")
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalDetainedManageFeeUpdateLetterInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getInternalDetainedManageFeeUpdateLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained manage fee update letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal detained manage fee update letter in compatible format");
        }
    }
}
