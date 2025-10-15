package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static nl.jqno.equalsverifier.Warning.BIGDECIMAL_EQUALITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;

class PaymentDtoTest {

    private final String id = "id";
    private final BigDecimal amount = new BigDecimal("140");
    private final String description = "Appeal with hearing";
    private final String reference = "RC-1627-5070-9329-7815";
    private final String currency = "GBP";
    private final String ccdCaseNumber = "1627506765384547";
    private final String channel = "online";
    private final String method = "card";
    private final String externalProvider = "gov pay";
    private final String externalReference = "8saf7t8kav53mmubrff738nord";
    private final String status = "Success";

    private PaymentDto paymentDto;

    @Test
    void should_test_equals_contract() {

        EqualsVerifier.simple()
            .forClass(PaymentDto.class)
            .suppress(BIGDECIMAL_EQUALITY)
            .verify();
    }

    @Test
    void should_hold_onto_values() {

        paymentDto = PaymentDto.builder()
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

        return Arrays.asList(
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
