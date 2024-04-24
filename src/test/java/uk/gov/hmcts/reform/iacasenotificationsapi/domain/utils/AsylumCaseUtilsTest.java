package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.generateAppellantPinIfNotPresent;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.getApplicantAndRespondent;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isLegalRepEjp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AccessCodeGenerator;


@ExtendWith(MockitoExtension.class)
public class AsylumCaseUtilsTest {

    @Mock
    private AsylumCase asylumCase;
    @Spy
    private AsylumCase asylumCaseSpy;
    @Mock
    private Document document;
    private final MockedStatic<AccessCodeGenerator> generatorMockedStatic = mockStatic(AccessCodeGenerator.class);


    private final String legalOfficerAddendumUploadedByLabel = "TCW";
    private final String legalOfficerAddendumUploadSuppliedByLabel = "The respondent";
    private static final String applyForCostsCreationDate = "2023-11-24";
    private final IdValue<DocumentWithMetadata> addendumOne = new IdValue<>(
        "1",
        new DocumentWithMetadata(
            document,
            "Some description",
            "2018-12-25",
            DocumentTag.ADDENDUM_EVIDENCE,
            legalOfficerAddendumUploadSuppliedByLabel,
            legalOfficerAddendumUploadedByLabel
        )
    );

    private final IdValue<DocumentWithMetadata> addendumTwo = new IdValue<>(
        "2",
        new DocumentWithMetadata(
            document,
            "Some description",
            "2018-12-26", DocumentTag.ADDENDUM_EVIDENCE,
            legalOfficerAddendumUploadSuppliedByLabel,
            legalOfficerAddendumUploadedByLabel
        )
    );

    private final String legalRepEmailEjp = "legalRep@example.com";
    private final String generatedCode = "12345";

    @AfterEach
    void tearDown() {
        generatorMockedStatic.close();
    }

    @Test
    void should_return_correct_value_for_det() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAppellantInDetention(asylumCase));
    }

    @Test
    void should_return_correct_value_for_ada() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase));
    }

    @Test
    void should_return_correct_value_for_aaa() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        assertTrue(AsylumCaseUtils.isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void isAdmin_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        assertTrue(AsylumCaseUtils.isInternalCase(asylumCase));
    }

    @Test
    void isAdmin_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        assertFalse(AsylumCaseUtils.isInternalCase(asylumCase));
    }

    @Test
    void isAipJourney_should_return_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(AsylumCaseUtils.isAipJourney(asylumCase));
    }

    @Test
    void isAipJourney_should_return_false() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(AsylumCaseUtils.isAipJourney(asylumCase));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_granted() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED, AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @Test
    void getFtpaDecisionOutcomeType_should_return_refused() {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertEquals(FtpaDecisionOutcomeType.FTPA_REFUSED, AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase).orElse(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"birmingham", "", "harmondsworth"})
    void isListed_should_return_correct_value(String hearingCentre) {
        Optional<HearingCentre> mayBeListCaseHearingCenter = HearingCentre.from(hearingCentre);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(mayBeListCaseHearingCenter);
        assertEquals(mayBeListCaseHearingCenter.isPresent(), AsylumCaseUtils.isAppealListed(asylumCase));
    }

    @Test
    void should_get_addendum_document_when_present() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.of(addendumOne), AsylumCaseUtils.getLatestAddendumEvidenceDocument(asylumCase));
    }

    @Test
    void should_get_addendum_documents_when_more_than_one_exists() {
        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        addendumDocuments.add(addendumTwo);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        assertEquals(addendumDocuments, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(2, AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase).size());
    }

    @Test
    void should_return_empty_list_when_no_addendum_evidence_documents_present() {
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.empty());

        assertEquals(Collections.emptyList(), AsylumCaseUtils.getAddendumEvidenceDocuments(asylumCase));
        assertEquals(Optional.empty(), AsylumCaseUtils.getLatestAddendumEvidenceDocument(asylumCase));
    }

    @Test
    public void testIsLegalRepEjp() {

        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepEmailEjp));
        assertTrue(isLegalRepEjp(asylumCase));
    }

    @Test
    public void testIsNotLegalRepEjp() {
        Mockito.when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.empty());
        assertFalse(isLegalRepEjp(asylumCase));
    }

    @Test
    void should_throw_when_applies_for_costs_are_not_present() {
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> AsylumCaseUtils.retrieveLatestApplyForCosts(asylumCase))
            .hasMessage("Applies for costs are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_retrieve_latest_created_apply_for_costs() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), AsylumCaseUtils.retrieveLatestApplyForCosts(asylumCase));
    }

    @Test
    void should_retrieve_application_by_id() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertEquals(applyForCostsList.get(0).getValue(), AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));
    }

    @Test
    void should_throw_if_applies_are_not_present() {
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
            .hasMessage("appliesForCost are not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_application_is_not_found_by_id() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Home office", "Legal representative", applyForCostsCreationDate)),
            new IdValue<>("1", new ApplyForCosts("Unreasonable costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023"), List.of(new Value("3", "Costs 3, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST))
            .hasMessage("Apply for costs with id 3 not found")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_check_if_logged_user_is_home_office() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Home office", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertTrue(AsylumCaseUtils.isLoggedUserIsHomeOffice(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)));
    }

    @Test
    void should_throw_if_logged_user_is__of_incorrect_type() {
        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Tribunal", "Wasted costs"))
        );
        DynamicList respondsToCostsList = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(respondsToCostsList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> AsylumCaseUtils.isLoggedUserIsHomeOffice(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_build_proper_pair_with_applicant_and_respondent() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Legal representative", "Home office", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        ImmutablePair<String, String> getApplicantAndRespondent = getApplicantAndRespondent(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST));

        assertEquals("Legal representative", getApplicantAndRespondent.getRight());
        assertEquals("Home office", getApplicantAndRespondent.getLeft());
    }

    @Test
    void should_throw_if_applicant_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Tribunal", "Case officer", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct applicant type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_respondent_type_is_not_correct() {
        DynamicList selectedValue = new DynamicList(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023"), List.of(new Value("2", "Costs 2, Wasted costs, 24 Nov 2023")));

        List<IdValue<ApplyForCosts>> applyForCostsList = List.of(
            new IdValue<>("2", new ApplyForCosts("Wasted costs", "Case officer", "Tribunal", applyForCostsCreationDate))
        );

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(selectedValue));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(applyForCostsList));

        assertThatThrownBy(() -> getApplicantAndRespondent(asylumCase, testFunc -> AsylumCaseUtils.getApplicationById(asylumCase, RESPOND_TO_COSTS_LIST)))
            .hasMessage("Correct respondent type is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void generateAppellantPin_return_existing_pin_if_present() {
        PinInPostDetails existingPin = PinInPostDetails.builder()
            .accessCode("123")
            .expiryDate(LocalDate.now().plusDays(30).toString())
            .pinUsed(YesOrNo.NO)
            .build();

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_PIN_IN_POST, PinInPostDetails.class))
            .thenReturn(Optional.of(existingPin));

        assertEquals(existingPin, generateAppellantPinIfNotPresent(asylumCase));
    }

    @Test
    void generateAppellantPin_generate_new_pin_if_not_present() {
        generatorMockedStatic.when(() -> AccessCodeGenerator.generateAccessCode())
            .thenReturn(generatedCode);

        PinInPostDetails generatedPinDetails = generateAppellantPinIfNotPresent(asylumCaseSpy);

        assertEquals(generatedCode, generatedPinDetails.getAccessCode());
        assertEquals(LocalDate.now().plusDays(30).toString(), generatedPinDetails.getExpiryDate());
        assertEquals(NO, generatedPinDetails.getPinUsed());
    }
}
