package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@ExtendWith(MockitoExtension.class)
public class AppealServiceTest {

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
    void isRemoteHearing() {
        AppealService appealService = new AppealService();
        assertFalse(appealService.isAppealListed(asylumCase));

        HearingCentre hearingCentre = HearingCentre.REMOTE_HEARING;
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentre));
        assertTrue(appealService.isRemoteHearing(asylumCase));
    }

}
