package uk.gov.hmcts.reform.iacasedocumentsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCircuitBreaker
@EnableFeignClients(basePackages =
    {
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.iacasedocumentsapi",
    })
@ComponentScan(
        basePackages = {"uk.gov.hmcts.reform.ccd.document.am"},
        lazyInit = true
)

@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

