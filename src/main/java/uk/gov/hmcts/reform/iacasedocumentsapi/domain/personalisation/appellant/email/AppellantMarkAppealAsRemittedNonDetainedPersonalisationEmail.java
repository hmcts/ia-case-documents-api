package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.INTERNAL_APPELLANT_EMAIL;

@Service
public class AppellantMarkAppealAsRemittedNonDetainedPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantMarkAppealAsRemittedNonDetainedTemplateId;
    private final String iaAipFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantMarkAppealAsRemittedNonDetainedPersonalisationEmail(
        @Value("${govnotify.template.markAppealAsRemitted.nonDetained.appellant.email}")
        String appellantMarkAppealAsRemittedNonDetainedTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.appellantMarkAppealAsRemittedNonDetainedTemplateId = appellantMarkAppealAsRemittedNonDetainedTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class)
            .map(email -> Collections.singleton(email))
            .orElse(Collections.emptySet());
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantMarkAppealAsRemittedNonDetainedTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_MARK_APPEAL_AS_REMITTED_NON_DETAINED_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String caseRefNumber = asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse("");
        PinInPostDetails pip = AsylumCaseUtils.generateAppellantPinIfNotPresent(asylumCase);
        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("remittalSource", asylumCase.read(AsylumCaseDefinition.SOURCE_OF_REMITTAL, SourceOfRemittal.class)
                    .orElseThrow(() -> new IllegalStateException("sourceOfRemittal is not present"))
                    .getValue())
            .put("urlLink", iaAipFrontendUrl)
            .put("ccdRefNumber", caseRefNumber)
            .put("securityCode", pip.getAccessCode())
            .put("expirationDate", defaultDateFormat(pip.getExpiryDate()));

        return listCaseFields.build();
    }
}
