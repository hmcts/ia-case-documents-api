package uk.gov.hmcts.reform.iacasedocumentsapi.consumer.payment;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.consumer.util.CardPaymentApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.PaymentApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    PaymentApi.class,
    CardPaymentApi.class
})
public class PaymentConsumerApplication {
    @MockBean
    RestTemplate restTemplate;
}
