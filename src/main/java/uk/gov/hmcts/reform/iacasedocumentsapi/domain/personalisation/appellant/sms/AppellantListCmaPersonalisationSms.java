package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@Service
public class AppellantListCmaPersonalisationSms implements SmsNotificationPersonalisation {

    private final String listCmaSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;


    public AppellantListCmaPersonalisationSms(
        @Value("${govnotify.template.listCma.appellant.sms}") String listCmaSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.listCmaSmsTemplateId = listCmaSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;

    }


    @Override
    public String getTemplateId() {
        return listCmaSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LIST_CMA_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase cannot be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
                .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreAddress(asylumCase))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
