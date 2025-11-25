package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AipAppellantRecordRemissionDecisionPaPersonalisationEmailTest {

    private Long caseId = 12345L;
    private String aipAppellantRemissionApprovedTemplateId = "aipAppellantRemissionApprovedTemplateId";
    private String aipAppellantRemissionPartiallyApprovedTemplateId = "aipAppellantRemissionPartiallyApprovedTemplateId";
    private String aipAppellantRemissionRejectedTemplateId = "aipAppellantRemissionRejectedTemplateId";
    private String iaAipFrontendUrl = "http://localhost";
    private String appellantEmail = "example@example.com";
    private String appealReferenceNumber = "appealReferenceNumber";
    private String onlineCaseReferenceNumber = "1111222233334444";
    private String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private String appellantGivenNames = "GivenNames";
    private String appellantFamilyName = "FamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer@example.com";
    private int daysAfterRemissionDecision = 14;
    private String amountLeftToPay = "4000";
    private String amountLeftToPayInGbp = "40.00";

    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    FeatureToggler featureToggler;

    private AipAppellantRecordRemissionDecisionPaPersonalisationEmail aipAppellantRecordRemissionDecisionPersonalisationEmail;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(onlineCaseReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        aipAppellantRecordRemissionDecisionPersonalisationEmail = new AipAppellantRecordRemissionDecisionPaPersonalisationEmail(
            aipAppellantRemissionApprovedTemplateId,
            aipAppellantRemissionPartiallyApprovedTemplateId,
            aipAppellantRemissionRejectedTemplateId,
            iaAipFrontendUrl,
            daysAfterRemissionDecision,
            customerServicesProvider,
            recipientsFinder,
            systemDateProvider,
            featureToggler
        );
    }

    @ParameterizedTest
    @EnumSource(
        value = RemissionDecision.class,
        names = {"APPROVED", "PARTIALLY_APPROVED", "REJECTED"})
    void should_return_approved_template_id(RemissionDecision remissionDecision) {
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(remissionDecision));
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(true);

        switch (remissionDecision) {
            case APPROVED ->
                assertEquals(aipAppellantRemissionApprovedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationEmail.getTemplateId(asylumCase));
            case PARTIALLY_APPROVED ->
                assertEquals(aipAppellantRemissionPartiallyApprovedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationEmail.getTemplateId(asylumCase));
            case REJECTED ->
                assertEquals(aipAppellantRemissionRejectedTemplateId, aipAppellantRecordRemissionDecisionPersonalisationEmail.getTemplateId(asylumCase));
            default -> throw new IllegalArgumentException("Unexpected remission decision: " + remissionDecision);
        }
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_REMISSION_DECISION_DECIDED_AIP_APPELLANT_EMAIL",
            aipAppellantRecordRemissionDecisionPersonalisationEmail.getReferenceId(12345L));
    }

    @Test
    void should_return_appellant_email_address_from_asylum_case() {
        when(featureToggler.getValue("dlrm-telephony-feature-flag", false)).thenReturn(true);
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
            .thenReturn(Collections.singleton(appellantEmail));

        assertTrue(aipAppellantRecordRemissionDecisionPersonalisationEmail.getRecipientsList(asylumCase)
            .contains(appellantEmail));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> aipAppellantRecordRemissionDecisionPersonalisationEmail.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));

        final String dueDate = LocalDate.now().plusDays(daysAfterRemissionDecision)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, String> personalisation =
            aipAppellantRecordRemissionDecisionPersonalisationEmail.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(onlineCaseReferenceNumber, personalisation.get("onlineCaseReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaAipFrontendUrl, personalisation.get("linkToService"));
        assertEquals(systemDateProvider.dueDate(daysAfterRemissionDecision), personalisation.get("payByDeadline"));
        assertEquals(amountLeftToPayInGbp, personalisation.get("remainingFee"));

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
