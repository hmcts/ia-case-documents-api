package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.serialization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization.Deserializer;

@Component
public class AsylumCaseCallbackDeserializer implements Deserializer<Callback<AsylumCase>> {

    private final ObjectMapper mapper;

    public AsylumCaseCallbackDeserializer(
        ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public Callback<AsylumCase> deserialize(
        String source
    ) {
        try {

            return mapper.readValue(
                source,
                new TypeReference<Callback<AsylumCase>>() {
                }
            );

        } catch (IOException e) {
            throw new IllegalArgumentException("Could not deserialize callback", e);
        }
    }
}
