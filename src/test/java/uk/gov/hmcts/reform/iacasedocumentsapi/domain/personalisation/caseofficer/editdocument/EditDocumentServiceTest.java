package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument.EditDocumentService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument.FormattedDocument;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument.FormattedDocumentList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingRecordingDocument;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class EditDocumentServiceTest {

    private final EditDocumentService editDocumentService = new EditDocumentService();

    private static Object[] generateOneFileEditedScenarios() {
        AsylumCase asylumCase = new AsylumCase();
        IdValue<DocumentWithMetadata> idDoc = getDocumentWithMetadata(
            "id1", "some name", "some desc");
        IdValue<DocumentWithMetadata> idDoc2 = getDocumentWithMetadata(
            "id2", "some other name", "some other desc");
        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, Arrays.asList(idDoc, idDoc2));

        List<String> doc2IsEditedInCaseNote = Collections.singletonList("some other name");
        List<String> doc1IsEditedInCaseNote = Collections.singletonList("some name");

        FormattedDocument expectedFormattedDocumentIsDoc2 =
            new FormattedDocument("some other name", "some other desc");

        FormattedDocument expectedFormattedDocumentIsDoc1 =
            new FormattedDocument("some name", "some desc");

        return new Object[] {
            new Object[] {
                asylumCase,
                doc2IsEditedInCaseNote,
                new FormattedDocumentList(Collections.singletonList(expectedFormattedDocumentIsDoc2))
            },
            new Object[] {
                asylumCase,
                doc1IsEditedInCaseNote,
                new FormattedDocumentList(Collections.singletonList(expectedFormattedDocumentIsDoc1))
            }
        };
    }

    private static Object[] generateMultipleFilesEditedScenarios() {
        AsylumCase asylumCase = new AsylumCase();
        IdValue<DocumentWithMetadata> idDoc = getDocumentWithMetadata(
            "id1", "some name", "some desc");
        IdValue<DocumentWithMetadata> idDoc2 = getDocumentWithMetadata(
            "id2", "some other name", "some other desc");
        asylumCase.write(LEGAL_REPRESENTATIVE_DOCUMENTS, Arrays.asList(idDoc, idDoc2));

        IdValue<HearingRecordingDocument> idDoc3 = getHearingRecordingDocument(
        );
        asylumCase.write(HEARING_RECORDING_DOCUMENTS, Collections.singletonList(idDoc3));

        List<String> doc1AndDoc2AreEditedInCaseNote = Arrays.asList("some name", "some other name");
        List<String> doc1AndDoc2AndDoc3AreEditedInCaseNote =
            Arrays.asList("some name", "some other name", "some hearing doc name");

        FormattedDocument expectedFormattedDocumentIsDoc2 =
            new FormattedDocument("some other name", "some other desc");

        FormattedDocument expectedFormattedDocumentIsDoc1 =
            new FormattedDocument("some name", "some desc");

        FormattedDocument expectedFormattedDocumentIsDoc3 =
            new FormattedDocument("some hearing doc name", "some hearing desc");

        return new Object[] {
            new Object[] {
                asylumCase,
                doc1AndDoc2AreEditedInCaseNote,
                new FormattedDocumentList(
                    Arrays.asList(
                        expectedFormattedDocumentIsDoc1,
                        expectedFormattedDocumentIsDoc2
                    ))
            },
            new Object[] {
                asylumCase,
                doc1AndDoc2AndDoc3AreEditedInCaseNote,
                new FormattedDocumentList(
                    Arrays.asList(
                        expectedFormattedDocumentIsDoc1,
                        expectedFormattedDocumentIsDoc2,
                        expectedFormattedDocumentIsDoc3
                    ))
            }
        };
    }

    private static IdValue<DocumentWithMetadata> getDocumentWithMetadata(String docId, String filename,
                                                                         String description) {
        DocumentWithMetadata docWithMetadata = new DocumentWithMetadata(buildDocument(docId, filename), description,
            LocalDate.now().toString(), DocumentTag.NONE);
        return new IdValue<>("1", docWithMetadata);
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
    @MethodSource({"generateOneFileEditedScenarios", "generateMultipleFilesEditedScenarios"})
    public void getFormattedDocumentsGivenCaseAndDocIds(AsylumCase asylumCase,
                                                        List<String> docNamesFromCaseNote,
                                                        FormattedDocumentList expectedFormattedDocumentList
    ) {
        FormattedDocumentList actualFormattedDocumentList =
            editDocumentService.getFormattedDocumentsGivenCaseAndDocNames(asylumCase, docNamesFromCaseNote);

        assertThat(actualFormattedDocumentList.toString()).isEqualTo(expectedFormattedDocumentList.toString());
    }

}
