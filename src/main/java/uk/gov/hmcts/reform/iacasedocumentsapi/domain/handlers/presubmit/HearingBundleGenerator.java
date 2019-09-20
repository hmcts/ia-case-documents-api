package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.*;

@Component
public class HearingBundleGenerator implements PreSubmitCallbackHandler<AsylumCase> {

    private final String fileExtension;
    private final String fileName;
    private final FileNameQualifier<AsylumCase> fileNameQualifier;
    private final DocumentBundler documentBundler;
    private final DocumentHandler documentHandler;

    private final BundleOrder bundleOrder;

    public HearingBundleGenerator(
        @Value("${hearingBundle.fileExtension}") String fileExtension,
        @Value("${hearingBundle.fileName}") String fileName,
        FileNameQualifier<AsylumCase> fileNameQualifier,
        DocumentBundler documentBundler,
        DocumentHandler documentHandler,
        BundleOrder bundleOrder
    ) {
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.fileNameQualifier = fileNameQualifier;
        this.documentBundler = documentBundler;
        this.documentHandler = documentHandler;
        this.bundleOrder = bundleOrder;
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

        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        AsylumCase asylumCase = caseDetails.getCaseData();

        String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);

        List<DocumentWithMetadata> bundleDocuments =
            Stream.of(LEGAL_REPRESENTATIVE_DOCUMENTS, RESPONDENT_DOCUMENTS, HEARING_DOCUMENTS, ADDITIONAL_EVIDENCE_DOCUMENTS)
                .map(asylumCase::<List<IdValue<DocumentWithMetadata>>>read)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(List::stream)
                .map(IdValue::getValue)
                .filter(document -> document.getTag() != DocumentTag.HEARING_BUNDLE
                                    && document.getTag() != DocumentTag.APPEAL_SKELETON_BUNDLE
                                    && document.getTag() != DocumentTag.END_APPEAL
                )
                .sorted(bundleOrder)
                .collect(Collectors.toList());

        Document hearingBundle = documentBundler.bundle(
            bundleDocuments,
            "Hearing documents",
            qualifiedDocumentFileName
        );

        documentHandler.addWithMetadata(
            asylumCase,
            hearingBundle,
            HEARING_DOCUMENTS,
            DocumentTag.HEARING_BUNDLE
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
