package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeAppealSubmittedPayOfflinePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;


    private Long caseId = 12345L;
    private String emailTemplateId = "emailTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String adaPrefix = "Accelerated detained appeal";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String homeOfficeEmail = "apchomeoffice@example.com";

    private HomeOfficeAppealSubmittedPayOfflinePersonalisation
            homeOfficeAppealSubmittedPayOfflinePersonalisation;

    @BeforeEach
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        homeOfficeAppealSubmittedPayOfflinePersonalisation =
            new HomeOfficeAppealSubmittedPayOfflinePersonalisation(
                    homeOfficeEmail,
                emailTemplateId,
                iaExUiFrontendUrl,
                customerServicesProvider
                );
    }

    @Test
    public void should_return_given_email_address() {
        assertTrue(homeOfficeAppealSubmittedPayOfflinePersonalisation.getRecipientsList(asylumCase)
            .contains(homeOfficeEmail));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(emailTemplateId, homeOfficeAppealSubmittedPayOfflinePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_SUBMITTED_PAY_OFFLINE_HOME_OFFICE",
                homeOfficeAppealSubmittedPayOfflinePersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(homeOfficeAppealSubmittedPayOfflinePersonalisation);
        Map<String, String> personalisation =
            homeOfficeAppealSubmittedPayOfflinePersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

}
