package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
