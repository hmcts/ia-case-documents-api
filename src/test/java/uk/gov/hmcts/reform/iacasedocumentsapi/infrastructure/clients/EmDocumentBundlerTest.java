package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.em.BundleCaseData;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class EmDocumentBundlerTest {

    private static final String BUNDLE_URL = "em-bundle-url";
    private static final String BUNDLE_STITCH_URL = "em-bundle-stitch-url";

    @Mock private DateProvider dateProvider;
    @Mock private BundleRequestExecutor bundleRequestExecutor;
    @Mock private PreSubmitCallbackResponse<BundleCaseData> callbackResponse;

    @Mock private BundleCaseData bundleCaseData;
    @Mock private Bundle bundle;

    private Document returnedDocument;

    private EmDocumentBundler emDocumentBundler;

    @BeforeEach
    public void setUp() {

        String bundleFilename = "some-bundle-filename";

        returnedDocument = new Document("http://some-url",
            "documentBinaryUrl",
            bundleFilename);

        emDocumentBundler = new EmDocumentBundler(
            BUNDLE_URL,
            BUNDLE_STITCH_URL,
            dateProvider,
            bundleRequestExecutor
        );

    }

    @Test
    public void should_initiate_bundle_creation_and_handle_response() {

        List<IdValue<Bundle>> bundleIdValues =
            ImmutableList.of(new IdValue<>("1", bundle));

        DocumentWithMetadata docMeta1 = mock(DocumentWithMetadata.class);
        DocumentWithMetadata docMeta2 = mock(DocumentWithMetadata.class);
        Document doc1 = mock(Document.class);
        Document doc2 = mock(Document.class);

        List<DocumentWithMetadata> documents = ImmutableList.of(docMeta1, docMeta2);
        String bundleTitle = "some-bundle-title";
        String bundleFilename = "some-bundle-filename";

        when(docMeta1.getDocument()).thenReturn(doc1);
        when(docMeta1.getDescription()).thenReturn("test-desc1");
        when(doc1.getDocumentFilename()).thenReturn("test-filename1");

        when(docMeta2.getDocument()).thenReturn(doc2);
        when(docMeta2.getDescription()).thenReturn("test-desc2");
        when(doc2.getDocumentFilename()).thenReturn("test-filename2");

        when(bundleRequestExecutor.post(
            any(Callback.class),
            eq(BUNDLE_URL + BUNDLE_STITCH_URL)))
            .thenReturn(callbackResponse
            );
        when(callbackResponse.getData()).thenReturn(bundleCaseData);
        when(bundleCaseData.getCaseBundles()).thenReturn(bundleIdValues);
        when(bundle.getStitchedDocument()).thenReturn(Optional.of(returnedDocument));

        Document documentBundle = emDocumentBundler.bundle(
            documents,
            bundleTitle,
            bundleFilename
        );

        assertEquals(documentBundle.getDocumentUrl(), returnedDocument.getDocumentUrl());
        assertEquals(documentBundle.getDocumentBinaryUrl(), returnedDocument.getDocumentBinaryUrl());
        assertEquals(documentBundle.getDocumentFilename(), returnedDocument.getDocumentFilename());
        assertEquals(documentBundle.getDocumentFilename(), returnedDocument.getDocumentFilename());

        ArgumentCaptor<Callback<BundleCaseData>> captor = ArgumentCaptor.forClass(Callback.class);

        verify(bundleRequestExecutor).post(
            captor.capture(),
            eq(BUNDLE_URL + BUNDLE_STITCH_URL)
        );

        final IdValue<Bundle> bundleIdValue =
            new IdValue<>(
                "1",
                new Bundle(
                    "1",
                    bundleTitle,
                    "",
                    "yes",
                    null,
                    bundleFilename
                )
            );

        Callback<BundleCaseData> caseDataCallback = captor.getValue();

        assertEquals(caseDataCallback.getEvent(), Event.GENERATE_HEARING_BUNDLE);
        assertEquals(caseDataCallback.getCaseDetails().getState(), State.UNKNOWN);
        assertEquals(caseDataCallback.getCaseDetails().getJurisdiction(), "IA");
        assertEquals(caseDataCallback.getCaseDetails().getId(), 1L);
        assertEquals(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().size(), 1);
        assertEquals(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().get(0).getId(), "1");
        assertThat(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().get(0).getValue())
            .usingRecursiveComparison().ignoringFields("documents").isEqualTo(bundleIdValue.getValue());

    }

    @Test
    public void should_initiate_bundleWithoutContentsOrCoverSheets_creation_and_handle_response() {

        List<IdValue<Bundle>> bundleIdValues =
            ImmutableList.of(new IdValue<>("1", bundle));

        DocumentWithMetadata docMeta1 = mock(DocumentWithMetadata.class);
        DocumentWithMetadata docMeta2 = mock(DocumentWithMetadata.class);
        Document doc1 = mock(Document.class);
        Document doc2 = mock(Document.class);

        List<DocumentWithMetadata> documents = ImmutableList.of(docMeta1, docMeta2);
        String bundleTitle = "some-bundle-title";
        String bundleFilename = "some-bundle-filename";

        when(docMeta1.getDocument()).thenReturn(doc1);
        when(docMeta1.getDescription()).thenReturn("test-desc1");
        when(doc1.getDocumentFilename()).thenReturn("test-filename1");

        when(docMeta2.getDocument()).thenReturn(doc2);
        when(docMeta2.getDescription()).thenReturn("test-desc2");
        when(doc2.getDocumentFilename()).thenReturn("test-filename2");

        when(bundleRequestExecutor.post(
            any(Callback.class),
            eq(BUNDLE_URL + BUNDLE_STITCH_URL)))
            .thenReturn(callbackResponse
            );
        when(callbackResponse.getData()).thenReturn(bundleCaseData);
        when(bundleCaseData.getCaseBundles()).thenReturn(bundleIdValues);
        when(bundle.getStitchedDocument()).thenReturn(Optional.of(returnedDocument));

        Document documentBundle = emDocumentBundler.bundleWithoutContentsOrCoverSheets(
            documents,
            bundleTitle,
            bundleFilename
        );

        assertEquals(documentBundle.getDocumentUrl(), returnedDocument.getDocumentUrl());
        assertEquals(documentBundle.getDocumentBinaryUrl(), returnedDocument.getDocumentBinaryUrl());
        assertEquals(documentBundle.getDocumentFilename(), returnedDocument.getDocumentFilename());
        assertEquals(documentBundle.getDocumentFilename(), returnedDocument.getDocumentFilename());

        ArgumentCaptor<Callback<BundleCaseData>> captor = ArgumentCaptor.forClass(Callback.class);

        verify(bundleRequestExecutor).post(
            captor.capture(),
            eq(BUNDLE_URL + BUNDLE_STITCH_URL)
        );

        final IdValue<Bundle> bundleIdValue =
            new IdValue<>(
                "1",
                new Bundle(
                    "1",
                    bundleTitle,
                    "",
                    "yes",
                    null,
                    YesOrNo.NO,
                    YesOrNo.NO,
                    bundleFilename
                )
            );

        Callback<BundleCaseData> caseDataCallback = captor.getValue();

        assertEquals(caseDataCallback.getEvent(), Event.GENERATE_HEARING_BUNDLE);
        assertEquals(caseDataCallback.getCaseDetails().getState(), State.UNKNOWN);
        assertEquals(caseDataCallback.getCaseDetails().getJurisdiction(), "IA");
        assertEquals(caseDataCallback.getCaseDetails().getId(), 1L);
        assertEquals(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().size(), 1);
        assertEquals(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().get(0).getId(), "1");
        assertThat(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().get(0).getValue())
            .usingRecursiveComparison().ignoringFields("documents").isEqualTo(bundleIdValue.getValue());

    }

    @Test
    public void should_throw_if_no_bundle_returned() {

        DocumentWithMetadata docMeta1 = mock(DocumentWithMetadata.class);
        DocumentWithMetadata docMeta2 = mock(DocumentWithMetadata.class);
        Document doc1 = mock(Document.class);
        Document doc2 = mock(Document.class);

        List<DocumentWithMetadata> documents = ImmutableList.of(docMeta1, docMeta2);
        String bundleTitle = "some-bundle-title";
        String bundleFilename = "some-bundle-filename";

        when(docMeta1.getDocument()).thenReturn(doc1);
        when(docMeta1.getDescription()).thenReturn("test-desc1");
        when(doc1.getDocumentFilename()).thenReturn("test-filename1");

        when(docMeta2.getDocument()).thenReturn(doc2);
        when(docMeta2.getDescription()).thenReturn("test-desc2");
        when(doc2.getDocumentFilename()).thenReturn("test-filename2");

        when(bundleRequestExecutor.post(
            any(Callback.class),
            eq(BUNDLE_URL + BUNDLE_STITCH_URL)))
            .thenReturn(callbackResponse
            );
        when(callbackResponse.getData()).thenReturn(bundleCaseData);
        when(bundleCaseData.getCaseBundles()).thenReturn(Collections.EMPTY_LIST);

        assertThatThrownBy(() -> emDocumentBundler.bundle(
            documents,
            bundleTitle,
            bundleFilename
        ))
            .isInstanceOf(DocumentServiceResponseException.class)
            .hasMessage("Bundle was not created")
            .hasFieldOrProperty("stitchingServiceResponse");

    }

    @Test
    public void should_throw_if_empty_stitched_document_returned() {

        List<IdValue<Bundle>> bundleIdValues =
            ImmutableList.of(new IdValue<>("1", bundle));

        DocumentWithMetadata docMeta1 = mock(DocumentWithMetadata.class);
        DocumentWithMetadata docMeta2 = mock(DocumentWithMetadata.class);
        Document doc1 = mock(Document.class);
        Document doc2 = mock(Document.class);

        List<DocumentWithMetadata> documents = ImmutableList.of(docMeta1, docMeta2);
        String bundleTitle = "some-bundle-title";
        String bundleFilename = "some-bundle-filename";

        when(docMeta1.getDocument()).thenReturn(doc1);
        when(docMeta1.getDescription()).thenReturn("test-desc1");
        when(doc1.getDocumentFilename()).thenReturn("test-filename1");

        when(docMeta2.getDocument()).thenReturn(doc2);
        when(docMeta2.getDescription()).thenReturn("test-desc2");
        when(doc2.getDocumentFilename()).thenReturn("test-filename2");

        when(bundleRequestExecutor.post(
            any(Callback.class),
            eq(BUNDLE_URL + BUNDLE_STITCH_URL)))
            .thenReturn(callbackResponse
            );
        when(callbackResponse.getData()).thenReturn(bundleCaseData);
        when(bundleCaseData.getCaseBundles()).thenReturn(bundleIdValues);
        when(bundle.getStitchedDocument()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emDocumentBundler.bundle(
            documents,
            bundleTitle,
            bundleFilename
        ))
            .isInstanceOf(DocumentServiceResponseException.class)
            .hasMessage("Stitched document was not created")
            .hasFieldOrProperty("stitchingServiceResponse");
    }

}

