package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;

import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantHearingBundleReadyPersonalisationSmsTest {

    private final String smsTemplateId = "someSmsTemplateId";
    private final String iaAipFrontendUrl = "http://localhost";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String ccdReferenceNumber = "1234 5678 4321 8765";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    private FeatureToggler featureToggler;

    private AppellantHearingBundleReadyPersonalisationSms
        appellantHearingBundleReadyPersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(mockedAppealReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class))
                .thenReturn(Optional.of(ccdReferenceNumber));

        appellantHearingBundleReadyPersonalisationSms =
            new AppellantHearingBundleReadyPersonalisationSms(
                smsTemplateId,
                iaAipFrontendUrl,
                recipientsFinder,
                featureToggler
            );
    }

    @Test
    void should_return_given_template_id() {
        Assert.assertEquals(smsTemplateId,
            appellantHearingBundleReadyPersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        Long caseId = 12345L;
        Assert.assertEquals(caseId + "_HEARING_BUNDLE_IS_READY_APPELLANT_SMS",
            appellantHearingBundleReadyPersonalisationSms.getReferenceId(caseId));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {

        when(recipientsFinder.findAll(null, NotificationType.SMS))
            .thenThrow(new NullPointerException("asylumCase must not be null"));
        when(featureToggler.getValue("aip-hearing-bundle-feature", false)).thenReturn(true);

        assertThatThrownBy(() -> appellantHearingBundleReadyPersonalisationSms.getRecipientsList(null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_given_mobile_mobile_list_from_subscribers_in_asylum_case() {

        String mockedAppellantMobilePhone = "07123456789";
        when(featureToggler.getValue("aip-hearing-bundle-feature", false)).thenReturn(true);
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
            .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantHearingBundleReadyPersonalisationSms.getRecipientsList(asylumCase)
            .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_return_empty_mobile_list_when_featureflag_is_not_enabled() {
        when(featureToggler.getValue("aip-hearing-bundle-feature", false)).thenReturn(false);

        assertTrue(appellantHearingBundleReadyPersonalisationSms.getRecipientsList(asylumCase)
            .isEmpty());
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> appellantHearingBundleReadyPersonalisationSms.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            appellantHearingBundleReadyPersonalisationSms.getPersonalisation(asylumCase);
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }

    @Test
    void should_return_personalisation_when_only_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            appellantHearingBundleReadyPersonalisationSms.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals(iaAipFrontendUrl, personalisation.get("Hyperlink to service"));

    }
}