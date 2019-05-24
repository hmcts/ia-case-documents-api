package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSubmissionCreatorTest {

    @Mock private DocumentCreator<AsylumCase> appealSubmissionDocumentCreator;
    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;
    @Mock private DocumentWithMetadata documentWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingLegalRepresentativeDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments;

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> legalRepresentativeDocumentsCaptor;

    private AppealSubmissionCreator appealSubmissionCreator;

    @Before
    public void setUp() {

        appealSubmissionCreator =
            new AppealSubmissionCreator(
                appealSubmissionDocumentCreator,
                documentReceiver,
                documentsAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(appealSubmissionDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS)).thenReturn(Optional.of(existingLegalRepresentativeDocuments));

        when(documentReceiver.receive(
            uploadedDocument,
            "",
            DocumentTag.APPEAL_SUBMISSION
        )).thenReturn(documentWithMetadata);

        when(documentsAppender.append(
            existingLegalRepresentativeDocuments,
            Collections.singletonList(documentWithMetadata),
            DocumentTag.APPEAL_SUBMISSION
        )).thenReturn(allLegalRepresentativeDocuments);
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_legal_representative_documents_for_the_case() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(appealSubmissionDocumentCreator, times(1)).create(caseDetails);

        verify(asylumCase, times(1)).read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        verify(documentReceiver, times(1)).receive(uploadedDocument, "", DocumentTag.APPEAL_SUBMISSION);
        verify(documentsAppender, times(1))
            .append(
                existingLegalRepresentativeDocuments,
                Collections.singletonList(documentWithMetadata),
                DocumentTag.APPEAL_SUBMISSION
            );

        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepresentativeDocuments);
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_the_case_when_no_legal_representative_documents_exist() {

        when(documentsAppender.append(
            any(List.class),
            eq(Collections.singletonList(documentWithMetadata)),
            eq(DocumentTag.APPEAL_SUBMISSION)
        )).thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentsAppender, times(1))
            .append(
                legalRepresentativeDocumentsCaptor.capture(),
                eq(Collections.singletonList(documentWithMetadata)),
                eq(DocumentTag.APPEAL_SUBMISSION)
            );

        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, allLegalRepresentativeDocuments);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealSubmissionCreator.canHandle(callbackStage, callback);

                if (event == Event.SUBMIT_APPEAL
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
}
