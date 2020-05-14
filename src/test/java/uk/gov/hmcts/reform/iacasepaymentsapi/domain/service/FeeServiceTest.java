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
    public void setUp() {

        feeService = new FeeService(feesConfiguration, feesRegisterApi);
    }

    @Test
    public void should_return_oral_hearing_fee() {

        when(feesConfiguration.getFees()).thenReturn(getFeeTypes());

        LookupReferenceData lookupReferenceData = feesConfiguration
            .getFees()
            .get(FeeType.ORAL_FEE.getValue());

        when(feesRegisterApi.findFee(
            lookupReferenceData.getChannel(),
            lookupReferenceData.getEvent(),
            lookupReferenceData.getJurisdiction1(),
            lookupReferenceData.getJurisdiction2(),
            lookupReferenceData.getKeyword(),
            lookupReferenceData.getService()
        )).thenReturn(getFeeResponse());

        Fee fee = feeService.getFee(FeeType.ORAL_FEE);

        assertThat(fee.getCode()).isEqualTo("FEE0123");
        assertThat(fee.getDescription()).isEqualTo("Appeal determined with a hearing");
        assertThat(fee.getVersion()).isEqualTo(1);
        assertThat(fee.getCalculatedAmount()).isEqualTo(new BigDecimal("140.00"));
    }

    @Test
    public void should_throw_for_null_fee_type() {

        assertThatThrownBy(() -> feeService.getFee(FeeType.ORAL_FEE))
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
        feeTypeMap.put("oralFee", lookupReferenceData);

        return feeTypeMap;
    }

    private FeeResponse getFeeResponse() {

        return new FeeResponse(
            "FEE0123",
            "Appeal determined with a hearing",
            1,
            new BigDecimal("140.00"));
    }
}
