package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HearingNoticeCreatorTest {

    @Mock private DocumentCreator<AsylumCase> hearingNoticeDocumentCreator;
    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentsAppender documentsAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private Document uploadedDocument;
    @Mock private DocumentWithMetadata documentWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingHearingDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allHearingDocuments;

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> hearingDocumentsCaptor;

    private HearingNoticeCreator hearingNoticeCreator;

    @Before
    public void setUp() {

        hearingNoticeCreator =
            new HearingNoticeCreator(
                hearingNoticeDocumentCreator,
                documentReceiver,
                documentsAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.LIST_CASE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(hearingNoticeDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        when(asylumCase.getHearingDocuments()).thenReturn(Optional.of(existingHearingDocuments));

        when(documentReceiver.receive(
            uploadedDocument,
            "",
            DocumentTag.HEARING_NOTICE
        )).thenReturn(documentWithMetadata);

        when(documentsAppender.append(
            existingHearingDocuments,
            Collections.singletonList(documentWithMetadata),
            DocumentTag.HEARING_NOTICE
        )).thenReturn(allHearingDocuments);
    }

    @Test
    public void should_create_hearing_notice_pdf_and_append_to_legal_representative_documents_for_the_case() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(hearingNoticeDocumentCreator, times(1)).create(caseDetails);

        verify(asylumCase, times(1)).getHearingDocuments();
        verify(documentReceiver, times(1)).receive(uploadedDocument, "", DocumentTag.HEARING_NOTICE);
        verify(documentsAppender, times(1))
            .append(
                existingHearingDocuments,
                Collections.singletonList(documentWithMetadata),
                DocumentTag.HEARING_NOTICE
            );

        verify(asylumCase, times(1)).setHearingDocuments(allHearingDocuments);
    }

    @Test
    public void should_create_hearing_notice_pdf_and_append_to_the_case_when_no_legal_representative_documents_exist() {

        when(documentsAppender.append(
            any(List.class),
            eq(Collections.singletonList(documentWithMetadata)),
            eq(DocumentTag.HEARING_NOTICE)
        )).thenReturn(allHearingDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            hearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentsAppender, times(1))
            .append(
                hearingDocumentsCaptor.capture(),
                eq(Collections.singletonList(documentWithMetadata)),
                eq(DocumentTag.HEARING_NOTICE)
            );

        verify(asylumCase, times(1)).setHearingDocuments(allHearingDocuments);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> hearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> hearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingNoticeCreator.canHandle(callbackStage, callback);

                if (event == Event.LIST_CASE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> hearingNoticeCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingNoticeCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
