package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@Service
public class AppellantDecideAnApplicationPersonalisationSms implements SmsNotificationPersonalisation {

    private static final String ROLE_CITIZEN = "citizen";
    private static final String DECISION_GRANTED = "Granted";
    private static final String DECISION_REFUSED = "Refused";

    private final String decideAnApplicationRefusedAppellantSmsTemplateId;
    private final String decideAnApplicationGrantedAppellantSmsTemplateId;
    private final String decideAnApplicationOtherPartySmsTempateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final MakeAnApplicationService makeAnApplicationService;


    public AppellantDecideAnApplicationPersonalisationSms(
            @Value("${govnotify.template.decideAnApplication.refused.applicant.appellant.beforeListing.sms}") String decideAnApplicationRefusedAppellantSmsTemplateId,
            @Value("${govnotify.template.decideAnApplication.granted.applicant.appellant.beforeListing.sms}") String decideAnApplicationGrantedAppellantSmsTemplateId,
            @Value("${govnotify.template.decideAnApplication.otherParty.appellant.sms}") String decideAnApplicationOtherPartySmsTempateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder,
            MakeAnApplicationService makeAnApplicationService) {
        this.decideAnApplicationRefusedAppellantSmsTemplateId = decideAnApplicationRefusedAppellantSmsTemplateId;
        this.decideAnApplicationGrantedAppellantSmsTemplateId = decideAnApplicationGrantedAppellantSmsTemplateId;
        this.decideAnApplicationOtherPartySmsTempateId = decideAnApplicationOtherPartySmsTempateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.makeAnApplicationService = makeAnApplicationService;
    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<MakeAnApplication> maybeMakeAnApplication = makeAnApplicationService.getMakeAnApplication(asylumCase, true);

        if (maybeMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = maybeMakeAnApplication.get();
            String decision = makeAnApplication.getDecision();
            String applicantRole = makeAnApplication.getApplicantRole();
            boolean isCitizenRole  = applicantRole.equals(ROLE_CITIZEN);

            if (isCitizenRole) {
                return DECISION_GRANTED.equals(decision)
                        ? decideAnApplicationGrantedAppellantSmsTemplateId
                        : decideAnApplicationRefusedAppellantSmsTemplateId;
            } else {
                return decideAnApplicationOtherPartySmsTempateId;
            }
        } else {
            return "";
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        Optional<MakeAnApplication> makeAnApplicationOptional = makeAnApplicationService.getMakeAnApplication(asylumCase, true);
        String decision = makeAnApplicationOptional.map(MakeAnApplication::getDecision).orElse("");

        ImmutableMap.Builder<String, String> builder = ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("applicationType", makeAnApplicationOptional.map(MakeAnApplication::getType).orElse(""))
                .put("decision", decision)
                .put("Hyperlink to service", iaAipFrontendUrl);

        if (DECISION_REFUSED.equals(decision)) {
            builder.put("decision maker role", makeAnApplicationOptional.map(MakeAnApplication::getDecisionMaker).orElse(""));
        }
        return builder.build();
    }
}
