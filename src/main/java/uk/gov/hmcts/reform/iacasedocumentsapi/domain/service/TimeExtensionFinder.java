package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TIME_EXTENSIONS;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class TimeExtensionFinder {

    /**
     * Looks inside the Time extensions and finds the latest time extension.
     *
     * @param currentState the state the case is in.
     * @param status       the time extension status to scan for GRANTED/REFUSED
     * @param asylumCase   the asylum case
     * @return the current in-flight time extension
     */
    public IdValue<TimeExtension> findCurrentTimeExtension(final State currentState, final TimeExtensionStatus status, AsylumCase asylumCase) {
        final List<IdValue<TimeExtension>> maybeTimeExtensions = getTimeExtensions(asylumCase);

        final Optional<IdValue<TimeExtension>> maybeTargetTimeExtension = maybeTimeExtensions.stream()
            .filter(timeExtensionIdValue ->
                timeExtensionIdValue.getId().equals(String.valueOf(maybeTimeExtensions.size()))
                && currentState == timeExtensionIdValue.getValue().getState()
                && status == timeExtensionIdValue.getValue().getStatus())
            .findFirst();

        if (!maybeTargetTimeExtension.isPresent()) {
            throw new IllegalStateException("No time extension found with state: '" + currentState + "' and status: '" + status + "'");
        }

        return maybeTargetTimeExtension.get();
    }

    private List<IdValue<TimeExtension>> getTimeExtensions(AsylumCase asylumCase) {
        return asylumCase.<List<IdValue<TimeExtension>>>read(TIME_EXTENSIONS)
            .orElse(emptyList());
    }

    /**
     * Given a current State maps the state to a text description that is used on Time Extension templates this is needed to make the notification generic and applicable to all states.
     *
     * @param currentState the state the case is in.
     * @return a text description of what the next step should be
     */
    public String findNextActionText(State currentState) {

        switch (currentState) {
            case AWAITING_REASONS_FOR_APPEAL:
                return "tell us why you think the Home Office decision is wrong";
            case AWAITING_CLARIFYING_QUESTIONS_ANSWERS:
                return "answer the Tribunal's questions";
            case AWAITING_CMA_REQUIREMENTS:
                return "tell us if you will need anything at your appointment";
            default:
                throw new IllegalArgumentException("No next step text description value found for state: " + currentState);

        }
    }
}
