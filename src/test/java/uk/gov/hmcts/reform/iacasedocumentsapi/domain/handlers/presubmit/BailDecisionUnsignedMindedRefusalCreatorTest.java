package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_IMA_ENABLED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.RECORD_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.RECORD_THE_DECISION_LIST_IMA;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail.BailDecisionUnsignedMindedRefusalCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class BailDecisionUnsignedMindedRefusalCreatorTest {
    @Mock private DocumentCreator<BailCase> bailDocumentCreator;
    @Mock private BailDocumentHandler bailDocumentHandler;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private Document bailDecisionUnsigned;

    private BailDecisionUnsignedMindedRefusalCreator bailDecisionUnsignedMindedRefusalCreator;
    private RecordDecisionType recordDecisionTypeRefusal = RecordDecisionType.REFUSED;
    private RecordDecisionType recordDecisionTypeGranted = RecordDecisionType.GRANTED;
    private String tribunalDecisionRefusal = "Refused";
    private String tribunalDecisionMindedToGrant = "mindedToGrant";

    @BeforeEach
    void setUp() {
        bailDecisionUnsignedMindedRefusalCreator =
                new BailDecisionUnsignedMindedRefusalCreator(bailDocumentCreator, bailDocumentHandler);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_handle_valid_events_stage(YesOrNo isImaEnabled) {
        when(bailCase.read(IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isImaEnabled));
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage preSubmitCallbackStage : PreSubmitCallbackStage.values()) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(bailCase);
                when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
                when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionMindedToGrant));
                when(bailCase.read(RECORD_THE_DECISION_LIST_IMA, String.class)).thenReturn(Optional.of(tribunalDecisionMindedToGrant));
                boolean canHandle = bailDecisionUnsignedMindedRefusalCreator.canHandle(preSubmitCallbackStage, callback);
                if (isImaEnabled.equals(YesOrNo.YES)) {
                    verify(bailCase, atLeastOnce()).read(RECORD_THE_DECISION_LIST_IMA, String.class);
                    verify(bailCase, never()).read(RECORD_THE_DECISION_LIST, String.class);
                } else {
                    verify(bailCase, atLeastOnce()).read(RECORD_THE_DECISION_LIST, String.class);
                    verify(bailCase, never()).read(RECORD_THE_DECISION_LIST_IMA, String.class);
                }
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
        assertThatThrownBy(() -> bailDecisionUnsignedMindedRefusalCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedMindedRefusalCreator.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedMindedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailDecisionUnsignedMindedRefusalCreator.handle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_not_handle_other_record_decision_type() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(recordDecisionTypeGranted));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionRefusal));
        assertThatThrownBy((() -> bailDecisionUnsignedMindedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void should_not_handle_is_not_judge_minded_refusal() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionRefusal));
        assertThatThrownBy((() -> bailDecisionUnsignedMindedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot handle callback");
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionMindedToGrant));
        assertThatThrownBy((() -> bailDecisionUnsignedMindedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_create_document_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.RECORD_THE_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.of(recordDecisionTypeRefusal));
        when(bailCase.read(RECORD_THE_DECISION_LIST, String.class)).thenReturn(Optional.of(tribunalDecisionMindedToGrant));
        when(bailDocumentCreator.create(caseDetails)).thenReturn(bailDecisionUnsigned);

        PreSubmitCallbackResponse<BailCase> response = bailDecisionUnsignedMindedRefusalCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .addWithMetadata(bailCase, bailDecisionUnsigned,
                    BailCaseFieldDefinition.UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA, DocumentTag.BAIL_DECISION_UNSIGNED);

        verify(bailDocumentHandler, times(1))
            .addDocumentWithoutMetadata(bailCase, bailDecisionUnsigned, BailCaseFieldDefinition.DECISION_UNSIGNED_DOCUMENT);
    }
}
