package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantHomeOfficeUploadAddendumEvidencePersonalisationSmsTest {
    private final String smsTemplateId = "someSmsTemplateId";
    private final String iaAipFrontendUrl = "iaAipFrontendUrl";
    private final String mockedAppealReferenceNumber = "someReferenceNumber";
    private final String mockedAppellantMobilePhone = "07123456789";
    @Mock
    AsylumCase asylumCase;
    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    private FeatureToggler featureToggler;
    private AppellantHomeOfficeUploadAddendumEvidencePersonalisationSms appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class))
                .thenReturn(Optional.of(mockedAppealReferenceNumber));

        appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms = new AppellantHomeOfficeUploadAddendumEvidencePersonalisationSms(
                smsTemplateId,
                iaAipFrontendUrl,
                recipientsFinder,
                featureToggler);
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(smsTemplateId, appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms.getTemplateId());
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_HOME_OFFICE_UPLOADED_ADDENDUM_EVIDENCE_AIP_APPELLANT_SMS",
                appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms.getReferenceId(12345L));
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.SMS))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_appellant_phone_number_from_asylum_case() {
        when(featureToggler.getValue("aip-upload-addendum-evidence-feature", false)).thenReturn(true);
        when(recipientsFinder.findAll(asylumCase, NotificationType.SMS))
                .thenReturn(Collections.singleton(mockedAppellantMobilePhone));

        assertTrue(appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms.getRecipientsList(asylumCase)
                .contains(mockedAppellantMobilePhone));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {
        Map<String, String> personalisation =
                appellantHomeOfficeTcwUploadAddendumEvidencePersonalisationSms.getPersonalisation(asylumCase);
        assertEquals(mockedAppealReferenceNumber, personalisation.get("Appeal Ref Number"));
        String directLinkToNewEvidencePage = iaAipFrontendUrl + "home-office-evidence/addendum";
        assertEquals(directLinkToNewEvidencePage, personalisation.get("Direct link to new evidence page"));

    }
}
