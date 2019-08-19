package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
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

        assertThat(documentBundle.getDocumentUrl()).isEqualTo(returnedDocument.getDocumentUrl());
        assertThat(documentBundle.getDocumentBinaryUrl()).isEqualTo(returnedDocument.getDocumentBinaryUrl());
        assertThat(documentBundle.getDocumentFilename()).isEqualTo(returnedDocument.getDocumentFilename());
        assertThat(documentBundle.getDocumentFilename()).isEqualTo(returnedDocument.getDocumentFilename());

        ArgumentCaptor<Callback<BundleCaseData>> captor = ArgumentCaptor.forClass(Callback.class);

        verify(bundleRequestExecutor).post(
            captor.capture(),
            eq(BUNDLE_URL + BUNDLE_STITCH_URL)
        );

        IdValue<Bundle> bundleIdValue =
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

        assertThat(caseDataCallback.getEvent()).isEqualTo(Event.GENERATE_HEARING_BUNDLE);
        assertThat(caseDataCallback.getCaseDetails().getState()).isEqualTo(State.UNKNOWN);
        assertThat(caseDataCallback.getCaseDetails().getJurisdiction()).isEqualTo("IA");
        assertThat(caseDataCallback.getCaseDetails().getId()).isEqualTo(1L);
        assertThat(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().size()).isEqualTo(1);
        assertThat(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().get(0).getId()).isEqualTo("1");
        assertThat(caseDataCallback.getCaseDetails().getCaseData().getCaseBundles().get(0).getValue())
            .isEqualToIgnoringGivenFields(bundleIdValue.getValue(), "documents");

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
            .hasMessage("Bundle was not created");

    }

    @Test
    public void should_throw_if_no_stitched_document_returned() {

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
            .hasMessage("Stitched document was not created");
    }

}
