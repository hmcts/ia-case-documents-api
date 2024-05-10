package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Optional;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;

public class AsylumCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AsylumCase() {
        objectMapper.registerModule(new Jdk8Module());

        JavaTimeModule javaTimeModule = new JavaTimeModule();

        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));

        objectMapper.registerModule(javaTimeModule);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /*
    Security vulnerabilities (CVE-2019-14540, CVE-2019-16335) forced us to update jackson-core library to version: 2.10.0.pr3
    That version has change in ObjectMapper API where <T> generic should be passed to "convertValue" method instead of taking any type <?>
    Returning Object and casting to T ref is safe because we defined all types in AsylumCaseDefinition enum.
    We cannot parametrized enum with T ref and we want to keep AsylumCaseDefinition as it is, that is why we need below workaround.
    */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> read(AsylumCaseDefinition asylumCaseDefinition) {

        Object o = this.get(asylumCaseDefinition.value());

        if (o == null) {
            return Optional.empty();
        }

        Object value = objectMapper.convertValue(o, asylumCaseDefinition.getTypeReference());

        return Optional.of((T) value);
    }

    public <T> Optional<T> read(AsylumCaseDefinition asylumCaseDefinition, Class<T> type) {
        return this.read(asylumCaseDefinition);
    }

    public <T> void write(AsylumCaseDefinition asylumCaseDefinition, T value) {
        this.put(asylumCaseDefinition.value(), value);
    }

    public void clear(AsylumCaseDefinition asylumCaseDefinition) {
        this.put(asylumCaseDefinition.value(), null);
    }
}