package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.serialization;

public interface Deserializer<T> {

    T deserialize(String source);
}
