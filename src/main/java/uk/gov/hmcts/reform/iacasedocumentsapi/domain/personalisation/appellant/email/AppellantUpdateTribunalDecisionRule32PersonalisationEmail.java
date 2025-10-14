package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class AppellantUpdateTribunalDecisionRule32PersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantUpdateTribunalDecisionRule32EmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AppellantUpdateTribunalDecisionRule32PersonalisationEmail(
        @Value("${govnotify.template.updateTribunalDecision.rule32.appellant.email}") String appellantUpdateTribunalDecisionRule32EmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider, RecipientsFinder recipientsFinder) {
        this.appellantUpdateTribunalDecisionRule32EmailTemplateId = appellantUpdateTribunalDecisionRule32EmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
    }

    @Override
    public String getTemplateId() {
        return appellantUpdateTribunalDecisionRule32EmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_UPDATE_TRIBUNAL_DECISION_RULE_32_EMAIL";
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
                .put("respondentReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkAipTimeline", iaAipFrontendUrl)
                .build();
    }
}
