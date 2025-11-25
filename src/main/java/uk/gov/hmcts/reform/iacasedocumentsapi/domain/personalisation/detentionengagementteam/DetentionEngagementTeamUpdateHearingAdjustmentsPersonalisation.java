package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamUpdateHearingAdjustmentsPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalUpdateHearingAdjustmentsTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private String adaPrefix;
    private String nonAdaPrefix;

    public DetentionEngagementTeamUpdateHearingAdjustmentsPersonalisation(
        @Value("${govnotify.template.updateHearingAdjustments.detentionEngagementTeam.email}") String internalUpdateHearingAdjustmentsTemplateId,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient,
        @Value("${govnotify.emailPrefix.adaInPerson}") String adaPrefix,
        @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix
    ) {
        this.internalUpdateHearingAdjustmentsTemplateId = internalUpdateHearingAdjustmentsTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
    }

    @Override
    public String getTemplateId() {

        return internalUpdateHearingAdjustmentsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {

        return caseId + "_INTERNAL_HEARING_ADJUSTMENTS_CHANGED_DET";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, Object>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("documentLink", getInternalUpdateHearingAdjustmentsDocumentInJsonObject(asylumCase))
            .build();
    }

    private JSONObject getInternalUpdateHearingAdjustmentsDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Hearing Adjustments changed document in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Hearing Adjustments changed document in compatible format");
        }
    }
}
