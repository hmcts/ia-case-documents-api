package uk.gov.hmcts.reform.iacasedocumentsapi.consumer.entities;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentDto;

@Setter
@Getter
public class CardPaymentRequest extends PaymentDto {

    private String siteId;

}
