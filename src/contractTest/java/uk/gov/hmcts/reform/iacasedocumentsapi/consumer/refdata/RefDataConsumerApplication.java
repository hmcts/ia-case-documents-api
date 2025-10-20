package uk.gov.hmcts.reform.iacasedocumentsapi.consumer.refdata;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.RefDataApi;

@SpringBootApplication
@EnableFeignClients(clients = {
    RefDataApi.class
})
public class RefDataConsumerApplication {
    @MockBean
    RestTemplate restTemplate;
}
