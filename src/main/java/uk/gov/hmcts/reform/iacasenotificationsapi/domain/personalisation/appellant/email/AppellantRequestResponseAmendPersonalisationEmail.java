package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@Service
public class AppellantRequestResponseAmendPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String requestResponseAmendAppellantEmailTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final CustomerServicesProvider customerServicesProvider;

    public AppellantRequestResponseAmendPersonalisationEmail(
            @Value("${govnotify.template.nonStandardDirectionBeforeListing.appellant.email}") String requestResponseAmendAppellantEmailTemplateId,
            RecipientsFinder recipientsFinder,
            CustomerServicesProvider customerServicesProvider) {

        this.requestResponseAmendAppellantEmailTemplateId = requestResponseAmendAppellantEmailTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return requestResponseAmendAppellantEmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_RESPONSE_AMEND_AIP_EMAIL";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }
}
