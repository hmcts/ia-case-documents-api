package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.UpdateTribunalDecisionRule31PersonalisationUtil;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantUpdateTribunalDecisionRule31PersonalisationEmail implements EmailNotificationPersonalisation, UpdateTribunalDecisionRule31PersonalisationUtil {

    private final String updateTribunalDecisionRule31DecisionTemplateId;
    private final String updateTribunalDecisionRule31DocumentTemplateId;
    private final String updateTribunalDecisionRule31BothTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AppellantUpdateTribunalDecisionRule31PersonalisationEmail(
        @Value("${govnotify.template.updateTribunalDecision.rule31.appellant.emailUpdatedDecision}") String updateTribunalDecisionRule31DecisionTemplateId,
        @Value("${govnotify.template.updateTribunalDecision.rule31.appellant.emailUpdatedDocument}") String updateTribunalDecisionRule31DocumentTemplateId,
        @Value("${govnotify.template.updateTribunalDecision.rule31.appellant.emailUpdatedBoth}") String updateTribunalDecisionRule31BothTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder) {
        this.updateTribunalDecisionRule31DecisionTemplateId = updateTribunalDecisionRule31DecisionTemplateId;
        this.updateTribunalDecisionRule31DocumentTemplateId = updateTribunalDecisionRule31DocumentTemplateId;
        this.updateTribunalDecisionRule31BothTemplateId = updateTribunalDecisionRule31BothTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (isUpdatedDecision(asylumCase)) {
            if (isUpdatedDocument(asylumCase)) {
                return updateTribunalDecisionRule31BothTemplateId;
            }
            return updateTribunalDecisionRule31DecisionTemplateId;
        } else {
            return updateTribunalDecisionRule31DocumentTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_UPDATE_TRIBUNAL_DECISION_RULE_31_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl);

        buildUpdatedDecisionData(asylumCase, personalizationBuilder);

        return personalizationBuilder.build();
    }
}
