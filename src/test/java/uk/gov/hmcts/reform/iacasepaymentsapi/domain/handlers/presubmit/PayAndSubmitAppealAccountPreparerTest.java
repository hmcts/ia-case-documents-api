package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PayAndSubmitAppealAccountPreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private RefDataService refDataService;
    @Mock private OrganisationEntityResponse organisationEntityResponse;
    @Mock private OrganisationResponse organisationResponse;

    private PayAndSubmitAppealAccountPreparer payAndSubmitAppealAccountPreparer;

    @BeforeEach
    public void setUp() {
        payAndSubmitAppealAccountPreparer = new PayAndSubmitAppealAccountPreparer(refDataService);
        organisationResponse = new OrganisationResponse(organisationEntityResponse);
    }

    @Test
    void should_get_payment_accounts_from_refDataService() {

        List<String> accountsFromOrg = new ArrayList<String>();
        accountsFromOrg.add("PBA1234567");
        accountsFromOrg.add("PBA1234588");

        List<Value> valueList = new ArrayList<Value>();
        valueList.add(new Value("PBA1234567", "PBA1234567"));
        valueList.add(new Value("PBA1234588", "PBA1234588"));

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(refDataService.getOrganisationResponse())
                 .thenReturn(organisationResponse);

        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = payAndSubmitAppealAccountPreparer
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
    void should_handle_no_payment_accounts() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        when(refDataService.getOrganisationResponse())
            .thenReturn(organisationResponse);

        List<String> accountsFromOrg = new ArrayList<String>();
        when(organisationEntityResponse.getPaymentAccount()).thenReturn(accountsFromOrg);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = payAndSubmitAppealAccountPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertTrue(callbackResponse.getErrors().contains("There are no payment accounts"));

        AsylumCase asylumCase = callbackResponse.getData();
        verify(asylumCase, times(0))
            .write(PAYMENT_ACCOUNT_LIST, null);

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> payAndSubmitAppealAccountPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> payAndSubmitAppealAccountPreparer
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> payAndSubmitAppealAccountPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> payAndSubmitAppealAccountPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> payAndSubmitAppealAccountPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> payAndSubmitAppealAccountPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = payAndSubmitAppealAccountPreparer.canHandle(callbackStage, callback);

                if (callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }
}
