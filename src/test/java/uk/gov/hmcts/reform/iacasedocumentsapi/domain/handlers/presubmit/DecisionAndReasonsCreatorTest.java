package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DecisionAndReasonsCreatorTest {

    @Mock
    private DocumentCreator<AsylumCase> decisionAndReasonsDocumentCreator;
    @Mock private DocumentHandler documentHandler;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;
    @Mock private DocumentWithMetadata documentWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingDecisionAndReasonDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allDraftDecisionAndReasonsDocuments;

    @Captor
    private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> hearingDocumentsCaptor;

    private DecisionAndReasonsCreator decisionAndReasonsCreator;

    @Before
    public void setUp() {

        decisionAndReasonsCreator =
                new DecisionAndReasonsCreator(
                        decisionAndReasonsDocumentCreator,
                        documentHandler
                );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_DECISION_AND_REASONS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(decisionAndReasonsDocumentCreator.create(caseDetails))
            .thenReturn(uploadedDocument);
    }

    @Test
    public void should_create_decision_and_reasons_document_and_append_to_decision_and_reasons_documents_for_the_case() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                decisionAndReasonsCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertEquals(asylumCase, callbackResponse.getData());

        verify(decisionAndReasonsDocumentCreator, times(1)).create(caseDetails);

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, DRAFT_DECISION_AND_REASONS_DOCUMENTS, DocumentTag.DECISION_AND_REASONS_DRAFT);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> decisionAndReasonsCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> decisionAndReasonsCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = decisionAndReasonsCreator.canHandle(callbackStage, callback);

                if (event == Event.GENERATE_DECISION_AND_REASONS
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> decisionAndReasonsCreator.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionAndReasonsCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionAndReasonsCreator.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decisionAndReasonsCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

}