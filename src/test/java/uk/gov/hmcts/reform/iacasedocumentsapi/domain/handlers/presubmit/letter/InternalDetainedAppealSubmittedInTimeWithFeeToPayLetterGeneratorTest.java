package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedAppealSubmittedInTimeWithFeeToPayLetterGeneratorTest {

    @Mock
    private DocumentCreator<AsylumCase> documentCreator;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document uploadedDocument;

    private InternalDetainedAppealSubmittedInTimeWithFeeToPayLetterGenerator letterGenerator;

    @BeforeEach
    void setUp() {
        letterGenerator = new InternalDetainedAppealSubmittedInTimeWithFeeToPayLetterGenerator(
                documentCreator,
                documentHandler
        );
    }

    @Test
    void should_create_letter_and_append_to_notification_documents() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();
        when(documentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                letterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
                asylumCase,
                uploadedDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_IN_TIME_WITH_FEE_TO_PAY_LETTER
        );
    }

    @Test
    void should_handle_callback_when_all_conditions_met() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();

        assertTrue(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_callback_if_not_submit_appeal_event() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_handle_callback_if_not_about_to_submit_stage() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCase();

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback));
    }

    @ParameterizedTest
    @MethodSource("provideInvalidCaseScenarios")
    void should_not_handle_callback_for_invalid_conditions(String scenarioName, Consumer<AsylumCase> invalidConditionSetup) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCaseExcept();
        invalidConditionSetup.accept(asylumCase);

        assertFalse(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    private static Stream<Arguments> provideInvalidCaseScenarios() {
        return Stream.of(
                Arguments.of("not internal case",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO))
                ),
                Arguments.of("submission is out of time",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES))
                ),
                Arguments.of("submission out of time is missing",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.empty())
                ),
                Arguments.of("is fee exempt appeal",
                        (Consumer<AsylumCase>) asylumCase -> {
                            when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
                        }
                ),
                Arguments.of("not detained",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO))
                ),
                Arguments.of("in other detention facility",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"))
                ),
                Arguments.of("is ada appeal",
                        (Consumer<AsylumCase>) asylumCase -> when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES))
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"immigrationRemovalCentre", "prison"})
    void should_handle_callback_for_valid_detention_facilities(String detentionFacility) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCaseExcept();
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(detentionFacility));

        assertTrue(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @MethodSource("provideFeeToPayAppealTypes")
    void should_handle_callback_for_fee_to_pay_appeal_types(AppealType appealType) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        setUpValidCaseExcept();
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        assertTrue(letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    private static Stream<AppealType> provideFeeToPayAppealTypes() {
        return Stream.of(AppealType.PA, AppealType.EA, AppealType.HU, AppealType.EU);
    }

    @Test
    void should_throw_exception_when_cannot_handle_callback() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> letterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> letterGenerator.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> letterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> letterGenerator.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> letterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_not_handle_for_all_events_except_submit_appeal() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            setUpValidCase();

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = letterGenerator.canHandle(callbackStage, callback);

                if (event == Event.SUBMIT_APPEAL && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    private void setUpValidCase() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
    }

    private void setUpValidCaseExcept() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
    }
}
