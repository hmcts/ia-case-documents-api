package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalCase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;

@Component
public class InternalAdaHearingBundleReadyHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> internalAdaHearingBundleReadyGenerator;
    private final DocumentHandler documentHandler;

    public InternalAdaHearingBundleReadyHandler(
            @Qualifier("internalAdaHearingBundle") DocumentCreator<AsylumCase> internalAdaGenerateHearingBundleGenerator,
            DocumentHandler documentHandler
    ) {
        this.internalAdaHearingBundleReadyGenerator = internalAdaGenerateHearingBundleGenerator;
        this.documentHandler = documentHandler;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                        && callback.getCaseDetails().getState() != State.FTPA_DECIDED
                        && "DONE".equalsIgnoreCase(getStitchStatus(callback))
                        && isInternalCase(callback.getCaseDetails().getCaseData())
                        && isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData());

    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        Document internalAdaGenerateHearingBundleDocument = internalAdaHearingBundleReadyGenerator.create(caseDetails);

        documentHandler.addWithMetadata(
                asylumCase,
                internalAdaGenerateHearingBundleDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.HEARING_BUNDLE_READY_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String getStitchStatus(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<List<IdValue<Bundle>>> maybeCaseBundles = asylumCase.read(AsylumCaseDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles.isPresent() ? maybeCaseBundles.get()
                .stream()
                .map(IdValue::getValue)
                .collect(Collectors.toList()) : Collections.emptyList();

        return caseBundles.isEmpty() ? "" : caseBundles.get(0).getStitchStatus().orElse("");
    }
}
