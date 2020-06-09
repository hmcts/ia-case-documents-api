package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentResponseTest {

    private String reference = "RC-1590-6786-1063-9996";
    private Date dateCreated = new Date();
    private String status = "Success";
    private String paymentGroupReference = "2020-1590678609071";
    private List<StatusHistories> statusHistories =
        Arrays.asList(new StatusHistories("Success", null, null));

    private PaymentResponse paymentResponse;

    @BeforeEach
    public void setUp() {

        paymentResponse = new PaymentResponse(
            reference,
            dateCreated,
            status,
            paymentGroupReference,
            statusHistories);
    }

    @Test
    void should_hold_onto_values() {

        assertEquals(paymentResponse.getReference(), reference);
        assertEquals(paymentResponse.getDateCreated(), dateCreated);
        assertEquals(paymentResponse.getStatus(), status);
        assertEquals(paymentResponse.getPaymentGroupReference(), paymentGroupReference);
    }

    @Test
    void should_throw_required_field_exception() {

        paymentResponse = new PaymentResponse(null, null,
            null, null, null);

        assertThatThrownBy(paymentResponse::getReference)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(paymentResponse::getDateCreated)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(paymentResponse::getStatus)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(paymentResponse::getPaymentGroupReference)
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(paymentResponse::getStatusHistories)
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
