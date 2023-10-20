package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.MANCHESTER;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class CmaAppointmentNoticeTemplateTest {

    private final String templateName = "HEARING_NOTICE_TEMPLATE.docx";
    @Mock private StringProvider stringProvider;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private HearingDetailsFinder hearingDetailsFinder;
    @Mock private DirectionFinder directionFinder;
    @Mock private Direction direction;

    private String appealReferenceNumber = "RP/11111/2020";
    private String appellantGivenNames = "Talha";
    private String appellantFamilyName = "Awan";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String legalRepReferenceNumber = "OUR-REF";
    private String hearingDate = "2020-12-25T12:34:56";
    private String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private String taylorHouseHearingCentreAddress = "London, 456 Somewhere, South";
    private String vulnerabilitiesTribunalResponse = "Vulnerabilities";
    private String multimediaTribunalResponse = "Multimedia";
    private String singleSexCourtTribunalResponse = "Single sex court";
    private String inCameraTribunalResponse = "In camera";
    private String otherHearingRequestTribunalResponse = "Other";
    private String pastExperienceTribunalResponse = "past experiences tribunal response ";

    private String expectedFormattedHearingDatePart = "25122020";
    private String expectedFormattedHearingTimePart = "1234";
    private String expectedFormattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";
    private String ariaListingReference = "AA/12345/1234";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "customer.services@example.com";
    private String hearingCentreUrl = "http://hearing-centre-url";
    private String frontendUrl = "http://localhost";

    private String directionExplanation = "an explanation";

    private YesOrNo isIntegrated = YesOrNo.NO;

    private CmaAppointmentNoticeTemplate cmaAppointmentNoticeTemplate;

    @BeforeEach
    public void setUp() {

        cmaAppointmentNoticeTemplate =
            new CmaAppointmentNoticeTemplate(
                templateName,
                frontendUrl,
                stringProvider,
                customerServicesProvider,
                hearingDetailsFinder,
                directionFinder

            );
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, cmaAppointmentNoticeTemplate.getName());
    }

    @Test
    public void should_throw_exception_when_hearing_centre_is_not_present() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));


        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cmaAppointmentNoticeTemplate.mapFieldValues(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    public void should_throw_exception_when_direction_is_not_present() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(vulnerabilitiesTribunalResponse));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(multimediaTribunalResponse));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(inCameraTribunalResponse));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(singleSexCourtTribunalResponse));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(otherHearingRequestTribunalResponse));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CMA_REQUIREMENTS)).thenReturn(Optional.of(direction));

        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CMA_REQUIREMENTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cmaAppointmentNoticeTemplate.mapFieldValues(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("direction 'requestCmaRequirements' is not present");
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(vulnerabilitiesTribunalResponse));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(multimediaTribunalResponse));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(inCameraTribunalResponse));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(singleSexCourtTribunalResponse));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(otherHearingRequestTribunalResponse));
        when(asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(pastExperienceTribunalResponse));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);


        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CMA_REQUIREMENTS)).thenReturn(Optional.of(direction));

        when((hearingDetailsFinder.getHearingCentreUrl(MANCHESTER))).thenReturn(hearingCentreUrl);
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(isIntegrated));

        Map<String, Object> templateFieldValues = cmaAppointmentNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(24, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(expectedFormattedHearingDatePart, templateFieldValues.get("hearingDate"));
        assertEquals(expectedFormattedHearingTimePart, templateFieldValues.get("hearingTime"));
        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals(vulnerabilitiesTribunalResponse, templateFieldValues.get("healthConditions"));
        assertEquals(multimediaTribunalResponse, templateFieldValues.get("multimedia"));
        assertEquals(singleSexCourtTribunalResponse, templateFieldValues.get("singleSexCourt"));
        assertEquals(inCameraTribunalResponse, templateFieldValues.get("inCamera"));
        assertEquals(otherHearingRequestTribunalResponse, templateFieldValues.get("otherHearingRequest"));
        assertEquals(pastExperienceTribunalResponse, templateFieldValues.get("pastExperiences"));
        assertEquals(ariaListingReference, templateFieldValues.get("ariaListingReference"));
        assertEquals(directionExplanation, templateFieldValues.get("reasonForAppointment"));
        assertEquals(MANCHESTER.toString(), templateFieldValues.get("hearingCentreName"));
        assertEquals(hearingCentreUrl, templateFieldValues.get("hearingCentreUrl"));
        assertEquals(isIntegrated, templateFieldValues.get("isIntegrated"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_be_tolerant_of_missing_data() {


        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(vulnerabilitiesTribunalResponse));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(multimediaTribunalResponse));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(inCameraTribunalResponse));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(singleSexCourtTribunalResponse));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(otherHearingRequestTribunalResponse));
        when(asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(pastExperienceTribunalResponse));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);


        when((direction.getExplanation())).thenReturn(directionExplanation);
        when(directionFinder.findFirst(asylumCase, DirectionTag.REQUEST_CMA_REQUIREMENTS)).thenReturn(Optional.of(direction));

        when((hearingDetailsFinder.getHearingCentreUrl(MANCHESTER))).thenReturn(hearingCentreUrl);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PAST_EXPERIENCES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.empty());


        Map<String, Object> templateFieldValues = cmaAppointmentNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(24, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("", templateFieldValues.get("hearingDate"));
        assertEquals("", templateFieldValues.get("hearingTime"));
        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals("No physical or mental health conditions", templateFieldValues.get("healthConditions"));
        assertEquals("No multimedia equipment is being provided", templateFieldValues.get("multimedia"));
        assertEquals("The court will not be single sex", templateFieldValues.get("singleSexCourt"));
        assertEquals("The hearing will be held in public court", templateFieldValues.get("inCamera"));
        assertEquals("No other adjustments are being made", templateFieldValues.get("otherHearingRequest"));
        assertEquals("No special adjustments are being made to accommodate past experiences", templateFieldValues.get("pastExperiences"));
        assertEquals("", templateFieldValues.get("ariaListingReference"));
        assertEquals("", templateFieldValues.get("ariaListingReference"));
        assertEquals(MANCHESTER.toString(), templateFieldValues.get("hearingCentreName"));
        assertEquals(directionExplanation, templateFieldValues.get("reasonForAppointment"));
        assertEquals(frontendUrl, templateFieldValues.get("aipFrontendUrl"));
        assertEquals(hearingCentreUrl, templateFieldValues.get("hearingCentreUrl"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isIntegrated"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
