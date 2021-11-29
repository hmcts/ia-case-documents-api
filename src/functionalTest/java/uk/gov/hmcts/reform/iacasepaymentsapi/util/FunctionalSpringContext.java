package uk.gov.hmcts.reform.iacasepaymentsapi.util;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.iacasepaymentsapi.Application;

@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients",
    "uk.gov.hmcts.reform.iacasepaymentsapi.util.clients"
})
@SuppressWarnings("HideUtilityClassConstructor")
public class FunctionalSpringContext {

    public static void main(final String[] args) {

        new SpringApplicationBuilder(Application.class)
            .web(WebApplicationType.NONE)
            .run(args);
    }
}
