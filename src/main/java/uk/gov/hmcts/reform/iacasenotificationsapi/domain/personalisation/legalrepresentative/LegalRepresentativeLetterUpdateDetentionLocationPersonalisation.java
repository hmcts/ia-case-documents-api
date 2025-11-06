package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DetentionFacilityNameFinder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.PREVIOUS_DETENTION_LOCATION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getAppellantOrLegalRepAddressLetterPersonalisation;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getDetentionFacilityName;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;

@Service
@Slf4j
public class LegalRepresentativeLetterUpdateDetentionLocationPersonalisation implements LetterNotificationPersonalisation {
    private final String templateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final DetentionFacilityNameFinder detentionFacilityNameFinder;

    public LegalRepresentativeLetterUpdateDetentionLocationPersonalisation(
            @Value("${govnotify.template.updateDetentionLocation.legalRep.letter}") String templateId,
            CustomerServicesProvider customerServicesProvider,
            DetentionFacilityNameFinder detentionFacilityNameFinder) {

        this.templateId = templateId;
        this.customerServicesProvider = customerServicesProvider;
        this.detentionFacilityNameFinder = detentionFacilityNameFinder;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPDATE_DETENTION_LOCATION_LETTER_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        log.info("Sending email notification for update detention location");
        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();


        String previousDetentionLocationName = asylumCase.read(PREVIOUS_DETENTION_LOCATION, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Previous Detention location is missing"));
        String newDetentionFacilityName = getDetentionFacilityName(asylumCase);

        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElse("");

        String oldDetentionLocation = "";
        String newDetentionLocation = "";

        if (detentionFacility.equals("other")) {
            oldDetentionLocation = previousDetentionLocationName;
            newDetentionLocation = newDetentionFacilityName;
        } else {
            oldDetentionLocation = detentionFacilityNameFinder.getDetentionFacility(previousDetentionLocationName);
            newDetentionLocation = detentionFacilityNameFinder.getDetentionFacility(newDetentionFacilityName);
        }

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("oldDetentionLocation", oldDetentionLocation)
                .put("newDetentionLocation", newDetentionLocation);

        List<String> address =  getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}
