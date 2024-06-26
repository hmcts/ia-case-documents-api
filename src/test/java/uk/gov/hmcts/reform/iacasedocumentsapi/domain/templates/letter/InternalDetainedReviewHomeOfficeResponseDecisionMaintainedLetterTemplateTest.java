package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DateProvider dateProvider;
    private final String telephoneNumber = "0300 123 1711";
    private final String email = "IAC-ADA-HW@justice.gov.uk";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private final String templateName = "INTERNAL_REVIEW_HOME_OFFICE_RESPONSE_NOTICE_TEMPLATE.docx";
    private final String logo = "[userImage:hmcts.png]";
    private final int dueInCalendarDays = 5;
    private InternalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterTemplate internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterTemplate;
    private Map<String, Object> fieldValuesMap;
    private final String directionExplanation = "Some explanation";
    private final Parties directionParties = Parties.APPELLANT;
    private final String directionDateDue = "2023-06-16";
    private final String directionDateSent = "2023-06-02";
    private final String directionUniqueId = "95e90870-2429-4660-b9c2-4111aff37304";
    private final String directionType = "someDirectionType";
    private final IdValue<Direction> hoReviewMaintainedDirection = new IdValue<>(
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
    public void setUp() {
        internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterTemplate =
                new InternalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterTemplate(
                        templateName,
                        dateProvider,
                        customerServicesProvider,
                        dueInCalendarDays);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterTemplate.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(email);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(hoReviewMaintainedDirection);
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));
        when(dateProvider.now()).thenReturn(LocalDate.now());
    }

    @Test
    void should_populate_template() {
        dataSetup();
        fieldValuesMap = internalDetainedReviewHomeOfficeResponseDecisionMaintainedLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals("21 Jun 2023", fieldValuesMap.get("dueDate"));
    }

}