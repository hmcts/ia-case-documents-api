package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeMarkAsReadyForUtTransferPersonalisationTest {
    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails caseDetails;
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String emailAddress = "legalRep@example.com";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private final String utAppealReferenceNumber = "1234567890";
    private String homeOfficeRefNumber = "someRepRefNumber";
    private final String homeOfficeApcEmailAddress = "homeOfficeAPC@example.com";
    private final String homeOfficeLartEmailAddress = "homeOfficeLART@example.com";
    private final String endAppealEmailAddresses = "HO-end-appeal@example.com";


    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    private HomeOfficeMarkAppealReadyForUtTransferPersonalisation homeOfficeMarkAppealReadyForUtTransferPersonalisation;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(utAppealReferenceNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        homeOfficeMarkAppealReadyForUtTransferPersonalisation = new HomeOfficeMarkAppealReadyForUtTransferPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            homeOfficeApcEmailAddress,
            homeOfficeLartEmailAddress,
            endAppealEmailAddresses,
            iaExUiFrontendUrl,
            emailAddressFinder,
            personalisationProvider,
            customerServicesProvider);
        initializePrefixes(homeOfficeMarkAppealReadyForUtTransferPersonalisation);
        when(personalisationProvider.getRespondentHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationForHomeOffice());

    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        assertEquals(afterListingTemplateId,
            homeOfficeMarkAppealReadyForUtTransferPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.empty());
        assertEquals(beforeListingTemplateId,
            homeOfficeMarkAppealReadyForUtTransferPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_HOME_OFFICE",
            homeOfficeMarkAppealReadyForUtTransferPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_the_ho_apc_email_address_until_case_under_review() {
        when(asylumCase.read(AsylumCaseDefinition.STATE_BEFORE_END_APPEAL, State.class))
                .thenReturn(Optional.of(State.CASE_UNDER_REVIEW));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), homeOfficeMarkAppealReadyForUtTransferPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_lart_email_address_until_listing() {
        when(asylumCase.read(AsylumCaseDefinition.STATE_BEFORE_END_APPEAL, State.class))
                .thenReturn(Optional.of(State.LISTING));

        assertEquals(Collections.singleton(homeOfficeLartEmailAddress), homeOfficeMarkAppealReadyForUtTransferPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_hearing_centre_email_address_after_listing() {
        String homeOfficeBhamEmailAddress = "ho-birmingham@example.com";
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeBhamEmailAddress);
        when(asylumCase.read(AsylumCaseDefinition.STATE_BEFORE_END_APPEAL, State.class))
                .thenReturn(Optional.of(State.PRE_HEARING));

        assertEquals(Collections.singleton(homeOfficeBhamEmailAddress), homeOfficeMarkAppealReadyForUtTransferPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_end_appeal_email_address_before_listing_aip() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(JourneyType.AIP));

        assertEquals(Collections.singleton(endAppealEmailAddresses), homeOfficeMarkAppealReadyForUtTransferPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_hearing_centre_email_address_after_listing_aip() {
        String homeOfficeBhamEmailAddress = "ho-birmingham@example.com";

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.BIRMINGHAM));
        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeBhamEmailAddress);

        assertEquals(Collections.singleton(homeOfficeBhamEmailAddress), homeOfficeMarkAppealReadyForUtTransferPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> homeOfficeMarkAppealReadyForUtTransferPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeMarkAppealReadyForUtTransferPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(utAppealReferenceNumber, personalisation.get("utAppealReferenceNumber"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {
        initializePrefixes(homeOfficeMarkAppealReadyForUtTransferPersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            homeOfficeMarkAppealReadyForUtTransferPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    public static void initializePrefixes(Object testClass) {
        ReflectionTestUtils.setField(testClass, "adaPrefix", "Accelerated detained appeal");
        ReflectionTestUtils.setField(testClass, "nonAdaPrefix", "Immigration and Asylum appeal");
    }

    private Map<String, String> getPersonalisationForHomeOffice() {
        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("ariaListingReference", ariaListingReference)
                .put("homeOfficeReference", homeOfficeRefNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("customerServicesTelephone", customerServicesTelephone)
                .put("customerServicesEmail", customerServicesEmail)
                .build();
    }

}
