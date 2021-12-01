package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.entities;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;

public class CardPaymentRequest extends PaymentDto {

    private String siteId;

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
