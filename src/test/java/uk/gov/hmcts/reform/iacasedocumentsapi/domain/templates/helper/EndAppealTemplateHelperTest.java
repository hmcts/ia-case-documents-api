package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
class EndAppealTemplateHelperTest {

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private final String appealReferenceNumber = "RP/11111/2020";
    private final String appellantGivenNames = "Talha";
    private final String appellantFamilyName = "Awan";
    private final String homeOfficeReferenceNumber = "A1234567/001";

    private final String endAppealOutcome = "Withdrawn";
    private final String endAppealOutcomeReason = "some reason";
    private final String endAppealDate = "2020-12-25";
    private final String endAppealApproverName = "John Doe";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";


    private EndAppealTemplateHelper endAppealTemplateHelper;

    @BeforeEach
    public void setUp() {

        endAppealTemplateHelper = new EndAppealTemplateHelper(
                customerServicesProvider
        );
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(endAppealOutcome));
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.of(endAppealOutcomeReason));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
        when(asylumCase.read(END_APPEAL_APPROVER_NAME, String.class)).thenReturn(Optional.of(endAppealApproverName));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = endAppealTemplateHelper.getCommonMapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(endAppealOutcome, templateFieldValues.get("outcomeOfAppeal"));
        assertEquals(endAppealOutcomeReason, templateFieldValues.get("reasonsOfOutcome"));
        String expectedEndAppealDate = "25122020";
        assertEquals(expectedEndAppealDate, templateFieldValues.get("endAppealDate"));
        assertEquals(endAppealApproverName, templateFieldValues.get("endAppealApprover"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(endAppealOutcome));
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.of(endAppealOutcomeReason));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
        when(asylumCase.read(END_APPEAL_APPROVER_NAME, String.class)).thenReturn(Optional.of(endAppealApproverName));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_APPROVER_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = endAppealTemplateHelper.getCommonMapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("outcomeOfAppeal"));
        assertEquals("", templateFieldValues.get("reasonsOfOutcome"));
        assertEquals("", templateFieldValues.get("endAppealDate"));
        assertEquals("", templateFieldValues.get("endAppealApprover"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

}
