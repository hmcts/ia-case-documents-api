package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator.CASE_TYPE_ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator.CLASSIFICATION;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;

@Service
public class SendDecisionAndReasonsRenameFileService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final DocumentDownloadClient documentDownloadClient;
    private final DocumentUploader documentUploader;
    private final String decisionAndReasonsFinalPdfFilename;

    public SendDecisionAndReasonsRenameFileService(
        DocumentDownloadClient documentDownloadClient,
        DocumentUploader documentUploader,
        @Value("${decisionAndReasonsFinalPdf.fileName}") String decisionAndReasonsFinalPdfFilename
    ) {
        this.documentDownloadClient = documentDownloadClient;
        this.documentUploader = documentUploader;
        this.decisionAndReasonsFinalPdfFilename = decisionAndReasonsFinalPdfFilename;
    }

    public Document updateDecisionAndReasonsFileName(CaseDetails<AsylumCase> caseDetails) {

        Document finalPdf = createFinalPdf(caseDetails.getCaseData());

        caseDetails.getCaseData().write(FINAL_DECISION_AND_REASONS_PDF, finalPdf);

        return finalPdf;
    }

    private Document createFinalPdf(AsylumCase asylumCase) {

        Document finalDecisionAndReasonsDoc = asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class)
            .orElseThrow(
                () -> new IllegalStateException("finalDecisionAndReasonsDocument must be present"));

        Resource finalDecisionAndReasonsPdf =
            documentDownloadClient.download(finalDecisionAndReasonsDoc.getDocumentBinaryUrl());

        ByteArrayResource byteArrayResource = getByteArrayResource(
            finalDecisionAndReasonsPdf,
            getDecisionAndReasonsFilename(asylumCase));

        return documentUploader.upload(
                byteArrayResource,
                CLASSIFICATION,
                CASE_TYPE_ID,
                asylumCase.getJurisdiction(),
                PDF_CONTENT_TYPE
        );
    }

    private ByteArrayResource getByteArrayResource(Resource finalDecisionAndReasonsPdf, String filename) {

        byte[] byteArray;

        try {
            byteArray = StreamUtils.copyToByteArray(finalDecisionAndReasonsPdf.getInputStream());

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
