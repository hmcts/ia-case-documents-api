package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Optional;

public interface StringProvider {

    Optional<String> get(
        String group,
        String code
    );
}
