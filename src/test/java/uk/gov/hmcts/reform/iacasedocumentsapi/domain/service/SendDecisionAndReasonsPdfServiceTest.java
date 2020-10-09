package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.io.File.createTempFile;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;

@ExtendWith(MockitoExtension.class)
public class SendDecisionAndReasonsPdfServiceTest {

    private final String binaryDocumentUrl = "binary-document-url";

    private File convertedPdf;

    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock private DocumentUploader documentUploader;
    @Mock private WordDocumentToPdfConverter wordDocumentToPdfConverter;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document finalDecisionAndReasonsDocument;
    @Mock private Document uploadedDocument;
    @Mock private Resource finalDecisionAndReasonsResource;

    private SendDecisionAndReasonsPdfService sendDecisionAndReasonsPdfService;

    @BeforeEach
    public void setUp() throws IOException {

        sendDecisionAndReasonsPdfService = new SendDecisionAndReasonsPdfService(
            documentDownloadClient,
            documentUploader,
            wordDocumentToPdfConverter,
            "some-file-name");

        convertedPdf = createTempFile("test-file", ".pdf");
    }

    @Test
    public void downloads__converts__and_uploads_final_decision_and_reasons_pdf() {


        when(caseDetails.getCaseData())
                .thenReturn(asylumCase);

        when(finalDecisionAndReasonsDocument.getDocumentBinaryUrl())
                .thenReturn(binaryDocumentUrl);

        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
                .thenReturn(Optional.of(finalDecisionAndReasonsDocument));

        when(documentDownloadClient.download(binaryDocumentUrl))
                .thenReturn(finalDecisionAndReasonsResource);

        when(wordDocumentToPdfConverter.convertResourceToPdf(finalDecisionAndReasonsResource))
                .thenReturn(convertedPdf);

        when(documentUploader.upload(any(ByteArrayResource.class), eq("application/pdf")))
                .thenReturn(uploadedDocument);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("some-appeal-reference-number"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("some-family-name"));

        Document uploadedDecisionAndReasonsPdf = sendDecisionAndReasonsPdfService.generatePdf(caseDetails);
        assertEquals(uploadedDecisionAndReasonsPdf, uploadedDocument);
        verify(documentDownloadClient, times(1))
            .download(binaryDocumentUrl);
        verify(wordDocumentToPdfConverter, times(1))
            .convertResourceToPdf(finalDecisionAndReasonsResource);
        verify(documentUploader, times(1))
            .upload(any(ByteArrayResource.class), eq("application/pdf"));
    }

    @Test
    public void throws_when_draft_document_missing() {


        when(caseDetails.getCaseData())
                .thenReturn(asylumCase);

        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDecisionAndReasonsPdfService.generatePdf(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("finalDecisionAndReasonsDocument must be present");

        verifyNoInteractions(documentDownloadClient);
        verifyNoInteractions(documentUploader);
        verifyNoInteractions(wordDocumentToPdfConverter);
    }
}
