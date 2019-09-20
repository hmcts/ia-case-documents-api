package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.util.HashMap;
import java.util.Optional;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;

public class AsylumCase extends HashMap<String, Object> implements CaseData {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AsylumCase() {
        objectMapper.registerModule(new Jdk8Module());
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

}
