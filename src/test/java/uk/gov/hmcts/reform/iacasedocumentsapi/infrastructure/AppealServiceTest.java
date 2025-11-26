package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;

@ExtendWith(MockitoExtension.class)
class AppealServiceTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    void isAppealListed() {
        AppealService appealService = new AppealService();
        assertFalse(appealService.isAppealListed(asylumCase));

        HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentre));
        assertTrue(appealService.isAppealListed(asylumCase));
    }

    @Test
    void isAppellantInPersonJourney() {
        AppealService appealService = new AppealService();
        assertFalse(appealService.isAppellantInPersonJourney(asylumCase));

        JourneyType journeyType = JourneyType.AIP;
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class))
            .thenReturn(Optional.of(journeyType));
        assertTrue(appealService.isAppellantInPersonJourney(asylumCase));
    }

}
