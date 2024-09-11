package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.EmBundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;


@Slf4j
@Component
public class UpperTribunalBundleHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final EmBundleRequestExecutor emBundleRequestExecutor;
    private final String emBundlerUrl;
    private final String emBundlerStitchUri;

    public UpperTribunalBundleHandler(
        @Value("${emBundler.url}") String emBundlerUrl,
        @Value("${emBundler.stitch.async.uri}") String emBundlerStitchUri,
        EmBundleRequestExecutor emBundleRequestExecutor) {
        this.emBundlerUrl = emBundlerUrl;
        this.emBundlerStitchUri = emBundlerStitchUri;
        this.emBundleRequestExecutor = emBundleRequestExecutor;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.GENERATE_UPPER_TRIBUNAL_BUNDLE;
    }

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

        boolean isOrWasAda = asylumCase.read(SUITABILITY_REVIEW_DECISION).isPresent();
        asylumCase.clear(HMCTS);
        asylumCase.write(AsylumCaseDefinition.HMCTS, "[userImage:hmcts.png]");
        asylumCase.clear(AsylumCaseDefinition.CASE_BUNDLES);
        asylumCase.write(AsylumCaseDefinition.BUNDLE_CONFIGURATION,
                isOrWasAda ? "iac-upper-tribunal-bundle-inc-tribunal-config.yaml" : "iac-upper-tribunal-bundle-config.yaml");

        asylumCase.write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, getBundlePrefix(asylumCase));

        final PreSubmitCallbackResponse<AsylumCase> response =
            emBundleRequestExecutor
                .post(callback, emBundlerUrl + emBundlerStitchUri);

        final AsylumCase responseData = response.getData();

        Optional<List<IdValue<Bundle>>> maybeCaseBundles =
            responseData
                .read(AsylumCaseDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles
            .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
            .stream()
            .map(IdValue::getValue)
            .toList();

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        //stictchStatusflags -  NEW, IN_PROGRESS, DONE, FAILED
        final String stitchStatus = caseBundles.get(0).getStitchStatus().orElse("");

        responseData.write(AsylumCaseDefinition.STITCHING_STATUS_UPPER_TRIBUNAL, stitchStatus);
        log.info("Stitch status in case documents api repo is " + stitchStatus + " on case id "
            + callback.getCaseDetails().getId());

        return new PreSubmitCallbackResponse<>(responseData);
    }

    private String getBundlePrefix(AsylumCase asylumCase) {

        final String appealReferenceNumber =
            asylumCase
                .read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("appealReferenceNumber is not present"));

        return appealReferenceNumber.replace("/", " ");
    }
}
