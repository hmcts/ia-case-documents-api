package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

@Service
public class LegalRepresentativeChangeHearingCentrePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String changeHearingCentreTemplateId;

    public LegalRepresentativeChangeHearingCentrePersonalisation(
        @Value("${govnotify.template.changeHearingCentre.legalRep.email}") String changeHearingCentreTemplateId) {

        this.changeHearingCentreTemplateId = changeHearingCentreTemplateId;
    }

    @Override
    public String getTemplateId() {
        return changeHearingCentreTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CHANGE_HEARING_CENTRE_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
