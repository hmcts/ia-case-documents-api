package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class UploadAppealResponseMaintainedDecisionLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private final String templateName = "HO-Decision-Maintained.docx";
    @Mock private DateProvider dateProvider;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private DueDateService dueDateService;
    private final Parties directionParties = Parties.RESPONDENT;
    private final IdValue<Direction> hoReviewDirection = new IdValue<>(
            "1",
            new Direction(
                    "HO Review Request Direction",
                    directionParties,
                    "2023-08-16",
                    "2023-06-02",
                    DirectionTag.RESPONDENT_REVIEW,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "95e90870-2429-4660-b9c2-4111aff37304",
                    "someDirectionType"
            )
    );

    private final IdValue<Direction> requestCaseBuildingDirection = new IdValue<>(
            "1",
            new Direction(
                    "Case Building Request Direction",
                    directionParties,
                    "2023-08-02",
                    "2023-06-05",
                    DirectionTag.REQUEST_CASE_BUILDING,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    "95e90870-2429-4660-b9c2-4111aff45604",
                    "someDirectionType"
            )
    );

    private UploadAppealResponseMaintainedDecisionLetterTemplate uploadAppealResponseMaintainedDecisionLetterTemplate;
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";



    @BeforeEach
    void setUp() {
        uploadAppealResponseMaintainedDecisionLetterTemplate =
                new UploadAppealResponseMaintainedDecisionLetterTemplate(
                        templateName,
                        dateProvider,
                        customerServicesProvider,
                        dueDateService
                );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, uploadAppealResponseMaintainedDecisionLetterTemplate.getName());
    }

    void dataSetUp() {
        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(hoReviewDirection);
        directionList.add(requestCaseBuildingDirection);
        final String appealSubmissionDate = "2023-05-23";
        final String saHearingDate = "2023-06-13";
        final ZonedDateTime zonedDate = LocalDate.parse(appealSubmissionDate).atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedSaHEaringDate = LocalDate.parse(saHearingDate).atStartOfDay(ZoneOffset.UTC);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)).thenReturn(Optional.of(appealSubmissionDate));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2023-08-22T09:00:00.000"));
        when(dateProvider.now()).thenReturn(LocalDate.parse("2023-06-27"));
        when(asylumCase.read(DIRECTIONS)).thenReturn(Optional.of(directionList));
        when(dueDateService.calculateWorkingDaysDueDate(zonedDate, 16)).thenReturn(zonedSaHEaringDate);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = uploadAppealResponseMaintainedDecisionLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(12, templateFieldValues.size());
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals("2 Aug 2023", templateFieldValues.get("caseBuildingDueDate"));
        assertEquals("13062023", templateFieldValues.get("suitabilityAssessmentHearingDate"));
        assertEquals("22082023", templateFieldValues.get("hearingDate"));
        assertEquals("27 Jun 2023", templateFieldValues.get("dateLetterSent"));
    }
}
