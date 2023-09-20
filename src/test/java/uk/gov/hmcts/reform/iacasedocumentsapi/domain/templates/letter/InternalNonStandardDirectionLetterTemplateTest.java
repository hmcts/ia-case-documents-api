package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)

public class InternalNonStandardDirectionLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DirectionFinder directionFinder;
    private InternalNonStandardDirectionLetterTemplate  internalNonStandardDirectionLetterTemplate;
    private final String telephoneNumber = "0300 123 1711";
    private final String email = "IAC-ADA-HW@justice.gov.uk";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final LocalDate now = LocalDate.now();
    private final String templateName = "TB-IAC-LET-ENG-00028.docx";

    private final String directionExplanation = "test reasons new direction sent";
    private final Parties directionParties = Parties.APPELLANT;
    private final String directionDateDue = "2023-06-16";
    private final String directionDateSent = "2023-06-02";
    private final String directionUniqueId = "95e90870-2429-4660-b9c2-4111aff37304";
    private final String directionType = "someDirectionType";

    private final Direction directionOne =  new Direction(
        directionExplanation,
        directionParties,
        directionDateDue,
        directionDateSent,
        DirectionTag.NONE,
        Collections.emptyList(),
        Collections.emptyList(),
        directionUniqueId,
        directionType
    );

    public InternalNonStandardDirectionLetterTemplateTest() {
    }

    @BeforeEach
    public void setUp() {
        internalNonStandardDirectionLetterTemplate =
            new InternalNonStandardDirectionLetterTemplate(
                templateName,
                customerServicesProvider,
                directionFinder
            );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalNonStandardDirectionLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(email);;
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();
        when(directionFinder.findFirst(asylumCase, DirectionTag.NONE)).thenReturn(Optional.of(directionOne));
        Map<String, Object> templateFieldValues = internalNonStandardDirectionLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals(telephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(email, templateFieldValues.get("customerServicesEmail"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(directionExplanation, templateFieldValues.get("sendDirectionContent"));


    }
}