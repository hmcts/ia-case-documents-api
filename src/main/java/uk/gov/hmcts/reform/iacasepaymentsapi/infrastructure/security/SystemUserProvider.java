package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

public interface SystemUserProvider {

    String getSystemUserId(String userToken);
}
