package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentResponse;

@FeignClient(name = "payment-api", url = "${payment.api.url}")
public interface PaymentApi {

    @PostMapping(value = "/credit-account-payments", consumes = "application/json")
    PaymentResponse creditAccountPaymentRequest(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody CreditAccountPayment request
    );
}
