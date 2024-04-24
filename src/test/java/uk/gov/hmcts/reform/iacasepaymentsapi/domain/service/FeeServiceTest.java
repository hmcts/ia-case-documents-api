package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.FeesConfiguration.LookupReferenceData;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.FeesConfiguration;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class FeeServiceTest {

    @Mock private FeesConfiguration feesConfiguration;
    @Mock private FeesRegisterApi feesRegisterApi;

    private FeeService feeService;

    @BeforeEach
    void setUp() {
        feeService = new FeeService(feesConfiguration, feesRegisterApi);
    }

    @Test
    void should_return_fee_with_hearing() {

        when(feesConfiguration.getFees()).thenReturn(getFeeTypes());

        LookupReferenceData lookupReferenceData = feesConfiguration
            .getFees()
            .get(FeeType.FEE_WITH_HEARING.getValue());

        when(feesRegisterApi.findFee(
            lookupReferenceData.getChannel(),
            lookupReferenceData.getEvent(),
            lookupReferenceData.getJurisdiction1(),
            lookupReferenceData.getJurisdiction2(),
            lookupReferenceData.getKeyword(),
            lookupReferenceData.getService()
        )).thenReturn(getFeeHearingResponse());

        Fee fee = feeService.getFee(FeeType.FEE_WITH_HEARING);

        assertThat(fee.getCode()).isEqualTo("FEE0123");
        assertThat(fee.getDescription()).isEqualTo("Appeal determined with a hearing");
        assertThat(fee.getVersion()).isEqualTo("1");
        assertThat(fee.getCalculatedAmount()).isEqualTo(new BigDecimal("140.00"));
    }

    @Test
    void should_return_fee_without_hearing() {

        when(feesConfiguration.getFees()).thenReturn(getFeeTypes());

        LookupReferenceData lookupReferenceData = feesConfiguration
            .getFees()
            .get(FeeType.FEE_WITHOUT_HEARING.getValue());

        when(feesRegisterApi.findFee(
            lookupReferenceData.getChannel(),
            lookupReferenceData.getEvent(),
            lookupReferenceData.getJurisdiction1(),
            lookupReferenceData.getJurisdiction2(),
            lookupReferenceData.getKeyword(),
            lookupReferenceData.getService()
        )).thenReturn(getFeeWithoutHearingResponse());

        Fee fee = feeService.getFee(FeeType.FEE_WITHOUT_HEARING);

        assertThat(fee.getCode()).isEqualTo("FEE0456");
        assertThat(fee.getDescription()).isEqualTo("Appeal determined without a hearing");
        assertThat(fee.getVersion()).isEqualTo("1");
        assertThat(fee.getCalculatedAmount()).isEqualTo(new BigDecimal("80.00"));
    }

    @Test
    void should_throw_for_null_fee_type() {

        assertThatThrownBy(() -> feeService.getFee(FeeType.FEE_WITH_HEARING))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feeService.getFee(FeeType.FEE_WITHOUT_HEARING))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private Map<String, LookupReferenceData> getFeeTypes() {

        final Map<String, LookupReferenceData> feeTypeMap = new HashMap<>();

        LookupReferenceData lookupReferenceData = new LookupReferenceData();
        lookupReferenceData.setChannel("default");
        lookupReferenceData.setEvent("issue");
        lookupReferenceData.setJurisdiction1("tribunal");
        lookupReferenceData.setJurisdiction2("immigration and asylum chamber");
        lookupReferenceData.setKeyword("ABC");
        lookupReferenceData.setService("other");
        feeTypeMap.put("feeWithHearing", lookupReferenceData);

        LookupReferenceData lookupReferenceWithoutFeeData = new LookupReferenceData();
        lookupReferenceWithoutFeeData.setChannel("default");
        lookupReferenceWithoutFeeData.setEvent("issue");
        lookupReferenceWithoutFeeData.setJurisdiction1("tribunal");
        lookupReferenceWithoutFeeData.setJurisdiction2("immigration and asylum chamber");
        lookupReferenceWithoutFeeData.setKeyword("DEF");
        lookupReferenceWithoutFeeData.setService("other");
        feeTypeMap.put("feeWithoutHearing", lookupReferenceWithoutFeeData);

        return feeTypeMap;
    }

    private FeeResponse getFeeHearingResponse() {

        return new FeeResponse(
            "FEE0123",
            "Appeal determined with a hearing",
            "1",
            new BigDecimal("140.00"));
    }

    private FeeResponse getFeeWithoutHearingResponse() {

        return new FeeResponse(
            "FEE0456",
            "Appeal determined without a hearing",
            "1",
            new BigDecimal("80.00"));
    }
}
