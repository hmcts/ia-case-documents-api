package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;

@ExtendWith(MockitoExtension.class)
class BailEndApplicationCreatorTest {

    @Mock private DocumentCreator<BailCase> bailDocumentCreator;
    @Mock private BailDocumentHandler bailDocumentHandler;
    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private Document bailEndApplicationDocument;

    private BailEndApplicationCreator bailEndApplicationCreator;

    @BeforeEach
    void setUp() {
        bailEndApplicationCreator =
            new BailEndApplicationCreator(bailDocumentCreator, bailDocumentHandler);
    }

    @Test
    void should_handle_valid_events_stage() {
        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);
            for (PreSubmitCallbackStage preSubmitCallbackStage : PreSubmitCallbackStage.values()) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(bailCase);
                boolean canHandle = bailEndApplicationCreator.canHandle(preSubmitCallbackStage, callback);
                if (event == Event.END_APPLICATION && preSubmitCallbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_create_end_application_document_and_add_to_bailcase() {
        when(callback.getEvent()).thenReturn(Event.END_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailDocumentCreator.create(caseDetails)).thenReturn(bailEndApplicationDocument);

        PreSubmitCallbackResponse<BailCase> response = bailEndApplicationCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailDocumentHandler, times(1))
            .addWithMetadata(bailCase, bailEndApplicationDocument,
                BailCaseFieldDefinition.TRIBUNAL_DOCUMENTS_WITH_METADATA, DocumentTag.BAIL_END_APPLICATION);
    }

    @Test
    void should_not_allow_null_args() {
        assertThatThrownBy(() -> bailEndApplicationCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailEndApplicationCreator.canHandle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> bailEndApplicationCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");

        assertThatThrownBy(() -> bailEndApplicationCreator.handle(null, callback))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");
    }

    @Test
    void should_throw_if_cannot_handle() {
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        assertThatThrownBy((() -> bailEndApplicationCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

}