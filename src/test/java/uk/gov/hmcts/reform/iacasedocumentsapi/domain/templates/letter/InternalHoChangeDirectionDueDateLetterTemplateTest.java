package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalHoChangeDirectionDueDateLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String telephoneNumber = "0300 123 1711";
    private final String email = "IAC-ADA-HW@justice.gov.uk";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private String directionEditDueDate = "2023-07-01";
    private String directionEditExplanation = "Test Explanation";
    private final String templateName = "IA_INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_TEMPLATE.docx";
    private final String logo = "[userImage:hmcts.png]";
    private InternalHoChangeDirectionDueDateLetterTemplate internalHoChangeDirectionDueDateLetterTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        internalHoChangeDirectionDueDateLetterTemplate =
                new InternalHoChangeDirectionDueDateLetterTemplate(templateName, customerServicesProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalHoChangeDirectionDueDateLetterTemplate.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(email);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(directionEditDueDate));
        when(asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.of(directionEditExplanation));
    }

    @Test
    void should_populate_template() {
        dataSetup();
        fieldValuesMap = internalHoChangeDirectionDueDateLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(directionEditExplanation, fieldValuesMap.get("directionExplaination"));
        assertEquals("1 Jul 2023", fieldValuesMap.get("dueDate"));
    }

    @Test
    void should_throw_if_direction_edit_due_date_is_not_present() {
        dataSetup();
        when(asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalHoChangeDirectionDueDateLetterTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Direction edit date due is not present");
    }

    @Test
    void should_throw_if_direction_edit_explanation_is_not_present() {
        dataSetup();
        when(asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalHoChangeDirectionDueDateLetterTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Direction edit explanation is not present");
    }
}