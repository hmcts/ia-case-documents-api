package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalAppellantFtpaDecidedPartiallyGrantedTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String templateName = "TB-IAC-LET-ENG-00020.docx";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final int adaDueInCalendarDays = 7;
    private final int nonAdaDueInCalendarDays = 14;
    private final LocalDate now = LocalDate.now();
    private InternalAppellantFtpaDecidedPartiallyGrantedTemplate internalAppellantFtpaDecidedPartiallyGrantedTemplate;

    @BeforeEach
    void setUp() {
        internalAppellantFtpaDecidedPartiallyGrantedTemplate =
            new InternalAppellantFtpaDecidedPartiallyGrantedTemplate(
                templateName,
                dateProvider,
                customerServicesProvider,
                adaDueInCalendarDays,
                nonAdaDueInCalendarDays
            );
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
            .thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
            .thenReturn(customerServicesEmail);

        when(dateProvider.now()).thenReturn(LocalDate.now());
    }

    @Test
    void should_return_template_name() {
        Assertions.assertEquals(templateName, internalAppellantFtpaDecidedPartiallyGrantedTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetup();

        Map<String, Object> templateFieldValues = internalAppellantFtpaDecidedPartiallyGrantedTemplate.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));

        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));

    }

    @Test
    void should_return_7_calendar_days_for_ada() {
        dataSetup();
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        Map<String, Object> templateFieldValues = internalAppellantFtpaDecidedPartiallyGrantedTemplate.mapFieldValues(caseDetails);
        Assertions.assertEquals(formatDateForNotificationAttachmentDocument(now.plusDays(adaDueInCalendarDays)), templateFieldValues.get("utApplicationDeadline"));
    }

    @Test
    void should_return_14_calendar_days_for_non_ada() {
        dataSetup();
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        Map<String, Object> templateFieldValues = internalAppellantFtpaDecidedPartiallyGrantedTemplate.mapFieldValues(caseDetails);
        Assertions.assertEquals(formatDateForNotificationAttachmentDocument(now.plusDays(nonAdaDueInCalendarDays)), templateFieldValues.get("utApplicationDeadline"));
    }
}
