package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetentionFacilityNameFinderTest {

    @Mock
    private StringProvider stringProvider;

    private DetentionFacilityNameFinder finder;

    @BeforeEach
    void setUp() {
        finder = new DetentionFacilityNameFinder(stringProvider);
    }

    @Test
    void should_return_value_from_prisonName_mapping() {
        String facilityInput = "Birmingham";
        String expected = "HM Prison Birmingham";

        when(stringProvider.get("prisonName", facilityInput)).thenReturn(Optional.of(expected));

        String result = finder.getDetentionFacility(facilityInput);

        assertEquals(expected, result);
        verify(stringProvider).get("prisonName", facilityInput);
        verify(stringProvider, never()).get("ircName", facilityInput);
    }

    @Test
    void should_return_value_from_ircName_mapping_when_prisonName_is_missing() {
        String facilityInput = "Harmondsworth";
        String expected = "Harmondsworth";

        when(stringProvider.get("prisonName", facilityInput)).thenReturn(Optional.empty());
        when(stringProvider.get("ircName", facilityInput)).thenReturn(Optional.of(expected));

        String result = finder.getDetentionFacility(facilityInput);

        assertEquals(expected, result);
        verify(stringProvider).get("prisonName", facilityInput);
        verify(stringProvider).get("ircName", facilityInput);
    }

    @Test
    void should_fallback_to_original_name_when_no_mapping_exists() {
        String facilityInput = "UnknownFacility";

        when(stringProvider.get("prisonName", facilityInput)).thenReturn(Optional.empty());
        when(stringProvider.get("ircName", facilityInput)).thenReturn(Optional.empty());

        String result = finder.getDetentionFacility(facilityInput);

        assertEquals(facilityInput, result);
        verify(stringProvider).get("prisonName", facilityInput);
        verify(stringProvider).get("ircName", facilityInput);
    }
}
