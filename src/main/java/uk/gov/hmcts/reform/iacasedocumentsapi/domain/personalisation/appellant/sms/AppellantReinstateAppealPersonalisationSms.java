package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;


@Service
public class AppellantReinstateAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String reinstateAppealAppellantSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AppellantReinstateAppealPersonalisationSms(
            @Value("${govnotify.template.reinstateAppeal.appellant.beforeListing.sms}") String reinstateAppealAppellantSmsTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder
    ) {
        this.reinstateAppealAppellantSmsTemplateId = reinstateAppealAppellantSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
    }


    @Override
    public String getTemplateId() {
        return reinstateAppealAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REINSTATE_APPEAL_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("reinstateAppealDate", asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_DATE, String.class)
                                .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                                .orElse("")
                        )
                        .put("reinstateAppealReason", asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_REASON, String.class).orElse("No reason given"))
                        .put("reinstatedDecisionMaker", asylumCase.read(AsylumCaseDefinition.REINSTATED_DECISION_MAKER, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .build();
    }
}
