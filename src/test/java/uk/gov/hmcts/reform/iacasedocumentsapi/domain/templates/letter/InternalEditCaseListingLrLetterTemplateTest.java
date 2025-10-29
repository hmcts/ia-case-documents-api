package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Nationality;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.HearingNoticeUpdatedTemplateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class InternalEditCaseListingLrLetterTemplateTest {

    private final String templateName = "EDIT_CASE_LISTING_TEMPLATE.docx";
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock
    AddressUk address;
    private String appealReferenceNumber = "RP/11111/2020";
    private String appellantGivenNames = "Talha";
    private String appellantFamilyName = "Awan";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String customerServicesEmail = "customer.services@example.com";
    private final String customerServicesTelephone = "0300 123 1711";
    private String addressLine1 = "50";
    private String addressLine2 = "Building name";
    private String addressLine3 = "Street name";
    private String postCode = "XX1 2YY";
    private String postTown = "Town name";
    private String oocAddressLine1 = "Calle Toledo 32";
    private String oocAddressLine2 = "Madrid";
    private String oocAddressLine3 = "28003";
    private NationalityFieldValue oocAddressCountry = mock(NationalityFieldValue.class);
    private String hearingDate = "2024-12-20T12:34:56";
    @Mock private HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider;
    @Mock private DynamicList hearingChannelDynamicList;
    @Mock private Value hearingChannelValue;
    private String hearingChannelLabel = "In person";
    private NationalityFieldValue nationalityOoc = mock(NationalityFieldValue.class);

    private InternalEditCaseListingLrLetterTemplate internalEditCaseListingLrLetterTemplate;

    @BeforeEach
    public void setUp() {
        internalEditCaseListingLrLetterTemplate =
                new InternalEditCaseListingLrLetterTemplate(
                        templateName,
                        customerServicesProvider,
                        hearingNoticeUpdatedTemplateProvider
                );
    }

    @Test
    public void should_return_template_name() {
        assertEquals(templateName, internalEditCaseListingLrLetterTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values_in_country() {
        dataSetupOoc(true);

        Map<String, Object> templateFieldValues = internalEditCaseListingLrLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        assertEquals(customerServicesEmail, customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), templateFieldValues.get("dateLetterSent"));
        assertEquals(addressLine1, templateFieldValues.get("address_line_1"));
        assertEquals(addressLine2, templateFieldValues.get("address_line_2"));
        assertEquals(addressLine3, templateFieldValues.get("address_line_3"));
        assertEquals(postTown, templateFieldValues.get("address_line_4"));
        assertEquals(postCode, templateFieldValues.get("address_line_5"));
        assertEquals("20 December 2024", templateFieldValues.get("hearingDate"));
        assertTrue("12:34 PM".equalsIgnoreCase(templateFieldValues.get("hearingTime").toString()));
        assertEquals(hearingChannelLabel, templateFieldValues.get("hearingChannel"));
        assertEquals(hearingChannelLabel, templateFieldValues.get("oldHearingChannel"));
        verify(hearingNoticeUpdatedTemplateProvider, times(1)).mapFieldValues(caseDetails, caseDetailsBefore);
    }

    @Test
    public void should_map_case_data_to_template_field_values_ooc() {
        dataSetupOoc(false);
        Map<String, Object> templateFieldValues = internalEditCaseListingLrLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        assertEquals(customerServicesEmail, customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), templateFieldValues.get("dateLetterSent"));
        Assert.assertEquals(oocAddressLine1, templateFieldValues.get("address_line_1"));
        Assert.assertEquals(oocAddressLine2, templateFieldValues.get("address_line_2"));
        Assert.assertEquals(oocAddressLine3, templateFieldValues.get("address_line_3"));
        Assert.assertEquals(Nationality.ES.toString(), templateFieldValues.get("address_line_4"));
        assertEquals("20 December 2024", templateFieldValues.get("hearingDate"));
        assertTrue("12:34 PM".equalsIgnoreCase(templateFieldValues.get("hearingTime").toString()));
        assertEquals(hearingChannelLabel, templateFieldValues.get("hearingChannel"));
        assertEquals(hearingChannelLabel, templateFieldValues.get("oldHearingChannel"));
        verify(hearingNoticeUpdatedTemplateProvider, times(1)).mapFieldValues(caseDetails, caseDetailsBefore);
    }

    @Test
    public void should_use_default_hearing_channel_when_missing() {
        dataSetupOoc(true);
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = internalEditCaseListingLrLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("Unknown", templateFieldValues.get("hearingChannel"));
        assertEquals("Unknown", templateFieldValues.get("oldHearingChannel"));
    }

    void dataSetupOoc(boolean legalRepInUk) {
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_GIVEN_NAME, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME_PAPER_J, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));
        when(asylumCase.read(APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(address));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelDynamicList));
        when(hearingChannelDynamicList.getValue()).thenReturn(hearingChannelValue);
        when(hearingChannelValue.getLabel()).thenReturn(hearingChannelLabel);

        if (legalRepInUk) {
            when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());
            when(asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
            when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(address));
            when(address.getAddressLine1()).thenReturn(Optional.of(addressLine1));
            when(address.getAddressLine2()).thenReturn(Optional.of(addressLine2));
            when(address.getAddressLine3()).thenReturn(Optional.of(addressLine3));
            when(address.getPostCode()).thenReturn(Optional.of(postCode));
            when(address.getPostTown()).thenReturn(Optional.of(postTown));
        } else {
            when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.empty());
            when(asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
            when(asylumCase.read(OOC_ADDRESS_LINE_1, String.class)).thenReturn(Optional.of(oocAddressLine1));
            when(asylumCase.read(OOC_ADDRESS_LINE_2, String.class)).thenReturn(Optional.of(oocAddressLine2));
            when(asylumCase.read(OOC_ADDRESS_LINE_3, String.class)).thenReturn(Optional.of(oocAddressLine3));
            when(asylumCase.read(OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)).thenReturn(Optional.of(nationalityOoc));
            when(nationalityOoc.getCode()).thenReturn(Nationality.ES.name());
        }
    }
}
