package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddEvidenceForCostsSubmittedSubmitterPersonalisationTest {
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "templateId";
    private String homeOfficeEmailAddress = "homeOfficeEmailAddress@gmail.com";
    private String legalRepEmailAddress = "legalRepEmailAddress@gmail.com";
    private String iaExUiFrontendUrl = "http://localhost";
    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private static String homeOffice = "Home office";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private static String newestApplicationCreatedNumber = "1";
    private static String unreasonableCostsType = "Unreasonable costs";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String expectedLegalRepRefNumber = "\nYour reference: someLegalRepRefNumber";
    private String expectedHomeOfficeReferenceNumber = "\nHome Office reference: A1234567/001";
    private String ariaReferenceNumber = "ariaReferenceNumber";
    private String expectedAriaReferenceNumber = "\nListing reference: ariaReferenceNumber";

    private AddEvidenceForCostsSubmittedSubmitterPersonalisation addEvidenceForCostsSubmittedSubmitterPersonalisation;

    @BeforeEach
    void setup() {
        addEvidenceForCostsSubmittedSubmitterPersonalisation = new AddEvidenceForCostsSubmittedSubmitterPersonalisation(
            templateId,
            homeOfficeEmailAddress,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> additionalEvidenceSubmittedSubmitterPersonalisationTemplate = new HashMap<>();

        additionalEvidenceSubmittedSubmitterPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        additionalEvidenceSubmittedSubmitterPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        additionalEvidenceSubmittedSubmitterPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(additionalEvidenceSubmittedSubmitterPersonalisationTemplate);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaReferenceNumber));
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, addEvidenceForCostsSubmittedSubmitterPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_ADD_EVIDENCE_FOR_COSTS_SUBMITTER_EMAIL",
                addEvidenceForCostsSubmittedSubmitterPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProviderWithJudge")
    void should_return_given_email_address(List<IdValue<ApplyForCosts>> applyForCostsList, DynamicList addEvidenceToCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(addEvidenceToCostsList));

        if (("Tribunal").equals(applyForCostsList.get(0).getValue().getApplyForCostsApplicantType())) {
            assertTrue(addEvidenceForCostsSubmittedSubmitterPersonalisation.getRecipientsList(asylumCase).isEmpty());
        } else if (applyForCostsList.get(0).getValue().getLoggedUserRole().equals(homeOffice)) {
            assertTrue(addEvidenceForCostsSubmittedSubmitterPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
        } else {
            assertTrue(addEvidenceForCostsSubmittedSubmitterPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
        }
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> addEvidenceForCostsSubmittedSubmitterPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProviderWithJudge")
    void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList, DynamicList addEvidenceToCostsList) {
        when(personalisationProvider.getTypeForSelectedApplyForCosts(any(), any())).thenReturn(Map.of("appliedCostsType", applyForCostsList.get(0).getValue().getAppliedCostsType().replaceAll("costs", "").trim()));
        when(personalisationProvider.retrieveSelectedApplicationId(any(), any())).thenReturn(Map.of("applicationId", applyForCostsList.get(0).getId()));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(addEvidenceToCostsList));

        Map<String, String> personalisation = addEvidenceForCostsSubmittedSubmitterPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(applyForCostsList.get(0).getId(), personalisation.get("applicationId"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

        if (applyForCostsList.get(0).getValue().getLoggedUserRole().equals("Home office")) {
            assertEquals("Wasted", personalisation.get("appliedCostsType"));
            assertEquals(expectedHomeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
            assertEquals("", personalisation.get("legalRepReferenceNumber"));
            assertEquals("", personalisation.get("ariaListingReference"));
        } else {
            assertEquals("Unreasonable", personalisation.get("appliedCostsType"));
            assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
            assertEquals(expectedLegalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
            assertEquals(expectedAriaReferenceNumber, personalisation.get("ariaListingReference"));
        }
    }

    static Stream<Arguments> appliesForCostsProviderWithJudge() {
        return Stream.of(
                Arguments.of(List.of(new IdValue<>("1", new ApplyForCosts("Legal representative", "Legal representative", homeOffice, "Unreasonable costs", "24 Nov 2023"))),
                        new DynamicList(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023"), List.of(new Value("1", "Costs 1, Unreasonable costs, 24 Nov 2023")))),
                Arguments.of(List.of(new IdValue<>("2", new ApplyForCosts(homeOffice, homeOffice, "Legal representative", "Wasted costs", "24 Nov 2023"))),
                        new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")))),
                Arguments.of(List.of(new IdValue<>("3", new ApplyForCosts(homeOffice, "Tribunal", homeOffice, "Wasted costs", "24 Nov 2023"))),
                        new DynamicList(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023"), List.of(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023"))))
        );
    }
}
