package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RespondentDirectionPersonalisationFactoryTest {

    @Mock private StringProvider stringProvider;

    @Mock private AsylumCase asylumCase;
    @Mock private Direction direction;

    final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    final String hearingCentreForDisplay = "Taylor House";
    final String homeOfficeReferenceNumber = "SOMETHING";
    final String appealReferenceNumber = "PA/001/2018";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";
    final String directionExplanation = "Send your evidence";
    final String directionDateDue = "2018-12-31";
    final String directionDateDueFormatted = "31 Dec 2018";

    final Map<String, String> expectedPersonalisation =
        ImmutableMap
            .<String, String>builder()
            .put("HearingCentre", hearingCentreForDisplay)
            .put("Appeal Ref Number", appealReferenceNumber)
            .put("HORef", homeOfficeReferenceNumber)
            .put("Given names", appellantGivenNames)
            .put("Family name", appellantFamilyName)
            .put("Explanation", directionExplanation)
            .put("due date", directionDateDueFormatted)
            .build();

    private RespondentDirectionPersonalisationFactory respondentDirectionPersonalisationFactory;

    @Before
    public void setUp() {

        respondentDirectionPersonalisationFactory =
            new RespondentDirectionPersonalisationFactory(
                stringProvider
            );

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(direction.getExplanation()).thenReturn(directionExplanation);
        when(direction.getDateDue()).thenReturn(directionDateDue);

        when(stringProvider.get("hearingCentre", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreForDisplay));
    }

    @Test
    public void should_create_personalisation_from_case() {

        Map<String, String> actualPersonalisation =
            respondentDirectionPersonalisationFactory.create(asylumCase, direction);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_create_personalisation_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("HearingCentre", hearingCentreForDisplay)
                .put("Appeal Ref Number", "")
                .put("HORef", "")
                .put("Given names", "")
                .put("Family name", "")
                .put("Explanation", directionExplanation)
                .put("due date", directionDateDueFormatted)
                .build();

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation =
            respondentDirectionPersonalisationFactory.create(asylumCase, direction);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_use_correct_hearing_centre_personalisation() {

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));

        when(stringProvider.get("hearingCentre", "manchester")).thenReturn(Optional.of("Manchester Hearing Centre"));

        Map<String, String> actualPersonalisation =
            respondentDirectionPersonalisationFactory.create(asylumCase, direction);

        verify(stringProvider, times(1)).get("hearingCentre", "manchester");

        assertEquals("Manchester Hearing Centre", actualPersonalisation.get("HearingCentre"));
    }

    @Test
    public void should_throw_when_hearing_centre_not_present() {

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentDirectionPersonalisationFactory.create(asylumCase, direction))
            .hasMessage("hearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_hearing_centre_display_string_not_present() {

        when(stringProvider.get("hearingCentre", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentDirectionPersonalisationFactory.create(asylumCase, direction))
            .hasMessage("hearingCentre display string is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> respondentDirectionPersonalisationFactory.create(null, direction))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondentDirectionPersonalisationFactory.create(asylumCase, null))
            .hasMessage("direction must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
