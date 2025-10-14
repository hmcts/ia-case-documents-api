package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_DET_MARK_AS_PAID_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamMarkAsPaidPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetMarkAsPaidTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private String detPrefix;

    public DetentionEngagementTeamMarkAsPaidPersonalisation(
        @Value("${govnotify.template.markAppealAsPaid.detentionEngagementTeam.email.nonAda}") String internalDetMarkAsPaidTemplateId,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient,
        @Value("${govnotify.emailPrefix.nonAdaByPost}") String detPrefix
    ) {
        this.internalDetMarkAsPaidTemplateId = internalDetMarkAsPaidTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.detPrefix = detPrefix;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return internalDetMarkAsPaidTemplateId;
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
        return caseId + "_INTERNAL_DET_MARK_APPEAL_AS_PAID_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, Object>builder()
            .put("subjectPrefix", detPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("documentLink", getInternalAdaSuitabilityDocumentInJsonObject(asylumCase))
            .build();
    }

    private JSONObject getInternalAdaSuitabilityDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_DET_MARK_AS_PAID_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Detained Mark As Paid document in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Detained Mark As Paid document in compatible format");
        }
    }
}
