package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
public class ApplicationContextProviderTest {

    @Mock
    ApplicationContext applicationContext;

    @Test
    void shouldReturnApplicationContext() {
        ApplicationContextProvider applicationContextProvider = new ApplicationContextProvider();
        applicationContextProvider.setApplicationContext(applicationContext);

        assertNotNull(ApplicationContextProvider.getApplicationContext());
    }

}
