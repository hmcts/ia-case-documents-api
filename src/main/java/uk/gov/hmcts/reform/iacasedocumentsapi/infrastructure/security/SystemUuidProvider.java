package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import java.util.UUID;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UuidProvider;

@Service
public class SystemUuidProvider implements UuidProvider {

    @Override
    public UUID randomUuid() {
        return UUID.randomUUID();
    }
}
