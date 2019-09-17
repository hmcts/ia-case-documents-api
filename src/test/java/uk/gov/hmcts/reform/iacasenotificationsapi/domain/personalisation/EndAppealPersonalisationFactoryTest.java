package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@RunWith(MockitoJUnitRunner.class)
public class EndAppealPersonalisationFactoryTest {

    @Mock private AsylumCase asylumCase;

    final String appealReferenceNumber = "PA/001/2018";
    final String homeOfficeReferenceNumber = "SOMETHING";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";


    final String endAppealOutcome = "Send your evidence";
    final String endAppealDate = "2018-08-13";
    final String endAppealExpectedDate = "13 Aug 2018";

    final String endAppealOutcomeReason = "some reason";
    final String endAppealApproverType = "Case Worker";

    private EndAppealPersonalisationFactory personalisationFactory;

    @Before
    public void setUp() {

        personalisationFactory = new EndAppealPersonalisationFactory();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.of(endAppealOutcome));
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.of(endAppealOutcomeReason));
        when(asylumCase.read(END_APPEAL_APPROVER_TYPE, String.class)).thenReturn(Optional.of(endAppealApproverType));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(endAppealDate));
    }

    @Test
    public void should_create_personalisation_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", "")
                .put("homeOfficeReferenceNumber", "")
                .put("appellantGivenNames", "")
                .put("appellantFamilyName", "")
                .put("outcomeOfAppeal", "")
                .put("reasonsOfOutcome", "No reason")
                .put("endAppealDate", "")
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_OUTCOME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_OUTCOME_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation = personalisationFactory.create(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }

    @Test
    public void should_create_personalisation_for_listed_case_notification() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("outcomeOfAppeal", endAppealOutcome)
                .put("reasonsOfOutcome", endAppealOutcomeReason)
                .put("endAppealDate", endAppealExpectedDate)
                .build();

        Map<String, String> actualPersonalisation = personalisationFactory.create(asylumCase);

        assertThat(actualPersonalisation).isEqualToComparingOnlyGivenFields(expectedPersonalisation);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> personalisationFactory.create(null))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}