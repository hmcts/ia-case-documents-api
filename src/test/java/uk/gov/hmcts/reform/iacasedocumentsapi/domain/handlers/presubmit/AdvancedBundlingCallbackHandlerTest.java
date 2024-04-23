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
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ReheardHearingDocuments;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemittalDocument;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.EmBundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.WARN)
class AdvancedBundlingCallbackHandlerTest {

    @Mock private EmBundleRequestExecutor emBundleRequestExecutor;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private Document document;
    @Mock private DocumentsAppender documentsAppender;
    @Mock private FeatureToggler featureToggler;
    @Mock private List<IdValue<DocumentWithMetadata>> latestRemittalDocs;

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
                emBundleRequestExecutor,
                documentsAppender,
                featureToggler);

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

    @Test
    void should_successfully_handle_the_callback() {

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
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-hearing-bundle-config.yaml");
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
        verify(asylumCase).write(AsylumCaseDefinition.APP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());
        verify(asylumCase).write(AsylumCaseDefinition.RESP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());

    }

    @Test
    void should_successfully_handle_the_callback_in_reheard_and_remitted_all_data_present() {

        final List<IdValue<ReheardHearingDocuments>> reheardDecisionDocs = buildReheardDocuments();
        final List<IdValue<ReheardHearingDocuments>> reheardHearingDocs = buildReheardDocuments();
        final List<IdValue<RemittalDocument>> remittalDocuments = buildRemittalDocuments();

        DocumentWithMetadata addendumEvidenceAppellantDocument = new DocumentWithMetadata(
                document, "test","2020-12-12", DocumentTag.ADDENDUM_EVIDENCE, "The appellant");
        DocumentWithMetadata addendumEvidenceRespondentDocument = new DocumentWithMetadata(
                document, "test","2020-12-12", DocumentTag.ADDENDUM_EVIDENCE, "The respondent");
        List<IdValue<DocumentWithMetadata>> documents = new ArrayList<>();
        documents.add(new IdValue<>("0", addendumEvidenceAppellantDocument));
        documents.add(new IdValue<>("1", addendumEvidenceRespondentDocument));

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(documents));
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(REHEARD_DECISION_REASONS_COLLECTION)).thenReturn(Optional.of(reheardDecisionDocs));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION)).thenReturn(Optional.of(reheardHearingDocs));
        when(asylumCase.read(REMITTAL_DOCUMENTS)).thenReturn(Optional.of(remittalDocuments));
        when(documentsAppender.append(remittalDocuments.get(0).getValue().getOtherRemittalDocs(),
                Collections.singletonList(remittalDocuments.get(0).getValue().getDecisionDocument())))
                .thenReturn(latestRemittalDocs);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_REFERENCE_NUMBER, String.class);
        verify(asylumCase, times(1)).read(APPELLANT_FAMILY_NAME, String.class);

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-remitted-reheard-hearing-bundle-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);

        documents.remove(1);
        verify(asylumCase, times(2)).read(ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase).write(AsylumCaseDefinition.APPELLANT_ADDENDUM_EVIDENCE_DOCS, documents);
        documents.clear();
        documents.add(new IdValue<DocumentWithMetadata>("1", addendumEvidenceRespondentDocument));

        verify(asylumCase, times(1)).read(ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).read(RESPONDENT_DOCUMENTS);
        verify(asylumCase).write(AsylumCaseDefinition.APP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());
        verify(asylumCase).write(AsylumCaseDefinition.RESP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());
        verify(asylumCase).write(LATEST_DECISION_AND_REASONS_DOCUMENTS, reheardDecisionDocs.get(0).getValue().getReheardHearingDocs());
        verify(asylumCase).write(LATEST_REHEARD_HEARING_DOCUMENTS, reheardHearingDocs.get(0).getValue().getReheardHearingDocs());
        verify(asylumCase).write(LATEST_REMITTAL_DOCUMENTS, latestRemittalDocs);
        verify(asylumCase).clear(LATEST_DECISION_AND_REASONS_DOCUMENTS);
        verify(asylumCase).clear(LATEST_REMITTAL_DOCUMENTS);
        verify(asylumCase).clear(LATEST_REHEARD_HEARING_DOCUMENTS);
    }

    @Test
    void should_successfully_handle_the_callback_in_reheard_and_remitted_minimum_data_present() {
        IdValue<DocumentWithMetadata> finalDecisionsAndReasonsDoc =
                new IdValue<>("1",
                        new DocumentWithMetadata(document, "test","2020-12-12",
                                DocumentTag.FTPA_DECISION_AND_REASONS, "The appellant"));
        final List<IdValue<DocumentWithMetadata>> finalDecisionsAndReasonsDocs =
                Lists.newArrayList(finalDecisionsAndReasonsDoc);

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS)).thenReturn(Optional.empty());
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);
        when(asylumCase.read(REHEARD_DECISION_REASONS_COLLECTION)).thenReturn(Optional.empty());
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS)).thenReturn(Optional.of(finalDecisionsAndReasonsDocs));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                advancedBundlingCallbackHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(APPEAL_REFERENCE_NUMBER, String.class);
        verify(asylumCase, times(1)).read(APPELLANT_FAMILY_NAME, String.class);

        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-remitted-reheard-hearing-bundle-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);

        verify(asylumCase).write(AsylumCaseDefinition.APP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());
        verify(asylumCase).write(AsylumCaseDefinition.RESP_ADDITIONAL_EVIDENCE_DOCS, Collections.emptyList());
        verify(asylumCase).write(LATEST_DECISION_AND_REASONS_DOCUMENTS, finalDecisionsAndReasonsDocs);
        verify(asylumCase).write(LATEST_REHEARD_HEARING_DOCUMENTS, Collections.emptyList());
        verify(asylumCase).write(LATEST_REMITTAL_DOCUMENTS, Collections.emptyList());
        verify(asylumCase).clear(LATEST_DECISION_AND_REASONS_DOCUMENTS);
        verify(asylumCase).clear(LATEST_REMITTAL_DOCUMENTS);
        verify(asylumCase).clear(LATEST_REHEARD_HEARING_DOCUMENTS);
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

    private List<IdValue<ReheardHearingDocuments>> buildReheardDocuments() {
        String description = "Some evidence";
        String dateUploaded = "2018-12-25";
        final DocumentWithMetadata coverLetterDocumentWithMetadata = new DocumentWithMetadata(
                document, description, dateUploaded, DocumentTag.DECISION_AND_REASONS_COVER_LETTER, "");
        final DocumentWithMetadata decisionDocumentWithMetadata = new DocumentWithMetadata(
                document, description, dateUploaded, DocumentTag.FINAL_DECISION_AND_REASONS_PDF, "");

        IdValue<DocumentWithMetadata> decisionDocWithMetadata =
                new IdValue<>("2", decisionDocumentWithMetadata);
        IdValue<DocumentWithMetadata> coverLetterDocWithMetadata =
                new IdValue<>("1", coverLetterDocumentWithMetadata);
        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = Lists.newArrayList(decisionDocWithMetadata, coverLetterDocWithMetadata);
        IdValue<ReheardHearingDocuments> reheardHearingDocuments =
                new IdValue<>("1", new ReheardHearingDocuments(listOfDocumentsWithMetadata));
        return Lists.newArrayList(reheardHearingDocuments);
    }

    private List<IdValue<RemittalDocument>> buildRemittalDocuments() {

        final DocumentWithMetadata remittalDec = new DocumentWithMetadata(
                document, "test","2023-12-12", DocumentTag.REMITTAL_DECISION, "");
        final DocumentWithMetadata remittalOtherDoc1 = new DocumentWithMetadata(
                document, "other-test-1","2023-12-12", DocumentTag.REMITTAL_DECISION, "");
        final DocumentWithMetadata remittalOtherDoc2 = new DocumentWithMetadata(
                document, "other-test-1","2023-12-12", DocumentTag.REMITTAL_DECISION, "");
        IdValue<DocumentWithMetadata> decisionDocWithMetadata =
                new IdValue<>("11", remittalOtherDoc1);
        IdValue<DocumentWithMetadata> coverLetterDocWithMetadata =
                new IdValue<>("12", remittalOtherDoc2);

        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = Lists.newArrayList(decisionDocWithMetadata, coverLetterDocWithMetadata);
        IdValue<RemittalDocument> remittalDocuments =
                new IdValue<>("1", new RemittalDocument(remittalDec, listOfDocumentsWithMetadata));
        return Lists.newArrayList(remittalDocuments);
    }
}
