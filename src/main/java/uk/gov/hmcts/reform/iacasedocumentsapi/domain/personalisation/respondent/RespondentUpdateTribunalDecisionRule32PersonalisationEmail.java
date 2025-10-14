package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class RespondentUpdateTribunalDecisionRule32PersonalisationEmail implements EmailNotificationPersonalisation {

    private final String respondentUpdateTribunalDecisionRule32EmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    private final EmailAddressFinder emailAddressFinder;
    private final String exUiFrontendUrl;

    public RespondentUpdateTribunalDecisionRule32PersonalisationEmail(
        @Value("${govnotify.template.updateTribunalDecision.rule32.respondent.email}") String respondentUpdateTribunalDecisionRule32EmailTemplateId,
        @Value("${iaExUiFrontendUrl}") String exUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider, EmailAddressFinder emailAddressFinder) {
        this.respondentUpdateTribunalDecisionRule32EmailTemplateId = respondentUpdateTribunalDecisionRule32EmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
        this.exUiFrontendUrl = exUiFrontendUrl;
    }

    @Override
    public String getTemplateId() {
        return respondentUpdateTribunalDecisionRule32EmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_UPDATE_TRIBUNAL_DECISION_RULE_32_EMAIL";
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
                .put("linkToService", exUiFrontendUrl)
                .build();
    }
}
