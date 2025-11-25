package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.editbaildocuments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@Service
public class EditBailDocumentService {

    public List<String> getFormattedDocumentsGivenCaseAndDocNames(BailCase bailCaseBefore,
                                                                           BailCase bailCaseAfter,
                                                                           List<String> docNamesFromCaseNote) {

        List<String> documentsList = new ArrayList<>();

        getListOfDocumentFields().forEach(fieldDefinition -> {

            Optional<List<IdValue<HasDocument>>> maybeDocumentCollectionBefore = bailCaseBefore.read(fieldDefinition);
            if (maybeDocumentCollectionBefore.isPresent()) {
                List<IdValue<HasDocument>> docs = maybeDocumentCollectionBefore.get();
                docs.forEach(doc -> addToListIfMatch(docNamesFromCaseNote, documentsList, doc.getValue()));

                Optional<List<IdValue<HasDocument>>> maybeDocumentCollectionAfter = bailCaseAfter.read(fieldDefinition);
                if (maybeDocumentCollectionAfter.isPresent()) {
                    List<IdValue<HasDocument>> addedDocs = removeDocsWithSameId(maybeDocumentCollectionAfter.get(),
                        maybeDocumentCollectionBefore.get());
                    addedDocs.forEach(doc -> addToListIfMatch(docNamesFromCaseNote, documentsList, doc.getValue()));
                }
            }

        });
        return documentsList;
    }


    private void addToListIfMatch(List<String> docNamesFromCaseNote,
                                  List<String> documentsList,
                                  HasDocument doc) {

        String documentName = doc.getDocument().getDocumentFilename();
        if (docNamesFromCaseNote.contains(documentName)) {
            documentsList.add(documentName);
        }
    }

    private List<BailCaseFieldDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA,
            BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA,
            BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA);
    }

    private List<IdValue<HasDocument>> removeDocsWithSameId(List<IdValue<HasDocument>> minuend,
                                                            List<IdValue<HasDocument>> subtrahend) {
        List<String> subtrahendIds = subtrahend.stream()
            .map(IdValue::getId)
            .collect(Collectors.toList());

        return minuend.stream()
            .filter(idValue -> !subtrahendIds.contains(idValue.getId()))
            .collect(Collectors.toList());
    }

}
