package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SIGNED_DECISION_DOCUMENT_WITH_METADATA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.bail.UploadSignedDecisionOrchestrator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.bail.UploadSignedDecisionPdfService;

@ExtendWith(MockitoExtension.class)
public class UploadSignedDecisionOrchestratorTest {

    @Mock private BailDocumentHandler documentHandler;
    @Mock private UploadSignedDecisionPdfService uploadSignedDecisionPdfService;
    @Mock private BailCase bailCase;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private Document finalSignedDecisionPdf;

    private UploadSignedDecisionOrchestrator uploadSignedDecisionOrchestrator;

    @BeforeEach
    void setUp() {
        uploadSignedDecisionOrchestrator = new UploadSignedDecisionOrchestrator(
            documentHandler,
            uploadSignedDecisionPdfService);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(uploadSignedDecisionPdfService.generatePdf(caseDetails)).thenReturn(finalSignedDecisionPdf);
    }

    @Test
    void test_generate_pdf_service_called_new_document_attached() {
        uploadSignedDecisionOrchestrator.uploadSignedDecision(caseDetails);
        verify(uploadSignedDecisionPdfService, times(1)).generatePdf(caseDetails);
        verify(bailCase, times(1)).clear(BailCaseFieldDefinition.DECISION_UNSIGNED_DOCUMENT);
        verify(bailCase, times(1)).clear(BailCaseFieldDefinition.UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA);
        verify(documentHandler, times(1))
            .addWithMetadata(
                bailCase,
                finalSignedDecisionPdf,
                SIGNED_DECISION_DOCUMENT_WITH_METADATA,
                DocumentTag.SIGNED_DECISION_NOTICE
            );
    }

    @Test
    void test_throw_exception_for_null_pdf_generation() {
        when(uploadSignedDecisionPdfService.generatePdf(caseDetails)).thenReturn(null);
        assertThatThrownBy(() -> uploadSignedDecisionOrchestrator.uploadSignedDecision(caseDetails))
            .hasMessage("Document to pdf conversion failed")
            .isExactlyInstanceOf(NullPointerException.class);

        verifyNoInteractions(documentHandler);
        verify(bailCase, times(1)).clear(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT);
    }

    @Test
    void test_throw_exception_for_failed_pdf_generation() {
        when(uploadSignedDecisionPdfService.generatePdf(caseDetails))
            .thenThrow(RuntimeException.class);
        assertThatThrownBy(() -> uploadSignedDecisionOrchestrator.uploadSignedDecision(caseDetails))
            .isInstanceOf(RuntimeException.class);

        verifyNoInteractions(documentHandler);
        verify(bailCase, times(1)).clear(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT);
    }

}
