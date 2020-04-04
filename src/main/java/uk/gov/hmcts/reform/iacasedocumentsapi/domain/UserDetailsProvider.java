package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;

public interface UserDetailsProvider {

    UserDetails getUserDetails();

    UserDetails getUserDetails(String authenticationHeader);
}
