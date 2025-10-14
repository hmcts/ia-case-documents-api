package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.CaseOfficerRecordAdjournmentDetailsPersonalisation;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CaseOfficerRecordAdjournmentDetailsPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    FeatureToggler featureToggler;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private String caseOfficerEmailAddress = "caseOfficer@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerRecordAdjournmentDetailsPersonalisation caseOfficerRecordAdjournmentDetailsPersonalisation;

    @BeforeEach
    public void setup() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(caseOfficerEmailAddress);

        caseOfficerRecordAdjournmentDetailsPersonalisation =
            new CaseOfficerRecordAdjournmentDetailsPersonalisation(templateId, emailAddressFinder, featureToggler);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerRecordAdjournmentDetailsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_OFFICER_RECORD_ADJOURNMENT_DETAILS",
            caseOfficerRecordAdjournmentDetailsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", false)).thenReturn(true);
        assertTrue(caseOfficerRecordAdjournmentDetailsPersonalisation.getRecipientsList(asylumCase)
            .contains(caseOfficerEmailAddress));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_Off() {
        assertTrue(caseOfficerRecordAdjournmentDetailsPersonalisation.getRecipientsList(asylumCase)
                .isEmpty());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> caseOfficerRecordAdjournmentDetailsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            caseOfficerRecordAdjournmentDetailsPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }
}
