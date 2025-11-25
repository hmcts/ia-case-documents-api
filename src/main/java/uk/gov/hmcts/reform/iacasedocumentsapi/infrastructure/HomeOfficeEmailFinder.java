package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;

@Service
public class HomeOfficeEmailFinder {
    public static final String CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT = "currentCaseStateVisibleToHomeOfficeAll flag is not present";
    String apcHomeOfficeEmailAddress;
    String lartHomeOfficeEmailAddress;

    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;

    public HomeOfficeEmailFinder(AppealService appealService,
                                 EmailAddressFinder emailAddressFinder,
                                 @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
                                 @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress) {
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
    }

    /*
    * This method is used to find the home office email address based on the current state of the case.
    * @param asylumCase
    * @return Set<String>
    * */
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        Optional<State> mayBeState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class);

        return mayBeState
                .map(currentState -> {
                    if (Arrays.asList(
                            State.APPEAL_SUBMITTED,
                            State.PENDING_PAYMENT,
                            State.AWAITING_RESPONDENT_EVIDENCE,
                            State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
                            State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED
                    ).contains(currentState)) {
                        return Collections.singleton(apcHomeOfficeEmailAddress);
                    } else if (Arrays.asList(
                            State.CASE_BUILDING,
                            State.CASE_UNDER_REVIEW,
                            State.RESPONDENT_REVIEW,
                            State.AWAITING_REASONS_FOR_APPEAL,
                            State.REASONS_FOR_APPEAL_SUBMITTED
                    ).contains(currentState)) {
                        return Collections.singleton(lartHomeOfficeEmailAddress);
                    } else if (Arrays.asList(
                            State.FTPA_SUBMITTED,
                            State.FTPA_DECIDED).contains(currentState)) {
                        return Collections.singleton(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase));
                    } else if (Arrays.asList(
                            State.LISTING,
                            State.SUBMIT_HEARING_REQUIREMENTS,
                            State.ENDED,
                            State.APPEAL_TAKEN_OFFLINE).contains(currentState)
                            && !appealService.isAppealListed(asylumCase)) {
                        return  Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
                    } else if (Arrays.asList(
                            State.LISTING,
                            State.PREPARE_FOR_HEARING,
                            State.FINAL_BUNDLING,
                            State.PRE_HEARING,
                            State.DECISION,
                            State.ADJOURNED,
                            State.DECIDED,
                            State.ENDED,
                            State.APPEAL_TAKEN_OFFLINE
                    ).contains(currentState) && appealService.isAppealListed(asylumCase)) {
                        final Optional<HearingCentre> maybeCaseIsListed = asylumCase
                                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

                        if (maybeCaseIsListed.isPresent()) {
                            return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
                        } else {
                            return  Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
                        }
                    }
                    throw new IllegalStateException(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT);
                })
                .orElseThrow(() -> new IllegalStateException(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT));
    }
}
