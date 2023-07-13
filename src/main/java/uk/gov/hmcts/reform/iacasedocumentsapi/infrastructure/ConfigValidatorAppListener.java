package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
@Setter
public class ConfigValidatorAppListener implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${ia.config.validator.secret}")
    private String iaConfigValidatorSecret;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        breakOnMissingIaConfigValidatorSecret();
    }

    void breakOnMissingIaConfigValidatorSecret() {
        if (StringUtils.isBlank(iaConfigValidatorSecret)) {
            log.info("IA Config Validator Secret Value: {}", iaConfigValidatorSecret);
            throw new IllegalArgumentException("ia.config.validator.secret is null or empty."
                + " This is not allowed and it will break production. This is a secret value stored in a vault"
                + " (unless running locally). Check application.yaml for further information.");

        }
    }
}
