package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
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
}
