package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailDirection;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Objects.requireNonNull;

@Service
public class HomeOfficeUploadBailSummaryDirectionPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeUploadBailSummaryDirectionPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;

    public HomeOfficeUploadBailSummaryDirectionPersonalisation(
        @NotNull(message = "homeOfficeBailApplicationSubmittedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.sendUploadBailSummaryDirection.email}") String homeOfficeUploadBailSummaryDirectionPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        this.homeOfficeUploadBailSummaryDirectionPersonalisationTemplateId = homeOfficeUploadBailSummaryDirectionPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_UPLOAD_BAIL_SUMMARY_DIRECTION_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return homeOfficeUploadBailSummaryDirectionPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        Optional<BailDirection> direction = findLatestCreatedDirection(bailCase);

        String dateOfCompliance = direction.map(BailDirection::getDateOfCompliance).orElse("");
        if (!dateOfCompliance.isBlank()) {
            dateOfCompliance = LocalDate.parse(dateOfCompliance).format(DateTimeFormatter.ofPattern("d MMM uuuu"));
        }

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)
                .map(lrr -> "\nLegal representative reference: " + lrr).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("dateOfCompliance", dateOfCompliance)
            .build();
    }

    private Optional<BailDirection> findLatestCreatedDirection(BailCase bailCase) {
        Optional<List<IdValue<BailDirection>>> optionalDirections = bailCase.read(BailCaseFieldDefinition.DIRECTIONS);
        List<IdValue<BailDirection>> directions = optionalDirections.orElse(Collections.emptyList());

        if (!directions.isEmpty()) {
            return directions.stream()
                .map(IdValue::getValue)
                .max(Comparator.comparing(direction -> LocalDateTime.parse(direction.getDateTimeDirectionCreated())));
        }
        return Optional.empty();
    }
}
