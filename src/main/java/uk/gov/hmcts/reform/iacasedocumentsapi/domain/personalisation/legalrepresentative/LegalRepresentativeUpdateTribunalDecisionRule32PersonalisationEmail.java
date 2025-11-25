package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class LegalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final String exUiFrontendUrl;

    public LegalRepresentativeUpdateTribunalDecisionRule32PersonalisationEmail(
        @Value("${govnotify.template.updateTribunalDecision.rule32.legalrep.email}") String legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId,
        @Value("${iaExUiFrontendUrl}") String exUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider) {
        this.legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId = legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.exUiFrontendUrl = exUiFrontendUrl;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalRepresentativeUpdateTribunalDecisionRule32EmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_UPDATE_TRIBUNAL_DECISION_RULE_32_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

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
