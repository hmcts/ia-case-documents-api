package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerEditDocumentsPersonalisation implements EmailNotificationPersonalisation {

    private final String appealDocumentDeletedTemplateId;
    private final EmailAddressFinder emailAddressFinder;
    private final EditDocumentService editDocumentService;

    public CaseOfficerEditDocumentsPersonalisation(
        @NotNull(message = "appealDocumentDeletedTemplateId cannot be null")
        @Value("${govnotify.template.appealDocumentDeleted.caseOfficer.email}") String appealDocumentDeletedTemplateId,
        EmailAddressFinder emailAddressFinder, EditDocumentService editDocumentService) {

        this.appealDocumentDeletedTemplateId = appealDocumentDeletedTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.editDocumentService = editDocumentService;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_DOCUMENT_DELETED";
    }

    @Override
    public String getTemplateId() {
        return appealDocumentDeletedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getEmailAddress(asylumCase));
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        return ImmutableMap.<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(
                AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(StringUtils.EMPTY))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(StringUtils.EMPTY))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(StringUtils.EMPTY))
            .put("legalRepReferenceNumber",
                asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(StringUtils.EMPTY))
            .put("homeOfficeReferenceNumber",
                asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("reasonForEditingOrDeletingDocuments", getReasonFromCaseNoteDescription(asylumCase))
            .put("editedOrDeletedDocumentList",
                getEditedOrDeletedDocumentList(asylumCase, callback.getCaseDetailsBefore().orElse(null)))
            .build();
    }

    private String getEditedOrDeletedDocumentList(AsylumCase asylumCase, CaseDetails<AsylumCase> caseDetailsBefore) {
        if (caseDetailsBefore == null) {
            return StringUtils.EMPTY;
        }
        return editDocumentService.getFormattedDocumentsGivenCaseAndDocIds(caseDetailsBefore.getCaseData(),
            getCaseNoteDocIdsFromCaseNote(asylumCase)).toString();
    }

    private List<String> getCaseNoteDocIdsFromCaseNote(AsylumCase asylumCase) {
        String caseNoteDescription = getCaseNoteDescriptionFromCaseNote(asylumCase);
        String[] temp = StringUtils.substringBetween(caseNoteDescription, "documentIds: [", "]")
            .split(",");
        return Stream.of(temp).map(String::trim).collect(Collectors.toList());
    }

    private String getCaseNoteDescriptionFromCaseNote(AsylumCase asylumCase) {
        Optional<List<IdValue<CaseNote>>> caseNotesOptional = asylumCase.read(CASE_NOTES);
        if (caseNotesOptional.isPresent()) {
            List<IdValue<CaseNote>> caseNotes = caseNotesOptional.get();
            int latestCaseNote = 0;
            return caseNotes.get(latestCaseNote).getValue().getCaseNoteDescription();
        }
        return StringUtils.EMPTY;
    }

    private String getReasonFromCaseNoteDescription(AsylumCase asylumCase) {
        String caseNoteDescription = getCaseNoteDescriptionFromCaseNote(asylumCase);
        return StringUtils.substringAfter(caseNoteDescription, "reason:").trim();
    }

}
