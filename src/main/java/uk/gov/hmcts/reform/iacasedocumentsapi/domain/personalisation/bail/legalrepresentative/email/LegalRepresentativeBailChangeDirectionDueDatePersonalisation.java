package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

@Service
public class LegalRepresentativeBailChangeDirectionDueDatePersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {


    private final String legalRepresentativeBailChangeDirectionDueDatePersonalisationTemplateId;

    public LegalRepresentativeBailChangeDirectionDueDatePersonalisation(
        @NotNull(message = "legalRepresentativeBailChangeDirectionDueDatePersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.changeBailDirectionDueDate.email}") String legalRepresentativeBailChangeDirectionDueDatePersonalisationTemplateId
    ) {
        this.legalRepresentativeBailChangeDirectionDueDatePersonalisationTemplateId = legalRepresentativeBailChangeDirectionDueDatePersonalisationTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CHANGE_BAIL_DIRECTION_DUE_DATE_LEGAL_REPRESENTATIVE";
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeBailChangeDirectionDueDatePersonalisationTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        String changeDirectionDueDate = bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_DATE_DUE, String.class).orElse("");
        if (!changeDirectionDueDate.isBlank()) {
            changeDirectionDueDate = LocalDate.parse(changeDirectionDueDate).format(DateTimeFormatter.ofPattern("d MMM uuuu"));
        }

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("party", bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_PARTIES, String.class).orElse(""))
            .put("directionDueDate", changeDirectionDueDate)
            .put("explanation", bailCase.read(BailCaseFieldDefinition.BAIL_DIRECTION_EDIT_EXPLANATION, String.class).orElse(""))
            .build();
    }

}
