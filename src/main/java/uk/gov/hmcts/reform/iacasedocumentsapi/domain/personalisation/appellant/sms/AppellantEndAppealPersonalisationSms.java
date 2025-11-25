package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantEndAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String endAppealAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final String iaAipFrontendPathToJudgeReview;
    private final RecipientsFinder recipientsFinder;

    public AppellantEndAppealPersonalisationSms(
            @Value("${govnotify.template.endAppeal.appellant.sms}") String endAppealAppellantSmsTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            @Value("${iaAipFrontendPathToJudgeReview}") String iaAipFrontendPathToJudgeReview,
            RecipientsFinder recipientsFinder
    ) {
        this.endAppealAppellantSmsTemplateId = endAppealAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.iaAipFrontendPathToJudgeReview = iaAipFrontendPathToJudgeReview;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return endAppealAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_END_APPEAL_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("endAppealApprover", asylumCase.read(AsylumCaseDefinition.END_APPEAL_APPROVER_TYPE, String.class).orElse(""))
                        .put("endAppealDate", asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)
                                .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                                .orElse(""))
                        .put("outcomeOfAppeal", asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .put("direct link to judges’ review page", iaAipFrontendUrl + iaAipFrontendPathToJudgeReview)
                        .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
