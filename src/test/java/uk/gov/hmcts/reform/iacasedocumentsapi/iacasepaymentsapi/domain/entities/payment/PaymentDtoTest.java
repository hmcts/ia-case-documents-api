package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.fee.FeeDto;

class PaymentDtoTest {

    private final BigDecimal amount = new BigDecimal("140");

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(PaymentDto.class)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        String status = "Success";
        String externalReference = "8saf7t8kav53mmubrff738nord";
        String externalProvider = "gov pay";
        String method = "card";
        String channel = "online";
        String ccdCaseNumber = "1627506765384547";
        String currency = "GBP";
        String reference = "RC-1627-5070-9329-7815";
        String description = "Appeal with hearing";
        String id = "id";
        PaymentDto paymentDto = PaymentDto.builder()
            .id(id)
            .amount(amount)
            .description(description)
            .reference(reference)
            .service("Immigration and Asylum Appeals")
            .dateCreated(new Date())
            .dateUpdated(new Date())
            .currency(currency)
            .ccdCaseNumber(ccdCaseNumber)
            .channel(channel)
            .method(method)
            .externalProvider(externalProvider)
            .externalReference(externalReference)
            .status(status)
            .fees(getFees())
            .links(getLinksDto())
            .build();

        assertEquals(id, paymentDto.getId());
        assertEquals(amount, paymentDto.getAmount());
        assertEquals(reference, paymentDto.getReference());
        assertEquals("Immigration and Asylum Appeals", paymentDto.getService());
        assertNotNull(paymentDto.getDateCreated());
        assertNotNull(paymentDto.getDateUpdated());
        assertEquals("GBP", paymentDto.getCurrency());
        assertEquals(method, paymentDto.getMethod());
        assertEquals(externalProvider, paymentDto.getExternalProvider());
        assertEquals(externalReference, paymentDto.getExternalReference());
        assertEquals(status, paymentDto.getStatus());
        assertEquals(getFees(), paymentDto.getFees());
        assertEquals(getLinksDto(), paymentDto.getLinks());
    }

    private List<FeeDto> getFees() {

        return Collections.singletonList(
            FeeDto.builder()
                .code("FEE0001")
                .version("1")
                .volume(1)
                .calculatedAmount(new BigDecimal("140"))
                .memoLine("memoLine")
                .ccdCaseNumber("1234")
                .reference("RC-1627-5070-9329-7815")
                .build()
        );
    }

    private PaymentDto.LinksDto getLinksDto() {

        PaymentDto.LinkDto linkDto = new PaymentDto.LinkDto("href", "post");
        PaymentDto.LinksDto linksDto = new PaymentDto.LinksDto();
        linksDto.setSelf(linkDto);
        linksDto.setCancel(linkDto);
        linksDto.setNextUrl(linkDto);
        return linksDto;
    }
}
