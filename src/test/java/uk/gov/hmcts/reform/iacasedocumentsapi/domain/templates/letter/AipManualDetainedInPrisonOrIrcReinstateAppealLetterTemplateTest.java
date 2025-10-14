package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class AipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    private AipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate;
    private final String templateName = "TB-IAC-LET-ENG-00073.docx";
    private final String appealReferenceNumber = "HU/11111/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final LocalDate now = LocalDate.now();
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final String reinstatedDecisionMaker = "Admin Officer";
    private final String reinstateAppealDate = "2023-09-28";
    private final String formattedReinstateAppealDate = "28 September 2023";
    private final String reinstateAppealReason = "The reason why the appeal was reinstated.";

    @BeforeEach
    void setUp() {
        aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate =
            new AipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate(
                templateName,
                customerServicesProvider
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of(reinstatedDecisionMaker));
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of(reinstateAppealDate));
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.of(reinstateAppealReason));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals("An " + reinstatedDecisionMaker, templateFieldValues.get("reinstatedDecisionMaker"));
        assertEquals(formattedReinstateAppealDate, templateFieldValues.get("reinstateAppealDate"));
        assertEquals(reinstateAppealReason, templateFieldValues.get("reinstateAppealReason"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_missing_optional_fields() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.empty());
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("reinstatedDecisionMaker"));
        assertEquals("", templateFieldValues.get("reinstateAppealDate"));
        assertEquals("", templateFieldValues.get("reinstateAppealReason"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_date_formatting() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        // Test different date formats
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of("2023-12-25"));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("25 December 2023", templateFieldValues.get("reinstateAppealDate"));
    }

    @Test
    void should_handle_empty_date() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        // Test empty date
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of(""));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("", templateFieldValues.get("reinstateAppealDate"));
    }

    @Test
    void should_add_correct_article_before_decision_maker_starting_with_vowel() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of("Officer"));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("An Officer", templateFieldValues.get("reinstatedDecisionMaker"));
    }

    @Test
    void should_add_correct_article_before_decision_maker_starting_with_consonant() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of("Legal Worker"));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("A Legal Worker", templateFieldValues.get("reinstatedDecisionMaker"));
    }

    @Test
    void should_handle_u_sound_correctly() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of("User"));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = aipManualDetainedInPrisonOrIrcReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("A User", templateFieldValues.get("reinstatedDecisionMaker"));
    }
}