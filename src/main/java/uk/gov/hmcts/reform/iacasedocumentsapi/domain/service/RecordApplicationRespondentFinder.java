package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils;

@Slf4j
@Service
public class RecordApplicationRespondentFinder {
    public static final String NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING = "No email address for decisions made without hearing";

    private final String recordApplicationHomeOfficeEmailAddress;
    private final String respondentReviewEmailAddress;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;

    public RecordApplicationRespondentFinder(
        @Value("${endAppealHomeOfficeEmailAddress}") String recordApplicationHomeOfficeEmailAddress,
        @Value("${respondentEmailAddresses.respondentReviewDirection}") String respondentReviewEmailAddress,
        Map<HearingCentre, String> homeOfficeEmailAddresses) {

        this.recordApplicationHomeOfficeEmailAddress = recordApplicationHomeOfficeEmailAddress;
        this.respondentReviewEmailAddress = respondentReviewEmailAddress;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
    }


    public String getRespondentEmail(AsylumCase asylumCase) {

        return asylumCase
            .read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
            .map(state -> mapAppealStateToRespondentEmail(asylumCase, state))
            .orElse(null);
    }

    public boolean requiresEmail(AsylumCase asylumCase) {
        return getRespondentEmail(asylumCase) != null;
    }

    private String mapAppealStateToRespondentEmail(AsylumCase asylumCase, State state) {

        return switch (state) {
            case APPEAL_SUBMITTED,
                PENDING_PAYMENT,
                AWAITING_RESPONDENT_EVIDENCE,
                CASE_BUILDING,
                CASE_UNDER_REVIEW,
                AWAITING_REASONS_FOR_APPEAL,
                REASONS_FOR_APPEAL_SUBMITTED,
                SUBMIT_HEARING_REQUIREMENTS,
                LISTING  -> recordApplicationHomeOfficeEmailAddress;
            case RESPONDENT_REVIEW -> respondentReviewEmailAddress;
            case PREPARE_FOR_HEARING, FINAL_BUNDLING, PRE_HEARING, DECISION, DECIDED ->
                AsylumCaseUtils.isDecisionWithoutHearingAppeal(asylumCase)
                    ? NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING
                    : homeOfficeEmailAddresses.get(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                        .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present")));
            default -> null;
        };
    }
}
