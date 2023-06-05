package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Component
public class InternalAdaBuildCaseTemplate implements DocumentTemplate<AsylumCase> {

    private final int hearingSupportResponseDueInWorkingDays;
    private final String templateName;
    private final CustomerServicesProvider customerServicesProvider;
    private final DueDateService dueDateService;


    public InternalAdaBuildCaseTemplate(
            @Value("${internalAdaBuildCaseDocument.hearingSupportResponseDueInWorkingDays}") int hearingSupportResponseDueInWorkingDays,
            @Value("${internalAdaBuildCaseDocument.templateName}") String templateName,
            CustomerServicesProvider customerServicesProvider,
            DueDateService dueDateService
    ) {
        this.hearingSupportResponseDueInWorkingDays = hearingSupportResponseDueInWorkingDays;
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.dueDateService = dueDateService;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
            CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        final Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);
        final List<IdValue<Direction>> existingDirections = maybeDirections
                .orElseThrow(() -> new RequiredFieldMissingException("No directions found"));

        List<Direction> requestBuildCaseDirectionList = existingDirections
                .stream()
                .map(IdValue::getValue)
                .filter(direction -> direction.getTag() == DirectionTag.REQUEST_CASE_BUILDING)
                .collect(Collectors.toUnmodifiableList());

        if (requestBuildCaseDirectionList.isEmpty() || requestBuildCaseDirectionList.size() > 1) {
            throw new IllegalStateException("There must be only 1 requestCaseBuilding direction. Either this is not present, or multiple requestCaseBuilding directions exist.");
        }
        Direction requestBuildCaseDirection = requestBuildCaseDirectionList.get(0);

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalAdaCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalAdaCustomerServicesEmail());

        fieldValues.put("dateLetterSent", LocalDate.now().toString());
        fieldValues.put("responseDueDate", requestBuildCaseDirection.getDateDue());

        fieldValues.put("hearingSupportRequirementsDueDate", dueDateService
                .calculateDueDate(ZonedDateTime.now(), hearingSupportResponseDueInWorkingDays)
                .toLocalDate()
                .toString());

        return fieldValues;
    }

}
