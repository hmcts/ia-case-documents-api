package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class AppealSubmissionDetainedPrisonIrcCreatorTest {

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

    private AppealSubmissionDetainedPrisonIrcCreator handler;

    @BeforeEach
    void setUp() {
        handler = new AppealSubmissionDetainedPrisonIrcCreator(
                appealSubmissionDocumentCreator,
                documentHandler
        );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void canHandle_should_return_true_when_all_conditions_met() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON.getValue()));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));

        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(result).isTrue();
    }

    @Test
    void canHandle_should_return_false_for_wrong_stage() {
        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertThat(result).isFalse();
    }

    @Test
    void canHandle_should_return_false_for_wrong_event() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(result).isFalse();
    }

    @Test
    void canHandle_should_return_false_when_submission_out_of_time() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(any(), eq(RemissionType.class))).thenReturn(Optional.of(HO_WAIVER_REMISSION));
        when(asylumCase.read(any(), eq(YesOrNo.class))).thenReturn(Optional.of(YES));

        boolean result = handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(result).isFalse();
    }

    @Test
    void handle_should_add_document_and_return_response() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(PRISON.getValue()));
        when(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)).thenReturn(Optional.of(NO));

        Document document = new Document("url", "binUrl", "filename");
        when(appealSubmissionDocumentCreator.create(caseDetails)).thenReturn(document);

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(appealSubmissionDocumentCreator).create(caseDetails);
        verify(documentHandler).addWithMetadata(
                asylumCase,
                document,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION
        );

        assertThat(response.getData()).isEqualTo(asylumCase);
    }

    @Test
    void handle_should_throw_exception_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot handle callback");
    }
}
