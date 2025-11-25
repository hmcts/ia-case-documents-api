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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_APPEAL_DETAINED_UPDATE_TRIBUNAL_DECISION_RULE_31_IRC_PRISON_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;

@Slf4j
@Service
public class DetentionEngagementTeamUpdateTribunalDecisionRule31IrcPrisonPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalUpdateTribunalDecisionRule31IrcPrisonTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetentionEmailService detentionEmailService;

    public DetentionEngagementTeamUpdateTribunalDecisionRule31IrcPrisonPersonalisation(
        @Value("${govnotify.template.detained-iaft-5-email-template}") String internalUpdateTribunalDecisionRule31IrcPrisonTemplateId,
        DetentionEmailService detentionEmailService,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.internalUpdateTribunalDecisionRule31IrcPrisonTemplateId = internalUpdateTribunalDecisionRule31IrcPrisonTemplateId;
        this.detentionEmailService = detentionEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId() {

        return internalUpdateTribunalDecisionRule31IrcPrisonTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        String email = detentionEmailService.getDetentionEmailAddress(asylumCase);
        return Collections.singleton(detentionEmailService.getDetentionEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DETAINED_UPDATE_TRIBUNAL_DECISION_RULE_31_IRC_PRISON";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, Object>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("documentLink", getInternalUpdateTribunalDecisionRule31IrcPrisonDocumentInJsonObject(asylumCase))
            .build();
    }

    private JSONObject getInternalUpdateTribunalDecisionRule31IrcPrisonDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_APPEAL_DETAINED_UPDATE_TRIBUNAL_DECISION_RULE_31_IRC_PRISON_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Update Tribunal Decision changed document in compatible format", e);
            throw new IllegalStateException("Failed to get Update Tribunal Decision changed document in compatible format");
        }
    }
}
