package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

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
public class InternalAdaRequestBuildCaseTemplateTest {
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
    private final LocalDate now = LocalDate.now();
    private final ZonedDateTime zonedDueDateTime = LocalDate.parse("2023-01-01").atStartOfDay(ZoneOffset.UTC);
    private final LocalDate hearingSupportRequirementsDueDate = zonedDueDateTime.toLocalDate();
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String directionExplanation = "Some explanation";
    private final Parties directionParties = Parties.APPELLANT;
    private final String directionDateDue = "2023-06-16";
    private final String directionDateSent = "2023-06-02";
    private final String directionUniqueId = "95e90870-2429-4660-b9c2-4111aff37304";
    private final String directionType = "someDirectionType";
    private final IdValue<Direction> requestCaseBuildingDirection = new IdValue<>(
            "1",
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    DirectionTag.REQUEST_CASE_BUILDING,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    private final IdValue<Direction> duplicateRequestCaseBuildingDirection = new IdValue<>(
            "2",
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    DirectionTag.REQUEST_CASE_BUILDING,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    private final IdValue<Direction> otherDirection = new IdValue<>(
            "3",
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    DirectionTag.ADA_LIST_CASE,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    private InternalAdaRequestBuildCaseTemplate internalAdaRequestBuildCaseTemplate;

    @BeforeEach
    void setUp() {
        internalAdaRequestBuildCaseTemplate =
                new InternalAdaRequestBuildCaseTemplate(
                        hearingSupportRequirementsDueInWorkingDays,
                        templateName,
                        customerServicesProvider,
                        dueDateService
                );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalAdaRequestBuildCaseTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalAdaCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalAdaCustomerServicesEmailAddress);

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestCaseBuildingDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        when(dueDateService.calculateDueDate(any(), eq(hearingSupportRequirementsDueInWorkingDays))).thenReturn(zonedDueDateTime);

    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalAdaRequestBuildCaseTemplate.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(directionDateDue)), templateFieldValues.get("responseDueDate"));
        assertEquals(formatDateForNotificationAttachmentDocument(hearingSupportRequirementsDueDate), templateFieldValues.get("hearingSupportRequirementsDueDate"));

    }

    @Test
    void should_throw_when_request_case_building_direction_not_present() {
        dataSetUp();
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalAdaRequestBuildCaseTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("No requestBuildCase directions found");

    }

    @Test
    void should_throw_when_multiple_request_case_building_directions_are_present() {
        dataSetUp();
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestCaseBuildingDirection);
        directionList.add(duplicateRequestCaseBuildingDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        assertThatThrownBy(() -> internalAdaRequestBuildCaseTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("More than 1 requestCaseBuilding direction");
    }
}
