package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealDecision;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedDecisionsAndReasonsLetterGeneratorTest {

    @Mock private DocumentCreator<AsylumCase> internalAdaDecisionsAndReasonsLetterAllowedDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> internalAdaDecisionsAndReasonsLetterDismissedDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> internalDetainedDecisionsAndReasonsLetterDismissedCreator;
    @Mock private DocumentHandler documentHandler;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;
    private InternalDetainedDecisionsAndReasonsLetterGenerator internalDetainedDecisionsAndReasonsLetterGenerator;

    @BeforeEach
    public void setUp() {
        internalDetainedDecisionsAndReasonsLetterGenerator =
            new InternalDetainedDecisionsAndReasonsLetterGenerator(
                internalAdaDecisionsAndReasonsLetterAllowedDocumentCreator,
                internalAdaDecisionsAndReasonsLetterDismissedDocumentCreator,
                internalDetainedDecisionsAndReasonsLetterDismissedCreator,
                documentHandler
            );
    }

    @Test
    public void should_create_internal_detained_appeal_decided_allowed_pdf_and_append_to_notifications_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.ALLOWED));

        when(internalAdaDecisionsAndReasonsLetterAllowedDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalDetainedDecisionsAndReasonsLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_DET_DECISION_AND_REASONS_LETTER);
    }

    @Test
    public void should_call_internal_detained_ada_decisions_and_reasons_letter_dismissed_creator() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.DISMISSED));

        internalDetainedDecisionsAndReasonsLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(internalAdaDecisionsAndReasonsLetterDismissedDocumentCreator, times(1)).create(caseDetails);
    }

    @Test
    public void should_call_internal_detained_non_ada_decisions_and_reasons_letter_dismissed_creator() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(callback.getCaseDetails().getCaseData().read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.DISMISSED));

        internalDetainedDecisionsAndReasonsLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(internalDetainedDecisionsAndReasonsLetterDismissedCreator, times(1)).create(caseDetails);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> internalDetainedDecisionsAndReasonsLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalDetainedDecisionsAndReasonsLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_cannot_handle_callback_if_is_admin_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetainedDecisionsAndReasonsLetterGenerator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_is_detained_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetainedDecisionsAndReasonsLetterGenerator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_appeal_decision_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetainedDecisionsAndReasonsLetterGenerator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void should_throw_when_appeal_decision_is_not_present() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SEND_DECISION_AND_REASONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.empty());

        when(internalAdaDecisionsAndReasonsLetterAllowedDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        assertThatThrownBy(() -> internalDetainedDecisionsAndReasonsLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Appeal decision is missing.");
    }

    @Test
    public void it_should_only_handle_about_to_submit_and_send_decisions_and_reasons_event() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
            when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetainedDecisionsAndReasonsLetterGenerator.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(Event.SEND_DECISION_AND_REASONS)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalDetainedDecisionsAndReasonsLetterGenerator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetainedDecisionsAndReasonsLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetainedDecisionsAndReasonsLetterGenerator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetainedDecisionsAndReasonsLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}