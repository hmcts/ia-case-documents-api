package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative.LegalRepresentativeRemissionDecisionPaPartiallyApprovedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeRemissionDecisionPaPartiallyApprovedPersonalisationTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private FeatureToggler featureToggler;

    private String appealReferenceNumber = "someReferenceNumber";
    private String legalRepRefNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String templateId = "someTemplateId";
    private String legalRepEmailAddress = "legalRepEmailAddress@example.com";
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String someTestDateEmail = "14/14/2024";
    private String onlineCaseReferenceNumber = "1111222233334444";

    private LegalRepresentativeRemissionDecisionPaPartiallyApprovedPersonalisation
        legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation;

    @BeforeEach
    void setUp() {

        legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation =
            new LegalRepresentativeRemissionDecisionPaPartiallyApprovedPersonalisation(
                templateId, iaExUiFrontendUrl, customerServicesProvider, featureToggler);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId,
            legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation.getTemplateId(asylumCase));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void should_return_given_email_address_from_lookup_map_when_feature_flag_is_on_or_off(boolean value) {
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(value);
        if (value) {
            when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
                    .thenReturn(Optional.of(legalRepEmailAddress));
            assertTrue(
                    legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation
                            .getRecipientsList(asylumCase).contains("legalRepEmailAddress@example.com"));
        } else {
            assertTrue(legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation
                    .getRecipientsList(asylumCase).isEmpty());
        }
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when(asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class)).thenReturn(Optional.of(someTestDateEmail));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        Map<String, String> personalisation =
            legalRepresentativeRemissionDecisionPartiallyApprovedPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(amountLeftToPayInGbp, personalisation.get("feeAmount"));
        assertEquals(someTestDateEmail, personalisation.get("14 days after remission decision"));
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
