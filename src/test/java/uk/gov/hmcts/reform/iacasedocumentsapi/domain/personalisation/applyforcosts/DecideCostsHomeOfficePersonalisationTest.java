package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecideCostsHomeOfficePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private final String decideCostsNotificationId = "decideCostsNotificationId";
    private String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private static String homeOffice = "Home office";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String homeOfficeReferenceNumber = "A1234567/001";

    private DecideCostsHomeOfficePersonalisation decideCostsHomeOfficePersonalisation;

    @BeforeEach
    void setup() {
        decideCostsHomeOfficePersonalisation = new DecideCostsHomeOfficePersonalisation(
            decideCostsNotificationId,
            homeOfficeEmailAddress,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> appellantCredentialsMap = new HashMap<>();
        appellantCredentialsMap.put("appellantGivenNames", appellantGivenNames);
        appellantCredentialsMap.put("appellantFamilyName", appellantFamilyName);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getAppellantCredentials(asylumCase)).thenReturn(appellantCredentialsMap);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(decideCostsNotificationId, decideCostsHomeOfficePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_A_COSTS_EMAIL_TO_HO",
            decideCostsHomeOfficePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address() {
        Set<String> recipientsSet = decideCostsHomeOfficePersonalisation.getRecipientsList(asylumCase);
        assertTrue(recipientsSet.contains(homeOfficeEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> decideCostsHomeOfficePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    public void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(personalisationProvider.getTypeForSelectedApplyForCosts(any(), any())).thenReturn(Map.of("appliedCostsType", applyForCostsList.get(0).getValue().getAppliedCostsType().replaceAll("costs", "").trim()));
        when(personalisationProvider.retrieveSelectedApplicationId(any(), any())).thenReturn(Map.of("applicationId", applyForCostsList.get(0).getId()));
        Map<String, String> decideCostsResult = new HashMap<>();
        decideCostsResult.put("costsDecisionType", "someCostsDecisionType");
        when(personalisationProvider.getDecideCostsPersonalisation(asylumCase)).thenReturn(decideCostsResult);

        Map<String, String> personalisation = decideCostsHomeOfficePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(applyForCostsList.get(0).getId(), personalisation.get("applicationId"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("someCostsDecisionType", personalisation.get("costsDecisionType"));

    }

    static Stream<Arguments> appliesForCostsProvider() {
        return Stream.of(
            Arguments.of(List.of(new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", homeOffice))),
                new DynamicList(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023"), List.of(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023")))),
            Arguments.of(List.of(new IdValue<>("2", new ApplyForCosts("Wasted costs", homeOffice, "Legal representative"))),
                new DynamicList(new Value("2", "Costs 1, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 1, Wasted costs, 24 Nov 2023"))))
        );
    }
}
