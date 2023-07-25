package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleDocument;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDetHearingBundleReadyHandlerTest {

    @Mock private DocumentCreator<AsylumCase> internalAdaGenerateHearingBundleDocumentCreator;
    @Mock private DocumentHandler documentHandler;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;
    private final YesOrNo yes = YesOrNo.YES;
    private final String bundleId = someRandomString();
    private final String bundleTitle = someRandomString();
    private final String bundleDescription = someRandomString();
    private final String bundleEligibleForStitching = someRandomString();
    private final List<IdValue<BundleDocument>> bundleDocuments = singletonList(mock(IdValue.class));
    private final Optional<String> bundleStitchStatus = Optional.of("DONE");
    private final Optional<Document> bundleStitchedDocument = Optional.of(mock(Document.class));
    private final YesOrNo bundleHasCoversheets = YesOrNo.YES;
    private final YesOrNo bundleHasTableOfContents = YesOrNo.NO;
    private final String bundleFilename = someRandomString();
    private final Bundle bundle = new Bundle(
            bundleId,
            bundleTitle,
            bundleDescription,
            bundleEligibleForStitching,
            bundleDocuments,
            bundleStitchStatus,
            bundleStitchedDocument,
            bundleHasCoversheets,
            bundleHasTableOfContents,
            bundleFilename
            );
    private final IdValue bundleEntry = new IdValue("1", bundle);

    private List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    private InternalDetHearingBundleReadyHandler internalDetHearingBundleReadyHandler;

    @BeforeEach
    public void setUp() {
        internalDetHearingBundleReadyHandler =
                new InternalDetHearingBundleReadyHandler(
                        internalAdaGenerateHearingBundleDocumentCreator,
                        documentHandler
                );

        when(caseDetails.getState()).thenReturn(State.FINAL_BUNDLING);
        when(callback.getEvent()).thenReturn(Event.ASYNC_STITCHING_COMPLETE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(yes));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(yes));

        caseBundles.add(bundleEntry);

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

    }

    @Test
    public void should_create_hearing_bundle_ready_letter_and_append_to_notification_attachment_documents() {

        when(internalAdaGenerateHearingBundleDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalDetHearingBundleReadyHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadata(asylumCase, uploadedDocument, NOTIFICATION_ATTACHMENT_DOCUMENTS, DocumentTag.HEARING_BUNDLE_READY_LETTER);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> internalDetHearingBundleReadyHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalDetHearingBundleReadyHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_cannot_handle_callback_if_is_admin_is_missing() {
        // isAdmin defaults to false if not present

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetHearingBundleReadyHandler.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_is_detained_is_missing() {
        // isAda defaults to false if not present

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.empty());

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetHearingBundleReadyHandler.canHandle(callbackStage, callback);
                assertFalse(canHandle);
            }
            reset(callback);
        }
    }

    @Test
    public void it_should_only_handle_about_to_submit_and_async_stitching_complete_event() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = internalDetHearingBundleReadyHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && callback.getEvent().equals(Event.ASYNC_STITCHING_COMPLETE)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    public void it_should_only_handle_internal_cases(YesOrNo yesOrNo) {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));

        boolean canHandle = internalDetHearingBundleReadyHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (yesOrNo == yes) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    public void it_should_only_handle_internal_detained_cases(YesOrNo yesOrNo) {

        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.ofNullable(yesOrNo));

        boolean canHandle = internalDetHearingBundleReadyHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        if (yesOrNo == yes) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @Test
    public void it_should_only_handle_when_case_bundle_is_provided() {

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertFalse(internalDetHearingBundleReadyHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DONE", "", "FAILED", "someRandomString"})
    public void it_should_only_handle_when_stitching_is_successful(String stitchStatus) {
        final Bundle testBundle = new Bundle(
                bundleId,
                bundleTitle,
                bundleDescription,
                bundleEligibleForStitching,
                bundleDocuments,
                Optional.of(stitchStatus),
                bundleStitchedDocument,
                bundleHasCoversheets,
                bundleHasTableOfContents,
                bundleFilename
        );
        final IdValue testBundleEntry = new IdValue("1", testBundle);
        caseBundles.clear();
        caseBundles.add(testBundleEntry);

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        if (stitchStatus.equals(bundleStitchStatus.get())) {
            assertTrue(internalDetHearingBundleReadyHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        } else {
            assertFalse(internalDetHearingBundleReadyHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalDetHearingBundleReadyHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetHearingBundleReadyHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetHearingBundleReadyHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalDetHearingBundleReadyHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    private String someRandomString() {
        return randomAlphabetic(8);
    }

}
