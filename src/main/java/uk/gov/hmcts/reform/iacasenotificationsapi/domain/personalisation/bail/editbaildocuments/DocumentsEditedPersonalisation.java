package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.editbaildocuments;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;

public abstract class DocumentsEditedPersonalisation implements BailEmailNotificationPersonalisation {

    protected final EditBailDocumentService editBailDocumentService;

    protected DocumentsEditedPersonalisation(EditBailDocumentService editBailDocumentService) {
        this.editBailDocumentService = editBailDocumentService;
    }

    protected List<String> getEditedOrDeletedOrAddedDocumentList(BailCase bailCase, CaseDetails<BailCase> caseDetailsBefore) {
        if (caseDetailsBefore == null) {
            return Collections.emptyList();
        }
        return editBailDocumentService.getFormattedDocumentsGivenCaseAndDocNames(caseDetailsBefore.getCaseData(),
            bailCase, getDocNamesFromCaseNote(bailCase));
    }

    protected List<String> getDocNamesFromCaseNote(BailCase bailCase) {
        String caseNoteDescription = getCaseNoteDescriptionFromCaseNote(bailCase);
        String[] temp = caseNoteDescription.isEmpty()
            ? new String[]{}
            : StringUtils.substringBetween(caseNoteDescription, "Document names: [", "]").split(",");
        return Stream.of(temp).map(String::trim).collect(Collectors.toList());
    }

    protected String getCaseNoteDescriptionFromCaseNote(BailCase bailCase) {
        Optional<List<IdValue<CaseNote>>> caseNotesOptional = bailCase.read(CASE_NOTES);
        if (caseNotesOptional.isPresent()) {
            List<IdValue<CaseNote>> caseNotes = caseNotesOptional.get();
            int latestCaseNote = 0;
            return caseNotes.get(latestCaseNote).getValue().getCaseNoteDescription();
        }
        return StringUtils.EMPTY;
    }

    protected String getReasonFromCaseNoteDescription(BailCase bailCase) {
        String caseNoteDescription = getCaseNoteDescriptionFromCaseNote(bailCase);
        return StringUtils.substringAfter(caseNoteDescription, "Reason:").trim();
    }
}
