package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.editbaildocuments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class EditBailDocumentServiceTest {

    @Mock
    private BailCase bailCase;
    @Mock
    private BailCase bailCaseBefore;

    private final EditBailDocumentService editBailDocumentService = new EditBailDocumentService();

    private static IdValue<DocumentWithMetadata> getDocumentWithMetadata(String docId, String filename,
                                                                         String description) {
        DocumentWithMetadata docWithMetadata = new DocumentWithMetadata(
            buildDocument(docId, filename), description,
            LocalDate.now().toString(), DocumentTag.NONE
        );
        return new IdValue<>(docId, docWithMetadata);
    }

    private static Document buildDocument(String docId, String filename) {
        String documentUrl = "http://dm-store/" + docId;
        return new Document(documentUrl, documentUrl + "/binary", filename);
    }

    @Test
    public void getFormattedDocumentsGivenCaseAndDocIds() {
        IdValue<DocumentWithMetadata> idDoc = getDocumentWithMetadata(
            "1", "document", "document left untouched");
        IdValue<DocumentWithMetadata> idDoc2 = getDocumentWithMetadata(
            "2", "documentToBeUpdated", "document getting updated");
        IdValue<DocumentWithMetadata> idDoc2updated = getDocumentWithMetadata(
            "2", "documentToBeUpdated", "document that got updated");
        IdValue<DocumentWithMetadata> idDoc3 = getDocumentWithMetadata(
            "3", "documentToBeDeleted", "document getting deleted");
        IdValue<DocumentWithMetadata> idDoc4 = getDocumentWithMetadata(
            "4", "addedDocument", "document getting added");

        when(bailCaseBefore.read(BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.of(Arrays.asList(idDoc, idDoc2, idDoc3)));
        when(bailCaseBefore.read(BailCaseFieldDefinition.HOME_OFFICE_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.empty());
        when(bailCaseBefore.read(BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA))
            .thenReturn(Optional.of(Arrays.asList(idDoc, idDoc2, idDoc4)));

        List<String> editedDocsInCaseNote = List.of("documentToBeUpdated", "documentToBeDeleted", "addedDocument");
        List<String> expectedDocumentsList = List.of("documentToBeUpdated", "documentToBeDeleted", "addedDocument");
        List<String> documents =
            editBailDocumentService.getFormattedDocumentsGivenCaseAndDocNames(
                bailCaseBefore,
                bailCase,
                editedDocsInCaseNote
            );

        assertThat(documents.toString()).isEqualTo(expectedDocumentsList.toString());
    }

}
