package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.editbaildocuments;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class EditBailDocumentServiceTest {

    private final EditBailDocumentService editBailDocumentService = new EditBailDocumentService();

    private static Object[] generateScenarios() {
        BailCase bailCase = new BailCase();
        BailCase bailCaseBefore = new BailCase();
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

        bailCaseBefore.write(BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA, Arrays.asList(idDoc, idDoc2, idDoc3));
        bailCase.write(BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA, Arrays.asList(idDoc, idDoc2, idDoc4));


        List<String> editedDocsInCaseNote = List.of("documentToBeUpdated", "documentToBeDeleted", "addedDocument");


        return new Object[] {
            new Object[] {
                bailCase,
                bailCaseBefore,
                editedDocsInCaseNote,
                List.of("documentToBeUpdated", "documentToBeDeleted", "addedDocument")
            }
        };
    }

    private static IdValue<DocumentWithMetadata> getDocumentWithMetadata(String docId, String filename,
                                                                         String description) {
        DocumentWithMetadata docWithMetadata = new DocumentWithMetadata(buildDocument(docId, filename), description,
            LocalDate.now().toString(), DocumentTag.NONE);
        return new IdValue<>(docId, docWithMetadata);
    }

    private static IdValue<HearingRecordingDocument> getHearingRecordingDocument() {
        HearingRecordingDocument hearingRecordingDocument =
            new HearingRecordingDocument(buildDocument("id3", "some hearing doc name"),
                "some hearing desc");
        return new IdValue<>("1", hearingRecordingDocument);
    }

    private static Document buildDocument(String docId, String filename) {
        String documentUrl = "http://dm-store/" + docId;
        return new Document(documentUrl, documentUrl + "/binary", filename);
    }

    @ParameterizedTest
    @MethodSource({"generateScenarios"})
    public void getFormattedDocumentsGivenCaseAndDocIds(BailCase bailCase,
                                                        BailCase bailCaseBefore,
                                                        List<String> docNamesFromCaseNote,
                                                        List<String> expectedDocumentsList
    ) {
        List<String> documents =
            editBailDocumentService.getFormattedDocumentsGivenCaseAndDocNames(bailCaseBefore, bailCase, docNamesFromCaseNote);

        assertThat(documents.toString()).isEqualTo(expectedDocumentsList.toString());
    }

}
