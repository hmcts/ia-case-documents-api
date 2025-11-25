package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamTransferOutOfAdaPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetainedTransferOutOfAdaTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    private String subjectPrefix;


    public DetentionEngagementTeamTransferOutOfAdaPersonalisation(
            @Value("${govnotify.template.transferOutOfAda.detentionEngagementTeam.email}") String internalDetainedTransferOutOfAdaTemplateId,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient,
            @Value("${govnotify.emailPrefix.nonAdaByPost}") String subjectPrefix,
            PersonalisationProvider personalisationProvider
    ) {
        this.internalDetainedTransferOutOfAdaTemplateId = internalDetainedTransferOutOfAdaTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.subjectPrefix = subjectPrefix;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return internalDetainedTransferOutOfAdaTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_TRANSFERRED_OUT_OF_ADA_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", subjectPrefix)
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("documentLink", getInternalDetainedTransferOutOfAdaLetterInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getInternalDetainedTransferOutOfAdaLetterInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal detained transferred out of ADA letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal detained transferred out of ADA letter in compatible format");
        }
    }
}
