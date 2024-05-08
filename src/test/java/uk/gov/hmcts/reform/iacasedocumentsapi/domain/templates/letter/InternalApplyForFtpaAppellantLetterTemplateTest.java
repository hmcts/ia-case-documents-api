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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalApplyForFtpaAppellantLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String templateName = "TB-IAC-DEC-ENG-00017.docx";
    private final String customerServicesTelephoneNumber = "0300 123 1711";
    private final String customerServicesEmailAddress = "IAC-ADA-HW@justice.gov.uk";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private InternalApplyForFtpaAppellantLetterTemplate internalApplyForFtpaAppellantLetterTemplate;

    @BeforeEach
    void setUp() {
        internalApplyForFtpaAppellantLetterTemplate =
                new InternalApplyForFtpaAppellantLetterTemplate(
                        templateName,
                        dateProvider,
                        customerServicesProvider
                );
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(customerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(customerServicesEmailAddress);

        when(dateProvider.now()).thenReturn(LocalDate.now());
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalApplyForFtpaAppellantLetterTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetup();

        Map<String, Object> templateFieldValues = internalApplyForFtpaAppellantLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(8, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(dateProvider.now()), templateFieldValues.get("dateLetterSent"));
    }
}
