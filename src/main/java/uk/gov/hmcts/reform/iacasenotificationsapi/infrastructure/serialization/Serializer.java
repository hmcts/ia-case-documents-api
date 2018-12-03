package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization;

public interface Serializer<T> {

    String serialize(T data);
}
