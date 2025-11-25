package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

import java.util.Optional;

@Service
public class DetentionFacilityNameFinder {
    private static final String PRISON_NAME = "prisonName";
    private static final String IRC_NAME = "ircName";

    private final StringProvider stringProvider;

    public DetentionFacilityNameFinder(StringProvider stringProvider) {
        this.stringProvider = stringProvider;
    }

    public String getDetentionFacility(String detentionFacilityName) {
        Optional<String> prisonOpt = stringProvider.get(PRISON_NAME, detentionFacilityName);
        if (prisonOpt.isPresent()) {
            return prisonOpt.get();
        } else {
            Optional<String> ircOpt = stringProvider.get(IRC_NAME, detentionFacilityName);
            return ircOpt.orElse(detentionFacilityName);
        }
    }
}
