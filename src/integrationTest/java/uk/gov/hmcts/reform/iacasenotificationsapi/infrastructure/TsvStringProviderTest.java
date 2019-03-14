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
            Optional.of("Manchester"),
            tsvStringProvider.get("hearingCentre", "manchester")
        );

        assertEquals(
            Optional.of("Taylor House"),
            tsvStringProvider.get("hearingCentre", "taylorHouse")
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
