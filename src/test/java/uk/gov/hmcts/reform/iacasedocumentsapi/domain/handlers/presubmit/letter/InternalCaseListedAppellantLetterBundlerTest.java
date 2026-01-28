package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalCaseListedAppellantLetterBundlerTest {

    private InternalCaseListedAppellantLetterBundler detainedCaseListedLetterHandler;
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

        detainedCaseListedLetterHandler =
            new InternalCaseListedAppellantLetterBundler(
                fileExtension,
                fileName,
                true,
                fileNameQualifier,
                documentBundler,
                documentHandler);
    }

    @ParameterizedTest
    @MethodSource("generateDifferentEventScenarios")
    public void it_can_handle_callback(InternalCaseListedAppellantLetterBundlerTest.TestScenario scenario) {
        when(callback.getEvent()).thenReturn(scenario.getEvent());
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        boolean canHandle = detainedCaseListedLetterHandler.canHandle(scenario.callbackStage, callback);

        assertEquals(canHandle, scenario.isExpected());
    }

    private static List<InternalCaseListedAppellantLetterBundlerTest.TestScenario> generateDifferentEventScenarios() {
        return InternalCaseListedAppellantLetterBundlerTest.TestScenario.builder();
    }

    @Value
    static class TestScenario {
        Event event;
        PreSubmitCallbackStage callbackStage;
        boolean expected;

        public static List<InternalCaseListedAppellantLetterBundlerTest.TestScenario> builder() {
            List<InternalCaseListedAppellantLetterBundlerTest.TestScenario> testScenarios = new ArrayList<>();
            for (Event e : Event.values()) {
                if (e.equals(Event.LIST_CASE)) {
                    testScenarios.add(new InternalCaseListedAppellantLetterBundlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new InternalCaseListedAppellantLetterBundlerTest.TestScenario(e, ABOUT_TO_SUBMIT, true));
                } else {
                    testScenarios.add(new InternalCaseListedAppellantLetterBundlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new InternalCaseListedAppellantLetterBundlerTest.TestScenario(e, ABOUT_TO_SUBMIT, false));
                    testScenarios.add(new InternalCaseListedAppellantLetterBundlerTest.TestScenario(e, ABOUT_TO_START, false));
                    testScenarios.add(new InternalCaseListedAppellantLetterBundlerTest.TestScenario(e, ABOUT_TO_SUBMIT, false));
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
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        detainedCaseListedLetterHandler =
            new InternalCaseListedAppellantLetterBundler(
                fileExtension,
                fileName,
                false,
                fileNameQualifier,
                documentBundler,
                documentHandler);

        boolean canHandle = detainedCaseListedLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    public void it_should_not_handle_callback_when_not_admin_and_not_detained_and_mobile_available() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(MOBILE_NUMBER, String.class)).thenReturn(Optional.of("07983000000"));

        detainedCaseListedLetterHandler =
                new InternalCaseListedAppellantLetterBundler(
                        fileExtension,
                        fileName,
                        true,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);

        boolean canHandle = detainedCaseListedLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    public void it_should_not_handle_callback_when_not_admin_and_not_detained_and_email_available() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(EMAIL, String.class)).thenReturn(Optional.of("test@test.com"));

        detainedCaseListedLetterHandler =
                new InternalCaseListedAppellantLetterBundler(
                        fileExtension,
                        fileName,
                        true,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);

        boolean canHandle = detainedCaseListedLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    public void it_should_handle_callback_when_not_admin_and_not_detained_and_mobile_email_unavailable() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        detainedCaseListedLetterHandler =
                new InternalCaseListedAppellantLetterBundler(
                        fileExtension,
                        fileName,
                        true,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);

        boolean canHandle = detainedCaseListedLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }


    @Test
    public void it_should_not_handle_callback_when_detained_not_in_other() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        detainedCaseListedLetterHandler =
            new InternalCaseListedAppellantLetterBundler(
                fileExtension,
                fileName,
                true,
                fileNameQualifier,
                documentBundler,
                documentHandler);

        boolean canHandle = detainedCaseListedLetterHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertFalse(canHandle);
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_for_internal_lr() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> doc1 = new IdValue<>("1", createDocumentWithMetadata());
        IdValue<DocumentWithMetadata> doc2 = new IdValue<>("2", createDocumentWithMetadata());

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(doc1, doc2)));
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            anyList(),
            eq("Letter bundle documents"),
            eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = detainedCaseListedLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE
        );
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_for_internal_aip() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> doc1 = new IdValue<>("1", createDocumentWithMetadata());
        IdValue<DocumentWithMetadata> doc2 = new IdValue<>("2", createDocumentWithMetadata());

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(doc1, doc2)));
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            anyList(),
            eq("Letter bundle documents"),
            eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = detainedCaseListedLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE
        );
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_for_digital_lr() {
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> doc1 = new IdValue<>("1", createDocumentWithMetadata());
        IdValue<DocumentWithMetadata> doc2 = new IdValue<>("2", createDocumentWithMetadata());

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(doc1, doc2)));
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            anyList(),
            eq("Letter bundle documents"),
            eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = detainedCaseListedLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE
        );
    }

    @Test
    void set_to_late_dispatch() {
        assertThat(detainedCaseListedLetterHandler.getDispatchPriority()).isEqualTo(LATE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        assertThatThrownBy(() -> detainedCaseListedLetterHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> detainedCaseListedLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    private Document createDocumentWithDescription() {
        return
            new Document("some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata() {

        return
            new DocumentWithMetadata(createDocumentWithDescription(),
                RandomStringUtils.randomAlphabetic(20),
                new SystemDateProvider().now().toString(), DocumentTag.INTERNAL_CASE_LISTED_LETTER,"test");

    }
}
