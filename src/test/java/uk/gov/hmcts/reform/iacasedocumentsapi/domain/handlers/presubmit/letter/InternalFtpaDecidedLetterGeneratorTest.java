package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
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
class InternalFtpaDecidedLetterGeneratorTest {

    @Mock
    private DocumentCreator<AsylumCase> internalAppellantFtpaDecidedGrantedCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalAppellantFtpaDecidedPartiallyGrantedCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalAppellantFtpaDecidedRefusedCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalHoFtpaDecidedGrantedCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalHoFtpaDecidedPartiallyGrantedCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalHoFtpaDecidedRefusedCreator;
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
    private final ApplicantType ftpaApplicantAppellant = ApplicantType.APPELLANT;
    private final ApplicantType ftpaApplicantRespondent = ApplicantType.RESPONDENT;
    private InternalFtpaDecidedLetterGenerator internalFtpaDecidedLetterGenerator;

    @BeforeEach
    public void setUp() {
        internalFtpaDecidedLetterGenerator =
            new InternalFtpaDecidedLetterGenerator(
                internalAppellantFtpaDecidedGrantedCreator,
                internalAppellantFtpaDecidedPartiallyGrantedCreator,
                internalAppellantFtpaDecidedRefusedCreator,
                internalHoFtpaDecidedGrantedCreator,
                internalHoFtpaDecidedPartiallyGrantedCreator,
                internalHoFtpaDecidedRefusedCreator,
                documentHandler
            );

        when(callback.getEvent()).thenReturn(Event.RESIDENT_JUDGE_FTPA_DECISION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
    }

    @Test
    void should_create_internal_appellant_ftpa_decided_granted_letter_and_append_to_notification_attachment_documents() {
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ftpaApplicantAppellant));

        when(internalAppellantFtpaDecidedGrantedCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
            asylumCase,
            uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER
        );
    }

    @Test
    void should_create_internal_appellant_ftpa_decided_partially_granted_letter_and_append_to_notification_attachment_documents() {
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ftpaApplicantAppellant));

        when(internalAppellantFtpaDecidedPartiallyGrantedCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
            asylumCase,
            uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER
        );
    }

    @Test
    void should_create_internal_appellant_ftpa_decided_refused_letter_and_append_to_notification_attachment_documents() {
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ftpaApplicantAppellant));

        when(internalAppellantFtpaDecidedRefusedCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
            asylumCase,
            uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER
        );
    }

    @Test
    void should_create_internal_ho_ftpa_decided_granted_letter_and_append_to_notification_attachment_documents() {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ftpaApplicantRespondent));

        when(internalHoFtpaDecidedGrantedCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
                asylumCase,
                uploadedDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER
        );
    }

    @Test
    void should_create_internal_ho_ftpa_decided_partially_granted_letter_and_append_to_notification_attachment_documents() {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ftpaApplicantRespondent));

        when(internalHoFtpaDecidedPartiallyGrantedCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
                asylumCase,
                uploadedDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER
        );
    }

    @ParameterizedTest
    @EnumSource(value = FtpaDecisionOutcomeType.class, names = {"FTPA_REFUSED", "FTPA_NOT_ADMITTED"})
    void should_create_internal_ho_ftpa_decided_refused_letter_and_append_to_notification_attachment_documents(FtpaDecisionOutcomeType ftpaDecisionOutcomeType) {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(ftpaDecisionOutcomeType));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ftpaApplicantRespondent));

        when(internalHoFtpaDecidedRefusedCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
                asylumCase,
                uploadedDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER
        );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_cannot_handle_callback_if_is_admin_is_missing() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalFtpaDecidedLetterGenerator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void it_cannot_handle_callback_if_not_in_detention() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
            when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalFtpaDecidedLetterGenerator.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    void it_should_only_handle_about_to_submit_and_resident_judge_ftpa_decision_event() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalFtpaDecidedLetterGenerator.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(Event.RESIDENT_JUDGE_FTPA_DECISION)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalFtpaDecidedLetterGenerator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalFtpaDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalFtpaDecidedLetterGenerator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalFtpaDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
