package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

import java.util.Optional;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PRISON_NAME;

@Service
public class DetentionFacilityEmailService {
    private final DetEmailService detEmailService;
    private final PrisonEmailMappingService prisonEmailMappingService;

    public DetentionFacilityEmailService(DetEmailService detEmailService, PrisonEmailMappingService prisonEmailMappingService) {
        this.detEmailService = detEmailService;
        this.prisonEmailMappingService = prisonEmailMappingService;
    }

    public String getDetentionEmailAddress(AsylumCase asylumCase) {
        Optional<String> detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class);

        if (detentionFacility.isPresent()) {
            if (detentionFacility.get().equals("immigrationRemovalCentre")) {
                return detEmailService.getDetEmailAddress(asylumCase);
            } else {
                Optional<String> prisonNameOpt = asylumCase.read(PRISON_NAME, String.class);
                if (prisonNameOpt.isPresent()) {
                    return prisonEmailMappingService.getPrisonEmail(prisonNameOpt.get())
                        .orElseThrow(() -> new IllegalStateException(
                            "Prison email address not found for Prison: " + prisonNameOpt.get())
                        );
                } else {
                    throw new IllegalStateException("Prison name is not present");
                }
            }
        } else {
            throw new IllegalStateException("Detention facility is not present");
        }
    }
}
