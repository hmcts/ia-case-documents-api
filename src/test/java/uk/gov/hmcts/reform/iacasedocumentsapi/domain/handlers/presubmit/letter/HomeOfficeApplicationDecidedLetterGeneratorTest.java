package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicationApplicant;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
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
class HomeOfficeApplicationDecidedLetterGeneratorTest {

    @Mock private DocumentCreator<AsylumCase> documentCreator;
    @Mock private DocumentHandler documentHandler;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;
    @Mock private MakeAnApplication makeAnApplication;

    private HomeOfficeApplicationDecidedLetterGenerator homeOfficeApplicationDecidedLetterGenerator;

    @BeforeEach
    public void setUp() {
        homeOfficeApplicationDecidedLetterGenerator =
            new HomeOfficeApplicationDecidedLetterGenerator(
                documentCreator,
                documentHandler
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);

        // Set up default mocks to avoid IllegalStateException in canHandle
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of("1"));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(java.util.Collections.singletonList(
            new uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue<>("1", makeAnApplication)
        )));
        when(makeAnApplication.getApplicant()).thenReturn(ApplicationApplicant.RESPONDENT.getValue());
    }

    @Test
    void should_create_home_office_application_decided_letter_and_append_to_notification_attachment_documents() {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(DetentionFacility.IRC.getValue()));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(documentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeApplicationDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(
            asylumCase,
            uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.HOME_OFFICE_APPLICATION_DECIDED_LETTER
        );
    }

    @Test
    void should_not_handle_if_not_about_to_submit() {
        boolean canHandle = homeOfficeApplicationDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_not_decide_an_application_event() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        boolean canHandle = homeOfficeApplicationDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_not_submitted_by_appellant_internal_case() {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        boolean canHandle = homeOfficeApplicationDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_not_detained_in_irc_or_prison() {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        boolean canHandle = homeOfficeApplicationDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_application_not_submitted_by_home_office() {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(DetentionFacility.IRC.getValue()));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        // Override the default mock to make application from admin officer instead
        when(makeAnApplication.getApplicant()).thenReturn(ApplicationApplicant.ADMIN_OFFICER.getValue());

        boolean canHandle = homeOfficeApplicationDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_handle_if_accelerated_detained_appeal() {
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(DetentionFacility.IRC.getValue()));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        boolean canHandle = homeOfficeApplicationDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertFalse(canHandle);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> homeOfficeApplicationDecidedLetterGenerator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeApplicationDecidedLetterGenerator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeApplicationDecidedLetterGenerator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeApplicationDecidedLetterGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}