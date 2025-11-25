package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
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
public class DetentionEngagementTeamHomeOfficeUploadAdditionalAddendumEvidencePersonalisation implements EmailWithLinkNotificationPersonalisation {

    @Value("${govnotify.emailPrefix.adaInPerson}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String nonAdaPrefix;
    private final String homeOfficeUploadAdditionalAddendumEvidenceTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;

    public DetentionEngagementTeamHomeOfficeUploadAdditionalAddendumEvidencePersonalisation(
            @Value("${govnotify.template.homeOfficeUploadAdditionalAddendumEvidence.detentionEngagementTeam.email}") String homeOfficeUploadAdditionalAddendumEvidenceTemplateId,
            PersonalisationProvider personalisationProvider,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.homeOfficeUploadAdditionalAddendumEvidenceTemplateId = homeOfficeUploadAdditionalAddendumEvidenceTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return homeOfficeUploadAdditionalAddendumEvidenceTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_HOME_OFFICE_UPLOAD_ADDITIONAL_EVIDENCE_DET_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("documentLink", getHomeOfficeUploadAdditionalAddendumEvidenceDocumentInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getHomeOfficeUploadAdditionalAddendumEvidenceDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Home Office upload additional/addendum evidence letter in compatible format", e);
            throw new IllegalStateException("Failed to get Home Office upload additional/addendum evidence letter in compatible format");
        }
    }

}
