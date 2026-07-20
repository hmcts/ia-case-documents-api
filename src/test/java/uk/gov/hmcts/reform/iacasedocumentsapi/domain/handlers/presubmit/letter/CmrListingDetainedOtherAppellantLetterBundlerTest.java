package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.CMR_LISTING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority.LATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CmrListingDetainedOtherAppellantLetterBundlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock private DocumentBundler documentBundler;
    @Mock private DocumentHandler documentHandler;
    @Mock private Document bundleDocument;

    private final String fileExtension = "PDF";
    private final String fileName = "some-file-name";

    private CmrListingDetainedOtherAppellantLetterBundler cmrListingDetainedOtherAppellantLetterBundler;

    @BeforeEach
    public void setUp() {
        cmrListingDetainedOtherAppellantLetterBundler = buildBundler(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));
    }

    private CmrListingDetainedOtherAppellantLetterBundler buildBundler(boolean isEmStitchingEnabled) {
        return new CmrListingDetainedOtherAppellantLetterBundler(
            fileExtension,
            fileName,
            isEmStitchingEnabled,
            fileNameQualifier,
            documentBundler,
            documentHandler);
    }

    @Test
    public void it_can_handle_callback() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertTrue(cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CMR_LISTING"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_for_non_cmr_listing_events(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_for_wrong_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertFalse(cmrListingDetainedOtherAppellantLetterBundler.canHandle(stage, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_not_rep_journey() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));

        assertFalse(cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_internal_case() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertFalse(cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_not_detained_in_other_facility() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("prison"));

        assertFalse(cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_not_detained() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_stitching_flag_is_false() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        cmrListingDetainedOtherAppellantLetterBundler = buildBundler(false);

        assertFalse(cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_read_and_bundle_letter_notification_documents() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("filename");

        IdValue<DocumentWithMetadata> doc1 = new IdValue<>("1", createDocumentWithMetadata());
        IdValue<DocumentWithMetadata> doc2 = new IdValue<>("2", createDocumentWithMetadata());

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(doc1, doc2)));
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            anyList(),
            eq("Letter bundle documents"),
            eq("filename")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response =
            cmrListingDetainedOtherAppellantLetterBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.INTERNAL_CMR_LISTING_LETTER_BUNDLE);
    }

    @Test
    void should_have_late_dispatch_priority() {
        assertThat(cmrListingDetainedOtherAppellantLetterBundler.getDispatchPriority()).isEqualTo(LATE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertThatThrownBy(() -> cmrListingDetainedOtherAppellantLetterBundler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> cmrListingDetainedOtherAppellantLetterBundler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> cmrListingDetainedOtherAppellantLetterBundler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> cmrListingDetainedOtherAppellantLetterBundler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private Document createDocumentWithDescription() {
        return new Document("some-url",
            "some-binary-url",
            RandomStringUtils.secure().nextAlphabetic(20));
    }

    private DocumentWithMetadata createDocumentWithMetadata() {
        return new DocumentWithMetadata(createDocumentWithDescription(),
            RandomStringUtils.secure().nextAlphabetic(20),
            new SystemDateProvider().now().toString(), DocumentTag.INTERNAL_CMR_LISTING_LETTER, "test");
    }
}
