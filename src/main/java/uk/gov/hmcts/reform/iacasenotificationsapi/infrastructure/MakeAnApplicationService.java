package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;


import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DECIDE_AN_APPLICATION_ID;

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
    public Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase, boolean decided) {
        Optional<MakeAnApplication> makeApplicationOptional = Optional.empty();
        Optional<List<IdValue<MakeAnApplication>>> makeAnApplicationsOptional = asylumCase.read(AsylumCaseDefinition.MAKE_AN_APPLICATIONS);
        if (makeAnApplicationsOptional.isPresent()) {
            List<IdValue<MakeAnApplication>> idValues = makeAnApplicationsOptional.orElse(Collections.emptyList());
            if (idValues.size() > 0) {
                if (decided) {
                    Optional<String>  decideAnApplicationIdOptional = asylumCase.read(DECIDE_AN_APPLICATION_ID);
                    String decideAnApplicationId = decideAnApplicationIdOptional.orElse("");
                    makeApplicationOptional = idValues.stream().filter(idValue -> idValue.getId().equals(decideAnApplicationId)).map(idValue -> idValue.getValue()).findAny();
                } else {
                    makeApplicationOptional = Optional.of(idValues.get(0).getValue());
                }
            }
        }
        return makeApplicationOptional;
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
