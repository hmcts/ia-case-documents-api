package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static java.io.File.createTempFile;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.PREVIOUS_DECISION_DETAILS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PreviousDecisionDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentToPdfConverter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.bail.UploadSignedDecisionPdfService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UploadSignedDecisionPdfServiceTest {

    @Mock private DocumentDownloadClient documentDownloadClient;
    @Mock private DocumentUploader documentUploader;
    @Mock private DocumentToPdfConverter documentToPdfConverter;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private Document mockSignedDecisionDocument;
    @Mock private Document mockSignedGeneratedPdfDocument;
    @Mock private Resource mockResource;
    @Mock private File mockSignedDecisionNoticePdf;

    private final String binaryDocumentUrl = "binary-document-url";

    private UploadSignedDecisionPdfService uploadSignedDecisionPdfService;

    @BeforeEach
    void setUp() throws IOException {
        uploadSignedDecisionPdfService = new UploadSignedDecisionPdfService(
            documentDownloadClient,
            documentUploader,
            documentToPdfConverter,
            "decision-notice"
        );
        mockSignedDecisionNoticePdf = createTempFile("test-file", ".pdf");
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class))
            .thenReturn(Optional.of(mockSignedDecisionDocument));
        when(mockSignedDecisionDocument.getDocumentBinaryUrl())
            .thenReturn(binaryDocumentUrl);
        when(documentDownloadClient.download(binaryDocumentUrl))
            .thenReturn(mockResource);
        when(documentToPdfConverter.convertWordDocResourceToPdf(mockResource))
            .thenReturn(mockSignedDecisionNoticePdf);
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("Smith"));
        when(documentUploader.upload(any(ByteArrayResource.class), eq("application/pdf")))
            .thenReturn(mockSignedGeneratedPdfDocument);
    }

    @Test
    void test_generate_pdf() {
        Document finalPdf = uploadSignedDecisionPdfService.generatePdf(caseDetails);
        assertNotNull(finalPdf);
        assertEquals(mockSignedGeneratedPdfDocument, finalPdf);
        verify(documentDownloadClient, times(1))
            .download(binaryDocumentUrl);
        verify(documentToPdfConverter, times(1))
            .convertWordDocResourceToPdf(mockResource);
        verify(bailCase, times(1))
            .write(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, finalPdf);
        verify(bailCase, times(1))
            .read(APPLICANT_FAMILY_NAME, String.class);
        verify(bailCase, times(1)).read(PREVIOUS_DECISION_DETAILS);
    }

    @Test
    void test_generate_pdf_with_previous_pdf() {
        List<PreviousDecisionDetails> storedPrevDecisionDetails =
            List.of(new PreviousDecisionDetails(
                "some-old-date",
                "some-old-type",
                mock(Document.class)));
        List<IdValue<PreviousDecisionDetails>> idValueStoredPrevDecisionDetails = new ArrayList<>();
        idValueStoredPrevDecisionDetails.add(new IdValue<>("1", storedPrevDecisionDetails.get(0)));
        when(bailCase.read(PREVIOUS_DECISION_DETAILS)).thenReturn(Optional.of(idValueStoredPrevDecisionDetails));
        Document finalPdf = uploadSignedDecisionPdfService.generatePdf(caseDetails);
        assertNotNull(finalPdf);
        assertEquals(mockSignedGeneratedPdfDocument, finalPdf);
        verify(documentDownloadClient, times(1))
            .download(binaryDocumentUrl);
        verify(documentToPdfConverter, times(1))
            .convertWordDocResourceToPdf(mockResource);
        verify(bailCase, times(1)).read(PREVIOUS_DECISION_DETAILS);
        verify(bailCase, times(1))
            .write(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, finalPdf);
        verify(bailCase, times(1))
            .read(APPLICANT_FAMILY_NAME, String.class);
    }

    @Test
    void throws_when_decision_document_is_missing() {
        when(bailCase.read(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> uploadSignedDecisionPdfService.generatePdf(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Signed decision document must be present");
        verifyNoInteractions(documentDownloadClient);
        verifyNoInteractions(documentUploader);
        verifyNoInteractions(documentToPdfConverter);
    }

    @Test
    void throws_when_family_name_is_missing() {
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> uploadSignedDecisionPdfService.generatePdf(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Applicant family name not present");
        verifyNoInteractions(documentUploader);
        verify(documentDownloadClient, times(1))
            .download(binaryDocumentUrl);
        verify(documentToPdfConverter, times(1))
            .convertWordDocResourceToPdf(mockResource);
    }


}
