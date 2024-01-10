package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.EmBundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;


@Component
public class AdvancedBundlingCallbackHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String SUPPLIED_BY_RESPONDENT = "The respondent";
    private static final String SUPPLIED_BY_APPELLANT = "The appellant";

    private final EmBundleRequestExecutor emBundleRequestExecutor;
    private final String emBundlerUrl;
    private final String emBundlerStitchUri;

    public AdvancedBundlingCallbackHandler(
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
               && callback.getEvent() == Event.GENERATE_HEARING_BUNDLE;
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
        asylumCase.clear(HMCTS);
        asylumCase.write(AsylumCaseDefinition.HMCTS,"[userImage:hmcts.png]");
        asylumCase.clear(AsylumCaseDefinition.CASE_BUNDLES);

        Optional<YesOrNo> maybeCaseFlagSetAsideReheardExists = asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS,YesOrNo.class);

        if (maybeCaseFlagSetAsideReheardExists.isPresent()
            && maybeCaseFlagSetAsideReheardExists.get() == YesOrNo.YES) {

            asylumCase.write(APPELLANT_ADDENDUM_EVIDENCE_DOCS,getIdValues(asylumCase,ADDENDUM_EVIDENCE_DOCUMENTS, SUPPLIED_BY_APPELLANT, DocumentTag.ADDENDUM_EVIDENCE));
            asylumCase.write(RESPONDENT_ADDENDUM_EVIDENCE_DOCS,getIdValues(asylumCase,ADDENDUM_EVIDENCE_DOCUMENTS, SUPPLIED_BY_RESPONDENT,DocumentTag.ADDENDUM_EVIDENCE));

            asylumCase.write(APP_ADDITIONAL_EVIDENCE_DOCS,getIdValues(asylumCase, ADDITIONAL_EVIDENCE_DOCUMENTS, SUPPLIED_BY_APPELLANT,DocumentTag.ADDITIONAL_EVIDENCE));
            asylumCase.write(RESP_ADDITIONAL_EVIDENCE_DOCS,getIdValues(asylumCase, RESPONDENT_DOCUMENTS, SUPPLIED_BY_RESPONDENT,DocumentTag.ADDITIONAL_EVIDENCE));

            asylumCase.write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-reheard-hearing-bundle-config.yaml");
        } else {
            asylumCase.write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-hearing-bundle-config.yaml");
        }
        asylumCase.write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, getBundlePrefix(asylumCase));

        final PreSubmitCallbackResponse<AsylumCase> response = emBundleRequestExecutor.post(callback, emBundlerUrl + emBundlerStitchUri);

        final AsylumCase responseData = response.getData();
        Optional<List<IdValue<Bundle>>> maybeCaseBundles  = responseData.read(AsylumCaseDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles
            .orElseThrow(() -> new IllegalStateException("caseBundle is not present"))
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList());

        if (caseBundles.size() != 1) {
            throw new IllegalStateException("case bundles size is not 1 and is : " + caseBundles.size());
        }

        //stictchStatusflags -  NEW, IN_PROGRESS, DONE, FAILED
        final String stitchStatus = caseBundles.get(0).getStitchStatus().orElse("");

        if("FAILED".equalsIgnoreCase(stitchStatus))
        {
            String allStitchSatuses = caseBundles.stream().map(bundle -> bundle.getStitchStatus().orElse("<empty>"))
                    .collect(Collectors.joining(","));
            LOG.warn("A stitching failure was reported for case {}. Stitch statuses are: {}", callback.getCaseDetails().getId(), allStitchSatuses);
        }


        responseData.write(AsylumCaseDefinition.STITCHING_STATUS, stitchStatus);

        return new PreSubmitCallbackResponse<>(responseData);
    }

    private String getBundlePrefix(AsylumCase asylumCase) {

        final String appealReferenceNumber =
            asylumCase
                .read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("appealReferenceNumber is not present"));

        final String appellantFamilyName =
            asylumCase
                .read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)
                .orElseThrow(() -> new IllegalStateException("appellantFamilyName is not present"));

        return appealReferenceNumber.replace("/", " ")
               + "-" + appellantFamilyName;
    }

    private List<IdValue<DocumentWithMetadata>> getIdValues(
        AsylumCase asylumCase,
        AsylumCaseDefinition fieldDefinition,String suppliedBy, DocumentTag tag
    ) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeIdValues = asylumCase
            .read(fieldDefinition);

        List<IdValue<DocumentWithMetadata>> documents =
            maybeIdValues.orElse(Collections.emptyList());
        if (fieldDefinition == ADDENDUM_EVIDENCE_DOCUMENTS) {
            return documents.stream()
                .filter(document -> document.getValue().getSuppliedBy().equals(suppliedBy))
                .filter(document -> document.getValue().getTag() == tag)
                .collect(Collectors.toList());
        } else {
            return documents.stream()
                .filter(document -> document.getValue().getTag() == tag)
                .collect(Collectors.toList());
        }
    }
}
