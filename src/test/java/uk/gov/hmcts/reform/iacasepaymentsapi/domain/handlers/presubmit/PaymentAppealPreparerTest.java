package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HAS_PBA_ACCOUNTS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HAS_SERVICE_REQUEST_ALREADY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_PENDING;

import feign.FeignException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.ServiceRequestService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PaymentAppealPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private RefDataService refDataService;
    @Mock private FeeService feeService;
    @Mock private ServiceRequestService serviceRequestService;
    @Mock private OrganisationEntityResponse organisationEntityResponse;
    @Mock private OrganisationResponse organisationResponse;

    private PaymentAppealPreparer paymentAppealPreparer;

    @BeforeEach
    public void setUp() {
        paymentAppealPreparer = new PaymentAppealPreparer(refDataService, feeService, serviceRequestService);
        organisationResponse = new OrganisationResponse(organisationEntityResponse);

        lenient().when(callback.getCaseDetails()).thenReturn(caseDetails);
        lenient().when(caseDetails.getCaseData()).thenReturn(asylumCase);
        lenient().when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        lenient().when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
    }

    @Test
    void should_get_payment_accounts_from_refDataService() {

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");
        accountsFromOrg.add("PBA1234588");

        List<Value> valueList = new ArrayList<Value>();
        valueList.add(new Value("PBA1234567", "PBA1234567"));
        valueList.add(new Value("PBA1234588", "PBA1234588"));

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        List<Value> accountListElements = accountsFromOrg
            .stream()
            .map(idValue -> new Value(idValue, idValue))
            .collect(Collectors.toList());

        verify(asylumCase, times(1))
            .write(PAYMENT_ACCOUNT_LIST, new DynamicList(valueList.get(0), valueList));
    }

    @Test
    void handler_should_throw_when_there_issue_with_ref_data_service() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(refDataService.getOrganisationResponse()).thenThrow(FeignException.class);

        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        verify(asylumCase, times(1)).write(HAS_PBA_ACCOUNTS, YesOrNo.NO);
    }

    @Test
    void should_get_payment_accounts_from_refDataService_when_remission_option_is_empty() {

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");
        accountsFromOrg.add("PBA1234588");

        List<Value> valueList = new ArrayList<Value>();
        valueList.add(new Value("PBA1234567", "PBA1234567"));
        valueList.add(new Value("PBA1234588", "PBA1234588"));

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(1))
            .write(PAYMENT_ACCOUNT_LIST, new DynamicList(valueList.get(0), valueList));
    }

    @Test
    void should_get_payment_accounts_from_refDataService_when_remission_option_is_ho_waiver() {

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");
        accountsFromOrg.add("PBA1234588");

        List<Value> valueList = new ArrayList<Value>();
        valueList.add(new Value("PBA1234567", "PBA1234567"));
        valueList.add(new Value("PBA1234588", "PBA1234588"));

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);

        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(0))
            .write(PAYMENT_ACCOUNT_LIST, new DynamicList(valueList.get(0), valueList));
    }

    @Test
    void should_handle_no_payment_accounts() {

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        List<String> accountsFromOrg = new ArrayList<String>();
        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().contains("There are no payment accounts"));

        AsylumCase asylumCase = callbackResponse.getData();
        verify(asylumCase, times(0))
            .write(PAYMENT_ACCOUNT_LIST, null);

    }

    @ParameterizedTest
    @MethodSource("feeOptionParameters")
    void should_return_valid_fee_for_decision_with_hearing(
        Event event, String hearingType, FeeType feeType, Fee fee
    ) {

        when(callback.getEvent()).thenReturn(event);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");

        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);
        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(hearingType));
        when(feeService.getFee(feeType)).thenReturn(fee);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        if (feeType == FeeType.FEE_WITH_HEARING) {
            verify(asylumCase, times(1)).write(FEE_WITH_HEARING, fee.getAmountAsString());
        } else {
            verify(asylumCase, times(1)).write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
        }
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
    }

    @ParameterizedTest
    @MethodSource("feeOptionParameters")
    void should_return_valid_fee_for_decision_with_hearing_with_remission_rejected(
        Event event, String hearingType, FeeType feeType, Fee fee
    ) {

        when(callback.getEvent()).thenReturn(event);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");

        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);
        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HELP_WITH_FEES));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(hearingType));
        when(feeService.getFee(feeType)).thenReturn(fee);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        if (feeType == FeeType.FEE_WITH_HEARING) {
            verify(asylumCase, times(1)).write(FEE_WITH_HEARING, fee.getAmountAsString());
        } else {
            verify(asylumCase, times(1)).write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
        }
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
    }

    @ParameterizedTest
    @MethodSource("feeOptionParameters")
    void should_not_return_valid_pba_accounts_but_return_fee_for_decision_with_hearing_with_remission_approved(
        Event event, String hearingType, FeeType feeType, Fee fee
    ) {

        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HELP_WITH_FEES));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(hearingType));
        when(feeService.getFee(feeType)).thenReturn(fee);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        if (feeType == FeeType.FEE_WITH_HEARING) {
            verify(asylumCase, times(1)).write(FEE_WITH_HEARING, fee.getAmountAsString());
        } else {
            verify(asylumCase, times(1)).write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
        }
        verify(asylumCase, times(1)).write(PAYMENT_STATUS, PAYMENT_PENDING);
        verifyNoInteractions(refDataService);
    }

    private static Stream<Arguments> feeOptionParameters() {

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        Fee feeWithoutHearing =
            new Fee("FEE0001", "Fee without hearing", "1", new BigDecimal("80"));

        return Stream.of(
            Arguments.of(
                Event.PAY_AND_SUBMIT_APPEAL, "decisionWithHearing", FeeType.FEE_WITH_HEARING, feeWithHearing),
            Arguments.of(
                Event.PAY_AND_SUBMIT_APPEAL, "decisionWithoutHearing", FeeType.FEE_WITHOUT_HEARING, feeWithoutHearing),
            Arguments.of(
                Event.PAYMENT_APPEAL, "decisionWithHearing", FeeType.FEE_WITH_HEARING, feeWithHearing),
            Arguments.of(
                Event.PAYMENT_APPEAL, "decisionWithoutHearing", FeeType.FEE_WITHOUT_HEARING, feeWithoutHearing)
        );
    }

    @ParameterizedTest
    @MethodSource("feeServiceIsDownParameters")
    void should_return_error_on_fee_service_is_down(Event event, String hearingType, FeeType feeType) {

        when(callback.getEvent()).thenReturn(event);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");

        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);
        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class))
            .thenReturn(Optional.of(hearingType));
        when(feeService.getFee(feeType)).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("Cannot retrieve the fee from fees-register.");
    }

    private static Stream<Arguments> feeServiceIsDownParameters() {

        return Stream.of(
            Arguments.of(Event.PAY_AND_SUBMIT_APPEAL, "decisionWithHearing", FeeType.FEE_WITH_HEARING),
            Arguments.of(Event.PAY_AND_SUBMIT_APPEAL, "decisionWithoutHearing", FeeType.FEE_WITHOUT_HEARING),
            Arguments.of(Event.PAYMENT_APPEAL, "decisionWithHearing", FeeType.FEE_WITH_HEARING),
            Arguments.of(Event.PAYMENT_APPEAL, "decisionWithoutHearing", FeeType.FEE_WITHOUT_HEARING)
        );
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> paymentAppealPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentAppealPreparer
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentAppealPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void handling_should_throw_if_appeal_type_is_wrong() {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        assertThatThrownBy(() -> paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = paymentAppealPreparer.canHandle(callbackStage, callback);

                if ((Arrays.asList(
                    Event.PAYMENT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL,
                    Event.PAY_FOR_APPEAL,
                    Event.RECORD_REMISSION_DECISION).contains(callback.getEvent())
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)
                    || isWaysToPay(callbackStage, callback, true)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_send_request_for_service_request_if_is_ways_to_pay_submit_appeal() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");

        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);
        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);

    }

    @Test
    void should_send_request_for_service_request_if_is_ways_to_pay_generate_service_request() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");

        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);
        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }


    private boolean isWaysToPay(PreSubmitCallbackStage callbackStage,
                                Callback<AsylumCase> callback,
                                boolean isLegalRepJourney) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.SUBMIT_APPEAL
                   || callback.getEvent() == Event.GENERATE_SERVICE_REQUEST
                   || callback.getEvent() == Event.RECORD_REMISSION_DECISION)
               && isLegalRepJourney;
    }
}
