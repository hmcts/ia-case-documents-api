package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;

@Slf4j
@Service
public class RecordApplicationRespondentFinder {
    private final String recordApplicationHomeOfficeEmailAddress;
    private final String respondentReviewEmailAddress;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;

    public RecordApplicationRespondentFinder(@Value("${endAppealHomeOfficeEmailAddress}") String recordApplicationHomeOfficeEmailAddress,
        @Value("${respondentEmailAddresses.respondentReviewDirection}") String respondentReviewEmailAddress,
                                             Map<HearingCentre, String> homeOfficeEmailAddresses) {
        this.recordApplicationHomeOfficeEmailAddress = recordApplicationHomeOfficeEmailAddress;
        this.respondentReviewEmailAddress = respondentReviewEmailAddress;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
    }


    public String getRespondentEmail(AsylumCase asylumCase) {
        State state = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
                .orElse(null);

        if (state == null) {
            return null;
        }

        if (state.equals(State.APPEAL_SUBMITTED)
            || state.equals(State.AWAITING_RESPONDENT_EVIDENCE)
            || state.equals(State.CASE_BUILDING)
            || state.equals(State.CASE_UNDER_REVIEW)
            || state.equals(State.AWAITING_REASONS_FOR_APPEAL)
            || state.equals(State.REASONS_FOR_APPEAL_SUBMITTED)
            || state.equals(State.SUBMIT_HEARING_REQUIREMENTS)
            || state.equals(State.LISTING)) {
            return recordApplicationHomeOfficeEmailAddress;
        } else if (state.equals(State.RESPONDENT_REVIEW)) {
            return respondentReviewEmailAddress;
        } else if (state.equals(State.PREPARE_FOR_HEARING)
            || state.equals(State.FINAL_BUNDLING)
            || state.equals(State.PRE_HEARING)
            || state.equals(State.DECISION)
            || state.equals(State.DECIDED)) {
            return homeOfficeEmailAddresses.get(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present")));
        } else {
            return null;
        }
    }

    public boolean requiresEmail(AsylumCase asylumCase) {
        return getRespondentEmail(asylumCase) != null;
    }
}
