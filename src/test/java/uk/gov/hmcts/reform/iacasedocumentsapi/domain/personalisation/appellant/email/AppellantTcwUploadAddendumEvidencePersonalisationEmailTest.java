package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppellantTcwUploadAddendumEvidencePersonalisationEmailTest {

    private final String templateId = "someTemplateId";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeReferenceNumber = "homeOfficeReferenceNumber";
    private final String ariaListingReference = "ariaListingReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String appellantEmailAddress = "appellant@example.com";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";
    private final String iaAipFrontendUrl = "iaAipFrontendUrl";

    @Mock
    AsylumCase asylumCase;

    @Mock
    RecipientsFinder recipientsFinder;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    FeatureToggler featureToggler;

    private AppellantTcwUploadAddendumEvidencePersonalisationEmail appellantTcwUploadAddendumEvidencePersonalisationEmail;

    @BeforeEach
    void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        appellantTcwUploadAddendumEvidencePersonalisationEmail =
                new AppellantTcwUploadAddendumEvidencePersonalisationEmail(
                        templateId,
                        iaAipFrontendUrl,
                        recipientsFinder,
                        customerServicesProvider,
                        featureToggler
                );
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(templateId, appellantTcwUploadAddendumEvidencePersonalisationEmail.getTemplateId());
    }

    @Test
    void should_throw_exception_on_recipients_when_case_is_null() {
        when(recipientsFinder.findAll(null, NotificationType.EMAIL))
                .thenThrow(new NullPointerException("asylumCase must not be null"));

        assertThatThrownBy(() -> appellantTcwUploadAddendumEvidencePersonalisationEmail.getRecipientsList(null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(12345L + "_TCW_UPLOADED_ADDENDUM_EVIDENCE_AIP_APPELLANT_EMAIL",
                appellantTcwUploadAddendumEvidencePersonalisationEmail.getReferenceId(12345L));
    }


    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
                () -> appellantTcwUploadAddendumEvidencePersonalisationEmail.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_appellant_email_address_from_asylum_case() {
        when(featureToggler.getValue("aip-upload-addendum-evidence-feature", false)).thenReturn(true);
        when(recipientsFinder.findAll(asylumCase, NotificationType.EMAIL))
                .thenReturn(Collections.singleton(appellantEmailAddress));

        assertTrue(appellantTcwUploadAddendumEvidencePersonalisationEmail.getRecipientsList(asylumCase)
                .contains(appellantEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(appellantTcwUploadAddendumEvidencePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation =
                appellantTcwUploadAddendumEvidencePersonalisationEmail.getPersonalisation(asylumCase);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        String directLinkToNewEvidencePage = iaAipFrontendUrl + "new-evidence";
        assertEquals(directLinkToNewEvidencePage, personalisation.get("Direct link to new evidence page"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(appellantTcwUploadAddendumEvidencePersonalisationEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
                appellantTcwUploadAddendumEvidencePersonalisationEmail.getPersonalisation(asylumCase);

        Assertions.assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
