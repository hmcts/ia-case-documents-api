package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;

@Service
public class SendDecisionAndReasonsPdfService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final DocumentDownloadClient documentDownloadClient;
    private final DocumentUploader documentUploader;
    private final WordDocumentToPdfConverter wordDocumentToPdfConverter;
    private final String decisionAndReasonsFinalPdfFilename;

    public SendDecisionAndReasonsPdfService(
        DocumentDownloadClient documentDownloadClient,
        DocumentUploader documentUploader,
        WordDocumentToPdfConverter wordDocumentToPdfConverter,
        @Value("${decisionAndReasonsFinalPdf.fileName}") String decisionAndReasonsFinalPdfFilename
    ) {
        this.documentDownloadClient = documentDownloadClient;
        this.documentUploader = documentUploader;
        this.wordDocumentToPdfConverter = wordDocumentToPdfConverter;
        this.decisionAndReasonsFinalPdfFilename = decisionAndReasonsFinalPdfFilename;
    }

    public Document generatePdf(CaseDetails<AsylumCase> caseDetails) {

        Document finalPdf = createFinalPdf(caseDetails.getCaseData());

        caseDetails.getCaseData().write(FINAL_DECISION_AND_REASONS_PDF, finalPdf);

        return finalPdf;
    }

    private Document createFinalPdf(AsylumCase asylumCase) {

        Document finalDecisionAndReasonsDoc = asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class)
            .orElseThrow(
                () -> new IllegalStateException("finalDecisionAndReasonsDocument must be present"));

        Resource resource =
            documentDownloadClient.download(finalDecisionAndReasonsDoc.getDocumentBinaryUrl());

        File finalDecisionAndReasonsPdf =
            wordDocumentToPdfConverter.convertResourceToPdf(resource);

        ByteArrayResource byteArrayResource = getByteArrayResource(
            finalDecisionAndReasonsPdf,
            getDecisionAndReasonsFilename(asylumCase));

        return documentUploader.upload(byteArrayResource, PDF_CONTENT_TYPE);
    }

    private ByteArrayResource getByteArrayResource(File finalDecisionAndReasonsPdf, String filename) {

        byte[] byteArray;

        try {
            byteArray = FileUtils.readFileToByteArray(finalDecisionAndReasonsPdf);

        } catch (IOException e) {
            throw new IllegalStateException("Error reading converted decision and reasons pdf");
        }

        return new ByteArrayResource(byteArray) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private String getDecisionAndReasonsFilename(AsylumCase asylumCase) {

        String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Appeal reference number not present"));

        String appellantFamilyName = asylumCase.read(APPELLANT_FAMILY_NAME, String.class)
            .orElseThrow(() -> new IllegalStateException("appellant family name not present"));

        return appealReferenceNumber.replace("/", " ")
            + "-"
            + appellantFamilyName
            + "-"
            + decisionAndReasonsFinalPdfFilename + ".pdf";
    }
}
