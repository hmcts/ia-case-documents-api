package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalCaseListedMultiDocBundlerTest {

    private InternalCaseListedMultiDocBundler multiDocBundler;

    @Mock
    private InternalCaseListedAppellantLetterBundler appellantLetterBundler;

    @Mock
    private InternalCaseListedLegalRepLetterBundler legalRepLetterBundler;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @BeforeEach
    public void setUp() {
        multiDocBundler = new InternalCaseListedMultiDocBundler(
            appellantLetterBundler,
            legalRepLetterBundler
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_throw_when_callback_stage_is_null() {
        assertThatThrownBy(() -> multiDocBundler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throw_when_callback_is_null() {
        assertThatThrownBy(() -> multiDocBundler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_true_when_only_appellant_bundler_can_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);

        boolean result = multiDocBundler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertTrue(result);
    }

    @Test
    void should_return_true_when_only_legal_rep_bundler_can_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);

        boolean result = multiDocBundler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertTrue(result);
    }

    @Test
    void should_return_true_when_both_bundlers_can_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);

        boolean result = multiDocBundler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertTrue(result);
    }

    @Test
    void should_return_false_when_neither_bundler_can_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);

        boolean result = multiDocBundler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(result);
    }

    @Test
    void should_handle_with_only_appellant_bundler_when_only_it_can_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);

        AsylumCase resultCase = new AsylumCase();
        PreSubmitCallbackResponse<AsylumCase> appellantResponse = new PreSubmitCallbackResponse<>(resultCase);
        when(appellantLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(appellantResponse);

        PreSubmitCallbackResponse<AsylumCase> response = multiDocBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(resultCase, response.getData());
        verify(appellantLetterBundler, times(1)).handle(ABOUT_TO_SUBMIT, callback);
        verify(legalRepLetterBundler, never()).handle(any(), any());
    }

    @Test
    void should_handle_with_only_legal_rep_bundler_when_only_it_can_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);

        AsylumCase resultCase = new AsylumCase();
        PreSubmitCallbackResponse<AsylumCase> legalRepResponse = new PreSubmitCallbackResponse<>(resultCase);
        when(legalRepLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(legalRepResponse);

        PreSubmitCallbackResponse<AsylumCase> response = multiDocBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(resultCase, response.getData());
        verify(legalRepLetterBundler, times(1)).handle(ABOUT_TO_SUBMIT, callback);
        verify(appellantLetterBundler, never()).handle(any(), any());
    }

    @Test
    void should_handle_both_concurrently_and_merge_when_both_can_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);

        // Create asylum cases with documents
        AsylumCase appellantCase = new AsylumCase();
        IdValue<DocumentWithMetadata> appellantDoc = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE));
        appellantCase.write(LETTER_BUNDLE_DOCUMENTS, List.of(appellantDoc));

        AsylumCase legalRepCase = new AsylumCase();
        IdValue<DocumentWithMetadata> legalRepDoc = new IdValue<>("2", createDocumentWithMetadata(DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE));
        legalRepCase.write(LETTER_BUNDLE_DOCUMENTS, List.of(legalRepDoc));

        PreSubmitCallbackResponse<AsylumCase> appellantResponse = new PreSubmitCallbackResponse<>(appellantCase);
        PreSubmitCallbackResponse<AsylumCase> legalRepResponse = new PreSubmitCallbackResponse<>(legalRepCase);

        when(appellantLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(appellantResponse);
        when(legalRepLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(legalRepResponse);

        PreSubmitCallbackResponse<AsylumCase> response = multiDocBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        AsylumCase mergedCase = response.getData();
        assertNotNull(mergedCase);

        // Verify both handlers were called
        verify(appellantLetterBundler, times(1)).handle(ABOUT_TO_SUBMIT, callback);
        verify(legalRepLetterBundler, times(1)).handle(ABOUT_TO_SUBMIT, callback);

        // Verify documents were merged
        Optional<List<IdValue<DocumentWithMetadata>>> documents = mergedCase.read(LETTER_BUNDLE_DOCUMENTS);
        assertTrue(documents.isPresent());
        assertEquals(2, documents.get().size(), "Should contain documents from both handlers");
    }

    @Test
    void should_merge_list_fields_correctly() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);

        // Create asylum cases with multiple documents
        AsylumCase appellantCase = new AsylumCase();
        IdValue<DocumentWithMetadata> appellantDoc1 = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE));
        IdValue<DocumentWithMetadata> appellantDoc2 = new IdValue<>("2", createDocumentWithMetadata(DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE));
        appellantCase.write(LETTER_BUNDLE_DOCUMENTS, List.of(appellantDoc1, appellantDoc2));

        AsylumCase legalRepCase = new AsylumCase();
        IdValue<DocumentWithMetadata> legalRepDoc1 = new IdValue<>("3", createDocumentWithMetadata(DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE));
        IdValue<DocumentWithMetadata> legalRepDoc2 = new IdValue<>("4", createDocumentWithMetadata(DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE));
        legalRepCase.write(LETTER_BUNDLE_DOCUMENTS, List.of(legalRepDoc1, legalRepDoc2));

        PreSubmitCallbackResponse<AsylumCase> appellantResponse = new PreSubmitCallbackResponse<>(appellantCase);
        PreSubmitCallbackResponse<AsylumCase> legalRepResponse = new PreSubmitCallbackResponse<>(legalRepCase);

        when(appellantLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(appellantResponse);
        when(legalRepLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(legalRepResponse);

        PreSubmitCallbackResponse<AsylumCase> response = multiDocBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        AsylumCase mergedCase = response.getData();

        // Verify all documents are present
        Optional<List<IdValue<DocumentWithMetadata>>> documents = mergedCase.read(LETTER_BUNDLE_DOCUMENTS);
        assertTrue(documents.isPresent());
        assertEquals(4, documents.get().size(), "Should contain all documents from both handlers");
    }

    @Test
    void should_preserve_non_list_fields_from_both_cases() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);

        // Create asylum cases with different fields
        AsylumCase appellantCase = new AsylumCase();
        appellantCase.put("field1", "value1");
        appellantCase.put("field2", "value2");

        AsylumCase legalRepCase = new AsylumCase();
        legalRepCase.put("field3", "value3");
        legalRepCase.put("field2", "value2_updated");

        PreSubmitCallbackResponse<AsylumCase> appellantResponse = new PreSubmitCallbackResponse<>(appellantCase);
        PreSubmitCallbackResponse<AsylumCase> legalRepResponse = new PreSubmitCallbackResponse<>(legalRepCase);

        when(appellantLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(appellantResponse);
        when(legalRepLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(legalRepResponse);

        PreSubmitCallbackResponse<AsylumCase> response = multiDocBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        AsylumCase mergedCase = response.getData();

        // Verify fields are preserved
        assertEquals("value1", mergedCase.get("field1"));
        assertEquals("value2_updated", mergedCase.get("field2")); // Should take the value from second case
        assertEquals("value3", mergedCase.get("field3"));
    }

    @Test
    void should_throw_when_cannot_handle() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);

        assertThatThrownBy(() -> multiDocBundler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_cannot_handle_at_wrong_stage() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_START, callback)).thenReturn(false);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_START, callback)).thenReturn(false);

        assertThatThrownBy(() -> multiDocBundler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_return_false_for_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(false);

        boolean result = multiDocBundler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertFalse(result);
    }

    @Test
    void should_handle_empty_list_fields_correctly() {
        when(appellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);
        when(legalRepLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback)).thenReturn(true);

        // Appellant case has documents, legal rep case has empty list
        AsylumCase appellantCase = new AsylumCase();
        IdValue<DocumentWithMetadata> appellantDoc = new IdValue<>("1", createDocumentWithMetadata(DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE));
        appellantCase.write(LETTER_BUNDLE_DOCUMENTS, List.of(appellantDoc));

        AsylumCase legalRepCase = new AsylumCase();
        legalRepCase.write(LETTER_BUNDLE_DOCUMENTS, List.of());

        PreSubmitCallbackResponse<AsylumCase> appellantResponse = new PreSubmitCallbackResponse<>(appellantCase);
        PreSubmitCallbackResponse<AsylumCase> legalRepResponse = new PreSubmitCallbackResponse<>(legalRepCase);

        when(appellantLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(appellantResponse);
        when(legalRepLetterBundler.handle(ABOUT_TO_SUBMIT, callback)).thenReturn(legalRepResponse);

        PreSubmitCallbackResponse<AsylumCase> response = multiDocBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        AsylumCase mergedCase = response.getData();

        Optional<List<IdValue<DocumentWithMetadata>>> documents = mergedCase.read(LETTER_BUNDLE_DOCUMENTS);
        assertTrue(documents.isPresent());
        assertEquals(1, documents.get().size(), "Should contain document from appellant case only");
    }

    private Document createDocument() {
        return new Document(
            "http://document-url/" + RandomStringUtils.randomAlphabetic(10),
            "http://binary-url/" + RandomStringUtils.randomAlphabetic(10),
            RandomStringUtils.randomAlphabetic(20) + ".pdf"
        );
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag tag) {
        return new DocumentWithMetadata(
            createDocument(),
            RandomStringUtils.randomAlphabetic(20),
            new SystemDateProvider().now().toString(),
            tag,
            "suppliedBy"
        );
    }
}