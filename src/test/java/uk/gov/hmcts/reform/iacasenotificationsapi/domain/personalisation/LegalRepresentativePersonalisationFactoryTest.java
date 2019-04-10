package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class LegalRepresentativePersonalisationFactoryTest {

    @Mock private AsylumCase asylumCase;
    @Mock private Direction direction;

    final String iaCcdFrontendUrl = "http://www.ccd.example.com";
    final String appealReferenceNumber = "PA/001/2018";
    final String legalRepReferenceNumber = "SOMETHING";
    final String appellantGivenNames = "Jane";
    final String appellantFamilyName = "Doe";
    final String directionExplanation = "Send your evidence";
    final String directionDateDue = "2018-12-31";
    final String directionDateDueFormatted = "31 Dec 2018";

    final Map<String, String> expectedPersonalisation =
        ImmutableMap
            .<String, String>builder()
            .put("Appeal Ref Number", appealReferenceNumber)
            .put("LR reference", legalRepReferenceNumber)
            .put("Given names", appellantGivenNames)
            .put("Family name", appellantFamilyName)
            .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
            .put("Explanation", directionExplanation)
            .put("due date", directionDateDueFormatted)
            .build();

    private LegalRepresentativePersonalisationFactory legalRepresentativePersonalisationFactory;

    @Before
    public void setUp() {

        legalRepresentativePersonalisationFactory =
            new LegalRepresentativePersonalisationFactory(
                iaCcdFrontendUrl
            );

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));

        when(direction.getExplanation()).thenReturn(directionExplanation);
        when(direction.getDateDue()).thenReturn(directionDateDue);
    }

    @Test
    public void should_create_personalisation_from_case() {

        Map<String, String> actualPersonalisation =
            legalRepresentativePersonalisationFactory.create(asylumCase, direction);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_create_personalisation_using_defaults_where_available() {

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", "")
                .put("LR reference", "")
                .put("Given names", "")
                .put("Family name", "")
                .put("Hyperlink to user’s case list", iaCcdFrontendUrl)
                .put("Explanation", directionExplanation)
                .put("due date", directionDateDueFormatted)
                .build();

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation =
            legalRepresentativePersonalisationFactory.create(asylumCase, direction);

        assertEquals(expectedPersonalisation, actualPersonalisation);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.create(null, direction))
            .hasMessage("asylumCase must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativePersonalisationFactory.create(asylumCase, null))
            .hasMessage("direction must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
