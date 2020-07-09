package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@RunWith(MockitoJUnitRunner.class)
public class CaseOfficerListCmaPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock StringProvider stringProvider;
    @Mock DateTimeExtractor dateTimeExtractor;
    @Mock Map<HearingCentre, String> homeOfficeEmailAddressMap;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String homeOfficeEmailAddress = "homeoffice@example.com";
    private String hearingCentreAddress = "some hearing centre address";

    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerListCmaPersonalisation caseOfficerListCmaPersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(homeOfficeEmailAddressMap.get(hearingCentre)).thenReturn(homeOfficeEmailAddress);
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        caseOfficerListCmaPersonalisation = new CaseOfficerListCmaPersonalisation(
            templateId,
            iaExUiFrontendUrl,
            stringProvider,
            dateTimeExtractor,
            homeOfficeEmailAddressMap
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerListCmaPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_LIST_CMA_CASE_OFFICER_EMAIL", caseOfficerListCmaPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertTrue(caseOfficerListCmaPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddress));
    }

    @Test
    public void should_throw_exception_on_email_address_when_home_office_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerListCmaPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_home_office() {
        when(homeOfficeEmailAddressMap.get(hearingCentre)).thenReturn(null);

        assertThatThrownBy(() -> caseOfficerListCmaPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Hearing centre email address not found: " + hearingCentre.toString());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerListCmaPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase cannot be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = caseOfficerListCmaPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_centre_is_empty() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerListCmaPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_centre_address_does_not_exist() {

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerListCmaPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_date_time_does_not_exist() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerListCmaPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingDateTime is not present");
    }
}
