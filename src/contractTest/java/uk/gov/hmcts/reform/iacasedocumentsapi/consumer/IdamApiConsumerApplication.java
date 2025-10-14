package uk.gov.hmcts.reform.iacasedocumentsapi.consumer;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(clients = {
    IdamApi.class
})
public class IdamApiConsumerApplication {
}
