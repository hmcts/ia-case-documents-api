package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplicationTypes.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter.AppellantInternalRespondentApplicationDecidedLetterPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantInternalRespondentApplicationDecidedLetterPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;
    @Mock
    SystemDateProvider systemDateProvider;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    MakeAnApplication makeAnApplication;

    private Long ccdCaseId = 12345L;
    private String letterTemplateId = "someLetterTemplateId";
    private String appealReferenceNumber = "someAppealRefNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "example@example.com";
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private String applicationGranted = "Granted";
    private String applicationRefused = "Refused";
    private String applicationReason = "Application decision reason example";
    private int daysAfterApplicationDecisionInCountry = 14;
    private int daysAfterApplicationDecisionOoc = 28;

    private static final String HOME_OFFICE_TIME_EXTENTION_CONTENT = "The tribunal will give the Home Office more time to complete its next task. You will get a notification with the new date soon.";
    private static final String HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT = "The details of the hearing will be updated and you will be sent a new Notice of Hearing with the agreed changes.";
    private static final String HOME_OFFICE_JUDGES_REVIEW_CONTENT = "The decision on the Home Office’s original request will be overturned. You will be notified if there is something you need to do next.";
    private static final String HOME_OFFICE_LINK_OR_UNLINK_CONTENT = "This appeal will be linked to or unlinked from the appeal in the Home Office application. You will be notified when this happens.";
    private static final String HOME_OFFICE_REINSTATE_APPEAL_CONTENT = "This appeal will be reinstated and will continue from the point where it was ended. You will be notified when this happens.";
    private static final String APPLICATION_TYPE_OTHER_CONTENT = "You will be notified when the tribunal makes the changes the Home Office asked for.";
    private static final String HOME_OFFICE_REFUSED_CONTENT = "The appeal will continue without any changes.";

    private AppellantInternalRespondentApplicationDecidedLetterPersonalisation appellantInternalRespondentApplicationDecidedLetterPersonalisation;

    @BeforeEach
    public void setup() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(ccdCaseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_1_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_2_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.ADDRESS_LINE_3_ADMIN_J, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.ofNullable(makeAnApplication));
        when(makeAnApplication.getDecisionReason()).thenReturn(applicationReason);
        final String dueDate = LocalDate.now().plusDays(daysAfterApplicationDecisionInCountry)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry)).thenReturn(dueDate);

        appellantInternalRespondentApplicationDecidedLetterPersonalisation = new AppellantInternalRespondentApplicationDecidedLetterPersonalisation(
            letterTemplateId,
            daysAfterApplicationDecisionInCountry,
            daysAfterApplicationDecisionOoc,
            customerServicesProvider,
            systemDateProvider,
            makeAnApplicationService
        );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(letterTemplateId, appellantInternalRespondentApplicationDecidedLetterPersonalisation.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(ccdCaseId + "_INTERNAL_RESPONDENT_APPLICATION_DECIDED_APPELLANT_LETTER",
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getReferenceId(ccdCaseId));
    }

    @Test
    void should_return_appellant_address_in_correct_format() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Spain"));
    }

    @Test
    void should_return_legalRep_address_in_correct_format() {
        legalRepInCountryDataSetup();
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("50_Buildingname_Streetname_Townname_XX12YY"));

        legalRepOutOfCountryDataSetup();
        assertTrue(appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase).contains("CalleToledo32_Madrid_28003_Townname_Spain"));
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_appellant_in_country() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appellantAddress is not present");
    }

    @Test
    void should_throw_exception_when_cannot_find_address_for_legalRep_in_country() {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepAddressUK is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @ParameterizedTest
    @EnumSource(
            value = MakeAnApplicationTypes.class,
            names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
                     "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_appellant_in_country_granted_application(MakeAnApplicationTypes applicationType) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                    systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
            value = MakeAnApplicationTypes.class,
            names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
                     "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_appellant_out_of_country_granted_application(MakeAnApplicationTypes applicationType) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        final String dueDate = LocalDate.now().plusDays(daysAfterApplicationDecisionOoc)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionOoc)).thenReturn(dueDate);
        Map<String, String> personalisation =
                appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(Nationality.ES.toString(), personalisation.get("address_line_4"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                    systemDateProvider.dueDate(daysAfterApplicationDecisionOoc) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
            value = MakeAnApplicationTypes.class,
            names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
                     "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_appellant_refused_application(MakeAnApplicationTypes applicationType) {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationRefused);
        Map<String, String> personalisation =
                appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(HOME_OFFICE_REFUSED_CONTENT, personalisation.get("nextStep"));

    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_legalRep_in_country_granted_application(MakeAnApplicationTypes applicationType) {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(addressLine1, personalisation.get("address_line_1"));
        assertEquals(addressLine2, personalisation.get("address_line_2"));
        assertEquals(addressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(postCode, personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                         systemDateProvider.dueDate(daysAfterApplicationDecisionInCountry) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_legalRep_out_of_country_granted_application(MakeAnApplicationTypes applicationType) {
        legalRepOutOfCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);
        final String dueDate = LocalDate.now().plusDays(daysAfterApplicationDecisionOoc)
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        when(systemDateProvider.dueDate(daysAfterApplicationDecisionOoc)).thenReturn(dueDate);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(oocAddressLine1, personalisation.get("address_line_1"));
        assertEquals(oocAddressLine2, personalisation.get("address_line_2"));
        assertEquals(oocAddressLine3, personalisation.get("address_line_3"));
        assertEquals(postTown, personalisation.get("address_line_4"));
        assertEquals(Nationality.ES.toString(), personalisation.get("address_line_5"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        if (applicationType == ADJOURN || applicationType == EXPEDITE || applicationType == TRANSFER) {
            assertEquals(HOME_OFFICE_ADJOURN_EXPEDITE_TRANSFER_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == TIME_EXTENSION) {
            assertEquals(HOME_OFFICE_TIME_EXTENTION_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == JUDGE_REVIEW_LO) {
            assertEquals(HOME_OFFICE_JUDGES_REVIEW_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == LINK_OR_UNLINK) {
            assertEquals(HOME_OFFICE_LINK_OR_UNLINK_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == REINSTATE) {
            assertEquals(HOME_OFFICE_REINSTATE_APPEAL_CONTENT, personalisation.get("nextStep"));
        } else if (applicationType == WITHDRAW) {
            assertEquals("Your appeal may end. If you do not want the appeal to end, you should contact the tribunal by " +
                         systemDateProvider.dueDate(daysAfterApplicationDecisionOoc) + " to explain why.", personalisation.get("nextStep"));
        } else if (applicationType == OTHER) {
            assertEquals(APPLICATION_TYPE_OTHER_CONTENT, personalisation.get("nextStep"));
        }
    }

    @ParameterizedTest
    @EnumSource(
        value = MakeAnApplicationTypes.class,
        names = {"ADJOURN", "EXPEDITE", "TRANSFER", "TIME_EXTENSION", "JUDGE_REVIEW_LO",
            "LINK_OR_UNLINK", "REINSTATE", "WITHDRAW", "OTHER"})
    void should_return_personalisation_when_all_information_given_legalRep_refused_application(MakeAnApplicationTypes applicationType) {
        legalRepInCountryDataSetup();
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(applicationType.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationRefused);
        Map<String, String> personalisation =
            appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback);

        assertEquals(HOME_OFFICE_REFUSED_CONTENT, personalisation.get("nextStep"));

    }

    @Test
    void should_throw_exception_when_cannot_find_next_step() {
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(makeAnApplication.getType()).thenReturn(UPDATE_APPEAL_DETAILS.toString());
        when(makeAnApplication.getDecision()).thenReturn(applicationGranted);

        assertThatThrownBy(() -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid MakeAnApplicationType: Couldn't find next steps.");
    }

    @Test
    void should_throw_exception_when_cannot_find_application() {
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, true)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appellantInternalRespondentApplicationDecidedLetterPersonalisation.getPersonalisation(callback))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Application is missing.");
    }

    private void legalRepOutOfCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(oocAddressLine1));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(oocAddressLine2));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(oocAddressLine3));
        when(asylumCase.read(AsylumCaseDefinition.OOC_ADDRESS_LINE_4, String.class)).thenReturn(Optional.of(postTown));
        when(asylumCase.read(AsylumCaseDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(oocAddressCountry));
        when(oocAddressCountry.getCode()).thenReturn(Nationality.ES.name());
    }

    private void legalRepInCountryDataSetup() {
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
        when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
        when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
        when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
        when(address.getPostCode()).thenReturn(Optional.of(postCode));
        when(address.getPostTown()).thenReturn(Optional.of(postTown));
    }
}
