package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentResponseTest {

    private final String reference = "RC-1590-6786-1063-9996";
    private final Date dateCreated = new Date();
    private final String status = "Success";
    private final String paymentGroupReference = "2020-1590678609071";
    private final List<StatusHistories> statusHistories =
        List.of(new StatusHistories("Success", null, null, null, null));

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

        Assertions.assertEquals(paymentResponse.getReference(), reference);
        Assertions.assertEquals(paymentResponse.getDateCreated(), dateCreated);
        Assertions.assertEquals(paymentResponse.getStatus(), status);
        Assertions.assertEquals(paymentResponse.getPaymentGroupReference(), paymentGroupReference);
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
