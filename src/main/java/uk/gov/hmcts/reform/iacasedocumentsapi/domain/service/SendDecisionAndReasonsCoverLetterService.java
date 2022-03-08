package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_AND_REASONS_COVER_LETTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.AIP;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;

@Service
public class SendDecisionAndReasonsCoverLetterService {

    private final DocumentCreator<AsylumCase> decisionAndReasonsCoverLetterDocumentCreator;
    private final DocumentCreator<AsylumCase> aipDecisionAndReasonsCoverLetterDocumentCreator;

    public SendDecisionAndReasonsCoverLetterService(
        @Qualifier("decisionAndReasonsCoverLetter") DocumentCreator<AsylumCase> decisionAndReasonsCoverLetterDocumentCreator,
        @Qualifier("aipDecisionAndReasonsCoverLetter") DocumentCreator<AsylumCase> aipDecisionAndReasonsCoverLetterDocumentCreator
    ) {
        this.decisionAndReasonsCoverLetterDocumentCreator = decisionAndReasonsCoverLetterDocumentCreator;
        this.aipDecisionAndReasonsCoverLetterDocumentCreator = aipDecisionAndReasonsCoverLetterDocumentCreator;
    }

    public Document create(CaseDetails<AsylumCase> caseDetails) {
        Document coverLetter = getCoverLetterCreatorInstance(caseDetails).create(caseDetails);
        caseDetails.getCaseData().write(DECISION_AND_REASONS_COVER_LETTER, coverLetter);

        return coverLetter;
    }

    private DocumentCreator<AsylumCase> getCoverLetterCreatorInstance(CaseDetails<AsylumCase> caseDetails) {
        boolean isAipJourney = caseDetails.getCaseData().read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);

        return isAipJourney ? aipDecisionAndReasonsCoverLetterDocumentCreator :
            decisionAndReasonsCoverLetterDocumentCreator;
    }

}
