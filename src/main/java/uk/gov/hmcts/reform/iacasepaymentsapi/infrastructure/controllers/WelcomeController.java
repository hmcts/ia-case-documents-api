package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {

    @ApiOperation("Welcome message for the Immigration & Asylum Case Payments Service")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Welcome message",
            response = String.class
        )
    })
    @GetMapping("/")
    public ResponseEntity<String> welcome() {
        return ok("Welcome to Case Payment Service");
    }
}
