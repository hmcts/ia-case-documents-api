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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalReinstateAppealLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    private InternalReinstateAppealLetterTemplate internalReinstateAppealLetterTemplate;
    private final String templateName = "TB-IAC-LET-ENG-00050.docx";
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
    private final String adaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
    private final String nonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";

    @BeforeEach
    void setUp() {
        internalReinstateAppealLetterTemplate =
            new InternalReinstateAppealLetterTemplate(
                templateName,
                customerServicesProvider
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, internalReinstateAppealLetterTemplate.getName());
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

        Map<String, Object> templateFieldValues = internalReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(12, templateFieldValues.size());
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(reinstatedDecisionMaker, templateFieldValues.get("reinstatedDecisionMaker"));
        assertEquals(formattedReinstateAppealDate, templateFieldValues.get("reinstateAppealDate"));
        assertEquals(reinstateAppealReason, templateFieldValues.get("reinstateAppealReason"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void should_return_correct_form_for_ada_and_non_ada_cases(YesOrNo yesOrNo) {
        dataSetUp();
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));
        Map<String, Object> templateFieldValues = internalReinstateAppealLetterTemplate.mapFieldValues(caseDetails);

        if (yesOrNo.equals(YesOrNo.YES)) {
            assertEquals(adaFormName, templateFieldValues.get("formName"));
        } else {
            assertEquals(nonAdaFormName, templateFieldValues.get("formName"));
        }
    }
}
