package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedRequestHearingRequirementsTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DueDateService mockDueDateService;
    private final String templateName = "TB-IAC-GNO-ENG-00006.docx";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "IAC-ADA-HW@justice.gov.uk";
    private final LocalDate now = LocalDate.now();
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String requestRespondentEvidenceDueDate = "2023-07-10";
    private InternalDetainedRequestHearingRequirementsTemplate internalDetainedRequestHearingRequirementsTemplate;
    private final String directionExplanation = "Some explanation";
    private final Parties directionParties = Parties.APPELLANT;
    private final String directionDateDue = "2023-08-10";
    private final String directionDateSent = "2023-06-02";
    private final String directionUniqueId = "95e90870-2429-4660-b9c2-4111aff37304";
    private final String directionType = "someDirectionType";
    private final ZonedDateTime zonedDueDateTime = LocalDate.parse("2023-08-17").atStartOfDay(ZoneOffset.UTC);

    private final IdValue<Direction> requestResponseReviewDirection = new IdValue<>(
            "1",
            new Direction(
                    directionExplanation,
                    directionParties,
                    directionDateDue,
                    directionDateSent,
                    DirectionTag.REQUEST_RESPONSE_REVIEW,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    directionUniqueId,
                    directionType
            )
    );

    @BeforeEach
    void setUp() {
        internalDetainedRequestHearingRequirementsTemplate =
                new InternalDetainedRequestHearingRequirementsTemplate(
                        templateName,
                        customerServicesProvider,
                        mockDueDateService
                );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDetainedRequestHearingRequirementsTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of(requestRespondentEvidenceDueDate));

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(requestResponseReviewDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));
        when(mockDueDateService.calculateDueDate(any(), eq(5))).thenReturn(zonedDueDateTime);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalDetainedRequestHearingRequirementsTemplate.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals("17 Aug 2023", templateFieldValues.get("hearingReqSubmissionDeadline"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
    }

}