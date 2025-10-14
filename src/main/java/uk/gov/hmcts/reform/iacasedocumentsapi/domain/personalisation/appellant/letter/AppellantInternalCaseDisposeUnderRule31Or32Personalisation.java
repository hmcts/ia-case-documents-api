package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.FtpaNotificationPersonalisationUtil.APPELLANT_APPLICANT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantInternalCaseDisposeUnderRule31Or32Personalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalCaseDisposeUnderRule31Or32LetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantInternalCaseDisposeUnderRule31Or32Personalisation(
        @Value("${govnotify.template.decideFtpaApplication.remadeRule31or32.appellant.letter}") String appellantInternalCaseDisposeUnderRule31Or32LetterTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantInternalCaseDisposeUnderRule31Or32LetterTemplateId = appellantInternalCaseDisposeUnderRule31Or32LetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseDisposeUnderRule31Or32LetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
            getAppellantAddressInCountryOrOoc(asylumCase) : getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_CASE_DISPOSE_UNDER_RULE_31_OR_32_APPELLANT_LETTER";
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
            .put("ftpaDisposedReason", getDisposedReason(asylumCase))
            .put("applicant", getApplicantType(asylumCase).equals(APPELLANT_APPLICANT) ? "your" : "the Home Office's");

        List<String> address =  getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }

    private String getApplicantType(AsylumCase asylumCase) {
        return asylumCase
            .read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .map(ApplicantType::getValue)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"));
    }

    private String getDisposedReason(AsylumCase asylumCase) {
        return getApplicantType(asylumCase).equals(APPELLANT_APPLICANT)
            ? asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT, String.class).orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionRemadeRule32Text is not present"))
            : asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT, String.class).orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionRemadeRule32Text is not present"));
    }
}
