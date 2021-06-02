package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.EmBundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.WARN)
class UpperTribunalBundleHandlerTest {

    @Mock private EmBundleRequestExecutor emBundleRequestExecutor;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private String emBundlerUrl = "bundleurl";
    private String emBundlerStitchUri = "stitchingUri";
    private String appealReference = "PA/50002/2020";
    private String appellantFamilyName = "bond";
    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();
    private UpperTribunalBundleHandler upperTribunalBundleHandler;

    @BeforeEach
    void setUp() {
        upperTribunalBundleHandler =
            new UpperTribunalBundleHandler(
                emBundlerUrl,
                emBundlerStitchUri,
                emBundleRequestExecutor);

        when(callback.getEvent()).thenReturn(Event.GENERATE_UPPER_TRIBUNAL_BUNDLE);
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

    @Test
    void should_successfully_handle_the_callback() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            upperTribunalBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_REFERENCE_NUMBER, String.class);
        verify(asylumCase, times(1)).write(STITCHING_STATUS_UPPER_TRIBUNAL, "NEW");
        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase).write(AsylumCaseDefinition.HMCTS, "[userImage:hmcts.png]");
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-upper-tribunal-bundle-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020");
    }

    @Test
    void should_throw_when_appeal_reference_is_not_present() {

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> upperTribunalBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealReferenceNumber is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_not_present() {

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> upperTribunalBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseBundle is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_when_case_bundle_is_empty() {

        caseBundles.clear();

        assertThatThrownBy(() -> upperTribunalBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("case bundles size is not 1 and is : 0")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> upperTribunalBundleHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> upperTribunalBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = upperTribunalBundleHandler.canHandle(callbackStage, callback);

                if (event == Event.GENERATE_UPPER_TRIBUNAL_BUNDLE
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

        assertThatThrownBy(() -> upperTribunalBundleHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> upperTribunalBundleHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> upperTribunalBundleHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> upperTribunalBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
