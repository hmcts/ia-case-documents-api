package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail.BailDecisionUnsignedRefusalCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@ExtendWith(MockitoExtension.class)
public class BailDecisionUnsignedRefusalCreatorTest {
    @Mock private DocumentCreator<BailCase> bailDocumentCreator;
    @Mock private BailDocumentHandler bailDocumentHandler;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private Document bailDecisionUnsigned;

    private BailDecisionUnsignedRefusalCreator bailDecisionUnsignedRefusalCreator;
    private String recordDecisionTypeRefusal = "refused";
    private String recordDecisionTypeGranted = "granted";
    private String tribunalDecisionRefusal = "Refused";
    private String tribunalDecisionMindedToGrant = "mindedToGrant";

    @BeforeEach
    void setUp() {
        bailDecisionUnsignedRefusalCreator =
                new BailDecisionUnsignedRefusalCreator(bailDocumentCreator, bailDocumentHandler);
    }

    @Test
    void should_handle_valid_events_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage preSubmitCallbackStage : PreSubmitCallbackStage.values()) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(bailCase);
                when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
                when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionRefusal));
                boolean canHandle = bailDecisionUnsignedRefusalCreator.canHandle(preSubmitCallbackStage, callback);
                if (event == Event.RECORD_THE_DECISION && preSubmitCallbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailDecisionUnsignedRefusalCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedRefusalCreator.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedRefusalCreator.handle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_not_handle_other_record_decision_type() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeGranted));
        assertThatThrownBy((() -> bailDecisionUnsignedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_handle_is_judge_minded_refusal() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionMindedToGrant));
        assertThatThrownBy((() -> bailDecisionUnsignedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionRefusal));
        assertThatThrownBy((() -> bailDecisionUnsignedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_create_document_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionRefusal));
        when(bailDocumentCreator.create(caseDetails)).thenReturn(bailDecisionUnsigned);

        PreSubmitCallbackResponse<BailCase> response = bailDecisionUnsignedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .addWithMetadata(bailCase, bailDecisionUnsigned,
                    BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA,
                    DocumentTag.BAIL_DECISION_UNSIGNED);

        verify(bailDocumentHandler, times(1))
            .addDocumentWithoutMetadata(bailCase, bailDecisionUnsigned,
                    BailCaseFieldDefinition.DECISION_UNSIGNED_DOCUMENT);
    }
}
