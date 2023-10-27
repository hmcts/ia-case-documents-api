package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@Service
public class SendDecisionAndReasonsOrchestrator {

    private final DocumentHandler documentHandler;
    private final SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService;

    public SendDecisionAndReasonsOrchestrator(
        DocumentHandler documentHandler,
        SendDecisionAndReasonsCoverLetterService sendDecisionAndReasonsCoverLetterService
    ) {
        this.documentHandler = documentHandler;
        this.sendDecisionAndReasonsCoverLetterService = sendDecisionAndReasonsCoverLetterService;
    }

    public void sendDecisionAndReasons(CaseDetails<AsylumCase> caseDetails) {

        AsylumCase asylumCase = caseDetails.getCaseData();

        try {

            Document coverLetter = requireNonNull(
                sendDecisionAndReasonsCoverLetterService.create(caseDetails),
                "Cover letter creation failed");

            Document finalDecisionAndReasonsPdf =
                    asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENT, Document.class)
                            .orElseThrow(
                                    () -> new IllegalStateException("finalDecisionAndReasonsDocument must be present"));
            asylumCase.write(FINAL_DECISION_AND_REASONS_PDF, finalDecisionAndReasonsPdf);

            if ((asylumCase.read(AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED, YesOrNo.class).equals(Optional.of(YesOrNo.YES))
                 && (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)))) {

                documentHandler.addWithMetadata(
                    asylumCase,
                    coverLetter,
                    REHEARD_DECISION_REASONS_DOCUMENTS,
                    DocumentTag.DECISION_AND_REASONS_COVER_LETTER
                );

                documentHandler.addWithMetadata(
                    asylumCase,
                    finalDecisionAndReasonsPdf,
                    REHEARD_DECISION_REASONS_DOCUMENTS,
                    DocumentTag.FINAL_DECISION_AND_REASONS_PDF
                );

            } else {

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
            }

            asylumCase.clear(FINAL_DECISION_AND_REASONS_DOCUMENT);
            asylumCase.clear(DRAFT_DECISION_AND_REASONS_DOCUMENTS);
            asylumCase.clear(DRAFT_REHEARD_DECISION_AND_REASONS);

        } catch (RuntimeException e) {

            asylumCase.clear(DECISION_AND_REASONS_COVER_LETTER);
            asylumCase.clear(FINAL_DECISION_AND_REASONS_PDF);

            throw e;
        }
    }
}
