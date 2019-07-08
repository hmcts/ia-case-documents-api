package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_AND_REASONS_COVER_LETTER;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;

@Service
public class SendDecisionAndReasonsCoverLetterService {

    private final DocumentCreator<AsylumCase> decisionAndReasonsCoverLetterDocumentCreator;

    public SendDecisionAndReasonsCoverLetterService(
        @Qualifier("decisionAndReasonsCoverLetter") DocumentCreator<AsylumCase> decisionAndReasonsCoverLetterDocumentCreator
    ) {
        this.decisionAndReasonsCoverLetterDocumentCreator = decisionAndReasonsCoverLetterDocumentCreator;
    }

    public Document create(CaseDetails<AsylumCase> caseDetails) {

        Document coverLetter = decisionAndReasonsCoverLetterDocumentCreator.create(caseDetails);

        caseDetails.getCaseData().write(DECISION_AND_REASONS_COVER_LETTER, coverLetter);

        return coverLetter;
    }

}
