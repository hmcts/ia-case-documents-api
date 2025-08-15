package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

public interface SystemUserProvider {

    String getSystemUserId(String userToken);
}
