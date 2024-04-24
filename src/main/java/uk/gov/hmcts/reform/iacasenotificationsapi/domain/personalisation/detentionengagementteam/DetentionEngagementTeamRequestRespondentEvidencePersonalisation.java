package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.detentionengagementteam;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;

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
public class DetentionEngagementTeamRequestRespondentEvidencePersonalisation implements EmailWithLinkNotificationPersonalisation {

    private final String internalDetainedRequestCaseBuildingTemplateId;
    private final DocumentDownloadClient documentDownloadClient;
    private final String detainedPrefix;
    private final DetEmailService detEmailService;

    public DetentionEngagementTeamRequestRespondentEvidencePersonalisation(
            @Value("${govnotify.template.requestRespondentEvidenceDirection.detentionEngagementTeam.email.nonAda}") String templateIdDetained,
            @Value("${govnotify.emailPrefix.nonAdaByPost}") String detainedPrefix,
            DetEmailService detEmailService,
            DocumentDownloadClient documentDownloadClient
    ) {
        this.internalDetainedRequestCaseBuildingTemplateId = templateIdDetained;
        this.detainedPrefix = detainedPrefix;
        this.detEmailService = detEmailService;
        this.documentDownloadClient = documentDownloadClient;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return internalDetainedRequestCaseBuildingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return detEmailService.getRecipientsList(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_DET_REQUEST_RESPONDENT_EVIDENCE_EMAIL";
    }

    @Override
    public Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, Object>builder()
                .put("subjectPrefix", detainedPrefix)
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("documentLink", getRequestRespondentEvidenceDocumentInJsonObject(asylumCase))
                .build();
    }

    private JSONObject getRequestRespondentEvidenceDocumentInJsonObject(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments = asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS);
        List<DocumentWithMetadata> documents = maybeDocuments
                .orElse(Collections.emptyList())
                .stream()
                .map(IdValue::getValue)
                .filter(document -> document.getTag() == DocumentTag.INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER)
                .collect(Collectors.toList());

        if (documents.size() == 0) {
            throw new RequiredFieldMissingException("Request respondent evidence document is not present");
        }
        DocumentWithMetadata document = documents.get(0);
        try {
            return documentDownloadClient.getJsonObjectFromDocument(document);
        } catch (IOException | NotificationClientException e) {
            log.error("Failed to get Request respondent evidence document in compatible format", e);
            throw new IllegalStateException("Failed to get Request respondent evidence document in compatible format");
        }
    }
}
