package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_REMOVAL_OF_24W_APPLICATION_REFUSED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.MAKE_AN_APPLICATIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMOVAL_OF_24W_DECISION_JUDGE;

@ExtendWith(MockitoExtension.class)
class Stf24WeeksRemovalDecisionCreatorTest {

    @Mock
    private DocumentCreator<AsylumCase> stf24WeeksRemovalDecisionDocumentCreator;
    @Mock
    private DocumentHandler documentHandler;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private Document mockDocument;
    @Mock
    private MakeAnApplication application;

    private Stf24WeeksRemovalDecisionCreator stf24WeeksRemovalDecisionCreator;

    @BeforeEach
    public void setUp() {

        stf24WeeksRemovalDecisionCreator =
            new Stf24WeeksRemovalDecisionCreator(
                stf24WeeksRemovalDecisionDocumentCreator,
                documentHandler
            );
    }

    @Test
    void canHandle_throws() {
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> stf24WeeksRemovalDecisionCreator.canHandle(null, callback));
        assertEquals("callbackStage must not be null", exception.getMessage());

        exception = assertThrows(NullPointerException.class,
            () -> stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));
        assertEquals("callback must not be null", exception.getMessage());
    }

    @Test
    void canHandle_returns_true_for_remove_statutory_timeframe_24_weeks_event() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertTrue(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_returns_true_for_decide_an_application_event_with_removal_of_24w_application_refused() {
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertTrue(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void canHandle_returns_false_for_decide_an_application_event_with_no_removal_of_24w_application_refused() {
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertFalse(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REMOVE_STATUTORY_TIMEFRAME_24_WEEKS", "DECIDE_AN_APPLICATION"},
        mode = EnumSource.Mode.EXCLUDE)
    void canHandle_returns_false_for_other_events(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertFalse(stf24WeeksRemovalDecisionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void handle_throws_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThrows(IllegalStateException.class, () ->
            stf24WeeksRemovalDecisionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void if_not_internal_case_then_add_document_to_legal_rep_documents() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(stf24WeeksRemovalDecisionDocumentCreator.create(caseDetails)).thenReturn(mockDocument);

        PreSubmitCallbackResponse<AsylumCase> response = stf24WeeksRemovalDecisionCreator.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );

        verify(documentHandler, never()).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );
        verify(asylumCase).clear(IS_REMOVAL_OF_24W_APPLICATION_REFUSED);
        verify(asylumCase).clear(REMOVAL_OF_24W_DECISION_JUDGE);
    }

    @Test
    void if_internal_case_then_also_add_document_to_letter_notification_documents() {
        when(callback.getEvent()).thenReturn(Event.REMOVE_STATUTORY_TIMEFRAME_24_WEEKS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(stf24WeeksRemovalDecisionDocumentCreator.create(caseDetails)).thenReturn(mockDocument);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response = stf24WeeksRemovalDecisionCreator.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );

        verify(documentHandler, never()).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_DECISION_DOCUMENT
        );
        verify(asylumCase).clear(IS_REMOVAL_OF_24W_APPLICATION_REFUSED);
        verify(asylumCase).clear(REMOVAL_OF_24W_DECISION_JUDGE);
    }

    @Test
    void if_refused_then_use_correct_tag() {
        when(callback.getEvent()).thenReturn(Event.DECIDE_AN_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(stf24WeeksRemovalDecisionDocumentCreator.create(caseDetails)).thenReturn(mockDocument);
        when(asylumCase.read(IS_REMOVAL_OF_24W_APPLICATION_REFUSED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> response = stf24WeeksRemovalDecisionCreator.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_REFUSED_DECISION_DOCUMENT
        );

        verify(documentHandler, never()).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(
            asylumCase,
            mockDocument,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.STF_24WEEKS_REMOVAL_REFUSED_DECISION_DOCUMENT
        );
        verify(asylumCase).clear(IS_REMOVAL_OF_24W_APPLICATION_REFUSED);
        verify(asylumCase).clear(REMOVAL_OF_24W_DECISION_JUDGE);
    }

    @Test
    void setDecidedApplicationRefusal24wRemovalDoc_sets_document_when_application_decision_changes() {
        MakeAnApplication applicationBefore = new MakeAnApplication();
        applicationBefore.setDecision("Pending");

        when(application.getDecision()).thenReturn("Refused");

        IdValue<MakeAnApplication> applicationBeforeIdValue =
            new IdValue<>("123", applicationBefore);
        IdValue<MakeAnApplication> applicationIdValue =
            new IdValue<>("123", application);

        when(asylumCase.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.of(List.of(applicationIdValue)));

        when(asylumCaseBefore.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.of(List.of(applicationBeforeIdValue)));

        stf24WeeksRemovalDecisionCreator.setDecidedApplicationRefusal24wRemovalDoc(
            asylumCase,
            asylumCaseBefore,
            mockDocument
        );

        verify(application).setRefusalOfRemoval24wDocument(mockDocument);
    }

    @Test
    void setDecidedApplicationRefusal24wRemovalDoc_does_not_set_document_when_application_decision_has_not_changed() {
        MakeAnApplication applicationBefore = new MakeAnApplication();
        applicationBefore.setDecision("Refused");

        when(application.getDecision()).thenReturn("Refused");

        IdValue<MakeAnApplication> applicationBeforeIdValue =
            new IdValue<>("123", applicationBefore);
        IdValue<MakeAnApplication> applicationIdValue =
            new IdValue<>("123", application);

        when(asylumCase.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.of(List.of(applicationIdValue)));

        when(asylumCaseBefore.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.of(List.of(applicationBeforeIdValue)));

        stf24WeeksRemovalDecisionCreator.setDecidedApplicationRefusal24wRemovalDoc(
            asylumCase,
            asylumCaseBefore,
            mockDocument
        );

        verify(application, never()).setRefusalOfRemoval24wDocument(mockDocument);
    }

    @Test
    void setDecidedApplicationRefusal24wRemovalDoc_sets_document_only_on_changed_application() {
        MakeAnApplication unchangedApplicationBefore = new MakeAnApplication();
        unchangedApplicationBefore.setDecision("Refused");

        MakeAnApplication unchangedApplication = mock(MakeAnApplication.class);
        when(unchangedApplication.getDecision()).thenReturn("Refused");

        MakeAnApplication changedApplicationBefore = new MakeAnApplication();
        changedApplicationBefore.setDecision("Pending");

        MakeAnApplication changedApplication = mock(MakeAnApplication.class);
        when(changedApplication.getDecision()).thenReturn("Refused");

        IdValue<MakeAnApplication> unchangedBefore =
            new IdValue<>("123", unchangedApplicationBefore);
        IdValue<MakeAnApplication> unchanged =
            new IdValue<>("123", unchangedApplication);

        IdValue<MakeAnApplication> changedBefore =
            new IdValue<>("456", changedApplicationBefore);
        IdValue<MakeAnApplication> changed =
            new IdValue<>("456", changedApplication);

        when(asylumCase.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.of(List.of(unchanged, changed)));

        when(asylumCaseBefore.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.of(List.of(unchangedBefore, changedBefore)));

        stf24WeeksRemovalDecisionCreator.setDecidedApplicationRefusal24wRemovalDoc(
            asylumCase,
            asylumCaseBefore,
            mockDocument
        );

        verify(changedApplication).setRefusalOfRemoval24wDocument(mockDocument);
        verify(unchangedApplication, never()).setRefusalOfRemoval24wDocument(mockDocument);
    }

    @Test
    void setDecidedApplicationRefusal24wRemovalDoc_does_nothing_when_current_applications_are_empty() {

        when(asylumCase.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.empty());
        when(asylumCaseBefore.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.empty());

        stf24WeeksRemovalDecisionCreator.setDecidedApplicationRefusal24wRemovalDoc(
            asylumCase,
            asylumCaseBefore,
            mockDocument
        );

        verifyNoInteractions(mockDocument);
    }

    @Test
    void setDecidedApplicationRefusal24wRemovalDoc_sets_document_when_application_is_new() {
        when(application.getDecision()).thenReturn("Refused");

        IdValue<MakeAnApplication> applicationIdValue =
            new IdValue<>("123", application);

        when(asylumCase.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.of(List.of(applicationIdValue)));
        when(asylumCaseBefore.read(MAKE_AN_APPLICATIONS))
            .thenReturn(Optional.empty());

        stf24WeeksRemovalDecisionCreator.setDecidedApplicationRefusal24wRemovalDoc(
            asylumCase,
            asylumCaseBefore,
            mockDocument
        );

        verify(application).setRefusalOfRemoval24wDocument(mockDocument);
    }
}