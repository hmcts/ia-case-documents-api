package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_JUDGE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class HomeOfficeFtpaApplicationDecisionAppellantPersonalisation implements EmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String upperTribunalNoticesEmailAddress;
    private final String applicationGrantedOtherPartyHomeOfficeTemplateId;
    private final String applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId;
    private final String applicationNotAdmittedOtherPartyHomeOfficeTemplateId;
    private final String applicationRefusedOtherPartyHomeOfficeTemplateId;
    private final String applicationReheardOtherPartyHomeHomeOfficeTemplateId;
    private final String applicationAllowedHomeOfficeTemplateId;
    private final String applicationDismissedHomeOfficeTemplateId;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeFtpaApplicationDecisionAppellantPersonalisation(
        @Value("${govnotify.template.applicationGranted.otherParty.homeOffice.email}") String applicationGrantedOtherPartyHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.otherParty.homeOffice.email}") String applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.otherParty.homeOffice.email}") String applicationNotAdmittedOtherPartyHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationRefused.otherParty.homeOffice.email}") String applicationRefusedOtherPartyHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationReheard.otherParty.homeOffice.email}") String applicationReheardOtherPartyHomeHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationAllowed.homeOffice.email}") String applicationAllowedHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationDismissed.homeOffice.email}") String applicationDismissedHomeOfficeTemplateId,
        PersonalisationProvider personalisationProvider,
        @Value("${upperTribunalNoticesEmailAddress}") String upperTribunalNoticesEmailAddress
    ) {
        this.applicationGrantedOtherPartyHomeOfficeTemplateId = applicationGrantedOtherPartyHomeOfficeTemplateId;
        this.applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId = applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId;
        this.applicationNotAdmittedOtherPartyHomeOfficeTemplateId = applicationNotAdmittedOtherPartyHomeOfficeTemplateId;
        this.applicationRefusedOtherPartyHomeOfficeTemplateId = applicationRefusedOtherPartyHomeOfficeTemplateId;
        this.applicationReheardOtherPartyHomeHomeOfficeTemplateId = applicationReheardOtherPartyHomeHomeOfficeTemplateId;
        this.applicationAllowedHomeOfficeTemplateId = applicationAllowedHomeOfficeTemplateId;
        this.applicationDismissedHomeOfficeTemplateId = applicationDismissedHomeOfficeTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.upperTribunalNoticesEmailAddress = upperTribunalNoticesEmailAddress;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        if (!ftpaDecisionOutcomeType.isPresent()) {
            ftpaDecisionOutcomeType = Optional.ofNullable(asylumCase
                .read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionOutcomeType is not present")));
        }

        if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedOtherPartyHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedOtherPartyHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && (ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                                                           || ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))) {
            return applicationReheardOtherPartyHomeHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString())) {
            FtpaDecisionOutcomeType ftpaDecisionRemade = asylumCase
                .read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaDecisionRemade is not present"));
            if (ftpaDecisionRemade.toString().equals(FtpaDecisionOutcomeType.FTPA_ALLOWED.toString())) {
                return applicationAllowedHomeOfficeTemplateId;
            }
            return applicationDismissedHomeOfficeTemplateId;
        } else {
            return applicationNotAdmittedOtherPartyHomeOfficeTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_JUDGE, State.class)
            .map(currentState -> {
                if (Arrays.asList(
                    State.FTPA_SUBMITTED,
                    State.FTPA_DECIDED).contains(currentState)
                    ) {
                    return Collections.singleton(upperTribunalNoticesEmailAddress);
                } else {
                    throw new IllegalStateException("homeOffice email Address cannot be found");
                }
            })
            .orElseThrow(() -> new IllegalStateException("homeOffice email Address cannot be found"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_APPELLANT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap.<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase))
            .build();
    }

}
