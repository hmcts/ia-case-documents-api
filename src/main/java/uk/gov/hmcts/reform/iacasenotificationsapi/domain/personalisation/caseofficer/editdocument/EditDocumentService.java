package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HasDocument;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class EditDocumentService {

    public FormattedDocumentList getFormattedDocumentsGivenCaseAndDocNames(AsylumCase asylumCase,
                                                                           List<String> docNamesFromCaseNote) {
        List<FormattedDocument> formattedDocList = new ArrayList<>();
        getListOfDocumentFields().forEach(fieldDefinition -> {
            Optional<List<IdValue<HasDocument>>> fieldOptional = asylumCase.read(fieldDefinition);
            if (fieldOptional.isPresent()) {
                List<IdValue<HasDocument>> docs = fieldOptional.get();
                docs.forEach(doc -> addToListIfMatch(docNamesFromCaseNote, formattedDocList, doc.getValue()));
            }
        });
        return new FormattedDocumentList(formattedDocList);
    }


    private void addToListIfMatch(List<String> docNamesFromCaseNote,
                                  List<FormattedDocument> formattedDocList,
                                  HasDocument doc) {

        if (docNamesFromCaseNote.contains(doc.getDocument().getDocumentFilename())) {
            formattedDocList.add(
                new FormattedDocument(
                    doc.getDocument().getDocumentFilename(),
                    doc.getDescription()
                ));
        }
    }

    private List<AsylumCaseDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            ADDITIONAL_EVIDENCE_DOCUMENTS,
            TRIBUNAL_DOCUMENTS,
            HEARING_DOCUMENTS,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            ADDENDUM_EVIDENCE_DOCUMENTS,
            RESPONDENT_DOCUMENTS,
            DRAFT_DECISION_AND_REASONS_DOCUMENTS,
            FINAL_DECISION_AND_REASONS_DOCUMENTS,
            HEARING_RECORDING_DOCUMENTS);
    }

}
