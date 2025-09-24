package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
