package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalReview24wTimeframeLetterBundlerTest {

    private InternalReview24wTimeframeLetterBundler handler;
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

    private final String fileExtension = "PDF";
    private final String fileName = "some-file-name";

    @BeforeEach
    public void setUp() {

        handler =
                new InternalReview24wTimeframeLetterBundler(
                        fileExtension,
                        fileName,
                        true,
                        fileNameQualifier,
                        documentBundler,
                        documentHandler);
    }

    @Test
    void dispatch_priority_is_late() {
        assertEquals(DispatchPriority.LATE, handler.getDispatchPriority());
    }


    @Test
    void canHandle_throws_for_null() {
        assertThatThrownBy(() -> handler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void canHandle_true_for_remove_24w_timeframe_event_internal() {
        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_false_for_emDisabled() {
        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        handler = new InternalReview24wTimeframeLetterBundler(
                fileExtension,
                fileName,
                false,
                fileNameQualifier,
                documentBundler,
                documentHandler);

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_false_for_complete_case_review_event_non_internal() {
        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"COMPLETE_CASE_REVIEW"}, mode = EnumSource.Mode.EXCLUDE)
    void canHandle_false_for_invalid_event_internal(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_throws_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    void should_read_and_bundle_letter_notification_documents_appellant_representing() {
        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> coverLetter = new IdValue<>("1", createDocumentWithMetadata(STF_24WEEKS_CASE_REVIEW_APPELLANT_DOCUMENT));
        //STF_24WEEKS_CASE_REVIEW_APPELLANT_LETTER
        IdValue<DocumentWithMetadata> removalDoc = new IdValue<>("2", createDocumentWithMetadata(STF_24WEEKS_CASE_REVIEW_APPELLANT_LETTER));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(coverLetter, removalDoc)));

        when(documentBundler.bundleWithoutContentsOrCoverSheets(
                anyList(),
                eq("Letter bundle documents"),
                eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(documentBundler, times(1)).bundleWithoutContentsOrCoverSheets(
                List.of(coverLetter.getValue(), removalDoc.getValue()),
                "Letter bundle documents",
                "filename"
        );
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, STF_24WEEKS_CASE_REVIEW_LETTER_BUNDLE
        );

    }

    private Document createDocumentWithDescription() {
        return
                new Document("some-url",
                        "some-binary-url",
                        RandomStringUtils.secure().nextAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag) {

        return
                new DocumentWithMetadata(createDocumentWithDescription(),
                        RandomStringUtils.secure().nextAlphabetic(20),
                        new SystemDateProvider().now().toString(), documentTag, "test");

    }
}