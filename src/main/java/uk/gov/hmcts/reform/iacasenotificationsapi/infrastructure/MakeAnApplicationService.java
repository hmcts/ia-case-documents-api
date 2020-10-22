package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;


import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MAKE_AN_APPLICATIONS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class MakeAnApplicationService {
    public String getMakeAnApplicationTypeName(AsylumCase asylumCase) {

        Optional<List<IdValue<MakeAnApplication>>> maybeMakeAnApplications = asylumCase.read(AsylumCaseDefinition.MAKE_AN_APPLICATIONS);

        if (maybeMakeAnApplications.isPresent()) {
            List<IdValue<MakeAnApplication>> makeAnApplications = maybeMakeAnApplications.orElse(Collections.emptyList());
            if (makeAnApplications.size() > 0) {
                return makeAnApplications.get(0).getValue().getType();
            }
        }

        return "";
    }

    public Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {

        Optional<String>  maybeDecideAnApplicationId = asylumCase.read(DECIDE_AN_APPLICATION_ID);

        if (maybeDecideAnApplicationId.isPresent()) {

            Optional<List<IdValue<MakeAnApplication>>>  maybeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);

            if (maybeMakeAnApplications.isPresent()) {
                List<IdValue<MakeAnApplication>> makeAnApplications = maybeMakeAnApplications.orElse(emptyList());

                if (makeAnApplications.size() > 0) {
                    for (IdValue<MakeAnApplication> applicationIdValue : makeAnApplications) {
                        if (applicationIdValue.getId().equals(maybeDecideAnApplicationId.get())) {
                            return Optional.of(applicationIdValue.getValue());
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    public boolean isApplicationListed(State state) {
        if (Arrays.asList(
                State.ADJOURNED,
                State.PREPARE_FOR_HEARING,
                State.FINAL_BUNDLING,
                State.PRE_HEARING,
                State.DECISION,
                State.DECIDED,
                State.FTPA_SUBMITTED,
                State.FTPA_DECIDED
        ).contains(state)) {
            return true;
        } else {
            return false;
        }
    }
}
