package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@Service
public class AppellantAppealOutcomePersonalisationSms implements SmsNotificationPersonalisation {

    private final String appealOutcomeAllowedAppellantTemplateId;
    private final String appealOutcomeDismissedAppellantTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;


    public AppellantAppealOutcomePersonalisationSms(
            @NotNull(message = "appealOutcomeAllowedAppellantTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeAllowed.appellant.sms}") String appealOutcomeAllowedAppellantTemplateId,
            @NotNull(message = "appealOutcomeDismissedAppellantTemplateId cannot be null") @Value("${govnotify.template.appealOutcomeDismissed.appellant.sms}") String appealOutcomeDismissedAppellantTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder) {
        this.appealOutcomeAllowedAppellantTemplateId = appealOutcomeAllowedAppellantTemplateId;
        this.appealOutcomeDismissedAppellantTemplateId = appealOutcomeDismissedAppellantTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        final AppealDecision appealOutcomeDecision = asylumCase
            .read(AsylumCaseDefinition.IS_DECISION_ALLOWED, AppealDecision.class)
            .orElseThrow(() -> new IllegalStateException("appealOutcomeDecision is not present"));

        return appealOutcomeDecision.getValue().equals(AppealDecision.ALLOWED.getValue())
            ? appealOutcomeAllowedAppellantTemplateId : appealOutcomeDismissedAppellantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_OUTCOME_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        YesOrNo appealOutOfCountry = asylumCase.read(AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY, YesOrNo.class).orElse(YesOrNo.NO);

        String period = appealOutOfCountry == YesOrNo.YES ? "28 days" : "14 days";

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("14 or 28 days after judge uploads decisions and reasons document", period)
                .put("link to timeline", iaAipFrontendUrl)
                .build();
    }
}
