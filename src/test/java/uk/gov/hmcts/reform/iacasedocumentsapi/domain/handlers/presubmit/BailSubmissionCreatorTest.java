package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail.BailSubmissionCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class BailSubmissionCreatorTest {
    @Mock
    private DocumentCreator<BailCase> bailSubmissionDocumentCreator;
    @Mock
    private BailDocumentHandler bailDocumentHandler;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private Document bailSubmission;

    private BailSubmissionCreator bailSubmissionCreator;

    @BeforeEach
    void setUp() {
        bailSubmissionCreator = new BailSubmissionCreator(bailSubmissionDocumentCreator, bailDocumentHandler);
        when(bailCase.read(BailCaseFieldDefinition.IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_handle_valid_events_stage(YesOrNo isImaEnabled) {
        when(bailCase.read(BailCaseFieldDefinition.IS_IMA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(isImaEnabled));
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage preSubmitCallbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = bailSubmissionCreator.canHandle(preSubmitCallbackStage, callback);
                if ((event == Event.SUBMIT_APPLICATION || event == Event.MAKE_NEW_APPLICATION
                    || event == Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT || event == Event.REGENERATE_BAIL_SUBMISSION_DOCUMENT)
                    && preSubmitCallbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && isImaEnabled == YesOrNo.NO) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailSubmissionCreator.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> bailSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailSubmissionCreator.handle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy((() -> bailSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_create_document_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailSubmissionDocumentCreator.create(caseDetails)).thenReturn(bailSubmission);

        PreSubmitCallbackResponse<BailCase> response = bailSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .addWithMetadata(bailCase, bailSubmission, BailCaseFieldDefinition.APPLICANT_DOCUMENTS_WITH_METADATA, DocumentTag.BAIL_SUBMISSION);
    }
}
