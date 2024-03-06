package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.UPDATED_APPEAL_DECISION;


@ExtendWith(MockitoExtension.class)
class UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelperTest {

    @Mock
    private CustomerServicesProvider customerServicesProvider;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private final String someAppealReferenceNumber = "appealReferenceNumber";
    private final String someHomeOfficeReferenceNumber = "homeOfficeRef";
    private final String someGivenNames = "some-given-name";
    private final String someFamilyName = "some-family-name";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";


    private UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper templateHelper;

    @BeforeEach
    public void setUp() {

        templateHelper = new UpdatedTribunalDecisionAndReasonsCoverLetterTemplateHelper(
                customerServicesProvider
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"Allowed", "Dismissed"})
    public void should_map_case_data_to_template_field_values(String updatedAppealDecision) {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(someGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(someFamilyName));
        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class)).thenReturn(Optional.of(updatedAppealDecision));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateValues = templateHelper.getCommonMapFieldValues(caseDetails);

        assertEquals(7, templateValues.size());
        assertEquals(templateValues.get("appealReferenceNumber"), someAppealReferenceNumber);
        assertEquals(templateValues.get("homeOfficeReferenceNumber"), someHomeOfficeReferenceNumber);
        assertEquals(templateValues.get("appellantGivenNames"), someGivenNames);
        assertEquals(templateValues.get("appellantFamilyName"), someFamilyName);
        if (updatedAppealDecision.equals("Allowed")) {
            assertEquals(templateValues.get("allowed"), "Yes");
        } else {
            assertEquals(templateValues.get("allowed"), "No");
        }
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void throws_if_appeal_decision_not_present() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someAppealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(someHomeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(someGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(someFamilyName));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(UPDATED_APPEAL_DECISION, String.class))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> templateHelper.getCommonMapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("update appeal decision must be present");
    }
}
