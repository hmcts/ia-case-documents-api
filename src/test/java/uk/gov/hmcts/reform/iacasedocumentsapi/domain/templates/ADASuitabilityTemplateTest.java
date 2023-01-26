package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class ADASuitabilityTemplateTest {

    private final String templateName = "ADA_SUITABILITY_TEMPLATE.docx";
    @Mock
    private StringProvider stringProvider;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    private ADASuitabilityTemplate adaSuitabilityTemplate;
    private LocalDateTime createdDate = LocalDateTime.parse("2020-12-31T12:34:56");
    private String appealReferenceNumber = "RP/11111/2020";
    private String legalRepReferenceNumber = "Legal-Rep-Fake-Ref";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Doe";
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
