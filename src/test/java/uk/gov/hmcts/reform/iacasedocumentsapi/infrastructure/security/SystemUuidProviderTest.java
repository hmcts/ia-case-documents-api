package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SystemUuidProviderTest {

    private final String uuidRegex = "([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}){1}";

    private final SystemUuidProvider systemUuidProvider = new SystemUuidProvider();

    @Test
    public void should_return_random_uuid() {
        assertThat(systemUuidProvider.randomUuid().toString()).matches(uuidRegex);
    }
}