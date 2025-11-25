package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;

@Service
public class HomeOfficeBailStopLegalRepresentingPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailStopLegalRepresentingPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;


    public HomeOfficeBailStopLegalRepresentingPersonalisation(
        @NotNull(message = "homeOfficeBailStopLegalRepresentingPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.stopLegalRepresenting.homeOffice}") String homeOfficeBailStopLegalRepresentingPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailStopLegalRepresentingPersonalisationTemplateId = homeOfficeBailStopLegalRepresentingPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_STOP_LEGAL_REPRESENTING_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return homeOfficeBailStopLegalRepresentingPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepName", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_NAME, String.class).orElse(""))
            .put("legalRepCompanyAddress", formatCompanyAddress(bailCase))
            .put("legalRepEmail", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class).orElse(""))
            .build();
    }

    public String formatCompanyAddress(BailCase bailCase) {

        StringBuilder str = new StringBuilder();

        if (bailCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class).isPresent()) {

            Optional<AddressUk> optionalLegalRepCompanyAddress =
                    bailCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class);

            final String addressLine1 =
                    optionalLegalRepCompanyAddress.flatMap(AddressUk::getAddressLine1).orElse("");

            final String addressLine2 =
                    optionalLegalRepCompanyAddress.flatMap(AddressUk::getAddressLine2).orElse("");

            final String addressLine3 =
                    optionalLegalRepCompanyAddress.flatMap(AddressUk::getAddressLine3).orElse("");

            final String postTown =
                    optionalLegalRepCompanyAddress.flatMap(AddressUk::getPostTown).orElse("");

            final String county =
                    optionalLegalRepCompanyAddress.flatMap(AddressUk::getCounty).orElse("");

            final String postCode =
                    optionalLegalRepCompanyAddress.flatMap(AddressUk::getPostCode).orElse("");

            final String country =
                    optionalLegalRepCompanyAddress.flatMap(AddressUk::getCountry).orElse("");

            if (!Optional.of(addressLine1).get().equals("")) {
                str.append(addressLine1);
                str.append(", ");
            }

            if (!Optional.of(addressLine2).get().isEmpty()) {
                str.append(addressLine2);
                str.append(", ");
            }

            if (!Optional.of(addressLine3).get().isEmpty()) {
                str.append(addressLine3);
                str.append(", ");
            }

            if (!Optional.of(postTown).get().isEmpty()) {
                str.append(postTown);
                str.append(", ");
            }

            if (!Optional.of(county).get().isEmpty()) {
                str.append(county);
                str.append(", ");
            }

            if (!Optional.of(postCode).get().isEmpty()) {
                str.append(postCode);
                str.append(", ");
            }

            if (!Optional.of(country).get().isEmpty()) {
                str.append(country);
            }

        } else {
            return "";
        }

        return str.toString();
    }
}
