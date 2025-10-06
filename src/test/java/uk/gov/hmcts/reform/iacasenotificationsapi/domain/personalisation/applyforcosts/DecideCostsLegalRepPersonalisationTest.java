package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecideCostsLegalRepPersonalisationTest {

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
    private String ariaRefNumber = "ariaRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private static String homeOffice = "Home office";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String expectedAriaReferenceNumber = "\nListing reference: ariaRefNumber";
    private DecideCostsLegalRepPersonalisation decideCostsLegalRepPersonalisation;

    @BeforeEach
    void setup() {
        decideCostsLegalRepPersonalisation = new DecideCostsLegalRepPersonalisation(
            decideCostsNotificationId,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> decideCostsRespondentAndApplicantPersonalisationTemplate = new HashMap<>();

        decideCostsRespondentAndApplicantPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        decideCostsRespondentAndApplicantPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        decideCostsRespondentAndApplicantPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(decideCostsRespondentAndApplicantPersonalisationTemplate);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaRefNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(decideCostsNotificationId, decideCostsLegalRepPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_A_COSTS_EMAIL_TO_LR",
            decideCostsLegalRepPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address() {
        Set<String> recipientsSet = decideCostsLegalRepPersonalisation.getRecipientsList(asylumCase);
        assertTrue(recipientsSet.contains(legalRepEmailAddress));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> decideCostsLegalRepPersonalisation.getPersonalisation((AsylumCase) null))
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

        Map<String, String> personalisation = decideCostsLegalRepPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(applyForCostsList.get(0).getId(), personalisation.get("applicationId"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("someCostsDecisionType", personalisation.get("costsDecisionType"));
        assertEquals(expectedAriaReferenceNumber, personalisation.get("ariaListingReference"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
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