package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantRemoveRepresentationPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String iaAipFrontendUrl;
    private final String iaAipPathToSelfRepresentation;
    private final String removeRepresentationAppellantEmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantRemoveRepresentationPersonalisationEmail(
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${iaAipPathToSelfRepresentation}") String iaAipPathToSelfRepresentation,
        @Value("${govnotify.template.removeRepresentation.appellant.email}") String removeRepresentationAppellantEmailTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.iaAipPathToSelfRepresentation = iaAipPathToSelfRepresentation;
        this.removeRepresentationAppellantEmailTemplateId = removeRepresentationAppellantEmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return removeRepresentationAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(AsylumCaseDefinition.EMAIL, String.class)
            .orElseThrow(() -> new IllegalStateException("appellantEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_REPRESENTATION_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        requireNonNull(asylumCase, "asylumCase must not be null");

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

        PinInPostDetails pip = asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class).orElse(null);
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
