package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@Service
public class SendDecisionAndReasonsOrchestrator {

    private final DocumentHandler documentHandler;
    private final SendDecisionAndReasonsPdfService sendDecisionAndReasonsPdfService;
    private final SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;

    public SendDecisionAndReasonsOrchestrator(
        DocumentHandler documentHandler,
        SendDecisionAndReasonsPdfService sendDecisionAndReasonsPdfService,
        SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService
    ) {
        this.documentHandler = documentHandler;
        this.sendDecisionAndReasonsPdfService = sendDecisionAndReasonsPdfService;
        this.sendDecisionAndReasonsCoverLetterService = sendDecisionAndReasonsCoverLetterService;
    }

    public void sendDecisionAndReasons(CaseDetails<AsylumCase> caseDetails) {

        AsylumCase asylumCase = caseDetails.getCaseData();

        try {

            Document coverLetter = requireNonNull(
                sendDecisionAndReasonsCoverLetterService.create(caseDetails),
                "Cover letter creation failed");

            Document finalDecisionAndReasonsPdf =
                sendDecisionAndReasonsPdfService.generatePdf(caseDetails);

            requireNonNull(finalDecisionAndReasonsPdf, "Document to pdf conversion failed");

            documentHandler.addWithMetadata(
                asylumCase,
                coverLetter,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER
            );

            documentHandler.addWithMetadata(
                asylumCase,
                finalDecisionAndReasonsPdf,
                FINAL_DECISION_AND_REASONS_DOCUMENTS,
                DocumentTag.FINAL_DECISION_AND_REASONS_PDF
            );

            asylumCase.clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
            asylumCase.clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);

        } catch (RuntimeException e) {

            asylumCase.clear(DECISION_AND_REASONS_COVER_LETTER);
            asylumCase.clear(FINAL_DECISION_AND_REASONS_PDF);

            throw e;
        }
    }
}
