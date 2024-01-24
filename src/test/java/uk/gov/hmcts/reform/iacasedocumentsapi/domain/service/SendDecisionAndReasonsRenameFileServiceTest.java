package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

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

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_PDF;

@ExtendWith(MockitoExtension.class)
public class SendDecisionAndReasonsRenameFileServiceTest {

    private final String binaryDocumentUrl = "binary-document-url";

    @Mock
    private DocumentDownloadClient documentDownloadClient;
    @Mock private DocumentUploader documentUploader;
    @Mock private DocumentReceiver documentReceiver;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document finalDecisionAndReasonsDocument;
    @Mock private Document uploadedDocument;
    @Mock private Resource finalDecisionAndReasonsResource;

    private SendDecisionAndReasonsRenameFileService sendDecisionAndReasonsPdfService;

    @BeforeEach
    public void setUp() throws IOException {

        sendDecisionAndReasonsPdfService = new SendDecisionAndReasonsRenameFileService(
            documentDownloadClient,
            documentUploader,
            documentReceiver,
            "some-file-name");
    }

    @Test
    public void downloads_and_updates_final_decision_and_reasons_pdf() {

        when(caseDetails.getCaseData())
                .thenReturn(asylumCase);

        when(finalDecisionAndReasonsDocument.getDocumentBinaryUrl())
                .thenReturn(binaryDocumentUrl);

        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
                .thenReturn(Optional.of(finalDecisionAndReasonsDocument));

        when(documentDownloadClient.download(binaryDocumentUrl))
                .thenReturn(finalDecisionAndReasonsResource);

        when(documentUploader.upload(any(ByteArrayResource.class), eq("application/pdf")))
                .thenReturn(uploadedDocument);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of("some-appeal-reference-number"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("some-family-name"));

        Document uploadedDecisionAndReasonsPdf = sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails);
        assertEquals(uploadedDecisionAndReasonsPdf, uploadedDocument);
        verify(documentDownloadClient, times(1))
            .download(binaryDocumentUrl);
        verify(documentUploader, times(1))
            .upload(any(ByteArrayResource.class), eq("application/pdf"));
        verify(caseDetails.getCaseData(),times(1)).write(FINAL_DECISION_AND_REASONS_PDF,uploadedDecisionAndReasonsPdf);

    }

    @Test
    public void throws_when_draft_document_missing() {


        when(caseDetails.getCaseData())
                .thenReturn(asylumCase);

        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("finalDecisionAndReasonsDocument must be present");

        verifyNoInteractions(documentDownloadClient);
        verifyNoInteractions(documentUploader);
    }

    @Test
    public void throws_when_draft_appeal_reference_number_missing() {


        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(finalDecisionAndReasonsDocument.getDocumentBinaryUrl())
            .thenReturn(binaryDocumentUrl);

        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
            .thenReturn(Optional.of(finalDecisionAndReasonsDocument));

        when(documentDownloadClient.download(binaryDocumentUrl))
            .thenReturn(finalDecisionAndReasonsResource);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal reference number not present");
    }

    @Test
    public void throws_when_draft_appellant_family_name_missing() {

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(finalDecisionAndReasonsDocument.getDocumentBinaryUrl())
            .thenReturn(binaryDocumentUrl);

        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class))
            .thenReturn(Optional.of(finalDecisionAndReasonsDocument));

        when(documentDownloadClient.download(binaryDocumentUrl))
            .thenReturn(finalDecisionAndReasonsResource);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of("some-appeal-reference-number"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sendDecisionAndReasonsPdfService.updateDecisionAndReasonsFileName(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellant family name not present");

    }
}
