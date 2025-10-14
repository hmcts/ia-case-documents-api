package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeRequestCaseEditPersonalisationTest {

    private static final String DIRECTION_DUE_DATE = "2020-05-03";
    private static final String TEMPLATE_ID = "someTemplateId";
    private static final String IA_EX_UI_FRONTEND_URL = "http://localhost";
    private static final String LEGAL_REP_EMAIL_ADDRESS = "legalrep@example.com";
    private static final String DIRECTION_EXPLANATION = "someDirectionExplanation";
    private static final String APPEAL_REFERENCE_NUMBER = "someAppealReferenceNumber";
    private static final String APPELLANT_GIVEN_NAMES = "someAppellantGivenNames";
    private static final String APPELLANT_FAMILY_NAMES = "someAppellantFamilyNames";
    private static final String SOME_LEGAL_REP_REF_NUMBER = "someLegalRepRefNumber";
    private static final String CUSTOMER_SERVICES_PROVIDER_PHONE = "555 555 555";
    private static final String CUSTOMER_SERVICES_PROVIDER_EMAIL = "customer.services@example.com";

    @Mock
    AsylumCase asylumCase;
    @Mock
    Direction direction;
    @Mock
    private DirectionFinder directionFinder;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private LegalRepresentativeRequestCaseEditPersonalisation personalisation;

    private static List<Scenario> generateScenarios() {
        return Scenario.builder();
    }

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(getExpectedValue(LEGAL_REP_EMAIL_ADDRESS));

        personalisation = new LegalRepresentativeRequestCaseEditPersonalisation(
            TEMPLATE_ID,
            IA_EX_UI_FRONTEND_URL,
            directionFinder,
            customerServicesProvider);
    }

    @Test
    public void getTemplateId() {
        assertEquals(TEMPLATE_ID, personalisation.getTemplateId());
    }

    @Test
    public void getRecipientsList() {
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(LEGAL_REP_EMAIL_ADDRESS));
    }

    @Test
    public void getReferenceId() {
        assertEquals("1234_LEGAL_REPRESENTATIVE_REQUEST_CASE_EDIT",
            personalisation.getReferenceId(1234L));
    }

    @ParameterizedTest
    @MethodSource("generateScenarios")
    public void getPersonalisation(Scenario scenario) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        initializePrefixes(personalisation);

        when((direction.getDateDue())).thenReturn(DIRECTION_DUE_DATE);
        when((direction.getExplanation())).thenReturn(DIRECTION_EXPLANATION);
        when(directionFinder.findFirst(asylumCase, DirectionTag.CASE_EDIT))
            .thenReturn(Optional.of(direction));

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(getExpectedValue(scenario.appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(getExpectedValue(scenario.appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(getExpectedValue(scenario.appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class))
            .thenReturn(getExpectedValue(scenario.legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(getExpectedValue(LEGAL_REP_EMAIL_ADDRESS));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(CUSTOMER_SERVICES_PROVIDER_PHONE);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(CUSTOMER_SERVICES_PROVIDER_EMAIL);


        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);

        ImmutableMap<String, String> expectedPersonalisation = ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", scenario.appealReferenceNumber)
            .put("appellantGivenNames", scenario.appellantGivenNames)
            .put("appellantFamilyName", scenario.appellantFamilyName)
            .put("directionExplanation", DIRECTION_EXPLANATION)
            .put("expectedDirectionDueDate", DIRECTION_DUE_DATE)
            .put("iaExUiFrontendUrl", IA_EX_UI_FRONTEND_URL)
            .put("legalRepRefNumber", scenario.legalRepRefNumber)
            .put("subjectPrefix", "Immigration and Asylum appeal")
            .build();

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
        assertEquals(CUSTOMER_SERVICES_PROVIDER_PHONE, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(CUSTOMER_SERVICES_PROVIDER_EMAIL, customerServicesProvider.getCustomerServicesEmail());
    }

    @NotNull
    private Optional<String> getExpectedValue(String expectedValue) {
        if (expectedValue.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(expectedValue);
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.CASE_EDIT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legal representative request case edit direction is not present");
    }

    @Value
    private static class Scenario {
        String appealReferenceNumber;
        String appellantGivenNames;
        String appellantFamilyName;
        String legalRepRefNumber;

        private static List<Scenario> builder() {
            List<Scenario> scenarios = new ArrayList<>();

            Scenario someValuesScenario = new Scenario(
                APPEAL_REFERENCE_NUMBER,
                APPELLANT_GIVEN_NAMES,
                APPELLANT_FAMILY_NAMES,
                SOME_LEGAL_REP_REF_NUMBER);

            Scenario emptyValuesScenario = new Scenario(
                "",
                "",
                "",
                "");

            scenarios.add(someValuesScenario);
            scenarios.add(emptyValuesScenario);

            return scenarios;
        }
    }

}
