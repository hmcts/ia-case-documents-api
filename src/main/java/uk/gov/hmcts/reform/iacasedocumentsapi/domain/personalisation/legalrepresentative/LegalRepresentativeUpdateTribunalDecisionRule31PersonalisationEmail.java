package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;


import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class LegalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final String exUiFrontendUrl;

    public LegalRepresentativeUpdateTribunalDecisionRule31PersonalisationEmail(
        @Value("${govnotify.template.updateTribunalDecision.rule31.legalrep.email}") String legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId,
        @Value("${iaExUiFrontendUrl}") String exUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider) {
        this.legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId = legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.exUiFrontendUrl = exUiFrontendUrl;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalRepresentativeUpdateTribunalDecisionRule31EmailTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_UPDATE_TRIBUNAL_DECISION_RULE_31_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String firstBulletPoint = "";
        String bothChanges = "no";

        boolean firstCheck = asylumCase.read(TYPES_OF_UPDATE_TRIBUNAL_DECISION, DynamicList.class).map(list -> list.getValue().getLabel().contains("Yes")).orElse(false);
        boolean secondCheck = asylumCase.read(UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false);

        if (firstCheck) {

            firstBulletPoint = "the appeal decision has been changed";

            if (secondCheck) {
                bothChanges = "yes";
            }
        } else if (secondCheck) {
            firstBulletPoint = "a new Decision and Reasons document is available to view in the documents tab";
        }

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToService", exUiFrontendUrl)
                .put("firstBulletPoint", firstBulletPoint)
                .put("bothChanges", bothChanges)
                .build();
    }

}
