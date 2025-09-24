package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
