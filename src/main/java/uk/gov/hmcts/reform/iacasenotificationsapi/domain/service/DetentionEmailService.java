package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

import java.util.Optional;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;

@Service
public class DetentionEmailService {
    private final DetEmailService detEmailService;
    private final String ctscEmailAddress;

    public DetentionEmailService(DetEmailService detEmailService, @Value("${ctscEmailAddress}") String ctscEmailAddress) {
        this.detEmailService = detEmailService;
        this.ctscEmailAddress = ctscEmailAddress;
    }

    public String getDetentionEmailAddress(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);

        if (detentionFacility.isPresent()) {
            if (detentionFacility.get().equals("immigrationRemovalCentre")) {
                return detEmailService.getDetEmailAddress(asylumCase);
            } else if (detentionFacility.get().equals("prison")) {
                return  this.ctscEmailAddress;
            } else {
                throw new IllegalStateException("Detention facility is not valid");
            }
        } else {
            throw new IllegalStateException("Detention facility is not present");
        }
    }
}
