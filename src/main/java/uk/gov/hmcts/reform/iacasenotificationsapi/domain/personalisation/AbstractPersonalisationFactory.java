package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

public abstract class AbstractPersonalisationFactory {

    public Map<String, String> create(AsylumCase asylumCase) {

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                        .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                        .build();
    }
}
