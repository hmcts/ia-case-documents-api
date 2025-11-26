package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalHomeOfficeAmendAppealResponseTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DirectionFinder directionFinder;
    private final String templateName = "TB-IAC-DEC-ENG-00029.docx";
    private final String customerServicesTelephoneNumber = "0300 123 1711";
    private final String customerServicesEmailAddress = "IAC-ADA-HW@justice.gov.uk";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private String directionDueDate = "2024-01-01";
    private String formattedDirectionDueDate = formatDateForNotificationAttachmentDocument(LocalDate.parse(directionDueDate));
    private final IdValue<Direction> requestResponseAmendDirectionOne = new IdValue<>(
            "1",
            new Direction(
                    "Some explanation",
                    Parties.RESPONDENT,
                    directionDueDate,
                    "2023-09-20",
                    DirectionTag.REQUEST_RESPONSE_AMEND,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "Some unique ID",
                    "Some direction type"
            )
    );

    private final IdValue<Direction> requestResponseAmendDirectionTwo = new IdValue<>(
            "2",
            new Direction(
                    "Some explanation",
                    Parties.RESPONDENT,
                    directionDueDate,
                    "2023-09-20",
                    DirectionTag.REQUEST_RESPONSE_AMEND,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "Some unique ID",
                    "Some direction type"
            )
    );

    private final IdValue<Direction> otherRandomDirection = new IdValue<>(
            "3",
            new Direction(
                    "Some explanation",
                    Parties.RESPONDENT,
                    directionDueDate,
                    "2023-09-20",
                    DirectionTag.REQUEST_CASE_BUILDING,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "Some unique ID",
                    "Some direction type"
            )
    );

    private InternalHomeOfficeAmendAppealResponseTemplate internalHomeOfficeAmendAppealResponseTemplate;

    @BeforeEach
    void setUp() {
        internalHomeOfficeAmendAppealResponseTemplate =
                new InternalHomeOfficeAmendAppealResponseTemplate(
                        templateName,
                        dateProvider,
                        customerServicesProvider,
                        directionFinder
                );
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestResponseAmendDirectionOne);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_AMEND)).thenReturn(Optional.of(requestResponseAmendDirectionOne.getValue()));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(customerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(customerServicesEmailAddress);

        when(dateProvider.now()).thenReturn(LocalDate.now());
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalHomeOfficeAmendAppealResponseTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetup();

        Map<String, Object> templateFieldValues = internalHomeOfficeAmendAppealResponseTemplate.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(formattedDirectionDueDate, templateFieldValues.get("directionDueDate"));
    }

    @Test
    void should_extract_correct_direction_and_map_case_data_to_template_field_values() {
        dataSetup();

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestResponseAmendDirectionOne);
        directionList.add(otherRandomDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_AMEND)).thenReturn(Optional.of(requestResponseAmendDirectionOne.getValue()));

        Map<String, Object> templateFieldValues = internalHomeOfficeAmendAppealResponseTemplate.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(formattedDirectionDueDate, templateFieldValues.get("directionDueDate"));
    }

    @Test
    void should_extract_latest_request_response_amend_direction_and_map_case_data_to_template_field_values() {
        dataSetup();

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestResponseAmendDirectionOne);
        directionList.add(requestResponseAmendDirectionTwo);
        directionList.add(otherRandomDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_RESPONSE_AMEND)).thenReturn(Optional.of(requestResponseAmendDirectionTwo.getValue()));

        Map<String, Object> templateFieldValues = internalHomeOfficeAmendAppealResponseTemplate.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(formattedDirectionDueDate, templateFieldValues.get("directionDueDate"));
    }
}
