package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DATE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ERROR_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_FAILED_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.values;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_PENDING;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.LegRepAddressUk;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.StatusHistories;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.PaymentProperties;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PaymentAppealHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;
    @Mock private Fee fee;
    @Mock private PaymentService paymentService;
    @Mock private RefDataService refDataService;
    @Mock private PaymentProperties paymentProperties;
    @Mock private PaymentResponse paymentResponse;
    @Mock private CreditAccountPayment creditAccountPayment;

    private ObjectMapper objectMapper;
    private long caseId = 1234;

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String townCity = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";
    private List<LegRepAddressUk> addresses;
    private LegRepAddressUk legRepAddressUk;

    private PaymentAppealHandler appealFeePaymentHandler;

    @BeforeEach
    public void setUp() {
        addresses = new ArrayList<>();
        legRepAddressUk = new LegRepAddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country,
            Arrays.asList("A", "B")
        );
        addresses.add(legRepAddressUk);

        objectMapper = new ObjectMapper();
        appealFeePaymentHandler =
            new PaymentAppealHandler(feeService, paymentService, refDataService, paymentProperties, objectMapper);
    }

    @Test
    void should_return_error_when_fee_does_not_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(null);

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "PAYMENT_APPEAL", "PAY_AND_SUBMIT_APPEAL", "PAY_FOR_APPEAL" })
    void handle_should_throw_error_for_no_appeal_type(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("AppealType is not present for caseId: " + caseId);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "PAYMENT_APPEAL", "PAY_AND_SUBMIT_APPEAL", "PAY_FOR_APPEAL" })
    void handle_should_throw_error_for_missing_appeal_reference_number(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);

        when(fee.getAmountAsString()).thenReturn("14000");
        when(fee.getCode()).thenReturn("FEE0123");
        when(fee.getVersion()).thenReturn("1");
        when(fee.getDescription()).thenReturn("IA hearing fee");

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("A payment description"));
        when(refDataService.getOrganisationResponse()).thenReturn(getOrganisationResponse());

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal reference number is not present for caseId: " + caseId);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "PAYMENT_APPEAL", "PAY_AND_SUBMIT_APPEAL", "PAY_FOR_APPEAL" })
    void handler_should_throw_error_for_missing_appellant_family_name(Event event) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);

        when(fee.getAmountAsString()).thenReturn("14000");
        when(fee.getCode()).thenReturn("FEE0123");
        when(fee.getVersion()).thenReturn("1");
        when(fee.getDescription()).thenReturn("IA hearing fee");

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("PA/12345/2021"));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("A payment description"));
        when(refDataService.getOrganisationResponse()).thenReturn(getOrganisationResponse());

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appellant family name is not present for caseId: " + caseId);
    }

    @Test
    void should_call_payment_api_on_pay_now() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("EA/50001/2020"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("AppellantFamilyName"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("LegRep001"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCalculatedAmount()).thenReturn(BigDecimal.valueOf(140.00));
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("Hearing appeal"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getAmountAsString()).thenReturn("140");

        when(paymentService.creditAccountPayment(any(CreditAccountPayment.class)))
            .thenReturn(new PaymentResponse("RC-1590-6748-2373-9129", new Date(),
                                            "Success",
                                            "2020-1590674823325", null
            ));

        when(refDataService.getOrganisationResponse()).thenReturn(
            getOrganisationResponse()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealFeePaymentHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(1))
            .write(PAYMENT_REFERENCE, "RC-1590-6748-2373-9129");

        String pattern = "d MMM yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        verify(asylumCase, times(1))
            .write(PAYMENT_DATE, simpleDateFormat.format(new Date()));
        verify(asylumCase, times(1))
            .write(FEE_AMOUNT_GBP, "14000");
        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, PAID);
        verify(asylumCase, times(1))
            .clear(PAYMENT_FAILED_FOR_DISPLAY);
        verify(asylumCase, times(1))
            .write(FEE_WITH_HEARING, "140");
    }

    @Test
    void should_call_payment_api_on_pay_now_and_return_failure() {

        StatusHistories statusHistory = new StatusHistories(
            "failed",
            "CA-E0004",
            "Your account is deleted",
            "2020-05-28T14:04:06.048+0000",
            "2020-05-28T14:04:06.048+0000"
        );

        List<StatusHistories> statusHistories = new ArrayList<>();
        statusHistories.add(statusHistory);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("EA/50001/2020"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("AppellantFamilyName"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("LegRep001"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCalculatedAmount()).thenReturn(BigDecimal.valueOf(140.00));
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("Hearing appeal"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getAmountAsString()).thenReturn("140");

        when(paymentService.creditAccountPayment(any(CreditAccountPayment.class)))
            .thenReturn(new PaymentResponse("RC-1590-6748-2373-9129", new Date(),
                                            "Failed",
                                            "2020-1590674823325",
                                            statusHistories
            ));

        when(refDataService.getOrganisationResponse()).thenReturn(
            getOrganisationResponse()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealFeePaymentHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(1))
            .write(PAYMENT_REFERENCE, "RC-1590-6748-2373-9129");

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, FAILED);

        verify(asylumCase, times(1))
            .write(PAYMENT_FAILED_FOR_DISPLAY, "Pending");

        verify(asylumCase, times(1))
            .write(PAYMENT_ERROR_CODE, statusHistories.get(0).getErrorCode());

        verify(asylumCase, times(1))
            .write(PAYMENT_ERROR_MESSAGE, statusHistories.get(0).getErrorMessage());
    }

    @Test
    void should_call_payment_api_on_pay_now_and_return_bad_request() {

        StatusHistories statusHistory = new StatusHistories(
            "failed",
            "CA-E0004",
            "Your account is deleted",
            "2020-05-28T14:04:06.048+0000",
            "2020-05-28T14:04:06.048+0000"
        );

        List<StatusHistories> statusHistories = new ArrayList<>();
        statusHistories.add(statusHistory);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("EA/50001/2020"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("AppellantFamilyName"));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("LegRep001"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCalculatedAmount()).thenReturn(BigDecimal.valueOf(140.00));
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("Hearing appeal"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getAmountAsString()).thenReturn("140");

        when(paymentService.creditAccountPayment(any(CreditAccountPayment.class)))
            .thenThrow(
                new FeignException.BadRequest(
                    "",
                    Request.create(
                        Request.HttpMethod.POST,
                        "",
                        Collections.emptyMap(),
                        new byte[0],
                        Charset.defaultCharset(),
                        null
                    ),
                    new byte[0]
                )
            );

        when(refDataService.getOrganisationResponse()).thenReturn(
            getOrganisationResponse()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealFeePaymentHandler
            .handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(1))
            .write(PAYMENT_REFERENCE, "No payment reference provided");

        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, FAILED);
    }

    @Test
    void should_throw_when_no_account_number_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(fee.getAmountAsString()).thenReturn("140");
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("PBA account number is not present for caseId: " + caseId);
    }

    @Test
    void should_throw_when_no_payment_description_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(fee.getAmountAsString()).thenReturn("140");
        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));

        when(refDataService.getOrganisationResponse()).thenReturn(getOrganisationResponse());

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Payment description is not present for caseId: " + caseId);
    }

    @Test
    void test_append_case_reference_and_appellant_family_name() {

        String result = appealFeePaymentHandler
            .appendCaseReferenceAndAppellantName(String.valueOf(caseId), "aSurname");

        assertEquals(result, String.valueOf(caseId) + "_aSurname");
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealFeePaymentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealFeePaymentHandler
            .canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : values()) {

                boolean canHandle = appealFeePaymentHandler.canHandle(callbackStage, callback);

                if ((event == Event.PAY_FOR_APPEAL
                    || event == Event.PAYMENT_APPEAL
                    || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL)
                    && (callbackStage == ABOUT_TO_SUBMIT)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealFeePaymentHandler
            .handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);

        assertThatThrownBy(() -> appealFeePaymentHandler
            .handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void should_return_error_when_payment_response_is_null() {

        assertThatThrownBy(() -> appealFeePaymentHandler.writePaymentResponseStatusToCaseData(null, asylumCase))
            .hasMessage("paymentResponse must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_return_error_when_credit_account_payment_is_null() {

        assertThatThrownBy(() -> appealFeePaymentHandler.makePayment(null))
            .hasMessage("creditAccountPayment must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void make_payment_call_should_return_payment_response() {

        StatusHistories statusHistory = new StatusHistories(
            "failed",
            "CA-E0004",
            "Your account is deleted",
            "2020-05-28T14:04:06.048+0000",
            "2020-05-28T14:04:06.048+0000"
        );

        List<StatusHistories> statusHistories = new ArrayList<>();
        statusHistories.add(statusHistory);

        PaymentResponse paymentResponse = new PaymentResponse(
            "RC-1590-6748-2373-9129",
            new Date(),
            "Failed",
            "2020-1590674823325",
            statusHistories
        );

        when(paymentService.creditAccountPayment(any(CreditAccountPayment.class))).thenReturn(paymentResponse);

        assertThat(appealFeePaymentHandler.makePayment(creditAccountPayment)).isEqualTo(paymentResponse);
    }

    @Test
    void should_write_paid_when_payment_response_success() {

        when(paymentResponse.getStatus()).thenReturn(("Success"));

        appealFeePaymentHandler.writePaymentResponseStatusToCaseData(paymentResponse, asylumCase);

        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAID);

        verify(asylumCase, times(1)).clear(PAYMENT_FAILED_FOR_DISPLAY);
    }

    @Test
    void should_write_payment_due_when_payment_response_success() {

        when(paymentResponse.getStatus()).thenReturn(("Payment pending"));

        appealFeePaymentHandler.writePaymentResponseStatusToCaseData(paymentResponse, asylumCase);

        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);

        verify(asylumCase, times(1)).clear(PAYMENT_FAILED_FOR_DISPLAY);
    }

    @Test
    void should_throw_on_appeal_reference_number_is_null() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("PaymentDescription"));
        when(refDataService.getOrganisationResponse()).thenReturn(
            new OrganisationResponse(new OrganisationEntityResponse("ia-legal-rep-org")));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(fee.getAmountAsString()).thenReturn("140");

        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));

        when(refDataService.getOrganisationResponse()).thenReturn(
            getOrganisationResponse()
        );

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Appeal reference number is not present for caseId: " + caseId);
    }

    @Test
    void should_throw_on_legal_rep_reference_is_null() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getId()).thenReturn(caseId);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("EA/50001/2020"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("AppellantFamilyName"));
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("PaymentDescription"));
        when(refDataService.getOrganisationResponse()).thenReturn(
            new OrganisationResponse(new OrganisationEntityResponse("ia-legal-rep-org")));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(DECISION_WITH_HEARING.value()));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(fee);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getCode()).thenReturn("FEE0123");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getDescription())
            .thenReturn("Appeal determined with a hearing");
        when(feeService.getFee(FeeType.FEE_WITH_HEARING).getVersion()).thenReturn("1");
        when(fee.getAmountAsString()).thenReturn("140");

        when(asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("PBA1234567")));

        when(refDataService.getOrganisationResponse()).thenReturn(
            getOrganisationResponse()
        );

        assertThatThrownBy(() -> appealFeePaymentHandler.handle(ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Legal rep reference number is not present for caseId: " + caseId);
    }

    private OrganisationResponse getOrganisationResponse() {

        return new OrganisationResponse(
            new OrganisationEntityResponse(
                "ia-legal-rep-org",
                "",
                "",
                "",
                "",
                "",
                "",
                null,
                newArrayList("PBA1234567"),
                addresses
            )
        );
    }
}
