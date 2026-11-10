package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.util.HashMap;
import java.util.Optional;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;

public class BailCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new JsonMapper();

    public BailCase() {
    }

    public <T> Optional<T> read(BailCaseFieldDefinition extractor, Class<T> type) {
        return this.read(extractor);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(BailCaseFieldDefinition extractor) {

        Object o = this.get(extractor.value());

        if (o == null) {
            return Optional.empty();
        }

        Object value = objectMapper.convertValue(o, extractor.getTypeReference());

        return Optional.of((T) value);
    }

    public <T> void write(BailCaseFieldDefinition extractor, T value) {
        this.put(extractor.value(), value);
    }

    public void clear(BailCaseFieldDefinition extractor) {
        this.put(extractor.value(), null);
    }
}
