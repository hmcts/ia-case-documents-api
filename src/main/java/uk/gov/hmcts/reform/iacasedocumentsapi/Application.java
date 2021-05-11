package uk.gov.hmcts.reform.iacasedocumentsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableHystrix
@EnableFeignClients(basePackages =
    {
    "uk.gov.hmcts.reform.auth",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.iacasedocumentsapi",
    "uk.gov.hmcts.reform.document"
    })
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
public class Application {

    public static void main(final String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

