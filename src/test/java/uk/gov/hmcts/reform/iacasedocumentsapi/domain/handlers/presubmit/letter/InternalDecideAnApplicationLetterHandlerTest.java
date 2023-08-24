package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDecideAnApplicationLetterHandlerTest {

    @Mock
    private DocumentCreator<AsylumCase> internalApplicationDecidedLetterCreator;
    @Mock
    private DocumentCreator<AsylumCase> internalApplicationDecidedRefusedLetterCreator;
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
    @Mock
    private MakeAnApplicationService makeAnApplicationService;
    private final YesOrNo yes = YesOrNo.YES;
    private final String decisionGranted = "Granted";
    private final String decisionRefused = "Refused";
    private final String decisionPending = "Pending";
    private List<IdValue<MakeAnApplication>> makeAnApplications = new ArrayList<>();
    private final MakeAnApplication makeAnApplication = new MakeAnApplication(
            "Admin Officer",
            MakeAnApplicationTypes.ADJOURN.getValue(),
            "someRandomDetails",
            new ArrayList<>(),
            LocalDate.now().toString(),
            decisionGranted,
            State.APPEAL_SUBMITTED.toString(),
            "caseworker-ia-admofficer");
    private InternalDecideAnApplicationLetterHandler internalDecideAnApplicationLetterHandler;

    @BeforeEach
    public void setUp() {
        internalDecideAnApplicationLetterHandler =
                new InternalDecideAnApplicationLetterHandler(
                        internalApplicationDecidedLetterCreator,
                        internalApplicationDecidedRefusedLetterCreator,
                        documentHandler,
                        makeAnApplicationService
                );

        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(yes));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(yes));

        makeAnApplications.add(new IdValue<>("1", makeAnApplication));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of("1"));

        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(makeAnApplication));
    }

    @Test // change
    public void should_create_application_decided_letter_and_append_to_notification_attachment_documents() {

        when(internalApplicationDecidedLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalDecideAnApplicationLetterHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_DECIDE_AN_APPLICATION_LETTER);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> internalDecideAnApplicationLetterHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalDecideAnApplicationLetterHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_cannot_handle_callback_if_is_admin_is_missing_or_set_to_no() {
        //isAdmin defaults to false if not present
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDecideAnApplicationLetterHandler.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_is_detained_is_missing_or_set_to_no() {
        //isDetained defaults to false if not present
        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDecideAnApplicationLetterHandler.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_should_only_handle_about_to_submit_and_decide_an_application_event() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDecideAnApplicationLetterHandler.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_SUBMIT && callback.getEvent().equals(Event.DECIDE_AN_APPLICATION)) {
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

        boolean canHandle = internalDecideAnApplicationLetterHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        if (yesOrNo == yes) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    public void it_should_handle_both_internal_detained_cases_and_internal_ada_cases(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        boolean canHandle = internalDecideAnApplicationLetterHandler.canHandle(ABOUT_TO_SUBMIT, callback);

        assertTrue(canHandle);
    }

    @ParameterizedTest
    @ValueSource(strings = {decisionGranted, decisionRefused, decisionPending})
    public void it_should_only_generate_the_letter_for_granted_and_refused_applications(String decision) {
        List<IdValue<MakeAnApplication>> testApplications = new ArrayList<>();
        final MakeAnApplication testApplication = new MakeAnApplication(
                "Admin Officer",
                MakeAnApplicationTypes.ADJOURN.getValue(),
                "someRandomDetails",
                new ArrayList<>(),
                LocalDate.now().toString(),
                decision.toString(),
                State.APPEAL_SUBMITTED.toString(),
                "caseworker-ia-admofficer");
        testApplications.add(new IdValue<>("1", testApplication));

        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(testApplications));
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID)).thenReturn(Optional.of("1"));
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.of(testApplication));

        when(internalApplicationDecidedLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);
        when(internalApplicationDecidedRefusedLetterCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalDecideAnApplicationLetterHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (List.of(decisionGranted, decisionRefused).contains(decision)) {
            verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_DECIDE_AN_APPLICATION_LETTER);
        } else {
            verify(documentHandler, times(0)).addWithMetadata(asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.INTERNAL_DECIDE_AN_APPLICATION_LETTER);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalDecideAnApplicationLetterHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDecideAnApplicationLetterHandler.canHandle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDecideAnApplicationLetterHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDecideAnApplicationLetterHandler.handle(ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}
