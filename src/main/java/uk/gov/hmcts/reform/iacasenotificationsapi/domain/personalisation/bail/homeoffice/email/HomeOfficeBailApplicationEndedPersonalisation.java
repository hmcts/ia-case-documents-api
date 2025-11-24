package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;

@Service
public class HomeOfficeBailApplicationEndedPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailApplicationEndedWithLegalRepPersonalisationTemplateId;
    private final String homeOfficeBailApplicationEndedWithoutLegalRepPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;


    public HomeOfficeBailApplicationEndedPersonalisation(
        @NotNull(message = "homeOfficeBailApplicationEndedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.endApplication.email}") String homeOfficeBailApplicationEndedWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.endApplicationWithoutLR.email}") String homeOfficeBailApplicationEndedWithoutLegalRepPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeBailApplicationEndedWithLegalRepPersonalisationTemplateId = homeOfficeBailApplicationEndedWithLegalRepPersonalisationTemplateId;
        this.homeOfficeBailApplicationEndedWithoutLegalRepPersonalisationTemplateId = homeOfficeBailApplicationEndedWithoutLegalRepPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_ENDED_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES
            ? homeOfficeBailApplicationEndedWithLegalRepPersonalisationTemplateId : homeOfficeBailApplicationEndedWithoutLegalRepPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
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
