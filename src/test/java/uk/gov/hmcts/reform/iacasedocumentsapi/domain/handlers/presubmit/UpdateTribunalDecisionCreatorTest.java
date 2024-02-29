package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UpdateTribunalRules;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.SendDecisionAndReasonsOrchestrator;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UpdateTribunalRules.UNDER_RULE_31;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class UpdateTribunalDecisionCreatorTest {

    @Mock private DocumentCreator<AsylumCase> updatedDecisionAndReasonsCoverLetterDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator;
    @Mock private DocumentHandler documentHandler;
    
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;

    private UpdateTribunalDecisionCreator updateTribunalDecisionCreator;

    @BeforeEach
    public void setUp() {

        updateTribunalDecisionCreator =
                new UpdateTribunalDecisionCreator(
                        updatedDecisionAndReasonsCoverLetterDocumentCreator,
                        aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator,
                        documentHandler
                );
    }

    @Test
    public void handling_should_throw_if_event_not_applicable() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertThatThrownBy(() -> updateTribunalDecisionCreator.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_not_bound_to_about_to_submit_callback_stage() {
        assertThatThrownBy(() -> updateTribunalDecisionCreator.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_handle_aip_update_tribunal_decision() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                .thenReturn(Optional.of(UNDER_RULE_31));
        JourneyType journeyType = JourneyType.AIP;
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(journeyType));
        when(aipUpdatedDecisionAndReasonsCoverLetterDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> response =
            updateTribunalDecisionCreator.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(documentHandler, times(1))
                .addWithMetadataWithoutReplacingExistingDocuments(asylumCase,
                        uploadedDocument,
                        FINAL_DECISION_AND_REASONS_DOCUMENTS,
                        DocumentTag.DECISION_AND_REASONS_COVER_LETTER);
    }

    @Test
    public void should_handle_lr_update_tribunal_decision() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.UPDATE_TRIBUNAL_DECISION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                .thenReturn(Optional.of(UNDER_RULE_31));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        when(updatedDecisionAndReasonsCoverLetterDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> response =
                updateTribunalDecisionCreator.handle(ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, response.getData());

        verify(documentHandler, times(1))
                .addWithMetadataWithoutReplacingExistingDocuments(asylumCase,
                        uploadedDocument,
                        FINAL_DECISION_AND_REASONS_DOCUMENTS,
                        DocumentTag.DECISION_AND_REASONS_COVER_LETTER);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, UpdateTribunalRules.class))
                    .thenReturn(Optional.of(UNDER_RULE_31));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = updateTribunalDecisionCreator.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_TRIBUNAL_DECISION
                    && callbackStage == ABOUT_TO_SUBMIT) {

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

        assertThatThrownBy(() -> updateTribunalDecisionCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionCreator.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> updateTribunalDecisionCreator.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
