package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.CompletableFuture.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class InternalCaseListedMultiDocBundler implements PreSubmitCallbackHandler<AsylumCase> {

    private final InternalCaseListedAppellantLetterBundler internalCaseListedAppellantLetterBundler;
    private final InternalCaseListedLegalRepLetterBundler internalCaseListedLegalRepLetterBundler;

    public InternalCaseListedMultiDocBundler(
            InternalCaseListedAppellantLetterBundler internalCaseListedAppellantLetterBundler,
            InternalCaseListedLegalRepLetterBundler internalCaseListedLegalRepLetterBundler) {

        this.internalCaseListedAppellantLetterBundler = internalCaseListedAppellantLetterBundler;
        this.internalCaseListedLegalRepLetterBundler = internalCaseListedLegalRepLetterBundler;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return internalCaseListedAppellantLetterBundler.canHandle(callbackStage, callback)
               || internalCaseListedLegalRepLetterBundler.canHandle(callbackStage, callback);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        boolean canHandleAppellant = internalCaseListedAppellantLetterBundler.canHandle(callbackStage, callback);
        boolean canHandleLegalRep = internalCaseListedLegalRepLetterBundler.canHandle(callbackStage, callback);

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (canHandleAppellant && canHandleLegalRep) {
            asylumCase = handleBothConcurrently(callbackStage, callback);
        } else if (canHandleAppellant) {
            asylumCase = internalCaseListedAppellantLetterBundler.handle(callbackStage, callback).getData();
        } else if (canHandleLegalRep) {
            asylumCase = internalCaseListedLegalRepLetterBundler.handle(callbackStage, callback).getData();
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private AsylumCase handleBothConcurrently(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<PreSubmitCallbackResponse<AsylumCase>> appellantFuture =
            supplyAsync(() -> {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                try {
                    return internalCaseListedAppellantLetterBundler.handle(callbackStage, callback);
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            });

        CompletableFuture<PreSubmitCallbackResponse<AsylumCase>> legalRepFuture =
            supplyAsync(() -> {
                RequestContextHolder.setRequestAttributes(requestAttributes);
                try {
                    return internalCaseListedLegalRepLetterBundler.handle(callbackStage, callback);
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            });

        try {
            AsylumCase appellantCase = appellantFuture.join().getData();
            AsylumCase legalRepCase = legalRepFuture.join().getData();

            return mergeAsylumCases(appellantCase, legalRepCase);
        } catch (Exception e) {
            throw new IllegalStateException("Error processing handlers concurrently", e);
        }
    }

    private AsylumCase mergeAsylumCases(AsylumCase case1, AsylumCase case2) {
        AsylumCase mergedCase = new AsylumCase();

        mergedCase.putAll(case1);

        for (Entry<String, Object> entry : case2.entrySet()) {
            String key = entry.getKey();
            Object value2 = entry.getValue();
            Object value1 = mergedCase.get(key);

            if (value1 instanceof List && value2 instanceof List) {
                List<Object> mergedList = new ArrayList<>((List<?>) value1);
                mergedList.addAll((List<?>) value2);
                mergedCase.put(key, mergedList);
            } else {
                mergedCase.put(key, value2);
            }
        }

        return mergedCase;
    }
}
