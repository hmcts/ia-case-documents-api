package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.apache.commons.lang3.RandomStringUtils.secure;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.EmBundleRequestExecutor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomiseHearingBundleHandlerTest {

    @Mock
    private EmBundleRequestExecutor bundleRequestExecutor;
    @Mock
    private Appender<DocumentWithMetadata> appender;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseCopy;
    @Mock
    Document document;
    @Mock
    private PreSubmitCallbackResponse<AsylumCase> callbackResponse;

    private final String appellantFamilyName = "bond";
    private final String coverPageLogo = "[userImage:hmcts.png]";
    private final List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    private CustomiseHearingBundleHandler customiseHearingBundleHandler;

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        String emBundlerStitchUri = "stitchingUri";
        String emBundlerUrl = "bundleurl";
        customiseHearingBundleHandler =
            new CustomiseHearingBundleHandler(
                emBundlerUrl,
                emBundlerStitchUri,
                bundleRequestExecutor,
                appender,
                dateProvider,
                objectMapper,
                featureToggler
            );

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetails));
        when(bundleRequestExecutor.post(any(Callback.class), eq(emBundlerUrl + emBundlerStitchUri)))
            .thenReturn(callbackResponse);

        when(objectMapper.writeValueAsString(any(AsylumCase.class))).thenReturn("Test");
        when(objectMapper.readValue("Test", AsylumCase.class)).thenReturn(asylumCaseCopy);

        String appealReference = "PA/50002/2020";
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(appealReference));

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of(appellantFamilyName));

        when(callbackResponse.getData()).thenReturn(asylumCase);
        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.of(caseBundles));

        Bundle bundle =
            new Bundle("id", "title", "desc", "yes", emptyList(), Optional.of("NEW"), Optional.empty(), YesOrNo.YES,
                YesOrNo.YES, "fileName");
        caseBundles.add(new IdValue<>("1", bundle));

        when(dateProvider.now()).thenReturn(LocalDate.now());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SUITABLE", "UNSUITABLE"})
    void should_successfully_handle_the_callback(String maybeDecision) throws JsonProcessingException {
        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(maybeDecision.isEmpty()
                ? Optional.empty() : Optional.of(AdaSuitabilityReviewDecision.valueOf(maybeDecision)));
        IdValue<DocumentWithDescription> legalRepDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> hearingDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> additionalEvidenceDoc = new IdValue<>("1", createDocumentWithDescription());
        List<IdValue<DocumentWithMetadata>> tribunalDocumentList = List.of(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADA_SUITABILITY, "test")));

        when(asylumCaseCopy.read(CUSTOM_HEARING_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(hearingDoc)));
        when(asylumCaseCopy.read(CUSTOM_LEGAL_REP_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(legalRepDoc)));
        when(asylumCaseCopy.read(CUSTOM_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentDoc)));
        when(asylumCaseCopy.read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(additionalEvidenceDoc)));
        when(asylumCase.read(CUSTOM_TRIBUNAL_DOCUMENTS))
                .thenReturn(Optional.of(tribunalDocumentList));

        IdValue<DocumentWithMetadata> legalRepDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test"));
        IdValue<DocumentWithMetadata> respondentDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.APPEAL_RESPONSE, "test"));
        IdValue<DocumentWithMetadata> hearingDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test"));
        IdValue<DocumentWithMetadata> additionalEvidenceDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test"));

        final List<IdValue<DocumentWithMetadata>> hearingDocuments = Lists.newArrayList(hearingDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            Lists.newArrayList(legalRepDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> additionalEvidenceDocuments =
            Lists.newArrayList(additionalEvidenceDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> respondentDocuments = Lists.newArrayList(respondentDocWithMetadata);

        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(hearingDocWithMetadata)));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(legalRepDocWithMetadata)));
        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentDocWithMetadata)));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(additionalEvidenceDocWithMetadata)));
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCaseCopy, times(2)).read(CUSTOM_HEARING_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_LEGAL_REP_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESPONDENT_DOCUMENTS);
        verify(asylumCaseCopy, maybeDecision.isEmpty() ? never() : times(1))
                .read(CUSTOM_TRIBUNAL_DOCUMENTS);

        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, hearingDocuments);
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, legalRepresentativeDocuments);
        verify(asylumCase, times(1)).write(ADDITIONAL_EVIDENCE_DOCUMENTS, additionalEvidenceDocuments);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, respondentDocuments);

        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);

        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase, times(1)).write(CASE_BUNDLES, Optional.of(caseBundles));
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION,
                maybeDecision.isEmpty() ? "iac-hearing-bundle-config.yaml" : "iac-hearing-bundle-inc-tribunal-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX,
            "PA 50002 2020-" + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(objectMapper, times(1)).readValue(anyString(), eq(AsylumCase.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SUITABLE", "UNSUITABLE"})
    void should_successfully_handle_the_callback_updated_bundle(String maybeDecision) throws JsonProcessingException {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(maybeDecision.isEmpty()
            ? Optional.empty() : Optional.of(AdaSuitabilityReviewDecision.valueOf(maybeDecision)));
        IdValue<DocumentWithDescription> legalRepDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> hearingDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> additionalEvidenceDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> appAddendumDoc = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> resAddendumDoc = new IdValue<>("1", createDocumentWithDescription());
        List<IdValue<DocumentWithMetadata>> tribunalDocumentList = List.of(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADA_SUITABILITY, "test")));

        when(asylumCaseCopy.read(CUSTOM_HEARING_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(hearingDoc)));
        when(asylumCaseCopy.read(CUSTOM_LEGAL_REP_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(legalRepDoc)));
        when(asylumCaseCopy.read(CUSTOM_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentDoc)));
        when(asylumCaseCopy.read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(additionalEvidenceDoc)));
        when(asylumCase.read(CUSTOM_TRIBUNAL_DOCUMENTS))
            .thenReturn(Optional.of(tribunalDocumentList));
        when(asylumCaseCopy.read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appAddendumDoc)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(resAddendumDoc)));

        IdValue<DocumentWithMetadata> legalRepDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test"));
        IdValue<DocumentWithMetadata> respondentDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.APPEAL_RESPONSE, "test"));
        IdValue<DocumentWithMetadata> hearingDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test"));
        IdValue<DocumentWithMetadata> additionalEvidenceDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test"));
        IdValue<DocumentWithMetadata> appAddendumDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant"));
        IdValue<DocumentWithMetadata> resAddendumDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent"));

        final List<IdValue<DocumentWithMetadata>> hearingDocuments =
            Lists.newArrayList(hearingDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> legalRepresentativeDocuments =
            Lists.newArrayList(legalRepDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> respondentDocuments =
            Lists.newArrayList(respondentDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> additionalEvidenceDocuments =
            Lists.newArrayList(additionalEvidenceDocWithMetadata);
        final List<IdValue<DocumentWithMetadata>> addendumDocuments =
            Lists.newArrayList(appAddendumDocWithMetadata, resAddendumDocWithMetadata);

        when(asylumCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocuments));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalRepresentativeDocuments));
        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(respondentDocuments));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(additionalEvidenceDocuments));
        when(asylumCaseCopy.read(APPELLANT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appAddendumDocWithMetadata)));
        when(asylumCaseCopy.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(resAddendumDocWithMetadata)));
        when(appender.append(any(DocumentWithMetadata.class), anyList()))
            .thenReturn(addendumDocuments);

        PreSubmitCallbackResponse<AsylumCase> response =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCaseCopy, times(2)).read(CUSTOM_HEARING_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_LEGAL_REP_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESPONDENT_DOCUMENTS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, maybeDecision.isEmpty() ? never() : times(1))
            .read(CUSTOM_TRIBUNAL_DOCUMENTS);

        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, hearingDocuments);
        verify(asylumCase, times(1)).write(LEGAL_REPRESENTATIVE_DOCUMENTS, legalRepresentativeDocuments);
        verify(asylumCase, times(1)).write(ADDITIONAL_EVIDENCE_DOCUMENTS, additionalEvidenceDocuments);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, respondentDocuments);
        verify(asylumCase, times(1)).write(ADDENDUM_EVIDENCE_DOCUMENTS, addendumDocuments);

        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);

        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase, times(1)).write(CASE_BUNDLES, Optional.of(caseBundles));
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION,
            maybeDecision.isEmpty() ? "iac-updated-hearing-bundle-config.yaml" : "iac-updated-hearing-bundle-inc-tribunal-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX,
            "PA 50002 2020-" + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(objectMapper, times(1)).readValue(anyString(), eq(AsylumCase.class));
    }

    private void verifyAsylumCaseReadsFor_should_successfully_handle_Reheard_the_callback() throws JsonProcessingException {
        verify(asylumCaseCopy, times(4)).read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(4)).read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FTPA_APPELLANT_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FTPA_RESPONDENT_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_REHEARD_HEARING_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(APPELLANT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESP_ADDITIONAL_EVIDENCE_DOCS);
        verify(objectMapper, times(1)).readValue(anyString(), eq(AsylumCase.class));
    }

    private void verifyAsylumCaseClearsFor_should_successfully_handle_Reheard_the_callback() {
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.RESPONDENT_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.FTPA_APPELLANT_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.FTPA_RESPONDENT_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_successfully_handle_Reheard_the_callback(Event event) throws JsonProcessingException {
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IdValue<DocumentWithDescription> appellantAdditionalEvidenceDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentAdditionalEvidenceDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> ftpaAppellantDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> ftpaRespondentDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> finalDecisionAndReasonsDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> hearingDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> appellantAddendumEvidenceDoc =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentAddendumEvidenceDoc =
            new IdValue<>("1", createDocumentWithDescription());


        when(asylumCaseCopy.read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FTPA_APPELLANT_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaAppellantDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FTPA_RESPONDENT_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaRespondentDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(finalDecisionAndReasonsDocuments)));
        when(asylumCaseCopy.read(CUSTOM_REHEARD_HEARING_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(hearingDocuments)));
        when(asylumCaseCopy.read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS)).thenReturn(Optional
            .of(Lists.newArrayList(new IdValue<>("2", createDocumentWithDescription()), appellantAddendumEvidenceDoc)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAddendumEvidenceDoc)));

        IdValue<DocumentWithMetadata> appellantAdditionalEvidenceDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "The appellant"));
        IdValue<DocumentWithMetadata> respondentAdditionalEvidenceDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "The respondent"));
        IdValue<DocumentWithMetadata> ftpaAppellantDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_APPELLANT, "test"));
        IdValue<DocumentWithMetadata> ftpaRespondentDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_RESPONDENT, "test"));
        IdValue<DocumentWithMetadata> finalDecisionsAndReasonsDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_DECISION_AND_REASONS, "test"));
        IdValue<DocumentWithMetadata> reheardHearingDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.REHEARD_HEARING_NOTICE, "test"));
        IdValue<DocumentWithMetadata> appellantAddendumEvidenceDocs =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant"));
        IdValue<DocumentWithMetadata> respondentAddendumEvidenceDocs =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent"));


        final List<IdValue<DocumentWithMetadata>> appellantAdditionalEvidenceDocs =
            Lists.newArrayList(appellantAdditionalEvidenceDoc);
        final List<IdValue<DocumentWithMetadata>> respondentAdditionalEvidenceDocs =
            Lists.newArrayList(respondentAdditionalEvidenceDoc);
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantDocs = Lists.newArrayList(ftpaAppellantDoc);
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentDocs = Lists.newArrayList(ftpaRespondentDoc);
        final List<IdValue<DocumentWithMetadata>> finalDecisionsAndReasonsDocs =
            Lists.newArrayList(finalDecisionsAndReasonsDoc);
        final List<IdValue<DocumentWithMetadata>> reheardHearingDocs = Lists.newArrayList(reheardHearingDoc);
        final List<IdValue<DocumentWithMetadata>> appellantAddendumEvidenceList =
            Lists.newArrayList(appellantAddendumEvidenceDocs);
        final List<IdValue<DocumentWithMetadata>> respondentAddendumEvidenceList =
            Lists.newArrayList(respondentAddendumEvidenceDocs);
        final List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentList = asList(
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        when(appender.append(any(DocumentWithMetadata.class), anyList()))
            .thenReturn(addendumEvidenceDocumentList);

        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocs)));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocs)));
        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(ftpaAppellantDocs)));
        when(asylumCase.read(FTPA_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaRespondentDocs)));
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(finalDecisionsAndReasonsDocs)));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCase.read(APPELLANT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCaseCopy.read(APPELLANT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAddendumEvidenceList)));
        when(asylumCaseCopy.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAddendumEvidenceList)));
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class), Optional.of(YesOrNo.YES));
        assertTrue(featureToggler.getValue("reheard-feature", false));
        verifyAsylumCaseReadsFor_should_successfully_handle_Reheard_the_callback();
        verifyAsylumCaseClearsFor_should_successfully_handle_Reheard_the_callback();
        verify(asylumCase, times(1)).write(APP_ADDITIONAL_EVIDENCE_DOCS, emptyList());
        verify(asylumCase, times(1)).write(ADDITIONAL_EVIDENCE_DOCUMENTS, appellantAdditionalEvidenceDocs);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, respondentAdditionalEvidenceDocs);
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_DOCUMENTS, ftpaAppellantDocs);
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_DOCUMENTS, ftpaRespondentDocs);
        verify(asylumCase, times(1)).write(FINAL_DECISION_AND_REASONS_DOCUMENTS, finalDecisionsAndReasonsDocs);
        verify(asylumCase, times(0)).write(REHEARD_HEARING_DOCUMENTS, reheardHearingDocs);
        verify(asylumCase, times(1)).write(ADDENDUM_EVIDENCE_DOCUMENTS, addendumEvidenceDocumentList);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-reheard-hearing-bundle-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-"
            + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_return_values_for_document_with_description_present(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IdValue<DocumentWithDescription> documentWithDescriptionIdValue =
            new IdValue<>("1", createDocumentWithDescription());

        final List<IdValue<DocumentWithMetadata>> documentMetadataList = asList(
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        Optional<IdValue<DocumentWithMetadata>> result = customiseHearingBundleHandler.isDocumentWithDescriptionPresent(
            documentMetadataList, documentWithDescriptionIdValue);
        assertTrue(result.isPresent());
        assertEquals(
            "some-binary-url",
            result.get().getValue().getDocument().getDocumentBinaryUrl());
    }

    @Test
        void should_successfully_handle_Reheard_the_callback_with_Remitted() throws JsonProcessingException {
        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);

        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SOURCE_OF_REMITTAL, String.class)).thenReturn(Optional.of("Court of Appeal"));
        final List<IdValue<RemittalDocument>> remittalDocuments = buildRemittalDocuments();

        IdValue<DocumentWithDescription> appellantAdditionalEvidenceDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentAdditionalEvidenceDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> ftpaAppellantDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> ftpaRespondentDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> finalDecisionAndReasonsDocuments =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> hearingDocuments = new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> appellantAddendumEvidenceDoc =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> respondentAddendumEvidenceDoc =
            new IdValue<>("1", createDocumentWithDescription());
        IdValue<DocumentWithDescription> remittalDocs =
            new IdValue<>("1", createDocumentWithDescription());


        when(asylumCaseCopy.read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FTPA_APPELLANT_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaAppellantDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FTPA_RESPONDENT_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaRespondentDocuments)));
        when(asylumCaseCopy.read(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(finalDecisionAndReasonsDocuments)));
        when(asylumCaseCopy.read(CUSTOM_REHEARD_HEARING_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(hearingDocuments)));
        when(asylumCaseCopy.read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS)).thenReturn(Optional
            .of(Lists.newArrayList(new IdValue<>("2", createDocumentWithDescription()), appellantAddendumEvidenceDoc)));
        when(asylumCaseCopy.read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAddendumEvidenceDoc)));
        when(asylumCaseCopy.read(CUSTOM_LATEST_REMITTAL_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(remittalDocs)));
        when(asylumCaseCopy.read(SOURCE_OF_REMITTAL))
            .thenReturn(Optional.of("Court of Appeal"));
        when(asylumCaseCopy.read(REMITTAL_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(remittalDocuments)));

        IdValue<DocumentWithMetadata> appellantAdditionalEvidenceDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "The appellant"));
        IdValue<DocumentWithMetadata> respondentAdditionalEvidenceDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "The respondent"));
        IdValue<DocumentWithMetadata> ftpaAppellantDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_APPELLANT, "test"));
        IdValue<DocumentWithMetadata> ftpaRespondentDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_RESPONDENT, "test"));
        IdValue<DocumentWithMetadata> finalDecisionsAndReasonsDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_DECISION_AND_REASONS, "test"));
        IdValue<DocumentWithMetadata> reheardHearingDoc =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.REHEARD_HEARING_NOTICE, "test"));
        IdValue<DocumentWithMetadata> appellantAddendumEvidenceDocs =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant"));
        IdValue<DocumentWithMetadata> respondentAddendumEvidenceDocs =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent"));


        final List<IdValue<DocumentWithMetadata>> appellantAdditionalEvidenceDocs =
            Lists.newArrayList(appellantAdditionalEvidenceDoc);
        final List<IdValue<DocumentWithMetadata>> respondentAdditionalEvidenceDocs =
            Lists.newArrayList(respondentAdditionalEvidenceDoc);
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantDocs = Lists.newArrayList(ftpaAppellantDoc);
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentDocs = Lists.newArrayList(ftpaRespondentDoc);
        final List<IdValue<DocumentWithMetadata>> finalDecisionsAndReasonsDocs =
            Lists.newArrayList(finalDecisionsAndReasonsDoc);
        final List<IdValue<DocumentWithMetadata>> reheardHearingDocs = Lists.newArrayList(reheardHearingDoc);
        final List<IdValue<DocumentWithMetadata>> appellantAddendumEvidenceList =
            Lists.newArrayList(appellantAddendumEvidenceDocs);
        final List<IdValue<DocumentWithMetadata>> respondentAddendumEvidenceList =
            Lists.newArrayList(respondentAddendumEvidenceDocs);
        final List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentList = asList(
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        when(appender.append(any(DocumentWithMetadata.class), anyList()))
            .thenReturn(addendumEvidenceDocumentList);

        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAdditionalEvidenceDocs)));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAdditionalEvidenceDocs)));
        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS)).thenReturn(Optional.of(Lists.newArrayList(ftpaAppellantDocs)));
        when(asylumCase.read(FTPA_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(ftpaRespondentDocs)));
        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(finalDecisionsAndReasonsDocs)));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCase.read(APPELLANT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCaseCopy.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(appellantAddendumEvidenceList)));
        when(asylumCaseCopy.read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(respondentAddendumEvidenceList)));
        when(document.getDocumentBinaryUrl()).thenReturn("some-binary-url");
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class), Optional.of(YesOrNo.YES));
        assertTrue(featureToggler.getValue("reheard-feature", false));

        verify(asylumCaseCopy, times(2)).read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FTPA_APPELLANT_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_REHEARD_HEARING_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_LATEST_REMITTAL_DOCS);


        verify(asylumCase, times(1)).read(APPELLANT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).write(APP_ADDITIONAL_EVIDENCE_DOCS, emptyList());

        verify(asylumCase, times(1)).write(ADDITIONAL_EVIDENCE_DOCUMENTS, appellantAdditionalEvidenceDocs);
        verify(asylumCase, times(1)).write(RESPONDENT_DOCUMENTS, respondentAdditionalEvidenceDocs);
        verify(asylumCase, times(1)).write(FTPA_APPELLANT_DOCUMENTS, ftpaAppellantDocs);
        verify(asylumCase, times(1)).write(FTPA_RESPONDENT_DOCUMENTS, ftpaRespondentDocs);
        verify(asylumCase, times(1)).write(FINAL_DECISION_AND_REASONS_DOCUMENTS, finalDecisionsAndReasonsDocs);
        verify(asylumCase, times(0)).write(REHEARD_HEARING_DOCUMENTS, reheardHearingDocs);
        verify(asylumCase, times(1)).write(ADDENDUM_EVIDENCE_DOCUMENTS, addendumEvidenceDocumentList);

        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.RESPONDENT_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.FTPA_APPELLANT_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.FTPA_RESPONDENT_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS);

        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_CONFIGURATION, "iac-remitted-reheard-hearing-bundle-config.yaml");
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX, "PA 50002 2020-" + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
        verify(objectMapper, times(1)).readValue(anyString(), eq(AsylumCase.class));
    }


    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_return_values_for_document_with_description_present_throw_exception(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IdValue<DocumentWithDescription> documentWithDescriptionIdValue =
            new IdValue<>("1", new DocumentWithDescription(null, "test"));
        final List<IdValue<DocumentWithMetadata>> documentMetadataList = asList(
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        assertThatThrownBy(() -> customiseHearingBundleHandler.isDocumentWithDescriptionPresent(
            documentMetadataList, documentWithDescriptionIdValue))
            .hasMessage("Document cannot be null")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertFalse(featureToggler.getValue("reheard-feature", false));

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void contains_should_return_true_for_document_with_metadata_present(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        IdValue<DocumentWithMetadata> documentWithMetadataIdValue =
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "test"));

        final List<IdValue<DocumentWithMetadata>> documentMetadataList = asList(
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        assertTrue(customiseHearingBundleHandler.contains(
            documentMetadataList, documentWithMetadataIdValue));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_create_custom_collections_if_source_collections_are_empty(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(AsylumCaseDefinition.APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.empty());
        when(asylumCase.read(RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.empty());

        customiseHearingBundleHandler.initializeNewCollections(asylumCase);

        verify(asylumCase, times(1)).read(APPELLANT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).write(APPELLANT_ADDENDUM_EVIDENCE_DOCS, emptyList());
        verify(asylumCase, times(1)).write(RESPONDENT_ADDENDUM_EVIDENCE_DOCS, emptyList());
        verify(asylumCase, times(1)).write(APP_ADDITIONAL_EVIDENCE_DOCS, emptyList());
        verify(asylumCase, times(1)).write(RESP_ADDITIONAL_EVIDENCE_DOCS, emptyList());
    }


    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_throw_when_appeal_reference_is_not_present(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appealReferenceNumber is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_throw_when_asylumcase_can_not_copied(Event event) throws JsonProcessingException {
        when(callback.getEvent()).thenReturn(event);

        when(objectMapper.readValue("Test", AsylumCase.class))
            .thenThrow(new IllegalStateException("Cannot make a deep copy of the case"));

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot make a deep copy of the case")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_throw_when_appellant_family_name_is_not_present(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("appellantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_throw_when_case_bundle_is_not_present(Event event) {
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(CASE_BUNDLES)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("caseBundle is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_throw_when_case_bundle_is_empty(Event event) {
        when(callback.getEvent()).thenReturn(event);

        caseBundles.clear();

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("case bundles size is not 1 and is : 0")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void handling_should_throw_if_cannot_actually_handle(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = customiseHearingBundleHandler.canHandle(callbackStage, callback);

                if ((event == Event.CUSTOMISE_HEARING_BUNDLE
                    || event == Event.GENERATE_UPDATED_HEARING_BUNDLE)
                    && callbackStage == ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_not_allow_null_arguments(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> customiseHearingBundleHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundleHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void test_contains_not_null(Event event) {
        when(callback.getEvent()).thenReturn(event);
        IdValue<DocumentWithMetadata> legalRepDocWithMetadata =
                new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test"));
        Boolean bool = customiseHearingBundleHandler.contains(List.of(), legalRepDocWithMetadata);
        assertThat(bool).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({
        "CUSTOMISE_HEARING_BUNDLE, true",
        "GENERATE_UPDATED_HEARING_BUNDLE, true",
        "CUSTOMISE_HEARING_BUNDLE, false",
        "GENERATE_UPDATED_HEARING_BUNDLE, false"
    })
    void should_ignore_existing_hearing_bundles_in_new_bundles(Event event, boolean hasExistingHearingBundle) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(Optional.empty());

        IdValue<DocumentWithDescription> hearingDoc = new IdValue<>("1", createDocumentWithDescription());

        final List<IdValue<DocumentWithDescription>> hearingDescriptionDocuments = Lists.newArrayList(hearingDoc);

        IdValue<DocumentWithMetadata> hearingDocWithMetadata =
            new IdValue<>("1", createDocumentWithMetadata(hasExistingHearingBundle ?
                DocumentTag.HEARING_BUNDLE : DocumentTag.HEARING_NOTICE, "test"));

        final List<IdValue<DocumentWithMetadata>> hearingDocuments = Lists.newArrayList(hearingDocWithMetadata);
        when(asylumCaseCopy.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(hearingDocuments));
        when(asylumCaseCopy.read(CUSTOM_HEARING_DOCUMENTS)).thenReturn(Optional.of(hearingDescriptionDocuments));
        when(asylumCase.read(HEARING_DOCUMENTS)).thenReturn(Optional.of(hearingDocuments));


        PreSubmitCallbackResponse<AsylumCase> response =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());

        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);
        verify(asylumCase, times(1)).clear(AsylumCaseDefinition.CASE_BUNDLES);

        verify(appender, times(hasExistingHearingBundle ? 0 : 1))
            .append(any(DocumentWithMetadata.class), anyList());
        verify(appender, times(hasExistingHearingBundle ? 1 : 0))
            .append(eq(null), anyList());

        verify(asylumCaseCopy, times(2)).read(CUSTOM_HEARING_DOCUMENTS);
        verify(asylumCaseCopy, times(1)).read(CUSTOM_LEGAL_REP_DOCUMENTS);
        verify(asylumCaseCopy, times(1)).read(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS);
        verify(asylumCaseCopy, times(1)).read(CUSTOM_RESPONDENT_DOCUMENTS);

        verify(asylumCase, times(1)).write(HEARING_DOCUMENTS, hearingDocuments);

        verify(asylumCase, times(1)).write(CASE_BUNDLES, Optional.of(caseBundles));
        verify(asylumCase).write(AsylumCaseDefinition.BUNDLE_FILE_NAME_PREFIX,
            "PA 50002 2020-" + appellantFamilyName);
        verify(asylumCase, times(1)).write(STITCHING_STATUS, "NEW");
    }

    //When the reheard case is not through remitted path, check the reheardHearingDocuments and add the updated document list in the
    // reheardHearingDocumentsCollection.
    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void test_reheard_documents_saved_in_collection_field(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(featureToggler.getValue("dlrm-remitted-feature-flag", false)).thenReturn(true);
        when(document.getDocumentBinaryUrl()).thenReturn("some-binary-url");
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        IdValue<DocumentWithDescription> reheardHearingDocs =
            new IdValue<>("1", createDocumentWithDescription());
        List<IdValue<ReheardHearingDocuments>> reheardHearingDocuments = buildReheardDocuments();
        List<IdValue<DocumentWithMetadata>> documentsListAfterAppend = reheardHearingDocuments.get(0).getValue().getReheardHearingDocs();

        when(asylumCaseCopy.read(CUSTOM_REHEARD_HEARING_DOCS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCaseCopy.read(REHEARD_HEARING_DOCUMENTS_COLLECTION)).thenReturn(Optional.of(reheardHearingDocuments));


        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(Lists.newArrayList(reheardHearingDocs)));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION))
            .thenReturn(Optional.of(reheardHearingDocuments));
        when(appender.append(any(DocumentWithMetadata.class), anyList()))
            .thenReturn(documentsListAfterAppend);
        reheardHearingDocuments.get(0).getValue().setReheardHearingDocs(documentsListAfterAppend);
        when(dateProvider.nowWithTime()).thenReturn(LocalDateTime.now());
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundleHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class), Optional.of(YesOrNo.YES));
        assertTrue(featureToggler.getValue("reheard-feature", false));
        verify(asylumCaseCopy, times(2)).read(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(1)).read(CUSTOM_FTPA_APPELLANT_DOCS);
        verify(asylumCaseCopy, times(1)).read(LATEST_DECISION_AND_REASONS_DOCUMENTS);
        verify(asylumCaseCopy, times(1)).read(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS);
        verify(asylumCaseCopy, times(2)).read(CUSTOM_REHEARD_HEARING_DOCS);
        verify(asylumCaseCopy, times(1)).read(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(1)).read(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCaseCopy, times(2)).read(REHEARD_HEARING_DOCUMENTS);

        verify(asylumCase, times(1)).read(APPELLANT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESPONDENT_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(APP_ADDITIONAL_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).read(RESP_ADDITIONAL_EVIDENCE_DOCS);

        verify(asylumCaseCopy, times(1)).read(REHEARD_DECISION_REASONS_COLLECTION);
        verify(asylumCase, times(1)).write(REHEARD_HEARING_DOCUMENTS_COLLECTION, reheardHearingDocuments);
        verify(asylumCase).clear(AsylumCaseDefinition.HMCTS);
        verify(asylumCase, times(1)).write(HMCTS, coverPageLogo);
        verify(asylumCase).clear(AsylumCaseDefinition.CASE_BUNDLES);
    }

    private DocumentWithDescription createDocumentWithDescription() {
        return
            new DocumentWithDescription(new Document("some-url",
                "some-binary-url",
                secure().nextAlphabetic(20)), "test");
    }

    private Document createDocument() {
        return
            new Document("some-url",
                "some-binary-url",
                "some-filename");
    }

    private DocumentWithMetadata createDocumentWithMetadata(DocumentTag documentTag, String suppliedBy) {

        return
            new DocumentWithMetadata(createDocument(),
                "some-description",
                new SystemDateProvider().now().toString(), documentTag, suppliedBy);

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
}
