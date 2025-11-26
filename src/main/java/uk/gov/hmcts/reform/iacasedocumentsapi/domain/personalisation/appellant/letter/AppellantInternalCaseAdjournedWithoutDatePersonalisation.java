package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ADJOURN_HEARING_WITHOUT_DATE_REASONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOocAsSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Service
public class AppellantInternalCaseAdjournedWithoutDatePersonalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalCaseAdjournedWithoutDateTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;

    public AppellantInternalCaseAdjournedWithoutDatePersonalisation(
        @Value("${govnotify.template.adjournHearingWithoutDate.nonDetained.appellant.letter}") String appellantInternalCaseAdjournedWithoutDateTemplateId,
        CustomerServicesProvider customerServicesProvider, DateTimeExtractor dateTimeExtractor, HearingDetailsFinder hearingDetailsFinder
    ) {
        this.appellantInternalCaseAdjournedWithoutDateTemplateId = appellantInternalCaseAdjournedWithoutDateTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseAdjournedWithoutDateTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
            getAppellantAddressInCountryOrOocAsSet(asylumCase) : getLegalRepAddressInCountryOrOocAsSet(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_CASE_ADJOURN_WITHOUT_DATE_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("hearingLocation", hearingDetailsFinder.getHearingCentreName(asylumCase))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("adjournedHearingReason", asylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class).orElse(""));

        List<String> address =  getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}
