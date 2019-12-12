package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPLICATION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPLICATION_SUPPLIER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPLICATION_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;


@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeRecordApplicationPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock Map<HearingCentre, String> homeOfficeEmailAddressMap;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String homeOfficeEmailAddress = "homeoffice@example.com";
    private String defaultHomeOfficeEmailAddress = "defaulthomeoffice@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String applicationType = "someApplicationType";
    private String expectedApplicationType = "someapplicationtype";

    private String applicationDecisionReason = "someApplicationDecisionReason";

    private String applicationSupplier = "someApplicationSupplier";
    private String expectedApplicationSupplier = "someapplicationsupplier";

    private HomeOfficeRecordApplicationPersonalisation homeOfficeRecordApplicationPersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(applicationType));
        when(asylumCase.read(APPLICATION_DECISION_REASON, String.class)).thenReturn(Optional.of(applicationDecisionReason));
        when(asylumCase.read(APPLICATION_SUPPLIER, String.class)).thenReturn(Optional.of(applicationSupplier));

        when(homeOfficeEmailAddressMap.get(hearingCentre)).thenReturn(homeOfficeEmailAddress);

        homeOfficeRecordApplicationPersonalisation = new HomeOfficeRecordApplicationPersonalisation(
            defaultHomeOfficeEmailAddress,
            templateId,
            homeOfficeEmailAddressMap
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, homeOfficeRecordApplicationPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_RECORD_APPLICATION_HOME_OFFICE", homeOfficeRecordApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertTrue(homeOfficeRecordApplicationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_return_default_email_address_when_home_office_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertTrue(homeOfficeRecordApplicationPersonalisation.getRecipientsList(asylumCase).contains(defaultHomeOfficeEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeRecordApplicationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = homeOfficeRecordApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));

        assertEquals(expectedApplicationType, personalisation.get("applicationType"));
        assertEquals(applicationDecisionReason, personalisation.get("applicationDecisionReason"));
        assertEquals(expectedApplicationSupplier, personalisation.get("applicationSupplier"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_DECISION_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_SUPPLIER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeRecordApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("applicationType"));
        assertEquals("No reason given", personalisation.get("applicationDecisionReason"));
        assertEquals("", personalisation.get("applicationSupplier"));
    }
}
