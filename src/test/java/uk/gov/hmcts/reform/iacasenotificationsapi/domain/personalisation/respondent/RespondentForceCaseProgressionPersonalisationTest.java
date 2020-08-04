package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@RunWith(MockitoJUnitRunner.class)
public class RespondentForceCaseProgressionPersonalisationTest {

    @Mock
    CustomerServicesProvider customerServicesProvider;

    private RespondentForceCaseProgressionPersonalisation personalisation;

    @Before
    public void setUp() {

        personalisation = new RespondentForceCaseProgressionPersonalisation(
            "templateId",
            "emailAddress",
            "https://manage-case.platform.hmcts.net/",
            customerServicesProvider);
    }


    @Test
    public void getTemplateId() {
        assertEquals("templateId", personalisation.getTemplateId());
    }

    @Test
    public void getRecipientsList() {
        assertTrue(personalisation.getRecipientsList(new AsylumCase()).contains("emailAddress"));
    }

    @Test
    public void getReferenceId() {
        assertEquals("1234_RESPONDENT_FORCE_CASE_PROGRESSION", personalisation.getReferenceId(1234L));
    }

    @Test
    public void getPersonalisation() {
        final ImmutableMap<String, String> customerServicesValues = ImmutableMap
            .<String, String>builder()
            .put("customerServicesTelephone", "555 555 555")
            .put("customerServicesEmail", "cust.services@example.com").build();
        when(customerServicesProvider.getCustomerServicesPersonalisation()).thenReturn(customerServicesValues);

        AsylumCase asylumCase = writeTestAsylumCase();

        Map<String, String> actualPersonalisation = personalisation.getPersonalisation(asylumCase);

        assertEquals("RP/50001/2020", actualPersonalisation.get("appealReferenceNumber"));
        assertEquals("Lacy Dawson", actualPersonalisation.get("appellantGivenNames"));
        assertEquals("Venus Blevins", actualPersonalisation.get("appellantFamilyName"));
        assertEquals("A1234567", actualPersonalisation.get("homeOfficeReferenceNumber"));
        assertEquals("555 555 555", actualPersonalisation.get("customerServicesTelephone"));
        assertEquals("cust.services@example.com", actualPersonalisation.get("customerServicesEmail"));
    }

    private AsylumCase writeTestAsylumCase() {
        AsylumCase asylumCase = new AsylumCase();
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "RP/50001/2020");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Lacy Dawson");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Venus Blevins");
        asylumCase.write(HOME_OFFICE_REFERENCE_NUMBER, "A1234567");
        return asylumCase;
    }
}