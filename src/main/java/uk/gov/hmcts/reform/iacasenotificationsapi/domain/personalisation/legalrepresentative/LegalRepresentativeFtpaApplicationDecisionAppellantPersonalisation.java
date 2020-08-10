package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation implements EmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String applicationGrantedApplicantLegalRepTemplateId;
    private final String applicationPartiallyGrantedApplicantLegalRepTemplateId;
    private final String applicationNotAdmittedApplicantLegalRepTemplateId;
    private final String applicationRefusedGrantedApplicantLegalRepTemplateId;
    private final String applicationReheardApplicantLegalRepTemplateId;
    private final String applicationAllowedLegalRepTemplateId;
    private final String applicationDismissedLegalRepTemplateId;


    public LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation(
        @Value("${govnotify.template.applicationGranted.applicant.legalRep.email}") String applicationGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.legalRep.email}") String applicationPartiallyGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.legalRep.email}") String applicationNotAdmittedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.legalRep.email}") String applicationRefusedGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationReheard.applicant.legalRep.email}") String applicationReheardApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationAllowed.legalRep.email}") String applicationAllowedLegalRepTemplateId,
        @Value("${govnotify.template.applicationDismissed.legalRep.email}") String applicationDismissedLegalRepTemplateId,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedApplicantLegalRepTemplateId = applicationGrantedApplicantLegalRepTemplateId;
        this.applicationPartiallyGrantedApplicantLegalRepTemplateId = applicationPartiallyGrantedApplicantLegalRepTemplateId;
        this.applicationNotAdmittedApplicantLegalRepTemplateId = applicationNotAdmittedApplicantLegalRepTemplateId;
        this.applicationRefusedGrantedApplicantLegalRepTemplateId = applicationRefusedGrantedApplicantLegalRepTemplateId;
        this.applicationReheardApplicantLegalRepTemplateId = applicationReheardApplicantLegalRepTemplateId;
        this.applicationAllowedLegalRepTemplateId = applicationAllowedLegalRepTemplateId;
        this.applicationDismissedLegalRepTemplateId = applicationDismissedLegalRepTemplateId;;
        this.personalisationProvider = personalisationProvider;
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
            return applicationGrantedApplicantLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedApplicantLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedGrantedApplicantLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && (ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                   || ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))) {
            return applicationReheardApplicantLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString())) {
            FtpaDecisionOutcomeType ftpaDecisionRemade = asylumCase
                .read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaDecisionRemade is not present"));
            if (ftpaDecisionRemade.toString().equals(FtpaDecisionOutcomeType.FTPA_ALLOWED.toString())) {
                return applicationAllowedLegalRepTemplateId;
            }
            return applicationDismissedLegalRepTemplateId;
        } else {
            return applicationNotAdmittedApplicantLegalRepTemplateId;
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
        return caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase);
    }
}
