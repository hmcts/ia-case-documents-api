package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedMarkAsAdaLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private DueDateService dueDateService;
    private InternalDetainedMarkAsAdaLetterTemplate internalDetainedMarkAsAdaLetterTemplate;
    private final String templateName = "TB-IAC-LET-ENG-00008.docx";
    private final String appealReferenceNumber = "HU/11111/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final LocalDate now = LocalDate.now();
    private final String appealSubmissionDate = "2023-08-01";
    private String reasonForAda = "Reason for ada";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final ZonedDateTime zonedDueDateTime = LocalDate.parse(appealSubmissionDate).atStartOfDay(ZoneOffset.UTC);
    private final int responseToTribunalDueInWorkingDays = 13;
    private final int homeOfficeResponseDueInWorkingDays = 15;

    @BeforeEach
    void setUp() {
        internalDetainedMarkAsAdaLetterTemplate =
                new InternalDetainedMarkAsAdaLetterTemplate(
                        templateName,
                        customerServicesProvider,
                        dueDateService
                );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDetainedMarkAsAdaLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of(appealSubmissionDate));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(REASON_APPEAL_MARKED_AS_ADA, String.class)).thenReturn(Optional.of(reasonForAda));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        when(dueDateService.calculateWorkingDaysDueDate(any(), eq(responseToTribunalDueInWorkingDays))).thenReturn(zonedDueDateTime.plusDays(13));
        when(dueDateService.calculateWorkingDaysDueDate(any(), eq(homeOfficeResponseDueInWorkingDays))).thenReturn(zonedDueDateTime.plusDays(15));

    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalDetainedMarkAsAdaLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(reasonForAda, templateFieldValues.get("reason"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(appealSubmissionDate).plusDays(13)), templateFieldValues.get("responseDueDate"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(appealSubmissionDate).plusDays(15)), templateFieldValues.get("hoReviewAppealDueDate"));
    }

    @Test
    void should_throw_when_appeal_submission_date_is_not_present() {
        dataSetUp();
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedMarkAsAdaLetterTemplate.mapFieldValues(caseDetails))
                .hasMessage("Appeal submission date is missing")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}
