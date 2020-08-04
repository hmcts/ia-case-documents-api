package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
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
public class LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation implements EmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String applicationGrantedApplicantLegalRepTemplateId;
    private final String applicationPartiallyGrantedApplicantLegalRepTemplateId;
    private final String applicationNotAdmittedApplicantLegalRepTemplateId;
    private final String applicationRefusedGrantedApplicantLegalRepTemplateId;


    public LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation(
        @Value("${govnotify.template.applicationGranted.applicant.legalRep.email}") String applicationGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.legalRep.email}") String applicationPartiallyGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.legalRep.email}") String applicationNotAdmittedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.legalRep.email}") String applicationRefusedGrantedApplicantLegalRepTemplateId,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedApplicantLegalRepTemplateId = applicationGrantedApplicantLegalRepTemplateId;
        this.applicationPartiallyGrantedApplicantLegalRepTemplateId = applicationPartiallyGrantedApplicantLegalRepTemplateId;
        this.applicationNotAdmittedApplicantLegalRepTemplateId = applicationNotAdmittedApplicantLegalRepTemplateId;
        this.applicationRefusedGrantedApplicantLegalRepTemplateId = applicationRefusedGrantedApplicantLegalRepTemplateId;;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        FtpaDecisionOutcomeType ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaAppellaneDecisionOutcomeType is not present"));

        if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedApplicantLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedApplicantLegalRepTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedGrantedApplicantLegalRepTemplateId;
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
