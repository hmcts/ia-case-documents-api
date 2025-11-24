package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailDirection;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

@Service
public class LegalRepresentativeBailDirectionSentPersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId;
    private final String legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId;

    public LegalRepresentativeBailDirectionSentPersonalisation(
        @NotNull(message = "legalRepresentativeBailDirectionSentPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.sendDirectionDirectRecipient.email}") String legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId,
        @Value("${govnotify.bail.template.sendDirectionOtherParties.email}") String legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId
    ) {
        this.legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId = legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId;
        this.legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId = legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_SENT_DIRECTION_LEGAL_REPRESENTATIVE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return isDirectRecipient(findLatestCreatedDirection(bailCase))
            ? legalRepresentativeBailDirectionSentDirectRecipientPersonalisationTemplateId
            : legalRepresentativeBailDirectionSentOtherPartiesPersonalisationTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        Optional<BailDirection> direction = findLatestCreatedDirection(bailCase);

        String dateOfCompliance = direction.map(BailDirection::getDateOfCompliance).orElse("");
        if (!dateOfCompliance.isBlank()) {
            dateOfCompliance = LocalDate.parse(dateOfCompliance).format(DateTimeFormatter.ofPattern("d MMM uuuu"));
        }

        return isDirectRecipient(direction)
            ? ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)
                .map(lrr -> "\nLegal representative reference: " + lrr).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("sendDirectionDescription", direction.map(BailDirection::getSendDirectionDescription).orElse(""))
            .put("dateOfCompliance", dateOfCompliance)
            .build()
            : ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)
                .map(lrr -> "\nLegal representative reference: " + lrr).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("sendDirectionDescription", direction.map(BailDirection::getSendDirectionDescription).orElse(""))
            .put("dateOfCompliance", dateOfCompliance)
            .put("party", direction.map(BailDirection::getSendDirectionList).orElse(""))
            .build();
    }

    private boolean isDirectRecipient(Optional<BailDirection> direction) {
        return direction.map(BailDirection::getSendDirectionList).orElse("").equals("Legal Representative");
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