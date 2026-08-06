package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_BUNDLE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentBundler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FileNameQualifier;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalCmrListingLetterBundlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FileNameQualifier<AsylumCase> fileNameQualifier;
    @Mock private DocumentBundler documentBundler;
    @Mock private DocumentHandler documentHandler;
    @Mock private Document bundleDocument;

    private final String fileExtension = "PDF";
    private final String fileName = "internal-cmr-listing-letter-with-attachment";

    private InternalCmrListingLetterBundler internalCmrListingLetterBundler;

    @BeforeEach
    public void setUp() {
        internalCmrListingLetterBundler = buildBundler(true);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(NO));
    }

    private InternalCmrListingLetterBundler buildBundler(boolean isEmStitchingEnabled) {
        return new InternalCmrListingLetterBundler(
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

        assertTrue(internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CMR_LISTING"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_for_non_cmr_listing_events(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertFalse(internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    public void it_cannot_handle_callback_for_wrong_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertFalse(internalCmrListingLetterBundler.canHandle(stage, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_not_internal_case() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertFalse(internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_can_handle_callback_when_appellant_detained_in_other_facility() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(DetentionFacility.OTHER.getValue()));

        assertTrue(internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = DetentionFacility.class, names = {"PRISON", "IRC"})
    public void it_cannot_handle_callback_when_appellant_detained_in_prison_or_irc(DetentionFacility detentionFacility) {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of(detentionFacility.getValue()));

        assertFalse(internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_can_handle_callback_when_submitted_as_legal_represented_internal_case() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertTrue(internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    public void it_cannot_handle_callback_when_stitching_flag_is_false() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        internalCmrListingLetterBundler = buildBundler(false);

        assertFalse(internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_bundle_cmr_listing_letters_and_append_to_letter_bundle_documents() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);
        when(fileNameQualifier.get(anyString(), eq(caseDetails))).thenReturn("qualified-filename.PDF");

        IdValue<DocumentWithMetadata> cmrLetter = new IdValue<>("1", documentWithTag(DocumentTag.INTERNAL_CMR_LISTING_LETTER));
        IdValue<DocumentWithMetadata> otherLetter = new IdValue<>("2", documentWithTag(DocumentTag.INTERNAL_CASE_LISTED_LETTER));

        when(asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS)).thenReturn(Optional.of(List.of(cmrLetter, otherLetter)));
        when(documentBundler.bundleWithoutContentsOrCoverSheets(
            eq(List.of(cmrLetter.getValue())),
            eq("Letter bundle documents"),
            eq("qualified-filename.PDF")
        )).thenReturn(bundleDocument);

        PreSubmitCallbackResponse<AsylumCase> response =
            internalCmrListingLetterBundler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(fileNameQualifier, times(1)).get(fileName + "." + fileExtension, caseDetails);
        verify(documentBundler, times(1)).bundleWithoutContentsOrCoverSheets(
            List.of(cmrLetter.getValue()), "Letter bundle documents", "qualified-filename.PDF");
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, bundleDocument, LETTER_BUNDLE_DOCUMENTS, DocumentTag.INTERNAL_CMR_LISTING_LETTER_BUNDLE);
    }

    @Test
    void should_have_late_dispatch_priority() {
        assertThat(internalCmrListingLetterBundler.getDispatchPriority()).isEqualTo(LATE);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(CMR_LISTING);

        assertThatThrownBy(() -> internalCmrListingLetterBundler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> internalCmrListingLetterBundler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> internalCmrListingLetterBundler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalCmrListingLetterBundler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private DocumentWithMetadata documentWithTag(DocumentTag tag) {
        Document document = new Document("some-url",
            "some-binary-url",
            RandomStringUtils.secure().nextAlphabetic(20));

        return new DocumentWithMetadata(document,
            RandomStringUtils.secure().nextAlphabetic(20),
            new SystemDateProvider().now().toString(), tag, "test");
    }
}
