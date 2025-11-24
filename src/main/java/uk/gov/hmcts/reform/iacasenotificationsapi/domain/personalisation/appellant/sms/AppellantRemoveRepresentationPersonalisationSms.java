package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantRemoveRepresentationPersonalisationSms implements SmsNotificationPersonalisation {

    private final String iaAipFrontendUrl;
    private final String iaAipPathToSelfRepresentation;
    private final String removeRepresentationAppellantSmsTemplateId;
    private final CustomerServicesProvider customerServicesProvider;


    public AppellantRemoveRepresentationPersonalisationSms(
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${iaAipPathToSelfRepresentation}") String iaAipPathToSelfRepresentation,
        @Value("${govnotify.template.removeRepresentation.appellant.sms}") String removeRepresentationAppellantSmsTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.iaAipPathToSelfRepresentation = iaAipPathToSelfRepresentation;
        this.removeRepresentationAppellantSmsTemplateId = removeRepresentationAppellantSmsTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return removeRepresentationAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        return Collections.singleton(asylumCase
            .read(MOBILE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("appellantMobileNumber is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_REPRESENTATION_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String linkToPiPStartPage = iaAipFrontendUrl + iaAipPathToSelfRepresentation;

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("appellantDateOfBirth", defaultDateFormat(asylumCase.read(AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH, String.class).orElse("")))
            .put("ccdCaseId", String.valueOf(callback.getCaseDetails().getId()))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("linkToPiPStartPage", linkToPiPStartPage);

        PinInPostDetails pip = asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class).orElse(null);
        if (pip != null) {
            personalizationBuilder.put("securityCode", pip.getAccessCode());
            personalizationBuilder.put("validDate", defaultDateFormat(pip.getExpiryDate()));
        } else {
            personalizationBuilder.put("securityCode", "");
            personalizationBuilder.put("validDate", "");
        }

        return personalizationBuilder.build();
    }
}
