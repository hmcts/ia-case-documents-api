package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.bail;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.DECISION_UNSIGNED_DOCUMENT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.SIGNED_DECISION_DOCUMENT_WITH_METADATA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailDocumentHandler;

@Service
public class UploadSignedDecisionOrchestrator {

    private final BailDocumentHandler documentHandler;
    private final UploadSignedDecisionPdfService uploadSignedDecisionPdfService;

    public UploadSignedDecisionOrchestrator(
        BailDocumentHandler documentHandler,
        UploadSignedDecisionPdfService uploadSignedDecisionPdfService
    ) {
        this.documentHandler = documentHandler;
        this.uploadSignedDecisionPdfService = uploadSignedDecisionPdfService;
    }

    public void uploadSignedDecision(CaseDetails<BailCase> caseDetails) {

        BailCase bailCase = caseDetails.getCaseData();

        try {
            Document finalSignedDecisionPdf =
                uploadSignedDecisionPdfService.generatePdf(caseDetails);

            requireNonNull(finalSignedDecisionPdf, "Document to pdf conversion failed");

            documentHandler.addWithMetadata(
                bailCase,
                finalSignedDecisionPdf,
                SIGNED_DECISION_DOCUMENT_WITH_METADATA,
                DocumentTag.SIGNED_DECISION_NOTICE
            );
            bailCase.clear(DECISION_UNSIGNED_DOCUMENT);
            bailCase.clear(UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA);
        } catch (RuntimeException e) {
            bailCase.clear(UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT);
            throw e;
        }
    }
}
