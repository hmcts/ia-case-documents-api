package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

@Service
public class AppellantRemoveRepresentationDetainedOtherPersonalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalCaseDecisionWithoutHearingLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantRemoveRepresentationDetainedOtherPersonalisation(
            @Value("${govnotify.template.removeRepresentation.appellant.detention.other.letter}") String appellantInternalCaseDecisionWithoutHearingLetterTemplateId,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantInternalCaseDecisionWithoutHearingLetterTemplateId = appellantInternalCaseDecisionWithoutHearingLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseDecisionWithoutHearingLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return getAppellantAddressInCountryOrOocAsSet(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_REPRESENTATION_DETAINED_OTHER_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AsylumCase asylumCaseBefore = callback.getCaseDetailsBefore().orElse(callback.getCaseDetails()).getCaseData();

        final String dateOfBirth = asylumCase
                .read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH,String.class)
                .orElseThrow(() -> new IllegalStateException("Appellant's birth of date is not present"));

        final  String formattedDateOfBirth = LocalDate.parse(dateOfBirth).format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("firstName", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("lastName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("dateofBirth", formattedDateOfBirth)
            .put("refNumber", asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("legalRepRef", asylumCaseBefore.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""));

        List<String> address =  getAppellantAddressAsList(asylumCase);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}
