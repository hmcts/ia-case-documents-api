package uk.gov.hmcts.reform.iacasedocumentsapi.consumer.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.RefDataApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    RefDataApi.class
})
public class RefDataConsumerApplication {

}
