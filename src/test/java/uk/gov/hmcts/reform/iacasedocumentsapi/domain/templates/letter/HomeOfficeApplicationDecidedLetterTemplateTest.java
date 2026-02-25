package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeApplicationDecidedLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private SystemDateProvider systemDateProvider;
    @Mock private MakeAnApplication makeAnApplication;

    private final String templateName = "TB-IAC-LET-ENG-00020.docx";
    private final int daysAfterApplicationDecisionInCountry = 14;
    private final String appealReferenceNumber = "HU/11111/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final String decidedApplicationId = "1";
    private final String dueDate = "2023-12-01";
    private final String applicationDate = "2023-10-01";
    private final String applicationReason = "Application granted as requested";
    private final String applicationType = "Time extension";

    private HomeOfficeApplicationDecidedLetterTemplate homeOfficeApplicationDecidedLetterTemplate;

    @BeforeEach
    void setUp() {
        homeOfficeApplicationDecidedLetterTemplate =
            new HomeOfficeApplicationDecidedLetterTemplate(
                templateName,
                daysAfterApplicationDecisionInCountry,
                customerServicesProvider,
                systemDateProvider
            );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    void dataSetUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of(decidedApplicationId));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(Collections.singletonList(
            new IdValue<>(decidedApplicationId, makeAnApplication)
        )));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry)).thenReturn(dueDate);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, homeOfficeApplicationDecidedLetterTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values_for_granted_application() {
        dataSetUp();
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getDate()).thenReturn(applicationDate);
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);

        Map<String, Object> templateFieldValues = homeOfficeApplicationDecidedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("grant", templateFieldValues.get("decision"));
        assertEquals(applicationType, templateFieldValues.get("applicationType"));
        assertEquals(applicationDate, templateFieldValues.get("applicationDate"));
        assertEquals(applicationReason, templateFieldValues.get("applicationReason"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals("The tribunal will give the Home Office more time to complete its next task. You will get a notification with the new date soon.", templateFieldValues.get("nextSteps"));
    }

    @Test
    void should_map_case_data_to_template_field_values_for_refused_application() {
        dataSetUp();
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getDate()).thenReturn(applicationDate);
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);

        Map<String, Object> templateFieldValues = homeOfficeApplicationDecidedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("refuse", templateFieldValues.get("decision"));
        assertEquals(applicationType, templateFieldValues.get("applicationType"));
        assertEquals(applicationDate, templateFieldValues.get("applicationDate"));
        assertEquals(applicationReason, templateFieldValues.get("applicationReason"));
        assertEquals("The appeal will continue without any changes.", templateFieldValues.get("nextSteps"));
    }

    @Test
    void should_map_case_data_to_template_field_values_for_partially_granted_application() {
        dataSetUp();
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(makeAnApplication.getDecision()).thenReturn("Partially granted");
        when(makeAnApplication.getDate()).thenReturn(applicationDate);
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);

        Map<String, Object> templateFieldValues = homeOfficeApplicationDecidedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("refuse", templateFieldValues.get("decision")); // Should map to "refuse" for anything not "Granted"
        assertEquals("The appeal will continue without any changes.", templateFieldValues.get("nextSteps"));
    }

    @Test
    void should_throw_exception_when_invalid_application_type() {
        dataSetUp();
        when(makeAnApplication.getType()).thenReturn("Invalid Type");
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getDate()).thenReturn(applicationDate);
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);

        assertThrows(IllegalStateException.class, () ->
            homeOfficeApplicationDecidedLetterTemplate.mapFieldValues(caseDetails));
    }

    @Test
    void should_map_case_data_for_other_application_type_refused() {
        dataSetUp();
        when(makeAnApplication.getType()).thenReturn("Other");
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getDate()).thenReturn(applicationDate);
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);

        Map<String, Object> templateFieldValues = homeOfficeApplicationDecidedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("refuse", templateFieldValues.get("decision"));
        assertEquals("The appeal will continue without any changes.", templateFieldValues.get("nextSteps"));
    }

    @Test
    void should_map_case_data_for_other_application_type_granted() {
        dataSetUp();
        when(makeAnApplication.getType()).thenReturn("Other");
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getDate()).thenReturn(applicationDate);
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);

        Map<String, Object> templateFieldValues = homeOfficeApplicationDecidedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("grant", templateFieldValues.get("decision"));
        assertEquals("You will be notified when the tribunal makes the changes the Home Office asked for.", templateFieldValues.get("nextSteps"));
    }

    @Test
    void should_handle_missing_optional_fields() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)).thenReturn(Optional.of(decidedApplicationId));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(Collections.singletonList(
            new IdValue<>(decidedApplicationId, makeAnApplication)
        )));
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getDate()).thenReturn(applicationDate);
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = homeOfficeApplicationDecidedLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("grant", templateFieldValues.get("decision"));
    }
}