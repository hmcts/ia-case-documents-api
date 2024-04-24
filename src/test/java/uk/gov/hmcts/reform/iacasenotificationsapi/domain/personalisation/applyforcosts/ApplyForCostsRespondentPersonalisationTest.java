package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplyForCostsRespondentPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String applyForCostsNotificationForRespondentTemplateId = "applyForCostsNotificationForRespondentTemplateId";
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
    private static final String applyForCostsCreationDate = "2023-11-24";

    private ApplyForCostsRespondentPersonalisation applyForCostsRespondentPersonalisation;

    @BeforeEach
    void setup() {
        applyForCostsRespondentPersonalisation = new ApplyForCostsRespondentPersonalisation(
            applyForCostsNotificationForRespondentTemplateId,
            homeOfficeEmailAddress,
            emailAddressFinder,
            customerServicesProvider,
            personalisationProvider
        );

        Map<String, String> applyForCostsRespondentPersonalisationTemplate = new HashMap<>();

        applyForCostsRespondentPersonalisationTemplate.put("appellantGivenNames", appellantGivenNames);
        applyForCostsRespondentPersonalisationTemplate.put("appellantFamilyName", appellantFamilyName);
        applyForCostsRespondentPersonalisationTemplate.put("appealReferenceNumber", appealReferenceNumber);
        applyForCostsRespondentPersonalisationTemplate.put("linkToOnlineService", iaExUiFrontendUrl);
        applyForCostsRespondentPersonalisationTemplate.put("appliedCostsType", "Wasted");

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(personalisationProvider.getApplyForCostsPersonalisation(asylumCase)).thenReturn(applyForCostsRespondentPersonalisationTemplate);
        when(emailAddressFinder.getLegalRepEmailAddress(asylumCase)).thenReturn(legalRepEmailAddress);
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));

        Map<String, String> homeOfficeRecipientHeader = new HashMap<>();
        homeOfficeRecipientHeader.put("recipient", homeOffice);
        homeOfficeRecipientHeader.put("recipientReferenceNumber", homeOfficeReferenceNumber);

        Map<String, String> legalRepRecipientHeader = new HashMap<>();
        legalRepRecipientHeader.put("recipient", "Your");
        legalRepRecipientHeader.put("recipientReferenceNumber", legalRepRefNumber);

        when(personalisationProvider.getHomeOfficeRecipientHeader(asylumCase)).thenReturn(homeOfficeRecipientHeader);
        when(personalisationProvider.getLegalRepRecipientHeader(asylumCase)).thenReturn(legalRepRecipientHeader);

    }

    @Test
    void should_return_given_template_id() {
        assertEquals(applyForCostsNotificationForRespondentTemplateId, applyForCostsRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_APPLY_FOR_COSTS_RESPONDENT_EMAIL",
            applyForCostsRespondentPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    void should_return_given_email_address(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        if (applyForCostsList.get(0).getValue().getApplyForCostsApplicantType().equals(homeOffice)) {
            assertTrue(applyForCostsRespondentPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
        } else {
            assertTrue(applyForCostsRespondentPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
        }
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> applyForCostsRespondentPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @MethodSource("appliesForCostsProvider")
    void should_return_personalisation_when_all_information_given(List<IdValue<ApplyForCosts>> applyForCostsList) {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));
        Map<String, String> applyForCostsCreatedDateMap = new HashMap<>();
        applyForCostsCreatedDateMap.put("creationDate", "24 Nov 2023");
        when(personalisationProvider.getApplyToCostsCreationDate(asylumCase)).thenReturn(applyForCostsCreatedDateMap);

        Map<String, String> personalisation = applyForCostsRespondentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("Wasted", personalisation.get("appliedCostsType"));
        assertEquals("24 Nov 2023", personalisation.get("creationDate"));

        if (applyForCostsList.get(0).getValue().getApplyForCostsApplicantType().equals("Home office")) {
            assertEquals("Your", personalisation.get("recipient"));
            assertEquals(legalRepRefNumber, personalisation.get("recipientReferenceNumber"));
        } else {
            assertEquals(applyForCostsList.get(0).getValue().getApplyForCostsRespondentRole(), personalisation.get("recipient"));
            assertEquals(homeOfficeReferenceNumber, personalisation.get("recipientReferenceNumber"));
        }
    }

    static Stream<Arguments> appliesForCostsProvider() {
        return Stream.of(
            Arguments.of(List.of(new IdValue<>(newestApplicationCreatedNumber, new ApplyForCosts(unreasonableCostsType, "Legal representative", homeOffice, applyForCostsCreationDate)))),
            Arguments.of(List.of(new IdValue<>(newestApplicationCreatedNumber, new ApplyForCosts("Wasted costs", homeOffice, "Legal representative", applyForCostsCreationDate))))
        );
    }
}