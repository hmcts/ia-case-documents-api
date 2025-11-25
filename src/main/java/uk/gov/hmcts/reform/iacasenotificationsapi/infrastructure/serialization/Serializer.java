package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
