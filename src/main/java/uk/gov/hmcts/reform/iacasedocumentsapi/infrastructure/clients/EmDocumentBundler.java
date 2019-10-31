package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleDocument;

@Slf4j
@Service
public class EmDocumentBundler implements DocumentBundler {

    private final String emBundlerUrl;
    private final String emBundlerStitchUri;
    private final DateProvider dateProvider;
    private final BundleRequestExecutor bundleRequestExecutor;

    public EmDocumentBundler(
        @Value("${emBundler.url}") String emBundlerUrl,
        @Value("${emBundler.stitch.uri}") String emBundlerStitchUri,
        DateProvider dateProvider,
        BundleRequestExecutor bundleRequestExecutor
    ) {
        this.emBundlerUrl = emBundlerUrl;
        this.emBundlerStitchUri = emBundlerStitchUri;
        this.dateProvider = dateProvider;
        this.bundleRequestExecutor = bundleRequestExecutor;
    }

    public Document bundle(
        List<DocumentWithMetadata> documents,
        String bundleTitle,
        String bundleFilename
    ) {

        Callback<BundleCaseData> payload =
            createBundlePayload(
                documents,
                bundleTitle,
                bundleFilename
            );

        PreSubmitCallbackResponse<BundleCaseData> response =
            bundleRequestExecutor.post(
                payload,
                emBundlerUrl + emBundlerStitchUri
            );

        Document bundle =
            response
                .getData()
                .getCaseBundles()
                .stream()
                .findFirst()
                .orElseThrow(() -> new DocumentBundlingErrorResponseException("Bundle was not created", response))
                .getValue()
                .getStitchedDocument()
                .orElseThrow(() -> new DocumentBundlingErrorResponseException("Stitched document was not created", response));

        // rename the bundle file name
        return new Document(
            bundle.getDocumentUrl(),
            bundle.getDocumentBinaryUrl(),
            bundleFilename
        );

    }

    private Callback<BundleCaseData> createBundlePayload(
        List<DocumentWithMetadata> documents,
        String bundleTitle,
        String bundleFilename
    ) {

        //create the bundle documents as a list of IdValue
        List<IdValue<BundleDocument>> bundleDocuments = new ArrayList<>();

        for (int i = 0; i < documents.size(); i++) {

            DocumentWithMetadata caseDocument = documents.get(i);

            bundleDocuments.add(
                new IdValue<>(
                    String.valueOf(i),
                    new BundleDocument(
                        caseDocument.getDocument().getDocumentFilename(),
                        caseDocument.getDescription(),
                        i,
                        caseDocument.getDocument()
                    )
                )
            );
        }

        return
            new Callback<>(
                new CaseDetails<>(
                    1L,
                    "IA",
                    State.UNKNOWN,
                    new BundleCaseData(
                        Collections.singletonList(
                            new IdValue<>(
                                "1",
                                new Bundle(
                                    "1",
                                    bundleTitle,
                                    "",
                                    "yes",
                                    bundleDocuments,
                                    bundleFilename
                                )
                            )
                        )
                    ),
                    dateProvider.nowWithTime()
                ),
                Optional.empty(),
                Event.GENERATE_HEARING_BUNDLE
            );
    }
}
