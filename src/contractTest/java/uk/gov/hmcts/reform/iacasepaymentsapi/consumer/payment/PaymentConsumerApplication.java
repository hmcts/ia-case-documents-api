package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.payment;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.PaymentApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    PaymentApi.class
})
public class PaymentConsumerApplication {

}
