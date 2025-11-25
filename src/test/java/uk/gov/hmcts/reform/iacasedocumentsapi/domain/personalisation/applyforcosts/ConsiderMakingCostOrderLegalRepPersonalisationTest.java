package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ConsiderMakingCostOrderLegalRepPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;
    private static final String applyForCostsCreationDate = "2023-11-24";
    private Long caseId = 12345L;
    private String templateId = "testTemplateId";
    private String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private ConsiderMakingCostOrderLegalRepPersonalisation considerMakingCostOrderLegalRepPersonalisation;

    @BeforeEach
    void setup() {
        considerMakingCostOrderLegalRepPersonalisation = new ConsiderMakingCostOrderLegalRepPersonalisation(
            templateId,
            emailAddressFinder,
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
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, considerMakingCostOrderLegalRepPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CONSIDER_MAKING_A_COST_ORDER_LEGAL_REP_EMAIL",
            considerMakingCostOrderLegalRepPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address() {
        assertTrue(considerMakingCostOrderLegalRepPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> considerMakingCostOrderLegalRepPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = considerMakingCostOrderLegalRepPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
    }

}
