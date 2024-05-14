package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
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
public class InternalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGeneratorTest {

    @Mock
    private DocumentCreator<AsylumCase> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterCreator;
    @Mock private DocumentHandler documentHandler;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;
    private final YesOrNo yes = YesOrNo.YES;
    private final YesOrNo no = YesOrNo.NO;
    private InternalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator;

    @BeforeEach
    public void setUp() {
        internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator =
                new InternalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator(
                        internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterCreator,
                        documentHandler
                );

        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_REVIEW);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(yes));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(yes));
        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)).thenReturn(Optional.of(AppealReviewOutcome.DECISION_WITHDRAWN));
    }

    @Test
    public void should_create_review_home_office_response_decision_withdrawn_letter_and_append_to_notification_attachment_documents() {
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_cannot_handle_callback_if_is_admin_is_missing() {
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(callbackStage, callback);
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
            when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_should_only_handle_about_to_submit_and_request_response_review_event() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && callback.getEvent().equals(Event.REQUEST_RESPONSE_REVIEW)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    public void it_should_only_handle_internal_cases(YesOrNo yesOrNo) {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        boolean canHandle = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (yesOrNo == yes) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    public void it_should_only_handle_detained_cases(YesOrNo yesOrNo) {

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        boolean canHandle = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (yesOrNo == yes) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    public void it_should_only_handle_non_ada_cases(YesOrNo yesOrNo) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        boolean canHandle = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (yesOrNo == no) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(AppealReviewOutcome.class)
    public void it_should_only_handle_appeal_review_outcome_decision_withdrawn(AppealReviewOutcome appealReviewOutcome) {

        when(asylumCase.read(APPEAL_REVIEW_OUTCOME, AppealReviewOutcome.class)).thenReturn(Optional.of(appealReviewOutcome));

        boolean canHandle = internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (appealReviewOutcome.equals(AppealReviewOutcome.DECISION_WITHDRAWN)) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetainedReviewHomeOfficeResponseDecisionWithdrawnLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}