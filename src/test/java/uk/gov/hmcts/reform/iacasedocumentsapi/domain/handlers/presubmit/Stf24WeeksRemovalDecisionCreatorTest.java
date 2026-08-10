package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_REMOVAL_OF_24W_APPLICATION_REFUSED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;

@ExtendWith(MockitoExtension.class)
class Stf24WeeksRemovalDecisionCreatorTest {

    @Mock
    private DocumentCreator<AsylumCase> stf24WeeksRemovalDecisionDocumentCreator;
    @Mock
    private DocumentHandler documentHandler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document mockDocument;

    private Stf24WeeksRemovalDecisionCreator stf24WeeksRemovalDecisionCreator;

    @BeforeEach
    public void setUp() {

        stf24WeeksRemovalDecisionCreator =
            new Stf24WeeksRemovalDecisionCreator(
                stf24WeeksRemovalDecisionDocumentCreator,
                documentHandler
            );
    }

    @Test
    void canHandle_throws() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> stf24WeeksRemovalDecisionCreator.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception = assertThrows(NullPointerException.class,
            () -> stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void canHandle_returns_true_for_remove_statutory_timeframe_24_weeks_event() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertTrue(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_returns_true_for_decide_an_application_event_with_removal_of_24w_application_refused() {
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_returns_false_for_decide_an_application_event_with_no_removal_of_24w_application_refused() {
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertFalse(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REMOVE_STATUTORY_TIMEFRAME_24_WEEKS", "DECIDE_AN_APPLICATION"},
        mode = EnumSource.Mode.EXCLUDE)
    void canHandle_returns_false_for_other_events(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertFalse(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_throws_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThrows(IllegalStateException.class, () ->
            stf24WeeksRemovalDecisionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void if_not_internal_case_then_add_document_to_legal_rep_documents() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(stf24WeeksRemovalDecisionDocumentCreator.create(caseDetails)).thenReturn(mockDocument);

        PreSubmitCallbackResponse<AsylumCase> response = stf24WeeksRemovalDecisionCreator.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );

        verify(documentHandler, never()).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );
    }

    @Test
    void if_internal_case_then_also_add_document_to_letter_notification_documents() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(stf24WeeksRemovalDecisionDocumentCreator.create(caseDetails)).thenReturn(mockDocument);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response = stf24WeeksRemovalDecisionCreator.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );

        verify(documentHandler, never()).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );
    }

    @Test
    void if_refused_then_use_correct_tag() {
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(stf24WeeksRemovalDecisionDocumentCreator.create(caseDetails)).thenReturn(mockDocument);
        when(asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response = stf24WeeksRemovalDecisionCreator.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_REFUSED_DECISION_DOCUMENT
        );

        verify(documentHandler, never()).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_REFUSED_DECISION_DOCUMENT
        );
    }
}