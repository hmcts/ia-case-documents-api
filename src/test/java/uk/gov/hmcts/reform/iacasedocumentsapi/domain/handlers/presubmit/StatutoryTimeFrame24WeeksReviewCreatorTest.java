package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class StatutoryTimeFrame24WeeksReviewCreatorTest {

    @Mock
    private DocumentCreator<AsylumCase> appealSubmissionDocumentCreator;
    @Mock
    private DocumentHandler documentHandler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private StatutoryTimeFrame24WeeksReviewCreator appealSubmissionCreator;

    @BeforeEach
    public void setUp() {

        appealSubmissionCreator =
                new StatutoryTimeFrame24WeeksReviewCreator(
                        appealSubmissionDocumentCreator,
                        documentHandler
                );
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"COMPLETE_CASE_REVIEW", "SEND_LATE_TIMELINE_NOTICE"})
    void canHandle_should_return_true_for_valid_callback(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"COMPLETE_CASE_REVIEW", "SEND_LATE_TIMELINE_NOTICE"},
        mode = EnumSource.Mode.EXCLUDE)
    void canHandle_should_return_false_for_invalid_event(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_false_for_ooc() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertFalse(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_should_return_false_for_non_stf() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

    }


    @Test
    public void should_create_appeal_submission_pdf_and_append_to_legal_representative_documents_for_the_case_when_pay_and_submit_appeal() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getEvent()).thenReturn(Event.COMPLETE_CASE_REVIEW);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());


    }

}