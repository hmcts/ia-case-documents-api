package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail.BailDecisionUnsignedGrantedCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@ExtendWith(MockitoExtension.class)
public class BailDecisionUnsignedGrantedCreatorTest {
    @Mock private DocumentCreator<BailCase> bailDocumentCreator;
    @Mock private BailDocumentHandler bailDocumentHandler;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private Document bailDecisionUnsigned;

    private BailDecisionUnsignedGrantedCreator bailDecisionUnsignedGrantedCreator;
    private String recordDecisionTypeRefusal = "refused";
    private String recordDecisionTypeGranted = "granted";


    @BeforeEach
    void setUp() {
        bailDecisionUnsignedGrantedCreator =
                new BailDecisionUnsignedGrantedCreator(bailDocumentCreator, bailDocumentHandler);
    }

    @Test
    void should_handle_valid_events_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage preSubmitCallbackStage : PreSubmitCallbackStage.values()) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(bailCase);
                when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeGranted));
                boolean canHandle = bailDecisionUnsignedGrantedCreator.canHandle(preSubmitCallbackStage, callback);
                if (event == Event.RECORD_THE_DECISION && preSubmitCallbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"granted", "conditionalGrant"})
    void should_handle_valid_events_stage(String recordDecisionType) {

        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionType));
        boolean canHandle = bailDecisionUnsignedGrantedCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertTrue(canHandle);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailDecisionUnsignedGrantedCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedGrantedCreator.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedGrantedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedGrantedCreator.handle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_not_handle_refused_record_decision_type() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
        assertThatThrownBy((() -> bailDecisionUnsignedGrantedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeGranted));
        assertThatThrownBy((() -> bailDecisionUnsignedGrantedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_create_document_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, String.class)).thenReturn(Optional.of(recordDecisionTypeGranted));
        when(bailDocumentCreator.create(caseDetails)).thenReturn(bailDecisionUnsigned);

        PreSubmitCallbackResponse<BailCase> response = bailDecisionUnsignedGrantedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .addWithMetadata(bailCase, bailDecisionUnsigned,
                    BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA, DocumentTag.BAIL_DECISION_UNSIGNED);

        verify(bailDocumentHandler, times(1))
            .addDocumentWithoutMetadata(bailCase, bailDecisionUnsigned, BailCaseFieldDefinition.DECISION_UNSIGNED_DOCUMENT);
    }
}
