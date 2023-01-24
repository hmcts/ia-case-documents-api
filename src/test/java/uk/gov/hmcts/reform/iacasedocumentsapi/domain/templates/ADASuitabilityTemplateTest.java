package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.TemplateUtils.*;
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class ADASuitabilityTemplateTest {

    private final String templateName = "ADA_SUITABILITY_TEMPLATE.docx";
    @Mock
    private StringProvider stringProvider;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private Document applicationOutOfTimeDocument;

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
    private YesOrNo appellantInDetention = YesOrNo.YES;
    private String detentionFacilityPrison = "prison";
    private String detentionFacilityIrc = "immigrationRemovalCentre";
    private String detentionFacilityOther = "other";
    private String prisonName = "HMP Gartree";
    private String nomsNumber = "{noms=Noms1234}";
    private String prisonerReleaseDate = "{release=01-01-2025T06:00:000}";
    private String ircName = "MoJ IRC";
    private String otherName = "{otherName=Other MoJ Facility}";
    private YesOrNo isAcceleratedDetainedAppeal = YesOrNo.YES;
    private BailApplicationStatus hasPendingBailApplication = BailApplicationStatus.YES;
    private String bailApplicationNumber = "BailRef1234";
    private String appealType = "revocationOfProtection";
    private YesOrNo removalOrderOption = YesOrNo.YES;
    private String removalOrderDate = "01-01-2023T16:03:002";
    private String newMatters = "Some new matters";
    private String email = "someone@something.com";
    private String mobileNumber = "07987654321";
    private String appellantTitle = "Mr";
    private String homeOfficeDecisionReceivedDate = "2020-12-23";
    private String dateEntryClearanceDecision = "2020-12-19";
    private String gwfReference = "GWF1234567";
    private String oocAddress = "1 Park Street, South Road, Kenya, KY 2AF";

    private AddressUk addressUk = new AddressUk(
            appellantAddressLine1,
            appellantAddressLine2,
            appellantAddressLine3,
            appellantAddressPostTown,
            appellantAddressCounty,
            appellantAddressPostCode,
            appellantAddressCountry
    );

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

    private String outOfTimeExplanation = "someOutOfTimeExplanation";
    private String outOfTimeDocumentFileName = "someOutOfTimeDocument.pdf";

    private YesOrNo wantsEmail = YesOrNo.YES;
    private ADASuitabilityTemplate adaSuitabilityTemplate;
    private AdaSuitabilityReviewDecision adaSuitability = AdaSuitabilityReviewDecision.UNSUITABLE;
    private String adaSuitabilityReason = "This is the reason";
    private String adaSuitabilityjudge = "Judgy Judgerson";


    @BeforeEach
    void setUp() {



        adaSuitabilityTemplate =
                new ADASuitabilityTemplate(
                        templateName,
                        stringProvider
                );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, adaSuitabilityTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getCreatedDate()).thenReturn(createdDate);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.ofNullable(adaSuitability));
        when(asylumCase.read(SUITABILITY_REVIEW_REASON, String.class)).thenReturn(Optional.ofNullable(adaSuitabilityReason));
        when(asylumCase.read(SUITABILITY_REVIEW_JUDGE, String.class)).thenReturn(Optional.ofNullable(adaSuitabilityjudge));
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = adaSuitabilityTemplate.mapFieldValues(caseDetails);

        assertEquals(8, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));

        assertEquals(appellantGivenNames.concat(" " + appellantFamilyName), templateFieldValues.get("fullName"));

        assertEquals(YesOrNo.NO, templateFieldValues.get("suitability"));
        assertEquals(adaSuitabilityReason, templateFieldValues.get("suitabilityReason"));
        assertEquals(adaSuitabilityjudge, templateFieldValues.get("suitabilityReason"));
    }

    @ParameterizedTest
    @EnumSource(AdaSuitabilityReviewDecision.class)
    void test_suitability_and_non_suitability(AdaSuitabilityReviewDecision adaSuitabilityReviewDecision) {
        dataSetUp();
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.ofNullable(adaSuitabilityReviewDecision));

        Map<String, Object> templateFieldValues = adaSuitabilityTemplate.mapFieldValues(caseDetails);

        assertEquals(8, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));

        assertEquals(appellantGivenNames.concat(" " + appellantFamilyName), templateFieldValues.get("fullName"));

//        assertEquals(convertAdaSuitabilityToYesOrNo(adaSuitabilityReviewDecision), templateFieldValues.get("suitability"));
        assertEquals(adaSuitabilityReason, templateFieldValues.get("suitabilityReason"));
        assertEquals(adaSuitabilityjudge, templateFieldValues.get("suitabilityReason"));
    }

}
