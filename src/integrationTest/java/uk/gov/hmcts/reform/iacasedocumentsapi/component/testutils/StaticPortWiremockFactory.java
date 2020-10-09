package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import ru.lanwen.wiremock.config.WiremockConfigFactory;

public class StaticPortWiremockFactory implements WiremockConfigFactory {

    public static final int WIREMOCK_PORT = 8992;

    @Override
    public WireMockConfiguration create() {
        return options().port(WIREMOCK_PORT).notifier(new Slf4jNotifier(true));
    }

}
