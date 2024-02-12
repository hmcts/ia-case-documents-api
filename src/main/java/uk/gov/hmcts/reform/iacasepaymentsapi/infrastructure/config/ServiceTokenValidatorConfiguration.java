package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;


@Slf4j
@Configuration
public class ServiceTokenValidatorConfiguration {

    @Bean
    public AuthTokenValidator tokenValidator(ServiceAuthorisationApi s2sApi) {
        return new ServiceAuthTokenValidator(s2sApi);
    }

}
