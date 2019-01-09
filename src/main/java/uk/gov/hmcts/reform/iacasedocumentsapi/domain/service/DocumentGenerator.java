package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.Map;
import org.springframework.core.io.Resource;

public interface DocumentGenerator {

    Resource generate(
        String fileName,
        String fileExtension,
        String templateName,
        Map<String, Object> templateFieldValues
    );
}
