package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security;

public interface SystemUserProvider {

    String getSystemUserId(String userToken);
}
