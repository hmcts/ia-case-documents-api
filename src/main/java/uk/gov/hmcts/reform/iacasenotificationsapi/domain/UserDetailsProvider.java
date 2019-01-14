package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;

public interface UserDetailsProvider {

    UserDetails getUserDetails();
}
