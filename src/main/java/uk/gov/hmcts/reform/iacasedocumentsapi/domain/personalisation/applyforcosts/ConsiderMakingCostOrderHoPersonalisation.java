package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class ConsiderMakingCostOrderHoPersonalisation implements EmailNotificationPersonalisation {
    private final String considerMakingCostOrderNotificationTemplateId;
    private final String homeOfficeEmailAddress;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public ConsiderMakingCostOrderHoPersonalisation(
        @Value("${govnotify.template.considerMakingCostOrder.homeOffice.email}") String considerMakingCostOrderNotificationTemplateId,
        @Value("${applyForCostsHomeOfficeEmailAddress}") String homeOfficeEmailAddress,
        CustomerServicesProvider customerServicesProvider,
        PersonalisationProvider personalisationProvider
    ) {
        this.considerMakingCostOrderNotificationTemplateId = considerMakingCostOrderNotificationTemplateId;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return considerMakingCostOrderNotificationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(homeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CONSIDER_MAKING_A_COST_ORDER_HOME_OFFICE_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getApplyForCostsPersonalisation(asylumCase))
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }
}
