package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;

@Component
public class PayAndSubmitAppealAccountPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final RefDataService refDataService;

    public PayAndSubmitAppealAccountPreparer(
        RefDataService refDataService
    ) {
        this.refDataService = refDataService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && (callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                    || callback.getEvent() == Event.PAYMENT_APPEAL));
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<String> accountsFromOrg = refDataService.getOrganisationResponse()
            .getOrganisationEntityResponse().getPaymentAccount();

        if (accountsFromOrg.isEmpty()) {
            PreSubmitCallbackResponse<AsylumCase> response =
                new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
            response.addErrors(Collections.singleton("There are no payment accounts"));

            return response;
        }

        List<Value> accountListElements = accountsFromOrg
            .stream()
            .map(idValue -> new Value(idValue, idValue))
            .collect(Collectors.toList());

        DynamicList accountList = new DynamicList(accountListElements.get(0), accountListElements);
        asylumCase.write(PAYMENT_ACCOUNT_LIST, accountList);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
