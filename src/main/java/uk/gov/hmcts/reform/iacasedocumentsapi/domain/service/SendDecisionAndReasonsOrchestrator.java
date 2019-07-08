package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@Service
public class SendDecisionAndReasonsOrchestrator {

    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final SendDecisionAndReasonsPdfService sendDecisionAndReasonsPdfService;
    private final SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;

    public SendDecisionAndReasonsOrchestrator(
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        SendDecisionAndReasonsPdfService sendDecisionAndReasonsPdfService,
        SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService
    ) {
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
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

            attachDocumentToCase(
                asylumCase,
                coverLetter,
                DocumentTag.DECISION_AND_REASONS_COVER_LETTER);

            attachDocumentToCase(
                asylumCase,
                finalDecisionAndReasonsPdf,
                DocumentTag.FINAL_DECISION_AND_REASONS_PDF);

            asylumCase.clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
            asylumCase.clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);

        } catch (RuntimeException e) {

            asylumCase.clear(DECISION_AND_REASONS_COVER_LETTER);
            asylumCase.clear(FINAL_DECISION_AND_REASONS_PDF);

            throw e;
        }
    }

    private void attachDocumentToCase(
        AsylumCase asylumCase,
        Document document,
        DocumentTag documentTag
    ) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeDocuments =
            asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> decisionAndReasonsDocuments =
            maybeDocuments
                .orElse(emptyList());

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                document,
                "",
                documentTag
            );

        List<IdValue<DocumentWithMetadata>> allFinalDecisionAndReasonsDocuments =
            documentsAppender.append(
                decisionAndReasonsDocuments,
                singletonList(documentWithMetadata),
                documentTag
            );

        asylumCase.write(FINAL_DECISION_AND_REASONS_DOCUMENTS, allFinalDecisionAndReasonsDocuments);
    }
}
