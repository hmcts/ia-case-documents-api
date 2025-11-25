package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeReheardUnderRule35Personalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepReheardUnder35RuleEmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final String exUiFrontendUrl;

    public LegalRepresentativeReheardUnderRule35Personalisation(
        @Value("${govnotify.template.decideFtpaApplication.reheardUnderRule35.legalRep.email}") String legalRepReheardUnder35RuleEmailTemplateId,
        @Value("${iaExUiFrontendUrl}") String exUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepReheardUnder35RuleEmailTemplateId = legalRepReheardUnder35RuleEmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.exUiFrontendUrl = exUiFrontendUrl;
    }

    @Override
    public String getTemplateId() {
        return legalRepReheardUnder35RuleEmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REP_REHEARD_UNDER_RULE_35_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToService", exUiFrontendUrl)
                .build();
    }
}
