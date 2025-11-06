package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PrisonEmailMappingServiceTest {

    private PrisonEmailMappingService prisonEmailMappingService;
    private final String validJsonData = """
            {
              "prisonEmailMappings": {
                "Addiewell": "addiewell@example.com",
                "Belmarsh": "belmarsh@example.com",
                "Askham Grange": "askham-grange@example.com"
              }
            }
            """;

    private final String emptyJsonData = """
            {
              "prisonEmailMappings": {}
            }
            """;

    @BeforeEach
    void setUp() {
        prisonEmailMappingService = new PrisonEmailMappingService(validJsonData);
        prisonEmailMappingService.init();
    }

    @Test
    void should_return_email_for_prison_when_mapping_exists() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("Addiewell");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("addiewell@example.com");
    }

    @Test
    void should_return_empty_when_prison_not_found() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("NonexistentPrison");

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_prison_name_is_null() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail(null);

        assertThat(result).isEmpty();
    }

    @Test
    void should_return_empty_when_prison_name_is_empty() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("");

        assertThat(result).isEmpty();
    }

    @Test
    void should_handle_empty_configuration() {
        PrisonEmailMappingService emptyService = new PrisonEmailMappingService(emptyJsonData);
        emptyService.init();

        Optional<String> result = emptyService.getPrisonEmail("Addiewell");

        assertThat(result).isEmpty();
    }

    @Test
    void should_handle_no_configuration() {
        PrisonEmailMappingService noConfigService = new PrisonEmailMappingService("");
        noConfigService.init();

        Optional<String> result = noConfigService.getPrisonEmail("Addiewell");

        assertThat(result).isEmpty();
    }

    @Test
    void should_handle_invalid_json() {
        PrisonEmailMappingService invalidJsonService = new PrisonEmailMappingService("invalid json");
        invalidJsonService.init();

        Optional<String> result = invalidJsonService.getPrisonEmail("Addiewell");

        assertThat(result).isEmpty();
    }

    @Test
    void should_check_if_prison_is_supported() {
        assertThat(prisonEmailMappingService.isPrisonSupported("Addiewell")).isTrue();
        assertThat(prisonEmailMappingService.isPrisonSupported("NonexistentPrison")).isFalse();
    }

    @Test
    void should_return_all_prison_emails() {
        Map<String, String> allEmails = prisonEmailMappingService.getAllPrisonEmails();

        assertThat(allEmails).containsEntry("Addiewell", "addiewell@example.com");
        assertThat(allEmails).containsEntry("Belmarsh", "belmarsh@example.com");
        assertThat(allEmails).containsEntry("Askham Grange", "askham-grange@example.com");
        assertThat(allEmails).hasSize(3);
    }

    @Test
    void should_return_supported_prisons() {
        Set<String> supportedPrisons = prisonEmailMappingService.getSupportedPrisons();

        assertThat(supportedPrisons).contains("Addiewell", "Belmarsh", "Askham Grange");
        assertThat(supportedPrisons).hasSize(3);
    }

    @Test
    void should_handle_prison_names_with_spaces() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("Askham Grange");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("askham-grange@example.com");
    }

    @Test
    void should_trim_whitespace_from_prison_name() {
        Optional<String> result = prisonEmailMappingService.getPrisonEmail("  Addiewell  ");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("addiewell@example.com");
    }

    @Test
    void should_handle_malformed_json_structure() {
        String malformedJson = """
                {
                  "wrongKey": {
                    "Addiewell": "addiewell@example.com"
                  }
                }
                """;

        PrisonEmailMappingService malformedService = new PrisonEmailMappingService(malformedJson);
        malformedService.init();

        Optional<String> result = malformedService.getPrisonEmail("Addiewell");

        assertThat(result).isEmpty();
    }
}