package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalAdaBuildCaseTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DueDateService dueDateService;
    private final String templateName = "TB-IAC-DEC-ENG-00002.docx";
    private final int hearingSupportRequirementsDueInWorkingDays = 15;
    private final String internalAdaCustomerServicesTelephoneNumber = "0300 123 1234";
    private final String internalAdaCustomerServicesEmailAddress = "example@email.com";
    private final String now = LocalDate.now().toString();
    private final ZonedDateTime zonedDueDateTime = LocalDate.parse("2023-01-01").atStartOfDay(ZoneOffset.UTC);
    private final String hearingSupportRequirementsDueDate = zonedDueDateTime.toLocalDate().toString();
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String directionIdValue = "1";
    private final String directionExplanation = "some explanation";
    private final Parties directionParties = Parties.APPELLANT;
    private final String directionDateDue = "2023-06-16";
    private final String directionDateSent = "2023-06-02";
    private final DirectionTag directionTag = DirectionTag.REQUEST_CASE_BUILDING;
    private final String directionUniqueId = "95e90870-2429-4660-b9c2-4111aff37304";
    private final String directionType = "requestCaseBuilding";
    private final IdValue<Direction> requestCaseBuildingDirection = new IdValue<>(
            directionIdValue,
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    directionTag,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    private InternalAdaBuildCaseTemplate internalAdaBuildCaseTemplate;

    @BeforeEach
    void setUp() {
        internalAdaBuildCaseTemplate =
                new InternalAdaBuildCaseTemplate(
                        hearingSupportRequirementsDueInWorkingDays,
                        templateName,
                        customerServicesProvider,
                        dueDateService
                );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalAdaBuildCaseTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(customerServicesProvider.getInternalAdaCustomerServicesTelephone()).thenReturn(internalAdaCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalAdaCustomerServicesEmail()).thenReturn(internalAdaCustomerServicesEmailAddress);

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestCaseBuildingDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        when(dueDateService.calculateDueDate(any(), eq(hearingSupportRequirementsDueInWorkingDays))).thenReturn(zonedDueDateTime);

    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalAdaBuildCaseTemplate.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));

        assertEquals(now, templateFieldValues.get("dateLetterSent"));
        assertEquals(directionDateDue, templateFieldValues.get("responseDueDate"));
        assertEquals(hearingSupportRequirementsDueDate, templateFieldValues.get("hearingSupportRequirementsDueDate"));

    }

    @Test
    void should_throw_when_no_directions_present() {
        dataSetUp();
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalAdaBuildCaseTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("No directions found");
    }

    @Test
    void should_throw_when_request_case_building_direction_not_present() {
        dataSetUp();

        final IdValue<Direction> randomDirection = new IdValue<>(
                "1",
                new Direction(
                        "Some explanation",
                        directionParties,
                        directionDateDue,
                        directionDateSent,
                        DirectionTag.CASE_EDIT,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        directionUniqueId,
                        "caseEdit"
                )
        );

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(randomDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        assertThatThrownBy(() -> internalAdaBuildCaseTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("There must be only 1 requestCaseBuilding direction. Either this is not present, or multiple requestCaseBuilding directions exist.");

    }

    @Test
    void should_throw_when_multiple_request_case_building_directions_are_present() {
        dataSetUp();

        final IdValue<Direction> randomDirection = new IdValue<>(
                "2",
                new Direction(
                        directionExplanation,
                        directionParties,
                        directionDateDue,
                        directionDateSent,
                        directionTag,
                        Collections.emptyList(),
                        Collections.emptyList(),
                        directionUniqueId + "123",
                        directionType
                )
        );
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestCaseBuildingDirection);
        directionList.add(randomDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        assertThatThrownBy(() -> internalAdaBuildCaseTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("There must be only 1 requestCaseBuilding direction. Either this is not present, or multiple requestCaseBuilding directions exist.");
    }
}
