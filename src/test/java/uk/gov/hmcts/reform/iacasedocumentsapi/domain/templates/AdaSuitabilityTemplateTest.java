package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdaSuitabilityTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private final String templateName = "ADA_SUITABILITY_TEMPLATE.docx";

    private AdaSuitabilityTemplate adaSuitabilityTemplate;
    private final String now = LocalDate.now().toString();
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String legalRepReferenceNumber = "Legal-Rep-Fake-Ref";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final AdaSuitabilityReviewDecision adaSuitability = AdaSuitabilityReviewDecision.UNSUITABLE;
    private final String adaSuitabilityReason = "This is the reason";
    private final String adaSuitabilityjudge = "Judgy Judgerson";


    @BeforeEach
    void setUp() {
        adaSuitabilityTemplate =
                new AdaSuitabilityTemplate(
                        templateName
                );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, adaSuitabilityTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.of(adaSuitability));
        when(asylumCase.read(SUITABILITY_REVIEW_REASON, String.class)).thenReturn(Optional.ofNullable(adaSuitabilityReason));
        when(asylumCase.read(SUITABILITY_REVIEW_JUDGE, String.class)).thenReturn(Optional.ofNullable(adaSuitabilityjudge));
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = adaSuitabilityTemplate.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("[userImage:decisionsandreasons.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));

        assertEquals(appellantGivenNames.concat(" " + appellantFamilyName), templateFieldValues.get("appellantFullName"));

        assertEquals(adaSuitability, templateFieldValues.get("suitability"));
        assertEquals(adaSuitabilityReason, templateFieldValues.get("suitabilityReason"));
        assertEquals(adaSuitabilityjudge, templateFieldValues.get("judgeName"));
    }

    @ParameterizedTest
    @EnumSource(AdaSuitabilityReviewDecision.class)
    void test_suitability_and_non_suitability(AdaSuitabilityReviewDecision adaSuitabilityReviewDecision) {
        dataSetUp();
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)).thenReturn(Optional.ofNullable(adaSuitabilityReviewDecision));

        Map<String, Object> templateFieldValues = adaSuitabilityTemplate.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("[userImage:decisionsandreasons.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));

        assertEquals(appellantGivenNames.concat(" " + appellantFamilyName), templateFieldValues.get("appellantFullName"));

        assertEquals(adaSuitabilityReviewDecision, templateFieldValues.get("suitability"));
        assertEquals(adaSuitabilityReason, templateFieldValues.get("suitabilityReason"));
        assertEquals(adaSuitabilityjudge, templateFieldValues.get("judgeName"));
        assertEquals(now, templateFieldValues.get("decisionDate"));
    }

}
