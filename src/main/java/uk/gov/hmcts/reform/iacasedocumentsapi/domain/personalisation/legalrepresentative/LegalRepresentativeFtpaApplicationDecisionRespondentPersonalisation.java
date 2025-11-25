package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation   implements LegalRepresentativeEmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String applicationGrantedOtherPartyLegalRepTemplateId;
    private final String applicationPartiallyGrantedOtherPartyLegalRepTemplateId;
    private final String applicationNotAdmittedOtherPartyLegalRepTemplateId;
    private final String applicationRefusedGrantedOtherPartyLegalRepTemplateId;
    private final String applicationReheardOtherPartyLegalRepTemplateId;
    private final String applicationAllowedLegalRepTemplateId;
    private final String applicationDismissedLegalRepTemplateId;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation(
        @Value("${govnotify.template.applicationGranted.otherParty.legalRep.email}") String applicationGrantedOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.otherParty.legalRep.email}") String applicationPartiallyGrantedOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.otherParty.legalRep.email}") String applicationNotAdmittedOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationRefused.otherParty.legalRep.email}") String applicationRefusedGrantedOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationReheard.otherParty.legalRep.email}") String applicationReheardOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationAllowed.legalRep.email}") String applicationAllowedLegalRepTemplateId,
        @Value("${govnotify.template.applicationDismissed.legalRep.email}") String applicationDismissedLegalRepTemplateId,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedOtherPartyLegalRepTemplateId = applicationGrantedOtherPartyLegalRepTemplateId;
        this.applicationPartiallyGrantedOtherPartyLegalRepTemplateId = applicationPartiallyGrantedOtherPartyLegalRepTemplateId;
        this.applicationNotAdmittedOtherPartyLegalRepTemplateId = applicationNotAdmittedOtherPartyLegalRepTemplateId;
        this.applicationRefusedGrantedOtherPartyLegalRepTemplateId = applicationRefusedGrantedOtherPartyLegalRepTemplateId;
        this.applicationReheardOtherPartyLegalRepTemplateId = applicationReheardOtherPartyLegalRepTemplateId;
        this.applicationAllowedLegalRepTemplateId = applicationAllowedLegalRepTemplateId;
        this.applicationDismissedLegalRepTemplateId = applicationDismissedLegalRepTemplateId;;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        if (!ftpaDecisionOutcomeType.isPresent()) {
            ftpaDecisionOutcomeType = Optional.ofNullable(asylumCase
                .read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present")));
        }

        if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedOtherPartyLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedOtherPartyLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedGrantedOtherPartyLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && (ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                   || ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))) {
            return applicationReheardOtherPartyLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString())) {
            FtpaDecisionOutcomeType ftpaDecisionRemade = asylumCase
                .read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaDecisionRemade is not present"));
            if (ftpaDecisionRemade.toString().equals(FtpaDecisionOutcomeType.FTPA_ALLOWED.toString())) {
                return applicationAllowedLegalRepTemplateId;
            }
            return applicationDismissedLegalRepTemplateId;
        } else {
            return applicationNotAdmittedOtherPartyLegalRepTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap.<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase))
            .build();
    }
}
