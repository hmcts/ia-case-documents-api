package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATE_TRIBUNAL_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private SystemDateProvider systemDateProvider;

    private final String templateName = "TB-IAC-LET-ENG-Internal-detained-update-tribunal-decision-rule-31-irc-prison.docx";
    private final int daysAfterSubmitAppeal = 14;
    private final String internalCustomerServicesTelephone = "0300 123 1711";
    private final String internalCustomerServicesEmail = "IAC-DetainedTeam@justice.gov.uk";
    private final LocalDate dueDate = LocalDate.of(2023, 8, 15);
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String updateTribunalDecisionDate =  "2025-10-01";
    private final String appealTribunalDecisionDeadlineDate =  "15 Oct 2025";
    private final String appealDecision =  "some appealDecision";
    private final String updatedAppealDecision =  "some updatedAppealDecision";
    private final String appealDecisionBefore =  "some appealDecisionBefore";
    private final String updatedAppealDecisionBefore =  "some updatedAppealDecisionBefore";

    private InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetterTemplate(
            templateName,
            daysAfterSubmitAppeal,
            customerServicesProvider,
            systemDateProvider
        );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, template.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(appealTribunalDecisionDeadlineDate, templateFieldValues.get("appealTribunalDecisionDeadlineDate"));
        assertEquals(appealDecisionBefore, templateFieldValues.get("oldDecision"));
        assertEquals(updatedAppealDecision, templateFieldValues.get("newDecision"));
    }


    @Test
    void should_map_case_data_to_template_field_values_updatedAppealDecisionBefore_populated() {
        dataSetUp();
        when(asylumCaseBefore.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(updatedAppealDecisionBefore));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(appealTribunalDecisionDeadlineDate, templateFieldValues.get("appealTribunalDecisionDeadlineDate"));
        assertEquals(updatedAppealDecisionBefore, templateFieldValues.get("oldDecision"));
        assertEquals(updatedAppealDecision, templateFieldValues.get("newDecision"));
    }

    @Test
    void should_handle_missing_optional_fields() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_DATE, String.class)).thenReturn(Optional.of(updateTribunalDecisionDate));
        when(asylumCase.read(APPEAL_DECISION, String.class)).thenReturn(Optional.of(appealDecision));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(updatedAppealDecision));
        when(asylumCaseBefore.read(UPDATE_TRIBUNAL_DECISION_DATE, String.class)).thenReturn(Optional.of(updateTribunalDecisionDate));
        when(asylumCaseBefore.read(APPEAL_DECISION, String.class)).thenReturn(Optional.of(appealDecisionBefore));
        //when(asylumCaseBefore.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(updatedAppealDecisionBefore));

        // Mock empty values for optional fields
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(appealTribunalDecisionDeadlineDate, templateFieldValues.get("appealTribunalDecisionDeadlineDate"));
        assertEquals(appealDecisionBefore, templateFieldValues.get("oldDecision"));
        assertEquals(updatedAppealDecision, templateFieldValues.get("newDecision"));
    }

    @Test
    void should_handle_null_values_from_providers() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(null);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(null);
        when(systemDateProvider.dueDate(daysAfterSubmitAppeal)).thenReturn(null);

        dataSetUpPersonalisation();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(null, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(appealDecisionBefore, templateFieldValues.get("oldDecision"));
        assertEquals(updatedAppealDecision, templateFieldValues.get("newDecision"));
    }

    private void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(systemDateProvider.dueDate(daysAfterSubmitAppeal)).thenReturn(formatDateForNotificationAttachmentDocument(dueDate));

        dataSetUpPersonalisation();
    }

    private void dataSetUpPersonalisation() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(UPDATE_TRIBUNAL_DECISION_DATE, String.class)).thenReturn(Optional.of(updateTribunalDecisionDate));
        when(asylumCase.read(APPEAL_DECISION, String.class)).thenReturn(Optional.of(appealDecision));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(updatedAppealDecision));

        when(asylumCaseBefore.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCaseBefore.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCaseBefore.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCaseBefore.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCaseBefore.read(UPDATE_TRIBUNAL_DECISION_DATE, String.class)).thenReturn(Optional.of(updateTribunalDecisionDate));
        when(asylumCaseBefore.read(APPEAL_DECISION, String.class)).thenReturn(Optional.of(appealDecisionBefore));
    }
}
