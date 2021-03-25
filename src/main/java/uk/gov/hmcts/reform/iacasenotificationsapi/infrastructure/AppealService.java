package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;


@Service
public class AppealService {

    public boolean isAppealListed(AsylumCase asylumCase) {

        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

    public boolean isRemoteHearing(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(hearingCentre -> hearingCentre == HearingCentre.REMOTE_HEARING)
            .orElse(false);
    }
}
