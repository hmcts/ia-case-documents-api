package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentServiceResponseException;

@RunWith(MockitoJUnitRunner.class)
public class SendDecisionAndReasonsOrchestratorTest {

    @Mock private DocumentHandler documentHandler;
    @Mock private SendDecisionAndReasonsPdfService sendDecisionAndReasonsPdfService;
    @Mock private SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document coverLetter;
    @Mock private Document pdf;

    private SendDecisionAndReasonsOrchestrator sendDecisionAndReasonsOrchestrator;
    private List<IdValue<DocumentWithMetadata>> existingDecisionAnReasonsDocuments = emptyList();

    @Before
    public void setUp() {
        sendDecisionAndReasonsOrchestrator =
            new SendDecisionAndReasonsOrchestrator(
                documentHandler,
                sendDecisionAndReasonsPdfService,
                sendDecisionAndReasonsCoverLetterService);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    public void throws_and_skips_document_conversion_if_cover_letter_null() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails)).thenReturn(null);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .hasMessage("Cover letter creation failed")
            .isExactlyInstanceOf(NullPointerException.class);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    public void throws_and_skips_document_conversion_if_cover_letter_fails_with_exception() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails)).thenThrow(
            DocumentServiceResponseException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .isExactlyInstanceOf(DocumentServiceResponseException.class);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    public void throws_and_fails_with_exception_when_pdf_generation_returns_null() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.generatePdf(caseDetails))
            .thenReturn(null);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .hasMessage("Document to pdf conversion failed")
            .isExactlyInstanceOf(NullPointerException.class);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    public void throws_and_fails_with_exception_when_pdf_generation_throws() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.generatePdf(caseDetails))
            .thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails))
            .isInstanceOf(RuntimeException.class);

        verify(asylumCase, times(1)).clear(DECISION_AND_REASONS_COVER_LETTER);
        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_PDF);
    }

    @Test
    public void attaches_new_documents_and_when_cover_letter_and_pdf_generated() {

        when(sendDecisionAndReasonsCoverLetterService.create(caseDetails))
            .thenReturn(coverLetter);

        when(sendDecisionAndReasonsPdfService.generatePdf(caseDetails))
            .thenReturn(pdf);

        sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails);


        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

        verify(documentHandler, times(1))
            .addWithMetadata(
                asylumCase,
                pdf,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.FINAL_DECISION_AND_REASONS_PDF
            );

        verify(asylumCase, times(1)).clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
        verify(asylumCase, times(1)).clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);
    }
}
