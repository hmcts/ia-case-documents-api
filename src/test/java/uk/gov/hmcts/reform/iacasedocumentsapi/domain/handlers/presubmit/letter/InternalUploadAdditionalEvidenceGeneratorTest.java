package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalUploadAdditionalEvidenceGeneratorTest {

    @Mock
    private DocumentCreator<AsylumCase> adminUploadEvidenceDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> homeOfficeUploadEvidenceDocumentCreator;
    @Mock
    private DocumentCreator<AsylumCase> legalOfficereUploadEvidenceDocumentCreator;
    @Mock
    private DocumentHandler documentHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Document uploadedDocument;

    private static final String LEGAL_OFFICER_ADDENDUM_SUPPLIED_BY_LABEL = "The respondent";
    private static final String LEGAL_OFFICER_ADDENDUM_UPLOADED_BY_LABEL = "TCW";

    private final IdValue<DocumentWithMetadata> addendumOne = new IdValue<>(
            "1",
            new DocumentWithMetadata(
                    uploadedDocument,
                    "Some description",
                    "2018-12-25", DocumentTag.ADDENDUM_EVIDENCE,
                    LEGAL_OFFICER_ADDENDUM_SUPPLIED_BY_LABEL,
                    LEGAL_OFFICER_ADDENDUM_UPLOADED_BY_LABEL
            )
    );

    private InternalUploadAdditionalEvidenceGenerator internalUploadAdditionalEvidenceGenerator;

    @BeforeEach
    void setUp() {
        internalUploadAdditionalEvidenceGenerator =
            new InternalUploadAdditionalEvidenceGenerator(
                    adminUploadEvidenceDocumentCreator,
                    homeOfficeUploadEvidenceDocumentCreator,
                    legalOfficereUploadEvidenceDocumentCreator,
                    documentHandler
            );

        when(callback.getEvent()).thenReturn(Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"UPLOAD_ADDITIONAL_EVIDENCE", "UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER"})
    void should_create_internal_admin_upload_additional_evidence_letter_pdf_and_append_to_notifications_documents(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(adminUploadEvidenceDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            internalUploadAdditionalEvidenceGenerator.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase, uploadedDocument,
            NOTIFICATION_ATTACHMENT_DOCUMENTS,
            DocumentTag.INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER
        );
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE", "UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE"})
    void should_create_internal_home_office_upload_additional_evidence_letter_pdf_and_append_to_notifications_documents(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(homeOfficeUploadEvidenceDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalUploadAdditionalEvidenceGenerator.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase, uploadedDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER
        );
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"UPLOAD_ADDENDUM_EVIDENCE"})
    void should_create_internal_legal_officer_upload_additional_evidence_letter_pdf_and_append_to_notifications_documents(Event event) {
        when(callback.getEvent()).thenReturn(event);

        List<IdValue<DocumentWithMetadata>> addendumDocuments = new ArrayList<>();
        addendumDocuments.add(addendumOne);
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(addendumDocuments));

        when(legalOfficereUploadEvidenceDocumentCreator.create(caseDetails)).thenReturn(uploadedDocument);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                internalUploadAdditionalEvidenceGenerator.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(documentHandler, times(1)).addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase, uploadedDocument,
                NOTIFICATION_ATTACHMENT_DOCUMENTS,
                DocumentTag.LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER
        );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> internalUploadAdditionalEvidenceGenerator.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);

        assertThatThrownBy(() -> internalUploadAdditionalEvidenceGenerator.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_cannot_handle_callback_if_not_internal() {
        when(callback.getCaseDetails().getCaseData().read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(internalUploadAdditionalEvidenceGenerator.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void it_cannot_handle_callback_if_appellant_is_not_in_detention() {
        when(callback.getCaseDetails().getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertFalse(internalUploadAdditionalEvidenceGenerator.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class)
    void it_should_handle_both_detained_non_ada_and_detained_ada_cases(YesOrNo yesOrNo) {
        when(callback.getCaseDetails().getCaseData().read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        assertTrue(internalUploadAdditionalEvidenceGenerator.canHandle(ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalUploadAdditionalEvidenceGenerator.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalUploadAdditionalEvidenceGenerator.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalUploadAdditionalEvidenceGenerator.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalUploadAdditionalEvidenceGenerator.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}