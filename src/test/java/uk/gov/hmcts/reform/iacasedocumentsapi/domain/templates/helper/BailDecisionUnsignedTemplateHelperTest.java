package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail.helper.BailDecisionUnsignedTemplateHelper;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class BailDecisionUnsignedTemplateHelperTest {

    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private CustomerServicesProvider customerServicesProvider;

    private final String govCallChargesUrl = "TEST|https://www.example.com/}";

    private String applicantGivenNames = "John";
    private String applicantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String bailReferenceNumber = "5555-5555-5555-9999";
    private String applicantDetainedLoc = "prison";
    private String applicantPrisonDetails = "4568";
    private String prisonName = "Aylesbury";
    private String legalRepReference = "TREF09";

    private BailDecisionUnsignedTemplateHelper bailDecisionUnsignedTemplateHelper;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        bailDecisionUnsignedTemplateHelper =
                new BailDecisionUnsignedTemplateHelper(govCallChargesUrl, customerServicesProvider);
    }

    @Test
    void should_set_prison_details_if_detained_in_prison() {
        dataSetUp();
        fieldValuesMap = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("applicantPrisonDetails"));
        assertTrue(fieldValuesMap.containsKey("prisonName"));
        assertFalse(fieldValuesMap.containsKey("ircName"));
    }

    @Test
    void should_set_prison_details_if_detained_in_irc() {
        dataSetUp();
        when(bailCase.read(APPLICANT_DETAINED_LOC, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));
        when(bailCase.read(IRC_NAME, String.class)).thenReturn(Optional.of("Derwentside"));
        fieldValuesMap = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("ircName"));
        assertFalse(fieldValuesMap.containsKey("applicantPrisonDetails"));
        assertFalse(fieldValuesMap.containsKey("prisonName"));
    }


    @Test
    void should_be_tolerant_to_empty_data() {
        dataSetUp();
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        fieldValuesMap = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        checkCommonFields();
        assertEquals("", fieldValuesMap.get("legalRepReference"));
    }

    @Test
    void should_set_LR_Ref_when_submitted_by_LR() {
        dataSetUp();
        fieldValuesMap = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("legalRepReference"));
        assertEquals(YesOrNo.YES, fieldValuesMap.get("isLegallyRepresentedForFlag"));
    }

    @Test
    void should_set_LR_Ref_when_submitted_by_applicant_and_has_LR() {
        dataSetUp();
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SENT_BY_CHECKLIST, String.class)).thenReturn(Optional.of("Applicant"));
        when(bailCase.read(HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        fieldValuesMap = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        checkCommonFields();
        assertTrue(fieldValuesMap.containsKey("legalRepReference"));
        assertEquals(YesOrNo.YES, fieldValuesMap.get("isLegallyRepresentedForFlag"));
    }

    @Test
    void should_not_set_LR_Ref_when_submitted_by_applicant_and_has_no_LR() {
        dataSetUp();
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(SENT_BY_CHECKLIST, String.class)).thenReturn(Optional.of("Applicant"));
        when(bailCase.read(HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        fieldValuesMap = bailDecisionUnsignedTemplateHelper.getCommonMapFieldValues(caseDetails);

        checkCommonFields();
        assertFalse(fieldValuesMap.containsKey("legalRepReference"));
        assertEquals(YesOrNo.NO, fieldValuesMap.get("isLegallyRepresentedForFlag"));
    }

    //Helper method for common assertions
    private void checkCommonFields() {
        assertTrue(fieldValuesMap.containsKey("applicantGivenNames"));
        assertTrue(fieldValuesMap.containsKey("applicantFamilyName"));
        assertTrue(fieldValuesMap.containsKey("bailReferenceNumber"));
        assertTrue(fieldValuesMap.containsKey("homeOfficeReferenceNumber"));
        assertTrue(fieldValuesMap.containsKey("applicantDetainedLoc"));
        assertTrue(fieldValuesMap.containsKey("isLegallyRepresentedForFlag"));
        assertTrue(fieldValuesMap.containsKey("judgeDetailsName"));
        assertTrue(fieldValuesMap.containsKey("customerServicesTelephone"));
        assertTrue(fieldValuesMap.containsKey("customerServicesEmail"));
        assertTrue(fieldValuesMap.containsKey("govCallChargesLink"));
    }

    // Helper method to set the common data
    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(HAS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(IS_LEGAL_REP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(APPLICANT_DETAINED_LOC, String.class)).thenReturn(Optional.of(applicantDetainedLoc));
        when(bailCase.read(APPLICANT_PRISON_DETAILS, String.class)).thenReturn(Optional.of(applicantPrisonDetails));
        when(bailCase.read(PRISON_NAME, String.class)).thenReturn(Optional.of(prisonName));
        when(bailCase.read(LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
    }
}
