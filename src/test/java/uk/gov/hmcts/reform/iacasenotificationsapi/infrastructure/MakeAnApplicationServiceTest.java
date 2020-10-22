package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.MAKE_AN_APPLICATIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
public class MakeAnApplicationServiceTest {
    @Mock private AsylumCase asylumCase;

    @Mock private List<IdValue<MakeAnApplication>> applications;
    private MakeAnApplicationService makeAnApplicationService;
    private final String applicationType = "";

    @Before
    public void setup() {
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(applications));
        makeAnApplicationService = new MakeAnApplicationService();
    }

    @Test
    public void should_return_empty_application_type() {
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.empty());
        verify(asylumCase,never()).read(MAKE_AN_APPLICATIONS);

        assertEquals("", makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase));
    }

    @Test
    public void should_return_other_application_type() {
        assertEquals("", makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase));

        List<IdValue<MakeAnApplication>> makeAnApplications = new ArrayList<>();
        MakeAnApplication makeAnApplication = new MakeAnApplication(
                "",
                "Other",
                "",
                new ArrayList<>(),
                "",
                "",
                "",
                "");

        makeAnApplications.add(new IdValue<>("1",makeAnApplication));
        when(asylumCase.read(MAKE_AN_APPLICATIONS)).thenReturn(Optional.of(makeAnApplications));

        String retrievedMakeAnApplicationType = makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase);

        assertEquals("Other",retrievedMakeAnApplicationType);
    }

}
