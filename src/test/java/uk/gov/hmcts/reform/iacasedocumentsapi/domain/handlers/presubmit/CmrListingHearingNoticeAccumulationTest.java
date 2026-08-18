package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CMR_IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getMaybeNotificationAttachmentDocuments;

import java.util.List;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

/**
 * Drives {@link CmrListingHearingNoticeCreator} against a real {@link DocumentHandler} so the
 * accumulation of INTERNAL_CMR_LISTING_LETTER documents across repeated cmrListing events is
 * observable. A mocked DocumentHandler cannot show this — the bug lives in which append strategy
 * the handler delegates to, not in whether it is called.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CmrListingHearingNoticeAccumulationTest {

    @Mock private DocumentCreator<AsylumCase> cmrHearingNoticeDocumentCreator;
    @Mock private DocumentCreator<AsylumCase> remoteCmrHearingNoticeDocumentCreator;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    private AsylumCase asylumCase;
    private CmrListingHearingNoticeCreator cmrListingHearingNoticeCreator;

    @BeforeEach
    void setUp() {
        DocumentHandler documentHandler = new DocumentHandler(
            new DocumentReceiver(new SystemDateProvider()),
            new DocumentsAppender()
        );

        cmrListingHearingNoticeCreator = new CmrListingHearingNoticeCreator(
            cmrHearingNoticeDocumentCreator,
            remoteCmrHearingNoticeDocumentCreator,
            documentHandler
        );

        asylumCase = new AsylumCase();
        asylumCase.write(CMR_IS_REMOTE_HEARING, YesOrNo.NO);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CMR_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = DetentionFacility.class, names = {"IRC", "PRISON"})
    void should_keep_only_the_latest_cmr_listing_letter_across_repeated_listings(DetentionFacility detentionFacility) {

        asylumCase.write(APPELLANT_IN_DETENTION, YesOrNo.YES);
        asylumCase.write(DETENTION_FACILITY, detentionFacility.getValue());

        Document firstNotice = new Document("first-url", "first-binary-url", "first-hearing-notice.PDF");
        Document secondNotice = new Document("second-url", "second-binary-url", "second-hearing-notice.PDF");

        when(cmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(firstNotice);
        cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        when(cmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(secondNotice);
        cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        List<DocumentWithMetadata> bundleDocuments =
            getMaybeNotificationAttachmentDocuments(asylumCase, DocumentTag.INTERNAL_CMR_LISTING_LETTER);

        assertEquals(1, bundleDocuments.size(),
            "a re-run of cmrListing must not leave the previous listing's letter behind to be bundled");
        assertEquals(secondNotice.getDocumentFilename(), bundleDocuments.getFirst().getDocument().getDocumentFilename());
    }

    @Test
    void should_not_discard_documents_carrying_other_tags() {

        asylumCase.write(APPELLANT_IN_DETENTION, YesOrNo.YES);
        asylumCase.write(DETENTION_FACILITY, DetentionFacility.IRC.getValue());

        Document unrelatedDocument = new Document("other-url", "other-binary-url", "other.PDF");
        DocumentHandler documentHandler = new DocumentHandler(
            new DocumentReceiver(new SystemDateProvider()),
            new DocumentsAppender()
        );
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            unrelatedDocument,
            uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_CASE_LISTED_LETTER
        );

        when(cmrHearingNoticeDocumentCreator.create(caseDetails))
            .thenReturn(new Document("url", "binary-url", "hearing-notice.PDF"));
        cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        List<DocumentWithMetadata> otherTagged =
            getMaybeNotificationAttachmentDocuments(asylumCase, DocumentTag.INTERNAL_CASE_LISTED_LETTER);

        assertEquals(1, otherTagged.size(), "replacing by tag must not touch documents with other tags");
        assertEquals(unrelatedDocument.getDocumentFilename(), otherTagged.getFirst().getDocument().getDocumentFilename());
    }

    @Test
    void should_append_when_a_later_handler_adds_another_letter_for_the_same_listing() {

        asylumCase.write(APPELLANT_IN_DETENTION, YesOrNo.YES);
        asylumCase.write(DETENTION_FACILITY, DetentionFacility.IRC.getValue());

        Document hearingNotice = new Document("notice-url", "notice-binary-url", "hearing-notice.PDF");
        Document detainedLetter = new Document("letter-url", "letter-binary-url", "detained-letter.PDF");

        when(cmrHearingNoticeDocumentCreator.create(caseDetails)).thenReturn(hearingNotice);
        cmrListingHearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        // DetainedCmrListingLetterGenerator runs later in the same event and appends under the same tag
        DocumentHandler documentHandler = new DocumentHandler(
            new DocumentReceiver(new SystemDateProvider()),
            new DocumentsAppender()
        );
        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            detainedLetter,
            uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_CMR_LISTING_LETTER
        );

        List<DocumentWithMetadata> bundleDocuments =
            getMaybeNotificationAttachmentDocuments(asylumCase, DocumentTag.INTERNAL_CMR_LISTING_LETTER);

        assertEquals(2, bundleDocuments.size(),
            "both documents produced for a single listing must reach the bundle");
    }
}
