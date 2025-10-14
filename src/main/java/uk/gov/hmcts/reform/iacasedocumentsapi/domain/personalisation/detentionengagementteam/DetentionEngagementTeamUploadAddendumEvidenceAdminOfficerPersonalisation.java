package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentTag.INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DetEmailService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.DocumentDownloadClient;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class DetentionEngagementTeamUploadAddendumEvidenceAdminOfficerPersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalUploadAddendumEvidenceTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;
    private final PersonalisationProvider personalisationProvider;
    @Value("${govnotify.emailPrefix.adaByPost}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAdaByPost}")
    private String nonAdaPrefix;

    public DetentionEngagementTeamUploadAddendumEvidenceAdminOfficerPersonalisation(
        @Value("${govnotify.template.uploadAddendumEvidenceAdminOfficer.detentionEngagementTeam.email}") String internalUploadAddendumEvidenceTemplateId,
        DetEmailService detEmailService,
        DocumentDownloadClient documentDownloadClient,
        PersonalisationProvider personalisationProvider
    ) {
        this.internalUploadAddendumEvidenceTemplateId = internalUploadAddendumEvidenceTemplateId;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return internalUploadAddendumEvidenceTemplateId;
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
        return caseId + "_INTERNAL_DET_UPLOAD_ADDENDUM_EVIDENCE_ADMIN_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, Object>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
            .put("documentLink", getInternalUploadAddendumEvidenceDocumentInJsonObject(asylumCase))
            .build();
    }

    private JSONObject getInternalUploadAddendumEvidenceDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Internal Upload addendum evidence letter in compatible format", e);
            throw new IllegalStateException("Failed to get Internal Upload addendum evidence letter in compatible format");
        }
    }

}
