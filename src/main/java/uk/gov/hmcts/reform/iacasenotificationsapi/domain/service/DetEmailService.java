package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IRC_NAME;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

/**
 * This bean provides the email address for Detention Engagement Team for unrepresented detained cases.
 */
@Service
public class DetEmailService {

    private final Map<String, String> detentionEngagementTeamIrcEmailAddresses;

    public DetEmailService(
        Map<String, String> detentionEngagementTeamIrcEmailAddresses
    ) {
        this.detentionEngagementTeamIrcEmailAddresses = detentionEngagementTeamIrcEmailAddresses;
    }

    public String getDetEmailAddressMapping(Map<String, String> detEmailAddressesMap, String name) {
        String formattedName = name.replaceAll("[^a-zA-Z]","");
        return detEmailAddressesMap.get(formattedName);
    }

    public String getDetEmailAddress(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);

        return detentionFacility.isPresent() && detentionFacility.get().equals("immigrationRemovalCentre")
            ?
            asylumCase
                .read(IRC_NAME, String.class)
                .map(it -> Optional.ofNullable(getDetEmailAddressMapping(detentionEngagementTeamIrcEmailAddresses, it))
                    .orElseThrow(() -> new IllegalStateException("DET email address not found for: " + it.toString()))
                )
                .orElseThrow(() -> new IllegalStateException("IRC name is not present"))
            :
                StringUtils.EMPTY;
    }

    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(getDetEmailAddress(asylumCase));
    }

}
