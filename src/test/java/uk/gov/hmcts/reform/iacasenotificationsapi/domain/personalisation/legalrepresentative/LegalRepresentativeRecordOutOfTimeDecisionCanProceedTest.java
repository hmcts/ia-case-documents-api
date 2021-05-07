package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeRecordOutOfTimeDecisionCanProceedTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private String recordOutOfDecisionCanProceedTemplateId = "recordOutOfDecisionCanProceedTemplateId";

    private Long caseId = 12345L;
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String legalRepEmailAddress = "legalrep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeRecordOutOfTimeDecisionCanProceed legalRepresentativeRecordOutOfTimeDecisionCanProceed;


    @BeforeEach
    void setUp() {

        legalRepresentativeRecordOutOfTimeDecisionCanProceed = new LegalRepresentativeRecordOutOfTimeDecisionCanProceed(
            recordOutOfDecisionCanProceedTemplateId,
            iaExUiFrontendUrl,
            customerServicesProvider
        );

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class))
            .thenReturn(Optional.of(OutOfTimeDecisionType.IN_TIME));
        Map<String, String> personalisation =
            legalRepresentativeRecordOutOfTimeDecisionCanProceed.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_throw_error_on_missing_decision_type() {

        assertThatThrownBy(() -> legalRepresentativeRecordOutOfTimeDecisionCanProceed.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Out of time decision is not present");
    }


    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_RECORD_OUT_OF_TIME_DECISION_CAN_PROCEED_LEGAL_REP",
            legalRepresentativeRecordOutOfTimeDecisionCanProceed.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeRecordOutOfTimeDecisionCanProceed.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeRecordOutOfTimeDecisionCanProceed.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeRecordOutOfTimeDecisionCanProceed.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }
}
