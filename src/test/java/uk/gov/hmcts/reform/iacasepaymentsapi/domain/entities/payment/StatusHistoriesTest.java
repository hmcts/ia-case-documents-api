package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StatusHistoriesTest {

    private String status = "Failed";
    private String errorCode = "Account has been deleted";
    private String errorMessage = "Some message";
    private String dateCreated = "2020-05-28T14:04:06.048+0000";
    private String dateUpdated = "2020-05-28T14:04:06.048+0000";

    private StatusHistories statusHistories;

    @BeforeEach
    public void setUp() {

        statusHistories = new StatusHistories(status, errorCode, errorMessage, dateCreated, dateUpdated);
    }

    @Test
    void should_hold_onto_values() {

        assertEquals(statusHistories.getStatus(), status);
        assertEquals(statusHistories.getErrorCode(), errorCode);
        assertEquals(statusHistories.getErrorMessage(), errorMessage);
        assertEquals(statusHistories.getDateCreated(), dateCreated);
        assertEquals(statusHistories.getDateUpdated(), dateUpdated);
    }

    @Test
    void should_throw_required_field_exception() {

        statusHistories = new StatusHistories(null, null, null, null, null);

        assertThatThrownBy(statusHistories::getStatus)
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
