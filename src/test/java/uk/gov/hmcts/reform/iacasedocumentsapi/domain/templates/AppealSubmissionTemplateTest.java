package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSubmissionTemplateTest {

    private final String templateName = "APPEAL_SUBMISSION_TEMPLATE.docx";
    @Mock private StringProvider stringProvider;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private LocalDateTime createdDate = LocalDateTime.parse("2020-12-31T12:34:56");
    private String appealReferenceNumber = "RP/11111/2020";
    private String legalRepReferenceNumber = "OUR-REF";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String homeOfficeDecisionDate = "2020-12-23";
    private String appellantGivenNames = "Talha";
    private String appellantFamilyName = "Awan";
    private String appellantDateOfBirth = "1999-12-31";
    private String appellantAddressLine1 = "123 Some Street";
    private String appellantAddressLine2 = "Some Area";
    private String appellantAddressLine3 = "Some District";
    private String appellantAddressPostTown = "Some Town";
    private String appellantAddressCounty = "South";
    private String appellantAddressPostCode = "AB1 2CD";
    private String appellantAddressCountry = "Iceland";
    private String appealType = "revocationOfProtection";
    private String newMatters = "Some new matters";

    private List<String> appealGroundsForDisplay = Arrays.asList(
        "protectionRefugeeConvention",
        "protectionHumanRights"
    );

    private List<IdValue<Map<String, String>>> appellantNationalities =
        Arrays.asList(
            new IdValue<>("111", ImmutableMap.of("code", "IS")),
            new IdValue<>("222", ImmutableMap.of("code", "FI"))
        );

    private List<IdValue<Map<String, String>>> otherAppeals =
        Arrays.asList(
            new IdValue<>("333", ImmutableMap.of("value", "1234")),
            new IdValue<>("444", ImmutableMap.of("value", "5678"))
        );

    private AppealSubmissionTemplate appealSubmissionTemplate;

    @Before
    public void setUp() {

        appealSubmissionTemplate =
            new AppealSubmissionTemplate(
                templateName,
                stringProvider
            );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(caseDetails.getCreatedDate()).thenReturn(createdDate);
        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.getHomeOfficeDecisionDate()).thenReturn(Optional.of(homeOfficeDecisionDate));
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.getAppellantDateOfBirth()).thenReturn(Optional.of(appellantDateOfBirth));
        when(asylumCase.getAppealType()).thenReturn(Optional.of(appealType));
        when(asylumCase.getNewMatters()).thenReturn(Optional.of(newMatters));

        when(asylumCase.getAppellantHasFixedAddress()).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.getAppellantAddress()).thenReturn(Optional.of(
            new AddressUk(
                appellantAddressLine1,
                appellantAddressLine2,
                appellantAddressLine3,
                appellantAddressPostTown,
                appellantAddressCounty,
                appellantAddressPostCode,
                appellantAddressCountry
            )
        ));

        when(asylumCase.getAppellantNationalities()).thenReturn(Optional.of(appellantNationalities));
        when(asylumCase.getAppealGroundsForDisplay()).thenReturn(Optional.of(appealGroundsForDisplay));
        when(asylumCase.getOtherAppeals()).thenReturn(Optional.of(otherAppeals));

        when(stringProvider.get("appealType", appealType)).thenReturn(Optional.of("The revocation of a protection status"));
        when(stringProvider.get("isoCountries", "FI")).thenReturn(Optional.of("Finland"));
        when(stringProvider.get("isoCountries", "IS")).thenReturn(Optional.of("Iceland"));
        when(stringProvider.get("appealGrounds", "protectionRefugeeConvention")).thenReturn(Optional.of("Refugee convention"));
        when(stringProvider.get("appealGrounds", "protectionHumanRights")).thenReturn(Optional.of("Human rights"));
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, appealSubmissionTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(15, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("31122020", templateFieldValues.get("CREATED_DATE"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("23122020", templateFieldValues.get("homeOfficeDecisionDate"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals("31121999", templateFieldValues.get("appellantDateOfBirth"));
        assertEquals("The revocation of a protection status", templateFieldValues.get("appealType"));
        assertEquals(newMatters, templateFieldValues.get("newMatters"));

        assertEquals(7, ((Map) templateFieldValues.get("appellantAddress")).size());
        assertEquals(appellantAddressLine1, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressLine1"));
        assertEquals(appellantAddressLine2, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressLine2"));
        assertEquals(appellantAddressLine3, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressLine3"));
        assertEquals(appellantAddressPostTown, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressPostTown"));
        assertEquals(appellantAddressCounty, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressCounty"));
        assertEquals(appellantAddressPostCode, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressPostCode"));
        assertEquals(appellantAddressCountry, ((Map) templateFieldValues.get("appellantAddress")).get("appellantAddressCountry"));

        assertEquals(2, ((List) templateFieldValues.get("appellantNationalities")).size());
        assertEquals(ImmutableMap.of("nationality", "Iceland"), ((List) templateFieldValues.get("appellantNationalities")).get(0));
        assertEquals(ImmutableMap.of("nationality", "Finland"), ((List) templateFieldValues.get("appellantNationalities")).get(1));

        assertEquals(2, ((List) templateFieldValues.get("appealGrounds")).size());
        assertEquals(ImmutableMap.of("appealGround", "Refugee convention"), ((List) templateFieldValues.get("appealGrounds")).get(0));
        assertEquals(ImmutableMap.of("appealGround", "Human rights"), ((List) templateFieldValues.get("appealGrounds")).get(1));

        assertEquals("1234, 5678", templateFieldValues.get("otherAppeals"));
    }

    @Test
    public void should_not_add_appeal_type_if_not_present() {

        when(asylumCase.getAppealType()).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(14, templateFieldValues.size());
        assertFalse(templateFieldValues.containsKey("appealType"));
    }

    @Test
    public void should_not_add_address_if_no_fixed_address_exists() {

        when(asylumCase.getAppellantHasFixedAddress()).thenReturn(Optional.of(YesOrNo.NO));

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(14, templateFieldValues.size());
        assertFalse(templateFieldValues.containsKey("appellantAddress"));
    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getHomeOfficeDecisionDate()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantDateOfBirth()).thenReturn(Optional.empty());
        when(asylumCase.getAppealType()).thenReturn(Optional.empty());
        when(asylumCase.getNewMatters()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantHasFixedAddress()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantNationalities()).thenReturn(Optional.empty());
        when(asylumCase.getAppealGroundsForDisplay()).thenReturn(Optional.empty());
        when(asylumCase.getOtherAppeals()).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = appealSubmissionTemplate.mapFieldValues(caseDetails);

        assertEquals(13, templateFieldValues.size());

        assertFalse(templateFieldValues.containsKey("appealType"));
        assertFalse(templateFieldValues.containsKey("appellantAddress"));

        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("31122020", templateFieldValues.get("CREATED_DATE"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeDecisionDate"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("appellantDateOfBirth"));
        assertEquals("", templateFieldValues.get("newMatters"));
        assertEquals(0, ((List) templateFieldValues.get("appellantNationalities")).size());
        assertEquals(0, ((List) templateFieldValues.get("appealGrounds")).size());
        assertEquals("", templateFieldValues.get("otherAppeals"));
    }
}
