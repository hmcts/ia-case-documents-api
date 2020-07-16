package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@RunWith(MockitoJUnitRunner.class)
public class AppealServiceTest {

    @Mock
    private AsylumCase asylumCase;

    @Test
    public void isAppealListed() {
        AppealService appealService = new AppealService();
        assertFalse(appealService.isAppealListed(asylumCase));

        HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentre));
        assertTrue(appealService.isAppealListed(asylumCase));
    }

}