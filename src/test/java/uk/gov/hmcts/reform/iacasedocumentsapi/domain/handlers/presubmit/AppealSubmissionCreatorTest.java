package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentUploader;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.AppealSubmissionTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AppealSubmissionCreatorTest {

    private final String documentContentType = "application/pdf";
    private final String documentFileExtension = "PDF";

    @Mock private AppealSubmissionTemplate appealSubmissionTemplate;
    @Mock private DocumentGenerator documentGenerator;
    @Mock private DocumentReceiver documentReceiver;
    @Mock private DocumentUploader documentUploader;
    @Mock private DocumentsAppender documentsAppender;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    private String appealReferenceNumber = "PA/12345/2020";
    private String appellantFamilyName = "Awan";
    private String templateName = "APPEAL_SUBMISSION_TEMPLATE.docx";
    @Mock private Map<String, Object> templateFieldValues;
    @Mock private Resource documentResource;
    @Mock private Document uploadedDocument;
    @Mock private DocumentWithMetadata documentWithMetadata;
    @Mock private List<IdValue<DocumentWithMetadata>> existingLegalRepresentativeDocuments;
    @Mock private List<IdValue<DocumentWithMetadata>> allLegalRepresentativeDocuments;

    private String expectedFileName = "PA 12345 2020-Awan-appeal-form";

    @Captor private ArgumentCaptor<List<IdValue<DocumentWithMetadata>>> legalRepresentativeDocumentsCaptor;

    private AppealSubmissionCreator appealSubmissionCreator;

    @Before
    public void setUp() {

        appealSubmissionCreator =
            new AppealSubmissionCreator(
                documentContentType,
                documentFileExtension,
                appealSubmissionTemplate,
                documentGenerator,
                documentUploader,
                documentReceiver,
                documentsAppender
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.getLegalRepresentativeDocuments()).thenReturn(Optional.of(existingLegalRepresentativeDocuments));
        when(appealSubmissionTemplate.getName()).thenReturn(templateName);
        when(appealSubmissionTemplate.mapFieldValues(caseDetails)).thenReturn(templateFieldValues);

        when(documentGenerator.generate(
            expectedFileName,
            documentFileExtension,
            templateName,
            templateFieldValues
        )).thenReturn(documentResource);

        when(documentUploader.upload(
            documentResource,
            documentContentType
        )).thenReturn(uploadedDocument);

        when(documentReceiver.receive(
            uploadedDocument,
            "",
            DocumentTag.APPEAL_SUBMISSION
        )).thenReturn(documentWithMetadata);

        when(documentsAppender.append(
            existingLegalRepresentativeDocuments,
            Collections.singletonList(documentWithMetadata),
            DocumentTag.APPEAL_SUBMISSION
        )).thenReturn(allLegalRepresentativeDocuments);
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_legal_representative_documents_for_the_case() {

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).getAppealReferenceNumber();
        verify(asylumCase, times(1)).getAppellantFamilyName();
        verify(asylumCase, times(1)).getLegalRepresentativeDocuments();
        verify(appealSubmissionTemplate, times(1)).getName();
        verify(appealSubmissionTemplate, times(1)).mapFieldValues(any());

        verify(documentGenerator, times(1)).generate(
            expectedFileName,
            documentFileExtension,
            templateName,
            templateFieldValues
        );

        verify(documentUploader, times(1)).upload(documentResource, documentContentType);
        verify(documentReceiver, times(1)).receive(uploadedDocument, "", DocumentTag.APPEAL_SUBMISSION);
        verify(documentsAppender, times(1))
            .append(
                existingLegalRepresentativeDocuments,
                Collections.singletonList(documentWithMetadata),
                DocumentTag.APPEAL_SUBMISSION
            );

        verify(asylumCase, times(1)).setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
    }

    @Test
    public void should_create_appeal_submission_pdf_and_append_to_the_case_when_no_legal_representative_documents_exist() {

        when(documentsAppender.append(
            any(List.class),
            eq(Collections.singletonList(documentWithMetadata)),
            eq(DocumentTag.APPEAL_SUBMISSION)
        )).thenReturn(allLegalRepresentativeDocuments);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentsAppender, times(1))
            .append(
                legalRepresentativeDocumentsCaptor.capture(),
                eq(Collections.singletonList(documentWithMetadata)),
                eq(DocumentTag.APPEAL_SUBMISSION)
            );

        verify(asylumCase, times(1)).setLegalRepresentativeDocuments(allLegalRepresentativeDocuments);
    }

    @Test
    public void should_throw_when_appeal_reference_number_is_not_present() {

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealReferenceNumber is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_appellant_family_name_is_not_present() {

        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealSubmissionCreator.canHandle(callbackStage, callback);

                if (event == Event.SUBMIT_APPEAL
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

        assertThatThrownBy(() -> appealSubmissionCreator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionCreator.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionCreator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmissionCreator.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
