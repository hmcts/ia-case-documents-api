package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HearingNoticeEditedCreatorTest {

    @Mock
    private DocumentCreator<AsylumCase> hearingNoticeUpdatedRequirementsDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> hearingNoticeUpdatedDetailsDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> remoteHearingNoticeUpdatedDetailsDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> adaHearingNoticeUpdatedDetailsDocumentCreator;
    @Mock
    private HearingDetailsFinder hearingDetailsFinder;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private Document uploadedDocument;
    @Mock
    private DocumentWithMetadata uploadedDocumentWithMetadata;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DocumentReceiver documentReceiver;
    @Mock
    private DocumentsAppender documentsAppender;
    @Mock
    private Appender<ReheardHearingDocuments> reheardHearingAppender;
    @Mock
    private ReheardHearingDocuments mockReheardHearingDocuments;
    @Mock
    private List<IdValue<DocumentWithMetadata>> documentWithMetadataList;

    private HearingNoticeEditedCreator hearingNoticeEditedCreator;

    private final String hearingCentreNameBefore = HearingCentre.MANCHESTER.toString();
    private final String oldHearingDate = "2020-01-05T12:30:00";

    @BeforeEach
    public void setUp() {

        hearingNoticeEditedCreator =
            new HearingNoticeEditedCreator(
                hearingNoticeUpdatedRequirementsDocumentCreator,
                hearingNoticeUpdatedDetailsDocumentCreator,
                remoteHearingNoticeUpdatedDetailsDocumentCreator,
                adaHearingNoticeUpdatedDetailsDocumentCreator,
                documentHandler,
                hearingDetailsFinder,
                featureToggler,
                documentReceiver,
                documentsAppender,
                reheardHearingAppender
            );
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case_feature_flag_enabled() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @ParameterizedTest
    @CsvSource({"NO, NO, NO", "NO, NO, YES", "YES, YES, NO", "YES, YES, YES", "NO, YES, NO", "NO, YES, YES"})
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case_for_remote_hearing(
        YesOrNo enabledRefData,
        YesOrNo isRefDataRemoteHearing,
        YesOrNo isVirtualHearing) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(enabledRefData));

        if (enabledRefData.equals(YesOrNo.YES)) {
            when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isRefDataRemoteHearing));
        } else if (isVirtualHearing.equals(YES)) {
            when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.IAC_NATIONAL_VIRTUAL));
            when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(isVirtualHearing));
        } else {
            when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        }

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case_for_virtual_hearing() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @ParameterizedTest
    @CsvSource({"NO, NO", "YES, YES", "NO, YES"})
    void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case_for_remote_hearing_reheard_case(
        YesOrNo enabledRefData,
        YesOrNo isRefDataRemoteHearing) {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(enabledRefData));
        if (enabledRefData.equals(YesOrNo.YES)) {
            when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isRefDataRemoteHearing));
        } else {
            when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        }

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @Test
    void should_create_reheard_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
    }

    @Test
    void should_create_reheard_hearing_notice_pdf_and_make_new_reheard_collection_when_empty_and_dlrm_flag_on() {
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);
        when(documentReceiver.receive(uploadedDocument, "", DocumentTag.REHEARD_HEARING_NOTICE_RELISTED))
            .thenReturn(uploadedDocumentWithMetadata);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
        verify(documentReceiver, times(1)).receive(uploadedDocument, "", DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
        verify(documentsAppender).append(Collections.emptyList(), Collections.singletonList(uploadedDocumentWithMetadata));
        verify(asylumCase).read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
        verify(reheardHearingAppender).append(any(ReheardHearingDocuments.class), anyList());
        verify(asylumCase).write(eq(REHEARD_HEARING_DOCUMENTS_COLLECTION), anyList());
    }

    @Test
    void should_create_reheard_hearing_notice_pdf_and_append_to_reheard_collection_when_not_empty_and_dlrm_flag_on() {
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        List<IdValue<ReheardHearingDocuments>> reheardDocumentsCollection =
            List.of(new IdValue<>("id", mockReheardHearingDocuments));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION)).thenReturn(Optional.of(reheardDocumentsCollection));
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);
        when(documentReceiver.receive(uploadedDocument, "", DocumentTag.REHEARD_HEARING_NOTICE_RELISTED))
            .thenReturn(uploadedDocumentWithMetadata);
        when(documentsAppender.append(eq(mockReheardHearingDocuments.getReheardHearingDocs()), anyList()))
            .thenReturn(documentWithMetadataList);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, REHEARD_HEARING_DOCUMENTS, DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
        verify(documentReceiver, times(1)).receive(uploadedDocument, "", DocumentTag.REHEARD_HEARING_NOTICE_RELISTED);
        verify(documentsAppender).append(Collections.emptyList(), Collections.singletonList(uploadedDocumentWithMetadata));
        verify(asylumCase).read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
        verify(reheardHearingAppender, never()).append(any(ReheardHearingDocuments.class), anyList());
        IdValue<ReheardHearingDocuments> newIdValue = new IdValue<>("id", mockReheardHearingDocuments);
        List<IdValue<ReheardHearingDocuments>> expectedList = List.of(newIdValue);
        verify(asylumCase).write(eq(REHEARD_HEARING_DOCUMENTS_COLLECTION), anyList());
        assertThat(asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION))
            .usingRecursiveComparison().isEqualTo(Optional.of(expectedList));
    }

    @Test
    void should_create_hearing_notice_pdf_for_updated_hearing_date_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        final String hearingDate = "2020-02-05T12:30:00";

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(hearingDate);
        when(hearingNoticeUpdatedDetailsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @Test
    void should_create_hearing_notice_pdf_for_updated_hearing_centre_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        final String listCaseHearingCentre = HearingCentre.GLASGOW.toString();

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);
        when(hearingNoticeUpdatedDetailsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @Test
    void should_create_hearing_notice_pdf_for_ada_updated_hearing_centre_and_append_to_legal_representative_documents_for_the_case() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        final String listCaseHearingCentre = HearingCentre.GLASGOW.toString();

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(listCaseHearingCentre);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);
        when(adaHearingNoticeUpdatedDetailsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(adaHearingNoticeUpdatedDetailsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @Test
    void should_throw_when_no_previous_case_data_exists() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("previous case data is not present")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(remoteHearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedDetailsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(0)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(0)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingNoticeEditedCreator.canHandle(callbackStage, callback);

                if ((event == Event.EDIT_CASE_LISTING)
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_create_hearing_notice_pdf_and_append_to_letter_notification_documents_for_detained_in_other_facility() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        // Mock required fields for generateDocument logic
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        
        // Mock reheard hearing fields
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        
        // Mock fields for letter notification conditions
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO)); // Not internal for this test
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("other"));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, LETTER_NOTIFICATION_DOCUMENTS, DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER);
    }

    @Test
    void should_not_append_to_letter_notification_documents_for_detained_in_irc_facility() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        // Mock required fields for generateDocument logic
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        
        // Mock reheard hearing fields
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        
        // Mock fields for letter notification conditions
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO)); // Not internal for this test
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(DETENTION_FACILITY, String.class)).thenReturn(Optional.of("immigrationRemovalCentre"));

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
        verify(documentHandler, never()).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, LETTER_NOTIFICATION_DOCUMENTS, DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER);
    }

    @Test
    void should_append_to_letter_notification_documents_for_internal_non_detained() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        // Mock required fields for generateDocument logic
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        
        // Mock reheard hearing fields
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        
        // Setup for internal non-detained case with address
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO)); // Non-detained

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
        // Should be called once for internal non-detained with address
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, LETTER_NOTIFICATION_DOCUMENTS, DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER);
    }

    @Test
    void should_append_to_letter_notifications_documents_for_legal_rep_manual() {
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(hearingNoticeUpdatedRequirementsDocumentCreator.create(caseDetails, caseDetailsBefore)).thenReturn(uploadedDocument);

        // Mock required fields for generateDocument logic
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        // Mock reheard hearing fields
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        // Setup for internal non-detained case with address
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO)); // Non-detained
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO)); // LR-manual

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData())).thenReturn(hearingCentreNameBefore);
        when(hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData())).thenReturn(oldHearingDate);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(hearingNoticeUpdatedRequirementsDocumentCreator, times(1)).create(caseDetails, caseDetailsBefore);
        verify(documentHandler, times(1)).addWithMetadataWithDateTimeWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, HEARING_DOCUMENTS, DocumentTag.HEARING_NOTICE_RELISTED);
        // Should be called once for internal non-detained with address
        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(asylumCase, uploadedDocument, LETTER_NOTIFICATION_DOCUMENTS, DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> hearingNoticeEditedCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeEditedCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
