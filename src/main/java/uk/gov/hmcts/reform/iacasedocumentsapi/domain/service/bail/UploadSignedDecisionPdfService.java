package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.bail;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.WordDocumentToPdfConverter;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.DocumentDownloadClient;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator.CASE_TYPE_ID;

@Service
@Slf4j
public class UploadSignedDecisionPdfService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final DocumentDownloadClient documentDownloadClient;
    private final DocumentUploader documentUploader;
    private final WordDocumentToPdfConverter wordDocumentToPdfConverter;
    private final String signedDecisionFinalPdfFilename;

    public UploadSignedDecisionPdfService(
        DocumentDownloadClient documentDownloadClient,
        DocumentUploader documentUploader,
        WordDocumentToPdfConverter wordDocumentToPdfConverter,
        @Value("${decisionSignedDocumentFinalPdf.fileName}") String signedDecisionFinalPdfFilename
    ) {
        this.documentDownloadClient = documentDownloadClient;
        this.documentUploader = documentUploader;
        this.wordDocumentToPdfConverter = wordDocumentToPdfConverter;
        this.signedDecisionFinalPdfFilename = signedDecisionFinalPdfFilename;
    }

    public Document generatePdf(CaseDetails<BailCase> caseDetails) {

        Document finalPdf = createFinalPdf(caseDetails.getCaseData());

        //Replacing the word document with the generated Pdf or pdf document with renamed document
        caseDetails.getCaseData().write(BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, finalPdf);

        return finalPdf;
    }

    private Document createFinalPdf(BailCase bailCase) {

        Document signedDecisionDocument = bailCase.read(BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT, Document.class)
            .orElseThrow(
                () -> new IllegalStateException("Signed decision document must be present"));

        Resource resource =
            documentDownloadClient.download(signedDecisionDocument.getDocumentBinaryUrl());

        File signedDecisionNoticePdf =
            wordDocumentToPdfConverter.convertResourceToPdf(resource);

        ByteArrayResource byteArrayResource = getByteArrayResource(
            signedDecisionNoticePdf,
            getSignedDecisionNoticeFilename(bailCase));

        log.info("Uploading final pdf for bail case");

        return documentUploader.upload(
            byteArrayResource,
            CASE_TYPE_ID,
            "IA",
            PDF_CONTENT_TYPE
        );
    }

    private ByteArrayResource getByteArrayResource(File signedDecisionFinalPdf, String filename) {

        byte[] byteArray;

        try {
            byteArray = FileUtils.readFileToByteArray(signedDecisionFinalPdf);

        } catch (IOException e) {
            throw new IllegalStateException("Error reading converted signed pdf");
        }

        return new ByteArrayResource(byteArray) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private String getSignedDecisionNoticeFilename(BailCase bailCase) {

        String applicantFamilyName = bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)
            .orElseThrow(() -> new IllegalStateException("Applicant family name not present"));

        return applicantFamilyName
            + "-"
            + signedDecisionFinalPdfFilename + ".pdf";
    }
}
