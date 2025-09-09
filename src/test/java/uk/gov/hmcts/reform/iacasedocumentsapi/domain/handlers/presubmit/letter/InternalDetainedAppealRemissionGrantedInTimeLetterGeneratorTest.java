package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;

class InternalDetainedAppealRemissionGrantedInTimeLetterGeneratorTest {

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

    private InternalDetainedAppealRemissionGrantedInTimeLetterGenerator handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new InternalDetainedAppealRemissionGrantedInTimeLetterGenerator(documentCreator, documentHandler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void canHandle_should_return_true_when_all_conditions_met() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        // we can mock static utils AsylumCaseUtils if needed
        try (var utils = mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class)) {
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase(asylumCase))
                    .thenReturn(true);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isFeeExemptAppeal(asylumCase))
                    .thenReturn(false);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes(asylumCase,
                    uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.IRC,
                    uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON)).thenReturn(true);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase))
                    .thenReturn(false);

            boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertThat(result).isTrue();
        }
    }

    @Test
    void canHandle_should_return_false_when_submission_out_of_time() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        try (var utils = mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class)) {
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase(asylumCase))
                    .thenReturn(true);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isFeeExemptAppeal(asylumCase))
                    .thenReturn(false);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes(any(), any(), any()))
                    .thenReturn(true);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase))
                    .thenReturn(false);

            boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertThat(result).isFalse();
        }
    }

    @Test
    void handle_should_add_document_and_return_response() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        Document doc = new Document("url", "binaryUrl", "filename");
        when(documentCreator.create(caseDetails)).thenReturn(doc);

        try (var utils = mockStatic(uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.class)) {
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase(asylumCase))
                    .thenReturn(true);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isFeeExemptAppeal(asylumCase))
                    .thenReturn(false);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isDetainedInOneOfFacilityTypes(any(), any(), any()))
                    .thenReturn(true);
            utils.when(() -> uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase))
                    .thenReturn(false);

            PreSubmitCallbackResponse<AsylumCase> response =
                    handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            verify(documentCreator).create(caseDetails);
            verify(documentHandler).addWithMetadata(
                    asylumCase,
                    doc,
                    AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS,
                    DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_IN_TIME_WITH_FEE_TO_PAY_LETTER
            );

            assertThat(response.getData()).isEqualTo(asylumCase);
        }
    }

    @Test
    void handle_should_throw_when_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        assertThatThrownBy(() ->
                handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot handle callback");
    }
}
