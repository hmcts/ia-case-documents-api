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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_LR;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalRemove24wTimeframeLetterBundlerTest {

    private InternalRemove24wTimeframeLetterBundler handler;
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

        handler =
            new InternalRemove24wTimeframeLetterBundler(
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
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class)).thenReturn(Optional.of("string"));

        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_false_for_emDisabled() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class)).thenReturn(Optional.of("string"));
        handler = new InternalRemove24wTimeframeLetterBundler(
            fileExtension,
            fileName,
            false,
            fileNameQualifier,
            documentBundler,
            documentHandler);

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_false_for_remove_24w_timeframe_event_non_internal() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_false_for_remove_24w_timeframe_event_no_case_review_date() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class)).thenReturn(Optional.empty());

        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REMOVE_STATUTORY_TIMEFRAME_24_WEEKS"}, mode = EnumSource.Mode.EXCLUDE)
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
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class)).thenReturn(Optional.of("string"));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> coverLetter = new IdValue<>("1", createDocumentWithMetadata(STF_24WEEKS_REMOVAL_DECISION_LETTER));
        IdValue<DocumentWithMetadata> removalDoc = new IdValue<>("2", createDocumentWithMetadata(STF_24WEEKS_REMOVAL_DECISION_DOCUMENT));
        IdValue<DocumentWithMetadata> coverLrLetter = new IdValue<>("3", createDocumentWithMetadata(STF_24WEEKS_REMOVAL_DECISION_LETTER_LR));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(coverLetter, removalDoc, coverLrLetter)));

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
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE
        );
        verify(documentBundler, never()).bundleWithoutContentsOrCoverSheets(
            List.of(coverLrLetter.getValue(), removalDoc.getValue()),
            "Letter bundle documents",
            "filename"
        );
        verify(documentHandler, never()).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE
        );
    }

    @Test
    void should_read_and_bundle_letter_notification_documents_legal_rep_representing() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(COMPLETE_CASE_REVIEW_DATE, String.class)).thenReturn(Optional.of("string"));
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> coverLetter = new IdValue<>("1", createDocumentWithMetadata(STF_24WEEKS_REMOVAL_DECISION_LETTER));
        IdValue<DocumentWithMetadata> removalDoc = new IdValue<>("2", createDocumentWithMetadata(STF_24WEEKS_REMOVAL_DECISION_DOCUMENT));
        IdValue<DocumentWithMetadata> coverLrLetter = new IdValue<>("3", createDocumentWithMetadata(STF_24WEEKS_REMOVAL_DECISION_LETTER_LR));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(coverLetter, removalDoc, coverLrLetter)));

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
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_BUNDLE
        );
        verify(documentBundler, times(1)).bundleWithoutContentsOrCoverSheets(
            List.of(coverLrLetter.getValue(), removalDoc.getValue()),
            "Letter bundle documents",
            "filename"
        );
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.STF_24WEEKS_REMOVAL_DECISION_LETTER_LR_BUNDLE
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
