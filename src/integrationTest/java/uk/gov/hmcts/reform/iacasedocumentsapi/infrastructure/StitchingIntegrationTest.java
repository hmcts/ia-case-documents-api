package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;

import com.google.common.collect.Lists;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.BundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.ClassCreatorForTests;

@SuppressWarnings("unchecked")
public class StitchingIntegrationTest extends SpringBootIntegrationTest {

    @MockBean
    private BundleRequestExecutor bundleRequestExecutor;

    @Mock
    private BundleCaseData bundleCaseData;

    @Mock
    private Bundle bundle;

    @Mock
    private IdValue<Bundle> bundleIdValue;

    @Mock
    private Document bundleDocument;

    @Autowired
    private ClassCreatorForTests classCreatorForTests;

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_return_200_when_api_returns_200_with_a_stitched_bundle() {

        List<IdValue<Bundle>> caseBundles = Lists.newArrayList(bundleIdValue);
        AsylumCase asylumCase = AsylumCaseFixtures.someAsylumCaseWithDefaults();

        PreSubmitCallbackResponse<BundleCaseData> stitchServiceResponse = new PreSubmitCallbackResponse<>(bundleCaseData);

        when(bundleRequestExecutor.post(any(Callback.class), anyString())).thenReturn(stitchServiceResponse);
        when(bundleCaseData.getCaseBundles()).thenReturn(caseBundles);
        when(bundleIdValue.getValue()).thenReturn(bundle);
        when(bundle.getStitchedDocument()).thenReturn(Optional.of(bundleDocument));
        when(bundleDocument.getDocumentBinaryUrl()).thenReturn("bundle-binary-url");
        when(bundleDocument.getDocumentUrl()).thenReturn("bundle-document-url");

        PreSubmitCallbackResponseForTest response = iaCaseDocumentsApiClient.aboutToSubmit(callback()
            .caseDetails(someCaseDetailsWith()
                .jurisdiction("IA")
                .state(State.FINAL_BUNDLING)
                .id(1L)
                .caseData(anAsylumCase()
                    .withCaseDetails(asylumCase)
                ))
            .event(Event.GENERATE_HEARING_BUNDLE)
        );

        assertThat(response).isNotNull();

        Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingDocuments =
            response.getData().read(AsylumCaseDefinition.HEARING_DOCUMENTS);

        assertThat(maybeHearingDocuments).isPresent();

        DocumentWithMetadata documentWithMetadata =
            maybeHearingDocuments.orElseThrow(() -> new IllegalStateException("No Hearing documents in test"))
                .stream()
                .map(IdValue::getValue).filter(doc -> doc.getTag().equals(DocumentTag.HEARING_BUNDLE))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Bundle returned in test"));

        assertThat(documentWithMetadata.getDateUploaded()).isEqualTo(LocalDate.now().toString());

        Document responseDoc = documentWithMetadata.getDocument();

        assertThat(responseDoc.getDocumentBinaryUrl()).isEqualTo("bundle-binary-url");
        assertThat(responseDoc.getDocumentUrl()).isEqualTo("bundle-document-url");

        verify(bundleRequestExecutor).post(any(Callback.class), anyString());
        verifyNoMoreInteractions(bundleRequestExecutor);

    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_return_500_when_api_returns_200_with_empty_case_bundle_list() {
        List<IdValue<Bundle>> caseBundles = Lists.newArrayList();

        AsylumCase asylumCase = AsylumCaseFixtures.someAsylumCaseWithDefaults();

        PreSubmitCallbackResponse<BundleCaseData> stitchServiceResponse = new PreSubmitCallbackResponse<>(bundleCaseData);

        when(bundleRequestExecutor.post(any(Callback.class), anyString())).thenReturn(stitchServiceResponse);
        when(bundleCaseData.getCaseBundles()).thenReturn(caseBundles);

        assertThatThrownBy(
            () -> iaCaseDocumentsApiClient.aboutToSubmit(callback()
                .caseDetails(someCaseDetailsWith()
                    .jurisdiction("IA")
                    .state(State.FINAL_BUNDLING)
                    .id(1L)
                    .caseData(anAsylumCase()
                        .withCaseDetails(asylumCase)
                    ))
                .event(Event.GENERATE_HEARING_BUNDLE)))
            .hasMessageContaining("500")
            .hasMessageContaining("Bundle was not created");
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_return_500_when_api_returns_200_with_null_bundle() throws Exception {

        List<IdValue<Bundle>> caseBundles = Lists.newArrayList(bundleIdValue);
        Bundle nullBundle = classCreatorForTests.findPrivateNoArgsConstructor(Bundle.class).newInstance();

        AsylumCase asylumCase = AsylumCaseFixtures.someAsylumCaseWithDefaults();

        PreSubmitCallbackResponse<BundleCaseData> stitchServiceResponse = new PreSubmitCallbackResponse<>(bundleCaseData);

        when(bundleRequestExecutor.post(any(Callback.class), anyString())).thenReturn(stitchServiceResponse);
        when(bundleCaseData.getCaseBundles()).thenReturn(caseBundles);
        when(bundleIdValue.getValue()).thenReturn(nullBundle);

        assertThatThrownBy(
            () -> iaCaseDocumentsApiClient.aboutToSubmit(callback()
                .caseDetails(someCaseDetailsWith()
                    .jurisdiction("IA")
                    .state(State.FINAL_BUNDLING)
                    .id(1L)
                    .caseData(anAsylumCase()
                        .withCaseDetails(asylumCase)
                    ))
                .event(Event.GENERATE_HEARING_BUNDLE)))
            .hasMessageContaining("500")
            .hasMessageContaining("Stitched document was not created");
    }

}
