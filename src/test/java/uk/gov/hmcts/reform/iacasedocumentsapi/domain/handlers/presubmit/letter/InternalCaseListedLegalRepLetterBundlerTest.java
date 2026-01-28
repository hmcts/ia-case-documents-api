package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_CASE_LISTED_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalCaseListedLegalRepLetterBundlerTest {

    private InternalCaseListedLegalRepLetterBundler internalCaseListedLrLetterHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock
    private DocumentBundler documentBundler;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Document bundleDocument;

    private String fileExtension = "PDF";
    private String fileName = "some-file-name";

    @BeforeEach
    public void setUp() {

        internalCaseListedLrLetterHandler =
            new InternalCaseListedLegalRepLetterBundler(
                fileExtension,
                fileName,
                true,
                fileNameQualifier,
                documentBundler,
                documentHandler);
    }

    @ParameterizedTest
    @MethodSource("generateDifferentEventScenarios")
    public void it_can_handle_callback(InternalCaseListedLegalRepLetterBundlerTest.TestScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        boolean canHandle = internalCaseListedLrLetterHandler.canHandle(scenario.callbackStage, callback);

        assertEquals(canHandle, scenario.isExpected());
    }

    private static List<InternalCaseListedLegalRepLetterBundlerTest.TestScenario> generateDifferentEventScenarios() {
        return InternalCaseListedLegalRepLetterBundlerTest.TestScenario.builder();
    }

    @Value
    static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean expected;

        public static List<InternalCaseListedLegalRepLetterBundlerTest.TestScenario> builder() {
            List<InternalCaseListedLegalRepLetterBundlerTest.TestScenario> testScenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.LIST_CASE)) {
                    testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_SUBMIT, true));
                } else {
                    testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_SUBMIT, false));
                    testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new InternalCaseListedLegalRepLetterBundlerTest.TestScenario(e, ABOUT_TO_SUBMIT, false));
                }
            }
            return testScenarios;
        }
    }

    @Test
    public void it_should_not_handle_callback_when_stitching_flag_is_false() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        internalCaseListedLrLetterHandler =
            new InternalCaseListedLegalRepLetterBundler(
                fileExtension,
                fileName,
                false,
                fileNameQualifier,
                documentBundler,
                documentHandler);

        boolean canHandle = internalCaseListedLrLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_read_and_bundle_letter_notification_documents() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> docID1 = new IdValue<>("1", createDocumentWithMetadata(INTERNAL_CASE_LISTED_LETTER));
        IdValue<DocumentWithMetadata> docID2 = new IdValue<>("2", createDocumentWithMetadata(INTERNAL_CASE_LISTED_LETTER));
        IdValue<DocumentWithMetadata> docID3 = new IdValue<>("3", createDocumentWithMetadata(INTERNAL_CASE_LISTED_LR_LETTER));
        IdValue<DocumentWithMetadata> docID4 = new IdValue<>("4", createDocumentWithMetadata(INTERNAL_CASE_LISTED_LR_LETTER));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(docID1, docID2, docID3, docID4)));

        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            anyList(),
            eq("Letter bundle documents"),
            eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = internalCaseListedLrLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE)
        );
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            eq(asylumCase), any(Document.class), eq(LETTER_BUNDLE_DOCUMENTS), eq(DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE)
        );
    }

    @Test
    void set_to_late_dispatch() {
        assertThat(internalCaseListedLrLetterHandler.getDispatchPriority()).isEqualTo(LATE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertThatThrownBy(() -> internalCaseListedLrLetterHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> internalCaseListedLrLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private Document createDocumentWithDescription() {
        return
            new Document("some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag) {

        return
            new DocumentWithMetadata(createDocumentWithDescription(),
                RandomStringUtils.randomAlphabetic(20),
                new SystemDateProvider().now().toString(), documentTag,"test");

    }
}
