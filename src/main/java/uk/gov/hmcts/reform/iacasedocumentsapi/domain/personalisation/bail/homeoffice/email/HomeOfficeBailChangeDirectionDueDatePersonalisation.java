package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;

@Service
public class HomeOfficeBailChangeDirectionDueDatePersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailChangeDirectionDueDateWithLegalRepPersonalisationTemplateId;
    private final String homeOfficeBailChangeDirectionDueDateWithoutLegalRepPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;


    public HomeOfficeBailChangeDirectionDueDatePersonalisation(
        @NotNull(message = "homeOfficeBailChangeDirectionDueDatePersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.changeBailDirectionDueDate.email}") String homeOfficeBailChangeDirectionDueDateWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.changeBailDirectionDueDateWithoutLR.email}") String homeOfficeBailChangeDirectionDueDateWithoutLegalRepPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailChangeDirectionDueDateWithLegalRepPersonalisationTemplateId = homeOfficeBailChangeDirectionDueDateWithLegalRepPersonalisationTemplateId;
        this.homeOfficeBailChangeDirectionDueDateWithoutLegalRepPersonalisationTemplateId = homeOfficeBailChangeDirectionDueDateWithoutLegalRepPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CHANGE_BAIL_DIRECTION_DUE_DATE_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return  bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES
            ? homeOfficeBailChangeDirectionDueDateWithLegalRepPersonalisationTemplateId : homeOfficeBailChangeDirectionDueDateWithoutLegalRepPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
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
