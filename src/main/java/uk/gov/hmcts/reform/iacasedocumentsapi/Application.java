package uk.gov.hmcts.reform.iacasedocumentsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages =
    {
    "uk.gov.hmcts.reform.auth",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.iacasedocumentsapi",
    "uk.gov.hmcts.reform.ccd.document.am.feign"
    })

public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

