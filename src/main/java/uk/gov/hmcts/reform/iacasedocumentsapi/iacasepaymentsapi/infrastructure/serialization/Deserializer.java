package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
