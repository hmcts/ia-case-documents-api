package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;

@Slf4j
public class DocumentCreator<T extends CaseData> {

    public static final String CASE_TYPE_ID = "Asylum";

    private final String documentContentType;
    private final String documentFileExtension;
    private final String documentFileName;
    private final FileNameQualifier<T> fileNameQualifier;
    private final DocumentTemplate<T> documentTemplate;
    private final DocumentGenerator documentGenerator;
    private final DocumentUploader documentUploader;

    public DocumentCreator(
        String documentContentType,
        String documentFileExtension,
        String documentFileName,
        FileNameQualifier<T> fileNameQualifier,
        DocumentTemplate<T> documentTemplate,
        DocumentGenerator documentGenerator,
        DocumentUploader documentUploader
    ) {
        this.documentContentType = documentContentType;
        this.documentFileExtension = documentFileExtension;
        this.documentFileName = documentFileName;
        this.fileNameQualifier = fileNameQualifier;
        this.documentTemplate = documentTemplate;
        this.documentGenerator = documentGenerator;
        this.documentUploader = documentUploader;
    }

    public Document create(
        CaseDetails<T> caseDetails
    ) {
        return create(caseDetails, null);
    }

    public Document create(
        CaseDetails<T> caseDetails,
        CaseDetails<T> caseDetailsBefore
    ) {
        final String qualifiedDocumentFileName = fileNameQualifier.get(documentFileName, caseDetails);
        final String templateName = documentTemplate.getName();

        Resource documentResource =
            documentGenerator.generate(
                qualifiedDocumentFileName,
                documentFileExtension,
                templateName,
                Optional.ofNullable(caseDetailsBefore)
                    .map(it -> documentTemplate.mapFieldValues(caseDetails, it))
                    .orElse(documentTemplate.mapFieldValues(caseDetails))
            );

        log.info("Uploading document: {}", qualifiedDocumentFileName);

        return documentUploader.upload(
            documentResource,
            CASE_TYPE_ID,
            caseDetails.getJurisdiction(),
            documentContentType
        );
    }
}
