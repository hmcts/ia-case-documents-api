package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.utils.UpdateTribunalDecisionRule31PersonalisationUtil;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantUpdateTribunalDecisionRule31PersonalisationSms implements SmsNotificationPersonalisation, UpdateTribunalDecisionRule31PersonalisationUtil {

    private final String updateTribunalDecisionRule31DecisionTemplateId;
    private final String updateTribunalDecisionRule31DocumentTemplateId;
    private final String updateTribunalDecisionRule31BothTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AppellantUpdateTribunalDecisionRule31PersonalisationSms(
        @Value("${govnotify.template.updateTribunalDecision.rule31.appellant.smsUpdatedDecision}") String updateTribunalDecisionRule31DecisionTemplateId,
        @Value("${govnotify.template.updateTribunalDecision.rule31.appellant.smsUpdatedDocument}") String updateTribunalDecisionRule31DocumentTemplateId,
        @Value("${govnotify.template.updateTribunalDecision.rule31.appellant.smsUpdatedBoth}") String updateTribunalDecisionRule31BothTemplateId,
        RecipientsFinder recipientsFinder,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl
    ) {
        this.updateTribunalDecisionRule31DecisionTemplateId = updateTribunalDecisionRule31DecisionTemplateId;
        this.updateTribunalDecisionRule31DocumentTemplateId = updateTribunalDecisionRule31DocumentTemplateId;
        this.updateTribunalDecisionRule31BothTemplateId = updateTribunalDecisionRule31BothTemplateId;
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
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_UPDATE_TRIBUNAL_DECISION_RULE_31_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl);

        buildUpdatedDecisionData(asylumCase, personalizationBuilder);

        return personalizationBuilder.build();
    }
}
