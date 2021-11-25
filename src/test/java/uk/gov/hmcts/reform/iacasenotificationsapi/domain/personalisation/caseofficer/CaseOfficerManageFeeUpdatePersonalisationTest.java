package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseOfficerManageFeeUpdatePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    private FeatureToggler featureToggler;
    private Long caseId = 12345L;
    private String ctscManageFeeUpdateBeforeListingTemplateId = "ctscBeforeListTemplateId";
    private String ctscManageFeeUpdateAfterListingTemplateId = "ctscAfterListTemplateId";
    private String nbcManageFeeUpdateBeforeListingTemplateId = "nbcBeforeListTemplateId";
    private String nbcManageFeeUpdateAfterListingTemplateId = "nbcAfterListTemplateId";

    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String nbcEmailAddress = "nbc-review@example.com";
    private String ctscEmailAddress = "ctsc-review@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerManageFeeUpdatePersonalisation caseOfficerManageFeeUpdatePersonalisation;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerManageFeeUpdatePersonalisation = new CaseOfficerManageFeeUpdatePersonalisation(
            ctscManageFeeUpdateBeforeListingTemplateId,
            ctscManageFeeUpdateAfterListingTemplateId,
            nbcManageFeeUpdateBeforeListingTemplateId,
            nbcManageFeeUpdateAfterListingTemplateId,
            nbcEmailAddress,
            ctscEmailAddress,
            iaExUiFrontendUrl,

                featureToggler);
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_PA_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        assertEquals(ctscManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_EA_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        assertEquals(ctscManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_HU_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payNow"));

        assertEquals(ctscManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_PA_after_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payLater"));

        assertEquals(ctscManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_ctsc_template_id_for_PBa_HU_after_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        assertEquals(ctscManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_PA_before_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_EA_before_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_HU_before_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateBeforeListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_nbc_template_id_for_PBa_PA_after_listing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of("payOffline"));

        assertEquals(nbcManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }


    @Test
    void should_return_nbc_template_id_for_PBa_HU_after_listing() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));

        assertEquals(nbcManageFeeUpdateAfterListingTemplateId,
            caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_nbc_email_address_hu_pay_by_card_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(nbcEmailAddress));
    }

    @Test
    void should_return_given_nbc_email_address_ea_pay_by_card_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(nbcEmailAddress));
    }

    @Test
    void should_return_given_nbc_email_address_pa_pay_by_card_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payOffline"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(nbcEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_hu_pay_by_PBa_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_ea_pay_by_PBa_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_pa_pay_by_PBa_now_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_ctsc_email_address_pa_pay_by_PBa_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.of("payLater"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
            .contains(ctscEmailAddress));
    }

    @Test
    void should_return_given_nbc_email_address_when_feature_flag_is_Off() {
        assertTrue(caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase)
                .isEmpty());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_MANAGE_FEE_UPDATE_CASE_OFFICER",
            caseOfficerManageFeeUpdatePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerManageFeeUpdatePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_on_email_when_case_is_null() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> caseOfficerManageFeeUpdatePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Email Address cannot be found");
    }

    @Test
    void should_throw_exception_on_template_when_case_is_null() {
        when(asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION,String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> caseOfficerManageFeeUpdatePersonalisation.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Template cannot be found");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            caseOfficerManageFeeUpdatePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }

    @Test
    void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            caseOfficerManageFeeUpdatePersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }
}
