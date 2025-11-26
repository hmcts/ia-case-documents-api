package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AppellantListCasePersonalisationSmsTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    StringProvider stringProvider;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;
    @Mock
    RecipientsFinder recipientsFinder;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String legallyReppedTemplateId = "legallyReppedTemplateId";
    private String iaAipFrontendUrl = "http://somefrontendurl";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreAddress = "some hearing centre address";
    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    private String mockedAppealReferenceNumber = "someReferenceNumber";
    private String mockedAppellantMobilePhone = "07123456789";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private AppellantListCasePersonalisationSms appellantListCasePersonalisationSms;

    @BeforeEach
    void setup() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentre.toString());
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantListCasePersonalisationSms = new AppellantListCasePersonalisationSms(
            templateId,
            legallyReppedTemplateId,
            iaAipFrontendUrl,
            dateTimeExtractor,
            hearingDetailsFinder,
            recipientsFinder
        );
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_LISTED_AIP_APPELLANT_SMS",
            appellantListCasePersonalisationSms.getReferenceId(caseId));
    }

    @Test
    public void should_return_correct_template_id() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertEquals(templateId, appellantListCasePersonalisationSms.getTemplateId(asylumCase));

        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertEquals(legallyReppedTemplateId, appellantListCasePersonalisationSms.getTemplateId(asylumCase));
    }


    @Test
    public void should_throw_exception_on_recipients_when_case_is_null() {
        assertThatThrownBy(() -> appellantListCasePersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantListCasePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(1)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Test
    public void should_return_given_mobile_mobile_list_from_sms_in_asylum_case_if_repped() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantListCasePersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(0)).findAll(asylumCase, NotificationType.SMS);
        verify(recipientsFinder, times(0)).findReppedAppellant(asylumCase, NotificationType.EMAIL);
        verify(recipientsFinder, times(1)).findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = appellantListCasePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(hearingCentre.getValue(), personalisation.get("tribunalCentre"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_return_personalisation_when_co_records_hearing_response() {

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation = appellantListCasePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals(mockedAppealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(hearingCentre.getValue(), personalisation.get("tribunalCentre"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = appellantListCasePersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(hearingCentre.getValue(), personalisation.get("tribunalCentre"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }


    @Test
    void should_throw_personalisation_when_no_hearing_centre() {
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        assertThatThrownBy(
            () -> appellantListCasePersonalisationSms.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No hearing centre present");
    }
}
