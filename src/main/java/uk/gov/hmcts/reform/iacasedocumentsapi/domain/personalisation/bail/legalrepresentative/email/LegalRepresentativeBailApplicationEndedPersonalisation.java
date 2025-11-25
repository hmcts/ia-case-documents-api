package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

@Service
public class LegalRepresentativeBailApplicationEndedPersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String bailApplicationEndedLegalRepresentativeTemplateId;


    public LegalRepresentativeBailApplicationEndedPersonalisation(
        @NotNull(message = "bailApplicationEndedLegalRepresentativeTemplateId cannot be null")
        @Value("${govnotify.bail.template.endApplicationLR.email}") String bailApplicationEndedLegalRepresentativeTemplateId
    ) {
        this.bailApplicationEndedLegalRepresentativeTemplateId = bailApplicationEndedLegalRepresentativeTemplateId;
    }

    @Override
    public String getTemplateId() {
        return bailApplicationEndedLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_ENDED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        String endApplicationDate = bailCase.read(BailCaseFieldDefinition.END_APPLICATION_DATE, String.class).orElse("");
        if (!endApplicationDate.isBlank()) {
            endApplicationDate = LocalDate.parse(endApplicationDate).format(DateTimeFormatter.ofPattern("d MMM uuuu"));
        }

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("endApplicationReasons", bailCase.read(BailCaseFieldDefinition.END_APPLICATION_REASONS, String.class).orElse("No reason given"))
            .put("endApplicationOutcome", bailCase.read(BailCaseFieldDefinition.END_APPLICATION_OUTCOME, String.class).orElse(""))
            .put("endApplicationDate", endApplicationDate)
            .build();
    }

}
