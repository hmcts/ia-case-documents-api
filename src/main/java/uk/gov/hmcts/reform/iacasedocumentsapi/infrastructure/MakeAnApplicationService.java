package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECIDE_AN_APPLICATION_ID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.ADJOURN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.EXPEDITE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.JUDGE_REVIEW;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.LINK_OR_UNLINK;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.REINSTATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.TIME_EXTENSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.TRANSFER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.UPDATE_APPEAL_DETAILS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplicationTypes.WITHDRAW;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@Service
public class MakeAnApplicationService {

    private static final Map<String,String> APPLICATION_PHRASE = Map.ofEntries(
            Map.entry(ADJOURN.toString(), "change the hearing date"),
            Map.entry(EXPEDITE.toString(), "have the hearing sooner"),
            Map.entry(JUDGE_REVIEW.toString(), "ask a judge to review the decision"),
            Map.entry(LINK_OR_UNLINK.toString(), "link or unlink the appeal"),
            Map.entry(TIME_EXTENSION.toString(), "ask for more time"),
            Map.entry(TRANSFER.toString(), "move the hearing to a different location"),
            Map.entry(WITHDRAW.toString(), "withdraw from the appeal"),
            Map.entry(UPDATE_HEARING_REQUIREMENTS.toString(), "change some of the hearing requirements"),
            Map.entry(UPDATE_APPEAL_DETAILS.toString(), "change some of the appeal details"),
            Map.entry(REINSTATE.toString(), "reinstate the appeal"),
            Map.entry(OTHER.toString(), "change something about the appeal")
    );

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
                    int targetIndex = Collections.max(idValues.stream().map(idValue -> Integer.parseInt(idValue.getId())).collect(Collectors.toList()));
                    makeApplicationOptional = idValues.stream().filter(idValue -> idValue.getId().equals(String.valueOf(targetIndex))).map(idValue -> idValue.getValue()).findAny();
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

    public String mapApplicationTypeToPhrase(MakeAnApplication application) {
        return APPLICATION_PHRASE.get(application.getType());
    }
}

