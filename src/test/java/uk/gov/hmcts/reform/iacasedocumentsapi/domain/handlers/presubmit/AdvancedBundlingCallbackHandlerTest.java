package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.EmBundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class AdvancedBundlingCallbackHandlerTest {

    @Mock private EmBundleRequestExecutor emBundleRequestExecutor;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private Document document;

    private String emBundlerUrl = "bundleurl";
    private String emBundlerStitchUri = "stitchingUri";
    private String appealReference = "PA/50002/2020";
    private String appellantFamilyName = "bond";
    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    private AdvancedBundlingCallbackHandler advancedBundlingCallbackHandler;

    @BeforeEach
    void setUp() {
        advancedBundlingCallbackHandler =
            new AdvancedBundlingCallbackHandler(
                emBundlerUrl,
                emBundlerStitchUri,
                emBundleRequestExecutor);

        when(callback.getEvent()).thenReturn(Event.GENERATE_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(emBundleRequestExecutor.post(callback, emBundlerUrl + emBundlerStitchUri)).thenReturn(callbackResponse);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReference));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(callbackResponse.getData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle = new Bundle("id", "title", "desc", "yes", Collections.emptyList(), Optional.of("NEW"), Optional.empty(), YesOrNo.YES, YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SUITABLE", "UNSUITABLE"})
    void should_successfully_handle_the_callback(String maybeDecision) {
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(maybeDecision.isEmpty()
                ? Optional.empty() : Optional.of(AdaSuitabilityReviewDecision.valueOf(maybeDecision)));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_REFERENCE_NUMBER, String.class);
        verify(asylumCase, times(1)).read(APPELLANT_FAMILY_NAME, String.class);

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase).write(AsylumCaseDefinition.HMCTS, "[userImage:hmcts.png]");
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION,
                maybeDecision.isEmpty() ? "iac-hearing-bundle-config.yaml" : "iac-hearing-bundle-inc-tribunal-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);

    }

    @Test
    void should_successfully_handle_the_callback_in_reheard() {

        DocumentWithMetadata addendumEvidenceAppellantDocument = new DocumentWithMetadata(
            document, "test","2020-12-12", DocumentTag.ADDENDUM_EVIDENCE, "The appellant");
        DocumentWithMetadata addendumEvidenceRespondentDocument = new DocumentWithMetadata(
            document, "test","2020-12-12", DocumentTag.ADDENDUM_EVIDENCE, "The respondent");
        List<IdValue<DocumentWithMetadata>> documents = new ArrayList<>();
        documents.add(new IdValue<DocumentWithMetadata>("0", addendumEvidenceAppellantDocument));
        documents.add(new IdValue<DocumentWithMetadata>("1", addendumEvidenceRespondentDocument));

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(documents));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_REFERENCE_NUMBER, String.class);
        verify(asylumCase, times(1)).read(APPELLANT_FAMILY_NAME, String.class);

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-reheard-hearing-bundle-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);

        documents.remove(1);
        verify(asylumCase, times(2)).read(ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase).write(AsylumCaseDefinition.APPELLANT_ADDENDUM_EVIDENCE_DOCS, documents);
        documents.clear();
        documents.add(new IdValue<DocumentWithMetadata>("1", addendumEvidenceRespondentDocument));
        //verify(asylumCase).write(AsylumCaseDefinition.RESPONDENT_ADDENDUM_EVIDENCE_DOCS, documents);

        verify(asylumCase, times(1)).read(ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).read(RESPONDENT_DOCUMENTS);
        verify(asylumCase, never()).read(TRIBUNAL_DOCUMENTS);
        verify(asylumCase).write(AsylumCaseDefinition.APP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());
        verify(asylumCase).write(AsylumCaseDefinition.RESP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());
    }

    @Test
    void should_throw_when_appeal_reference_is_not_present() {

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealReferenceNumber is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_appellant_family_name_is_not_present() {

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_not_present() {

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseBundle is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_empty() {

        caseBundles.clear();

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("case bundles size is not 1 and is : 0")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = advancedBundlingCallbackHandler.canHandle(callbackStage, callback);

                if (event == Event.GENERATE_HEARING_BUNDLE
                    && callbackStage == ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> advancedBundlingCallbackHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
