package uk.gov.hmcts.reform.iacasedocumentsapi.consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.IdamApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    IdamApi.class
})
public class IdamApiConsumerApplication {
}
