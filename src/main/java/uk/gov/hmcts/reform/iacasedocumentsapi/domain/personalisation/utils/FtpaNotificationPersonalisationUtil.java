package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;

public interface FtpaNotificationPersonalisationUtil {

    String APPELLANT_APPLICANT = "appellant";
    String APPELLANT_RESPONDENT = "respondent";

    default Optional<FtpaDecisionOutcomeType> ftpaRespondentLjRjDecision(AsylumCase asylumCase) {
        return asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .or(() -> asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class));
    }

    default Optional<FtpaDecisionOutcomeType> ftpaAppellantLjRjDecision(AsylumCase asylumCase) {
        return asylumCase
            .read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .or(() -> asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class));
    }

    default String camelCaseToSentenceCase(FtpaDecisionOutcomeType decisionType) {
        switch (decisionType) {
            case FTPA_GRANTED:
                return "granted";
            case FTPA_PARTIALLY_GRANTED:
                return "partially granted";
            case FTPA_REFUSED:
                return "refused";
            case FTPA_NOT_ADMITTED:
                return "not admitted";
            default:
                return "";
        }
    }

    default String ftpaDecisionVerbalization(FtpaDecisionOutcomeType decision) {
        return camelCaseToSentenceCase(decision);
    }

    default FtpaDecisionOutcomeType getDecisionOutcomeType(AsylumCase asylumCase) {
        String applicantType = asylumCase
            .read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .map(ApplicantType::getValue)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"));

        FtpaDecisionOutcomeType decisionOutcomeType;

        if (applicantType.equals(APPELLANT_APPLICANT)) {
            decisionOutcomeType = ftpaAppellantLjRjDecision(asylumCase)
                .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionOutcomeType is not present"));
        } else if (applicantType.equals(APPELLANT_RESPONDENT)) {
            decisionOutcomeType = ftpaRespondentLjRjDecision(asylumCase)
                .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present"));
        } else {
            throw new IllegalStateException("applicantType not of type appellant or respondent");
        }

        return decisionOutcomeType;
    }

    default String dueDate(long days) {
        return LocalDate.now().plusDays(days).format(DateTimeFormatter.ofPattern("d MMM yyyy"));
    }
}
