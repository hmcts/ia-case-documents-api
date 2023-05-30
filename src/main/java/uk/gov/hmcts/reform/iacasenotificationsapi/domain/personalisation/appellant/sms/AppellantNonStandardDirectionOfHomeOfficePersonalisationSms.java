package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;

@Service
public class AppellantNonStandardDirectionOfHomeOfficePersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantNonStandardDirectionBeforeListingTemplateId;
    private final String appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final DirectionFinder directionFinder;

    public AppellantNonStandardDirectionOfHomeOfficePersonalisationSms(
            @Value("${govnotify.template.nonStandardDirectionOfHomeOfficeBeforeListing.appellant.sms}") String appellantNonStandardDirectionBeforeListingTemplateId,
            @Value("${govnotify.template.nonStandardDirectionToAppellantAndRespondentBeforeListing.appellant.sms}") String appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            DirectionFinder directionFinder) {
        this.appellantNonStandardDirectionBeforeListingTemplateId = appellantNonStandardDirectionBeforeListingTemplateId;
        this.appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId = appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.directionFinder = directionFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .map(direction -> direction.getParties().equals(Parties.APPELLANT_AND_RESPONDENT))
                .orElse(false)) {
            return appellantNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
        } else {
            return appellantNonStandardDirectionBeforeListingTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_NON_STANDARD_DIRECTION_OF_HOME_OFFICE_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
