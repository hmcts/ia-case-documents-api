package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final DueDateService dueDateService;
    private final PersonalisationProvider personalisationProvider;
    private final String applicationGrantedApplicantLegalRepTemplateId;
    private final String applicationPartiallyGrantedApplicantLegalRepTemplateId;
    private final String applicationNotAdmittedApplicantLegalRepTemplateId;
    private final String applicationRefusedGrantedApplicantLegalRepTemplateId;
    private final String applicationReheardApplicantLegalRepTemplateId;
    private final String applicationAllowedLegalRepTemplateId;
    private final String applicationDismissedLegalRepTemplateId;
    private final int calendarDaysToWaitInCountry;
    private final int calendarDaysToWaitOutOfCountry;
    private final int workingDaysaysToWaitAda;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;


    public LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation(
        @Value("${govnotify.template.applicationGranted.applicant.legalRep.email}") String applicationGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.legalRep.email}") String applicationPartiallyGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.legalRep.email}") String applicationNotAdmittedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.legalRep.email}") String applicationRefusedGrantedApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationReheard.applicant.legalRep.email}") String applicationReheardApplicantLegalRepTemplateId,
        @Value("${govnotify.template.applicationAllowed.legalRep.email}") String applicationAllowedLegalRepTemplateId,
        @Value("${govnotify.template.applicationDismissed.legalRep.email}") String applicationDismissedLegalRepTemplateId,
        @Value("${ftpaApplicationDecidedDaysToWait.inCountry}") int calendarDaysToWaitInCountry,
        @Value("${ftpaApplicationDecidedDaysToWait.outOfCountry}") int calendarDaysToWaitOutOfCountry,
        @Value("${ftpaApplicationDecidedDaysToWait.ada}") int workingDaysaysToWaitAda,
        DueDateService dueDateService,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedApplicantLegalRepTemplateId = applicationGrantedApplicantLegalRepTemplateId;
        this.applicationPartiallyGrantedApplicantLegalRepTemplateId = applicationPartiallyGrantedApplicantLegalRepTemplateId;
        this.applicationNotAdmittedApplicantLegalRepTemplateId = applicationNotAdmittedApplicantLegalRepTemplateId;
        this.applicationRefusedGrantedApplicantLegalRepTemplateId = applicationRefusedGrantedApplicantLegalRepTemplateId;
        this.applicationReheardApplicantLegalRepTemplateId = applicationReheardApplicantLegalRepTemplateId;
        this.applicationAllowedLegalRepTemplateId = applicationAllowedLegalRepTemplateId;
        this.applicationDismissedLegalRepTemplateId = applicationDismissedLegalRepTemplateId;
        this.calendarDaysToWaitInCountry = calendarDaysToWaitInCountry;
        this.calendarDaysToWaitOutOfCountry = calendarDaysToWaitOutOfCountry;
        this.workingDaysaysToWaitAda = workingDaysaysToWaitAda;
        this.dueDateService = dueDateService;
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
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        ImmutableMap.Builder<String, String> personalisationBuilder = ImmutableMap
            .<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase));

        boolean setDynamicDate = Arrays.asList(
            applicationPartiallyGrantedApplicantLegalRepTemplateId,
            applicationNotAdmittedApplicantLegalRepTemplateId,
            applicationRefusedGrantedApplicantLegalRepTemplateId).contains(getTemplateId(asylumCase));

        if (setDynamicDate) {
            boolean inCountryAppeal = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).map(value -> value.equals(YesOrNo.YES)).orElse(true);
            if (isAcceleratedDetainedAppeal(asylumCase)) {
                return personalisationBuilder.put("due date", dueDateService.calculateWorkingDaysDueDate(ZonedDateTime.now(), workingDaysaysToWaitAda)
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))).build();
            } else if (inCountryAppeal) {
                return personalisationBuilder.put("due date", dueDateService.calculateCalendarDaysDueDate(ZonedDateTime.now(), calendarDaysToWaitInCountry)
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))).build();
            } else {
                return personalisationBuilder.put("due date", dueDateService.calculateCalendarDaysDueDate(ZonedDateTime.now(), calendarDaysToWaitOutOfCountry)
                    .format(DateTimeFormatter.ofPattern("d MMMM yyyy"))).build();
            }
        }

        return personalisationBuilder.build();
    }
}
