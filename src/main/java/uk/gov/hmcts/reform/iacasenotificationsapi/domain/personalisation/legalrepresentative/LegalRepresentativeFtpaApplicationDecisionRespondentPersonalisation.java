package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation   implements EmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String applicationGrantedOtherPartyLegalRepTemplateId;
    private final String applicationPartiallyGrantedOtherPartyLegalRepTemplateId;
    private final String applicationNotAdmittedOtherPartyLegalRepTemplateId;
    private final String applicationRefusedGrantedOtherPartyLegalRepTemplateId;

    public LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation(
        @Value("${govnotify.template.applicationGranted.otherParty.legalRep.email}") String applicationGrantedOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.otherParty.legalRep.email}") String applicationPartiallyGrantedOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.otherParty.legalRep.email}") String applicationNotAdmittedOtherPartyLegalRepTemplateId,
        @Value("${govnotify.template.applicationRefused.otherParty.legalRep.email}") String applicationRefusedGrantedOtherPartyLegalRepTemplateId,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedOtherPartyLegalRepTemplateId = applicationGrantedOtherPartyLegalRepTemplateId;
        this.applicationPartiallyGrantedOtherPartyLegalRepTemplateId = applicationPartiallyGrantedOtherPartyLegalRepTemplateId;
        this.applicationNotAdmittedOtherPartyLegalRepTemplateId = applicationNotAdmittedOtherPartyLegalRepTemplateId;
        this.applicationRefusedGrantedOtherPartyLegalRepTemplateId = applicationRefusedGrantedOtherPartyLegalRepTemplateId;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        FtpaDecisionOutcomeType ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionOutcomeType is not present"));

        if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedOtherPartyLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedOtherPartyLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedGrantedOtherPartyLegalRepTemplateId;
        } else {
            return applicationNotAdmittedOtherPartyLegalRepTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase);
    }
}
