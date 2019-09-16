package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.Application;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("integration")
public class TsvStringProviderTest {

    @Autowired private TsvStringProvider tsvStringProvider;

    @Test
    public void should_load_strings_from_resources_and_return() {

        assertEquals(
            Optional.of("Bradford"),
            tsvStringProvider.get("hearingCentre", "bradford")
        );

        assertEquals(
            Optional.of("Manchester"),
            tsvStringProvider.get("hearingCentre", "manchester")
        );

        assertEquals(
            Optional.of("Newport"),
            tsvStringProvider.get("hearingCentre", "newport")
        );

        assertEquals(
            Optional.of("Taylor House"),
            tsvStringProvider.get("hearingCentre", "taylorHouse")
        );

        assertEquals(
            Optional.of("Bradford"),
            tsvStringProvider.get("hearingCentreName", "bradford")
        );

        assertEquals(
            Optional.of("Newport"),
            tsvStringProvider.get("hearingCentreName", "newport")
        );

        assertEquals(
            Optional.of("IAC Bradford, Phoenix House, Rushton Avenue, Thornbury, Bradford, BD3 7BH"),
            tsvStringProvider.get("hearingCentreAddress", "bradford")
        );

        assertEquals(
            Optional.of("IAC Manchester, 1st Floor Piccadilly Exchange, 2 Piccadilly Plaza, Mosley Street, Manchester, M1 4AH"),
            tsvStringProvider.get("hearingCentreAddress", "manchester")
        );

        assertEquals(
            Optional.of("IAC Newport, Columbus House, Langstone Business Park, Chepstow Road, Newport, NP18 2LX"),
            tsvStringProvider.get("hearingCentreAddress", "newport")
        );

        assertEquals(
            Optional.of("IAC Taylor House, 88 Rosebery Avenue, London, EC1R 4QU"),
            tsvStringProvider.get("hearingCentreAddress", "taylorHouse")
        );
    }

    @Test
    public void should_return_empty_optional_if_string_not_found() {

        assertEquals(
            Optional.empty(),
            tsvStringProvider.get("not", "exists")
        );
    }
}
