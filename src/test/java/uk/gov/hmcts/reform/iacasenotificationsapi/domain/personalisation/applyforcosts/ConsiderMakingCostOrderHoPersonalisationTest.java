package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsiderMakingCostOrderHoPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;
    private Long caseId = 12345L;
    private String templateId = "testTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private String homeOfficeReferenceNumber = "A1234567/001";

    private ConsiderMakingCostOrderHoPersonalisation considerMakingCostOrderHoPersonalisation;

    @BeforeEach
    void setup() {
        considerMakingCostOrderHoPersonalisation = new ConsiderMakingCostOrderHoPersonalisation(
            templateId,
            homeOfficeEmailAddress,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> applyForCostsApplicantPersonalisationTemplate = new HashMap<>();

        applyForCostsApplicantPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        applyForCostsApplicantPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        applyForCostsApplicantPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);
        applyForCostsApplicantPersonalisationTemplate.put("linkToOnlineService", iaExUiFrontendUrl);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(applyForCostsApplicantPersonalisationTemplate);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, considerMakingCostOrderHoPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CONSIDER_MAKING_A_COST_ORDER_HOME_OFFICE_EMAIL",
            considerMakingCostOrderHoPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address() {
        assertTrue(considerMakingCostOrderHoPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> considerMakingCostOrderHoPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = considerMakingCostOrderHoPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
    }
}