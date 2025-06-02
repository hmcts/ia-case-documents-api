package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
