package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryCircumstances;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OOC_APPEAL_ADMIN_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus.PAID;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class AppealSubmissionCreatorTest {

    @Mock
    private DocumentCreator<AsylumCase> appealSubmissionDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalAppealSubmissionDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalOocAppealSubmissionDocumentCreator;
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

    private AppealSubmissionCreator appealSubmissionCreator;

    @BeforeEach
    public void setUp() {

        appealSubmissionCreator =
            new AppealSubmissionCreator(
                appealSubmissionDocumentCreator,
                internalAppealSubmissionDocumentCreator,
                internalOocAppealSubmissionDocumentCreator,
                documentHandler
            );
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(appealSubmissionDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, LEGAL_REPRESENTATIVE_DOCUMENTS, DocumentTag.APPEAL_SUBMISSION);
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_legal_representative_documents_for_the_case_when_edit_appeal_after_submit() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(appealSubmissionDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL_AFTER_SUBMIT);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, LEGAL_REPRESENTATIVE_DOCUMENTS, DocumentTag.APPEAL_SUBMISSION);
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_legal_representative_documents_for_the_case_when_pay_and_submit_appeal() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(appealSubmissionDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, LEGAL_REPRESENTATIVE_DOCUMENTS, DocumentTag.APPEAL_SUBMISSION);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_handle_callback_if_payment_failed_during_pay_and_submit() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, "Failed");

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertFalse(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void should_handle_callback_if_payment_success_during_pay_and_submit() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, "Paid");

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertTrue(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void should_handle_callback_if_payment_failed_during_pay_and_submit_and_then_pay_offline_selected() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, "Failed");
        asylumCase.write(PA_APPEAL_TYPE_PAYMENT_OPTION, "payOffline");

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertTrue(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void should_handle_callback_if_payment_failed_during_pay_and_submit_and_then_pay_later_selected() {

        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(PAYMENT_STATUS, "Failed");
        asylumCase.write(PA_APPEAL_TYPE_PAYMENT_OPTION, "payLater");

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertTrue(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT", "EDIT_APPELLANT_PERSONAL_DATA", "PAY_AND_SUBMIT_APPEAL"})
    public void it_can_handle_callback(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertTrue(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SUBMIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT", "EDIT_APPELLANT_PERSONAL_DATA", "PAY_AND_SUBMIT_APPEAL"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_bad_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertFalse(appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_bad_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertFalse(appealSubmissionCreator.canHandle(stage, callback));
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSubmissionCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_call_internal_appeal_submission_creator_when_case_is_internal_ada_appellant_in_detention() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(OutOfCountryCircumstances.LEAVE_UK));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));

        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(internalAppealSubmissionDocumentCreator, times(1)).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadata(eq(asylumCase), any(), eq(NOTIFICATION_ATTACHMENT_DOCUMENTS), eq(DocumentTag.INTERNAL_APPEAL_SUBMISSION));
    }

    @Test
    public void should_generate_internal_appeal_summary_and_det_letter_when_case_is_internal_ada_appellant_in_detention() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(OutOfCountryCircumstances.LEAVE_UK));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(appealSubmissionDocumentCreator, times(1)).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadata(eq(asylumCase), any(), eq(LEGAL_REPRESENTATIVE_DOCUMENTS), eq(DocumentTag.APPEAL_SUBMISSION));

        verify(internalAppealSubmissionDocumentCreator, times(1)).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadata(eq(asylumCase), any(), eq(NOTIFICATION_ATTACHMENT_DOCUMENTS), eq(DocumentTag.INTERNAL_APPEAL_SUBMISSION));
    }

    @Test
    public void should_call_internal_ooc_appeal_submission_creator_when_case_is_internal_ooc_circumstance_none() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(OutOfCountryCircumstances.NONE));

        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(PAID));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(internalOocAppealSubmissionDocumentCreator, times(1)).create(caseDetails);
        verify(documentHandler, times(1)).addWithMetadata(eq(asylumCase), any(), eq(LEGAL_REPRESENTATIVE_DOCUMENTS), eq(DocumentTag.APPEAL_SUBMISSION));
    }
}
