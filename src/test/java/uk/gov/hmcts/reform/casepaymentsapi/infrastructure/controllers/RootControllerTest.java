package uk.gov.hmcts.reform.casepaymentsapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@ExtendWith(MockitoExtension.class)
public class RootControllerTest {

    private RootController rootController;

    @BeforeEach
    public void setup() {
        rootController = new RootController();
    }

    @Test
    public void should_get_correct_welcome_response() {

        final ResponseEntity<String> response = rootController.welcome();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
            .isEqualTo("Welcome to ia-case-payments-api");

    }

}
