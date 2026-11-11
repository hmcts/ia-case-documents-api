package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import tools.jackson.databind.json.JsonMapper;

import static tools.jackson.databind.cfg.EnumFeature.*;

public final class JsonMapperFactory {

    public static final JsonMapper CUSTOM_MAPPER = create();

    private JsonMapperFactory() {
    }

    // .disable(FAIL_ON_NULL_FOR_PRIMITIVES) ??
    public static JsonMapper create() {
        return JsonMapper.builder()
            .enable(
                READ_ENUMS_USING_TO_STRING,
                READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE,
                WRITE_ENUMS_USING_TO_STRING)
            .changeDefaultPropertyInclusion(incl -> incl
                    .withValueInclusion(JsonInclude.Include.NON_ABSENT)
                    .withContentInclusion(JsonInclude.Include.NON_ABSENT))
            .changeDefaultVisibility(vc -> vc.withVisibility(
                    PropertyAccessor.CREATOR,
                    JsonAutoDetect.Visibility.NONE
            ))
            .build();
    }
}
