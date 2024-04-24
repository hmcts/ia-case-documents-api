package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Service
@Slf4j
public class DetentionEngagementTeamHearingBundleReadyPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String detHearingBundleReadyTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private String adaPrefix;
    private String nonAdaPrefix;
    private final DetEmailService detEmailService;

    public DetentionEngagementTeamHearingBundleReadyPersonalisation(
        @Value("${govnotify.template.hearingBundleReady.detentionEngagementTeam.email}") String detHearingBundleReadyTemplateId,
        @Value("${govnotify.emailPrefix.adaInPerson}") String adaPrefix,
        @Value("${govnotify.emailPrefix.nonAdaInPerson}") String nonAdaPrefix,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient
    ) {
        this.detHearingBundleReadyTemplateId = detHearingBundleReadyTemplateId;
        this.adaPrefix = adaPrefix;
        this.nonAdaPrefix = nonAdaPrefix;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId() {
        return detHearingBundleReadyTemplateId;
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
        return caseId + "_HEARING_BUNDLE_IS_READY_INTERNAL_DET_EMAIL";
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
            .put("documentLink", getHearingBundleReadyLetterInJsonObject(asylumCase))
            .build();
    }

    private JSONObject getHearingBundleReadyLetterInJsonObject(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeNotificationAttachmentDocuments = asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS);
        List<DocumentWithMetadata> documents = maybeNotificationAttachmentDocuments
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(document -> document.getTag() == DocumentTag.HEARING_BUNDLE_READY_LETTER)
                .collect(Collectors.toList());

        if (documents.size() == 0) {
            throw new RequiredFieldMissingException("Hearing Bundle ready letter is not available");
        }
        DocumentWithMetadata document = documents.get(0);
        try {
            return documentDownloadClient.getJsonObjectFromDocument(document);
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Hearing bundle ready letter in compatible format", e);
            throw new IllegalStateException("Failed to get Hearing bundle ready letter in compatible format");
        }
    }
}
