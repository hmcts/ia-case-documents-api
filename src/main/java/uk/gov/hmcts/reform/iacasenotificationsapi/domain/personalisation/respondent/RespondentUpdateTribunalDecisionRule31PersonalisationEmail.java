package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class RespondentUpdateTribunalDecisionRule31PersonalisationEmail implements EmailNotificationPersonalisation {

    private final String respondentUpdateTribunalDecisionRule31EmailTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    private final EmailAddressFinder emailAddressFinder;
    private final String exUiFrontendUrl;

    public RespondentUpdateTribunalDecisionRule31PersonalisationEmail(
        @Value("${govnotify.template.updateTribunalDecision.rule31.respondent.email}") String respondentUpdateTribunalDecisionRule31EmailTemplateId,
        @Value("${iaExUiFrontendUrl}") String exUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider, EmailAddressFinder emailAddressFinder) {
        this.respondentUpdateTribunalDecisionRule31EmailTemplateId = respondentUpdateTribunalDecisionRule31EmailTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
        this.exUiFrontendUrl = exUiFrontendUrl;
    }

    @Override
    public String getTemplateId() {
        return respondentUpdateTribunalDecisionRule31EmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_UPDATE_TRIBUNAL_DECISION_RULE_31_EMAIL";
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
                .put("respondentReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToService", exUiFrontendUrl)
                .put("firstBulletPoint", firstBulletPoint)
                .put("bothChanges", bothChanges)
                .build();
    }
}
