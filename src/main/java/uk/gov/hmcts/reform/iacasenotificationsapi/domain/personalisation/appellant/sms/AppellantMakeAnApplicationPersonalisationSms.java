package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Service
public class AppellantMakeAnApplicationPersonalisationSms implements SmsNotificationPersonalisation {

    private static final String ROLE_CITIZEN = "citizen";
    private final String makeAnApplicationAppellantSmsTemplateId;
    private final String otherPartyMakeAnApplicationAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final MakeAnApplicationService makeAnApplicationService;
    private final UserDetailsProvider userDetailsProvider;


    public AppellantMakeAnApplicationPersonalisationSms(
            @Value("${govnotify.template.makeAnApplication.appellant.sms}") String makeAnApplicationAppellantSmsTemplateId,
            @Value("${govnotify.template.makeAnApplication.otherParty.appellant.sms}") String otherPartyMakeAnApplicationAppellantSmsTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            MakeAnApplicationService makeAnApplicationService,
            UserDetailsProvider userDetailsProvider) {
        this.makeAnApplicationAppellantSmsTemplateId = makeAnApplicationAppellantSmsTemplateId;
        this.otherPartyMakeAnApplicationAppellantSmsTemplateId = otherPartyMakeAnApplicationAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.makeAnApplicationService = makeAnApplicationService;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public String getTemplateId() {
        return hasRole(ROLE_CITIZEN)
                ? makeAnApplicationAppellantSmsTemplateId
                : otherPartyMakeAnApplicationAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MAKE_AN_APPLICATION_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("applicationType", makeAnApplicationService.getMakeAnApplication(asylumCase, false).map(MakeAnApplication::getType).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }

    private boolean hasRole(String roleName) {
        return userDetailsProvider
                .getUserDetails()
                .getRoles()
                .contains(roleName);
    }
}
