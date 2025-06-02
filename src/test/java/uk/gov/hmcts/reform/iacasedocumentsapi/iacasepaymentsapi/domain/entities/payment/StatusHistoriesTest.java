package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusHistoriesTest {

    private final String status = "Failed";
    private final String errorCode = "Account has been deleted";
    private final String errorMessage = "Some message";
    private final String dateCreated = "2020-05-28T14:04:06.048+0000";
    private final String dateUpdated = "2020-05-28T14:04:06.048+0000";

    private StatusHistories statusHistories;

    @BeforeEach
    public void setUp() {

        statusHistories = new StatusHistories(status, errorCode, errorMessage, dateCreated, dateUpdated);
    }

    @Test
    void should_hold_onto_values() {

        Assertions.assertEquals(statusHistories.getStatus(), status);
        Assertions.assertEquals(statusHistories.getErrorCode(), errorCode);
        Assertions.assertEquals(statusHistories.getErrorMessage(), errorMessage);
        Assertions.assertEquals(statusHistories.getDateCreated(), dateCreated);
        Assertions.assertEquals(statusHistories.getDateUpdated(), dateUpdated);
    }

    @Test
    void should_throw_required_field_exception() {

        statusHistories = new StatusHistories(null, null, null, null, null);

        assertThatThrownBy(statusHistories::getStatus)
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
