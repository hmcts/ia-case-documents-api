package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
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
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final HearingBundleOrder hearingBundleOrder;

    public HearingBundleGenerator(
        @Value("${hearingBundle.fileExtension}") String fileExtension,
        @Value("${hearingBundle.fileName}") String fileName,
        FileNameQualifier<AsylumCase> fileNameQualifier,
        DocumentBundler documentBundler,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        HearingBundleOrder hearingBundleOrder
    ) {
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.fileNameQualifier = fileNameQualifier;
        this.documentBundler = documentBundler;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.hearingBundleOrder = hearingBundleOrder;
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

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final String qualifiedDocumentFileName = fileNameQualifier.get(fileName + "." + fileExtension, caseDetails);

        Optional<List<IdValue<DocumentWithMetadata>>> maybeLegalRepresentativeDocuments = asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS);
        Optional<List<IdValue<DocumentWithMetadata>>> maybeRespondentDocuments = asylumCase.read(RESPONDENT_DOCUMENTS);
        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments = asylumCase.read(HEARING_DOCUMENTS);

        List<DocumentWithMetadata> bundleDocuments =
            Stream.concat(
                    maybeLegalRepresentativeDocuments
                    .orElse(Collections.emptyList())
                    .stream(),
                Stream.concat(
                    maybeRespondentDocuments
                        .orElse(Collections.emptyList())
                        .stream(),
                    maybeHearingDocuments
                        .orElse(Collections.emptyList())
                        .stream()
                ))
                .map(IdValue::getValue)
                .filter(document -> document.getTag() != DocumentTag.HEARING_BUNDLE)
                .sorted(hearingBundleOrder)
                .collect(Collectors.toList());

        Document hearingBundle = documentBundler.bundle(
            bundleDocuments,
            "Hearing documents",
            qualifiedDocumentFileName
        );

        attachDocumentToCase(asylumCase, hearingBundle);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void attachDocumentToCase(
        AsylumCase asylumCase,
        Document hearingBundle
    ) {

        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments = asylumCase
                .read(HEARING_DOCUMENTS);

        final List<IdValue<DocumentWithMetadata>> hearingDocuments =
                maybeHearingDocuments
                        .orElse(Collections.emptyList());

        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                hearingBundle,
                "",
                DocumentTag.HEARING_BUNDLE
            );

        List<IdValue<DocumentWithMetadata>> allHearingDocuments =
            documentsAppender.append(
                hearingDocuments,
                Collections.singletonList(documentWithMetadata),
                DocumentTag.HEARING_BUNDLE
            );

        asylumCase.write(HEARING_DOCUMENTS, allHearingDocuments);
    }
}
