package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLetterForNotification;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInDetention;

import java.io.IOException;
import java.util.*;
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
public class DetentionEngagementTeamLegalOfficerUploadAddendumEvidence implements EmailWithLinkNotificationPersonalisation {

    @Value("${govnotify.emailPrefix.adaInPerson}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAdaInPerson}")
    private String nonAdaPrefix;
    private final String legalOfficerUploadAddendumEvidenceTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final DocumentDownloadClient documentDownloadClient;
    private final DetEmailService detEmailService;

    public DetentionEngagementTeamLegalOfficerUploadAddendumEvidence(
            @Value("${govnotify.template.hoOrTcwUploadedAddendumEvidence.detentionEngagementTeam.email}") String legalOfficerUploadAddendumEvidenceTemplateId,
            PersonalisationProvider personalisationProvider,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.legalOfficerUploadAddendumEvidenceTemplateId = legalOfficerUploadAddendumEvidenceTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalOfficerUploadAddendumEvidenceTemplateId;
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
        return caseId + "_INTERNAL_DETAINED_LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_DET_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .putAll(personalisationProvider.getAppellantPersonalisation(asylumCase))
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("documentLink", getLegalOfficerUploadAddendumEvidenceDocumentInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getLegalOfficerUploadAddendumEvidenceDocumentInJsonObject(AsylumCase asylumCase) {
        try {
            return documentDownloadClient.getJsonObjectFromDocument(getLetterForNotification(asylumCase, LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER));
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Legal Officer upload addendum evidence letter in compatible format", e);
            throw new IllegalStateException("Failed to get Legal Officer upload addendum evidence letter in compatible format");
        }
    }

}
