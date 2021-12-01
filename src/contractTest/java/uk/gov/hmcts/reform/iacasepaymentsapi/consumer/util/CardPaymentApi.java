package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.util;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacasepaymentsapi.consumer.entities.CardPaymentRequest;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;

@FeignClient(name = "payment-api", url = "${payment.api.url}")
public interface CardPaymentApi {

    @PostMapping(value = "/card-payments", consumes = "application/json")
    PaymentResponse cardPaymentRequest(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CardPaymentRequest request
    );

    @GetMapping(value = "/card-payments/{ref}")
    PaymentDto getPayment(@RequestHeader(AUTHORIZATION) String authorization,
                          @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
                          @PathVariable("ref") String paymentReference
    );
}

