package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;

public class ServiceRequestRequestTest {

    private static final String CALLBACK_URL = "some-callback-url";
    private CasePaymentRequestDto casePaymentRequestDto;
    private static final String ACTION = "some-action";
    private static final String RESPONSIBLE_PARTY = "some-responsible-party";
    private static final String CASE_REFERENCE = "some-case-reference";
    private static final String CCD_CASE_NUMBER = "some-ccd-case-number";
    private static final String CODE = "some-code";
    private static final String VERSION = "some-version";
    private static final Integer VOLUME = 1;
    private static final String MEMO_LINE = "some-memo-line";
    private static final String REFERENCE = "some-reference";
    private FeeDto[] fees;


    private static final BigDecimal CALCULATED_AMOUNT = new BigDecimal(80);

    private static final String HMCTS_ORG_ID = "some-hmcts-org-id";

    @BeforeEach
    void setup() {
        casePaymentRequestDto = CasePaymentRequestDto.builder()
            .responsibleParty(RESPONSIBLE_PARTY)
            .action(ACTION)
            .build();

        FeeDto feeDto = FeeDto.builder()
            .calculatedAmount(CALCULATED_AMOUNT)
            .ccdCaseNumber(CCD_CASE_NUMBER)
            .code(CODE)
            .memoLine(MEMO_LINE)
            .reference(REFERENCE)
            .version(VERSION)
            .volume(VOLUME)
            .build();

        fees = new FeeDto[]{feeDto};
    }

    @Test
    void should_hold_onto_values() {
        ServiceRequestRequest serviceRequestRequest = new ServiceRequestRequest(CALLBACK_URL,
                                                                                casePaymentRequestDto,
                                                                                CASE_REFERENCE,
                                                                                CCD_CASE_NUMBER,
                                                                                fees,
                                                                                HMCTS_ORG_ID);

        assertEquals("some-callback-url", serviceRequestRequest.getCallBackUrl());
        assertEquals(casePaymentRequestDto, serviceRequestRequest.getCasePaymentRequest());
        assertEquals("some-case-reference", serviceRequestRequest.getCaseReference());
        assertEquals("some-ccd-case-number", serviceRequestRequest.getCcdCaseNumber());
        assertEquals("some-hmcts-org-id", serviceRequestRequest.getHmctsOrgId());
        assertEquals(fees, serviceRequestRequest.getFees());

        FeeDto feeDto = serviceRequestRequest.getFees()[0];
        assertEquals("some-ccd-case-number", feeDto.getCcdCaseNumber());
        assertEquals("some-memo-line", feeDto.getMemoLine());
        assertEquals("some-reference", feeDto.getReference());
        assertEquals("some-code", feeDto.getCode());
        assertEquals("some-version", feeDto.getVersion());
        assertEquals(1, feeDto.getVolume());
        assertEquals("some-version", feeDto.getVersion());

        CasePaymentRequestDto casePaymentRequestDto = serviceRequestRequest.getCasePaymentRequest();
        assertEquals(ACTION, casePaymentRequestDto.getAction());
        assertEquals(RESPONSIBLE_PARTY, casePaymentRequestDto.getResponsibleParty());
    }
}
