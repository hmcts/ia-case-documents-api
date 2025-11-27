package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CustomiseHearingBundlePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private Appender<DocumentWithDescription> appender;
    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private Document document;
    @Captor
    private ArgumentCaptor<DocumentWithDescription> documentsCaptor;

    private CustomiseHearingBundlePreparer customiseHearingBundlePreparer;

    @BeforeEach
    void setUp() {
        customiseHearingBundlePreparer =
            new CustomiseHearingBundlePreparer(appender, featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SUITABLE", "UNSUITABLE"})
    void should_create_custom_collections(String maybeDecision) {
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(maybeDecision.isEmpty()
            ? Optional.empty() : Optional.of(AdaSuitabilityReviewDecision.valueOf(maybeDecision)));

        List<IdValue<DocumentWithDescription>> customCollections =
            List.of(new IdValue<>("1", createDocumentWithDescription()));
        List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test")));

        List<IdValue<DocumentWithMetadata>> legalDocumentList = List.of(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.CASE_ARGUMENT, "test")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.APPEAL_SUBMISSION, "tes")),
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.CASE_SUMMARY, "test")));

        List<IdValue<DocumentWithMetadata>> tribunalDocumentList = List.of(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADA_SUITABILITY, "test")));

        List<IdValue<DocumentWithMetadata>> additionalEvidenceList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test")));
        List<IdValue<DocumentWithMetadata>> respondentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.RESPONDENT_EVIDENCE, "test")));

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customCollections);

        when(asylumCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalDocumentList));

        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(additionalEvidenceList));

        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(respondentList));

        when(asylumCase.read(TRIBUNAL_DOCUMENTS))
            .thenReturn(Optional.of(tribunalDocumentList));

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(CUSTOM_HEARING_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_RESPONDENT_DOCUMENTS, customCollections);
        verify(asylumCase,times(0)).write(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS,customCollections);
        verify(asylumCase,times(0)).read(ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(maybeDecision.isEmpty() ? 0 : 1))
                .write(CUSTOM_TRIBUNAL_DOCUMENTS,customCollections);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SUITABLE", "UNSUITABLE"})
    void should_create_custom_collections_updated_bundle_without_addendum(String maybeDecision) {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(maybeDecision.isEmpty()
            ? Optional.empty() : Optional.of(AdaSuitabilityReviewDecision.valueOf(maybeDecision)));

        List<IdValue<DocumentWithDescription>> customCollections =
            List.of(new IdValue<>("1", createDocumentWithDescription()));
        List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test")));

        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.CASE_ARGUMENT, "test")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.APPEAL_SUBMISSION, "tes")),
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.CASE_SUMMARY, "test")));

        List<IdValue<DocumentWithMetadata>> tribunalDocumentList = List.of(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADA_SUITABILITY, "test")));

        List<IdValue<DocumentWithMetadata>> additionalEvidenceList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test")));
        List<IdValue<DocumentWithMetadata>> respondentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.RESPONDENT_EVIDENCE, "test")));

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customCollections);

        when(asylumCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalDocumentList));

        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(additionalEvidenceList));

        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(respondentList));

        when(asylumCase.read(TRIBUNAL_DOCUMENTS))
            .thenReturn(Optional.of(tribunalDocumentList));

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(CUSTOM_HEARING_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_RESPONDENT_DOCUMENTS, customCollections);
        verify(asylumCase,times(0)).write(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS,customCollections);
        verify(asylumCase,times(0)).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS,customCollections);
        verify(asylumCase,times(2)).read(ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase,times(maybeDecision.isEmpty() ? 0 : 1))
            .write(CUSTOM_TRIBUNAL_DOCUMENTS,customCollections);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SUITABLE", "UNSUITABLE"})
    void should_create_custom_collections_updated_bundle_with_addendum(String maybeDecision) {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(maybeDecision.isEmpty()
            ? Optional.empty() : Optional.of(AdaSuitabilityReviewDecision.valueOf(maybeDecision)));

        List<IdValue<DocumentWithDescription>> customCollections =
            List.of(new IdValue<>("1", createDocumentWithDescription()));
        List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test")));

        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.CASE_ARGUMENT, "test")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.APPEAL_SUBMISSION, "tes")),
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.CASE_SUMMARY, "test")));

        List<IdValue<DocumentWithMetadata>> tribunalDocumentList = List.of(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADA_SUITABILITY, "test")));

        List<IdValue<DocumentWithMetadata>> additionalEvidenceList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test")));
        List<IdValue<DocumentWithMetadata>> respondentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.RESPONDENT_EVIDENCE, "test")));

        List<IdValue<DocumentWithMetadata>> addendumEvidenceList =
            List.of(
                new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
                new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
                new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")),
                new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "none"))
            );

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customCollections);

        when(asylumCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalDocumentList));

        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(additionalEvidenceList));

        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(respondentList));

        when(asylumCase.read(TRIBUNAL_DOCUMENTS))
            .thenReturn(Optional.of(tribunalDocumentList));

        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(addendumEvidenceList));

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(CUSTOM_HEARING_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_RESPONDENT_DOCUMENTS, customCollections);
        verify(asylumCase,times(1)).write(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS,customCollections);
        verify(asylumCase,times(1)).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS,customCollections);
        verify(asylumCase,times(4)).read(ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase,times(maybeDecision.isEmpty() ? 0 : 1))
            .write(CUSTOM_TRIBUNAL_DOCUMENTS,customCollections);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_create_custom_collections_in_reheard_case(Event event) {
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        when(callback.getEvent()).thenReturn(event);

        final List<IdValue<DocumentWithDescription>> customDocumentList =
            List.of(new IdValue<>("1", createDocumentWithDescription()));

        final List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.REHEARD_HEARING_NOTICE, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantEvidenceDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentEvidenceDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_APPELLANT, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_RESPONDENT, "test")));
        final List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FINAL_DECISION_AND_REASONS_PDF, "test")));

        final List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentList = List.of(
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customDocumentList);

        when(asylumCase.read(APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(ftpaAppellantEvidenceDocumentList));

        when(asylumCase.read(RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(ftpaRespondentEvidenceDocumentList));

        when(asylumCase.read(FTPA_APPELLANT_DOCUMENTS))
            .thenReturn(Optional.of(ftpaAppellantDocumentList));

        when(asylumCase.read(FTPA_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(ftpaRespondentDocumentList));

        when(asylumCase.read(FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.of(finalDecisionAndReasonsDocumentList));

        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(addendumEvidenceDocumentList));


        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
        verify(asylumCase, times(3)).read(REHEARD_HEARING_DOCUMENTS);
        verify(asylumCase, times(2)).write(CUSTOM_REHEARD_HEARING_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FTPA_APPELLANT_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FTPA_RESPONDENT_DOCS, customDocumentList);
        verify(asylumCase, times(2)).write(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase,times(1)).write(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS,customDocumentList);
        verify(asylumCase,times(4)).read(ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase, never()).read(TRIBUNAL_DOCUMENTS);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_filter_legal_rep_document_with_correct_tags(Event event) {

        when(callback.getEvent()).thenReturn(event);

        List<IdValue<DocumentWithDescription>> customCollections = new ArrayList<>();

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customCollections);

        DocumentWithMetadata legalDocument = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description",
                "dateUploaded",
                DocumentTag.CASE_ARGUMENT
        );
        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
                new IdValue<>(
                        "1",
                        legalDocument
                ),
                new IdValue<>(
                        "2",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.APPEAL_SUBMISSION
                        )
                ),
                new IdValue<>(
                        "3",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.CASE_SUMMARY
                        )
                ), new IdValue<>(
                        "4",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.APPEAL_RESPONSE
                        )
                )
        );

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalDocumentList));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(CUSTOM_LEGAL_REP_DOCUMENTS);
        verify(asylumCase, times(1)).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);

        verify(appender, times(2)).append(
            documentsCaptor.capture(), eq(customCollections));

        List<DocumentWithDescription> legalRepresentativeDocuments =
            documentsCaptor
                .getAllValues();
        assertEquals(2, legalRepresentativeDocuments.size());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_show_all_legal_rep_document_with_for_aip(Event event) {

        when(callback.getEvent()).thenReturn(event);

        List<IdValue<DocumentWithDescription>> customCollections = new ArrayList<>();

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customCollections);

        DocumentWithMetadata legalDocument = new DocumentWithMetadata(
            new Document("documentUrl", "binaryUrl", "documentFilename"),
            "description",
            "dateUploaded",
            DocumentTag.CASE_ARGUMENT
        );
        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
            new IdValue<>(
                "1",
                legalDocument
            ),
            new IdValue<>(
                "2",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename"),
                    "description",
                    "dateUploaded",
                    DocumentTag.APPEAL_SUBMISSION
                )
            ),
            new IdValue<>(
                "3",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename"),
                    "description",
                    "dateUploaded",
                    DocumentTag.CASE_SUMMARY
                )
            ), new IdValue<>(
                "4",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename"),
                    "description",
                    "dateUploaded",
                    DocumentTag.APPEAL_RESPONSE
                )
            )
        );

        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalDocumentList));
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).clear(CUSTOM_LEGAL_REP_DOCUMENTS);
        verify(asylumCase, times(1)).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);

        verify(appender, times(4)).append(
            documentsCaptor.capture(), eq(customCollections));

        List<DocumentWithDescription> legalRepresentativeDocuments =
            documentsCaptor
                .getAllValues();
        assertEquals(4, legalRepresentativeDocuments.size());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_filter_addendum_evidence_document_with_correct_appellant_respondent_tags(Event event) {

        when(callback.getEvent()).thenReturn(event);
        List<IdValue<DocumentWithDescription>> customCollections = new ArrayList<>();

        when(appender.append(any(DocumentWithDescription.class), anyList()))
                .thenReturn(customCollections);

        DocumentWithMetadata addendumEvidenceDocuments = new DocumentWithMetadata(
                new Document("documentUrl", "binaryUrl", "documentFilename"),
                "description",
                "dateUploaded",
                DocumentTag.ADDENDUM_EVIDENCE,
                "test"
        );
        List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentsList = asList(
                new IdValue<>(
                        "1",
                        addendumEvidenceDocuments
                ),
                new IdValue<>(
                        "2",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.ADDENDUM_EVIDENCE,
                                "The appellant"
                        )
                ),
                new IdValue<>(
                        "3",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.ADDENDUM_EVIDENCE,
                                "The respondent"
                        )
                ),
                new IdValue<>(
                        "4",
                        new DocumentWithMetadata(
                                new Document("documentUrl", "binaryUrl", "documentFilename"),
                                "description",
                                "dateUploaded",
                                DocumentTag.ADDENDUM_EVIDENCE,
                                "test"
                        )
                )
        );

        when(asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(addendumEvidenceDocumentsList));

        customiseHearingBundlePreparer.populateCustomCollections(
            asylumCase, ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, false);

        verify(asylumCase, times(1)).clear(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, customCollections);

        customiseHearingBundlePreparer.populateCustomCollections(
            asylumCase, ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, false);

        verify(asylumCase, times(1)).clear(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).write(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, customCollections);

        verify(appender, times(2)).append(
            documentsCaptor.capture(), eq(customCollections));

        List<DocumentWithDescription> appDocuments = documentsCaptor.getAllValues();
        assertEquals(2, appDocuments.size());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_not_create_custom_collections_if_source_collections_are_empty(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);


        when(asylumCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.empty());
        when(asylumCase.read(ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.empty());
        when(asylumCase.read(RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.empty());

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, never()).write(any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_not_create_custom_collections_if_source_collections_are_empty_in_reheard_case(Event event) {
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, never()).write(any(), any());
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_create_custom_collections_in_reheard_case_with_remittal_enabled() {
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(SOURCE_OF_REMITTAL, String.class)).thenReturn(Optional.of("Court of Appeal"));
        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class),
            Optional.of(YesOrNo.YES));

        when(callback.getEvent()).thenReturn(Event.CUSTOMISE_HEARING_BUNDLE);

        final List<IdValue<DocumentWithDescription>> customDocumentList =
            List.of(new IdValue<>("1", createDocumentWithDescription()));

        final List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.REHEARD_HEARING_NOTICE, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantEvidenceDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentEvidenceDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_APPELLANT, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FTPA_RESPONDENT, "test")));
        final List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocumentList =
            List.of(new IdValue<>("1", createDocumentWithMetadata(DocumentTag.FINAL_DECISION_AND_REASONS_PDF, "test")));

        final List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentList = List.of(
            new IdValue<>("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "")),
            new IdValue<>("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

        final List<IdValue<ReheardHearingDocuments>> reheardHearingDocs = buildReheardDocuments();
        final List<IdValue<ReheardHearingDocuments>> reheardDecisionDocs = buildReheardDocuments();
        final List<IdValue<RemittalDocument>> remittalDocuments = buildRemittalDocuments();
        when(asylumCase.read(REHEARD_DECISION_REASONS_COLLECTION)).thenReturn(Optional.of(reheardDecisionDocs));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION)).thenReturn(Optional.of(reheardHearingDocs));
        when(asylumCase.read(REMITTAL_DOCUMENTS)).thenReturn(Optional.of(remittalDocuments));

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customDocumentList);

        when(asylumCase.read(APP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(ftpaAppellantEvidenceDocumentList));

        when(asylumCase.read(RESP_ADDITIONAL_EVIDENCE_DOCS))
            .thenReturn(Optional.of(ftpaRespondentEvidenceDocumentList));

        when(asylumCase.read(AsylumCaseDefinition.FTPA_APPELLANT_DOCUMENTS))
            .thenReturn(Optional.of(ftpaAppellantDocumentList));

        when(asylumCase.read(AsylumCaseDefinition.FTPA_RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(ftpaRespondentDocumentList));

        // These reheard documents will be stored in collection of objects format after Remittal feature is released.
        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(AsylumCaseDefinition.REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.empty());

        when(asylumCase.read(AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(addendumEvidenceDocumentList));


        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(CUSTOM_REHEARD_HEARING_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FTPA_APPELLANT_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_LATEST_REMITTAL_DOCS, customDocumentList);
        verify(asylumCase, times(1)).write(AsylumCaseDefinition.CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase, times(4)).read(AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = customiseHearingBundlePreparer.canHandle(callbackStage, callback);

                if ((event == Event.CUSTOMISE_HEARING_BUNDLE
                    || event == Event.GENERATE_UPDATED_HEARING_BUNDLE)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {
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

        assertThatThrownBy(() -> customiseHearingBundlePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundlePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundlePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"CUSTOMISE_HEARING_BUNDLE", "GENERATE_UPDATED_HEARING_BUNDLE"})
    void should_ignore_existing_hearing_bundles_in_new_bundles(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(Optional.empty());

        List<IdValue<DocumentWithMetadata>> hearingDocumentList = List.of(
            new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_BUNDLE, "test")));
        when(asylumCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        verify(appender, times(0)).append(any(DocumentWithDescription.class), anyList());

        hearingDocumentList = asList(
                new IdValue<>("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test")),
                new IdValue<>("2", createDocumentWithMetadata(DocumentTag.HEARING_BUNDLE, "test")));

        when(asylumCase.read(HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));
        when(asylumCase.read(REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        customiseHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        verify(appender, times(1)).append(any(DocumentWithDescription.class), anyList());
    }

    private DocumentWithDescription createDocumentWithDescription() {
        return
            new DocumentWithDescription(new Document("some-url",
                "some-binary-url",
                RandomStringUtils.secure().nextAlphabetic(20)), "test");
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
            document, "test", "2023-12-12", DocumentTag.REMITTAL_DECISION, "");
        final DocumentWithMetadata remittalOtherDoc1 = new DocumentWithMetadata(
            document, "other-test-1", "2023-12-12", DocumentTag.REMITTAL_DECISION, "");
        final DocumentWithMetadata remittalOtherDoc2 = new DocumentWithMetadata(
            document, "other-test-1", "2023-12-12", DocumentTag.REMITTAL_DECISION, "");
        IdValue<DocumentWithMetadata> decisionDocWithMetadata =
            new IdValue<>("11", remittalOtherDoc1);
        IdValue<DocumentWithMetadata> coverLetterDocWithMetadata =
            new IdValue<>("12", remittalOtherDoc2);

        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = List.of(decisionDocWithMetadata, coverLetterDocWithMetadata);
        IdValue<RemittalDocument> remittalDocuments =
            new IdValue<>("1", new RemittalDocument(remittalDec, listOfDocumentsWithMetadata));
        return List.of(remittalDocuments);
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
        final List<IdValue<DocumentWithMetadata>> listOfDocumentsWithMetadata = List.of(decisionDocWithMetadata, coverLetterDocWithMetadata);
        IdValue<ReheardHearingDocuments> reheardHearingDocuments =
            new IdValue<>("1", new ReheardHearingDocuments(listOfDocumentsWithMetadata));
        return List.of(reheardHearingDocuments);
    }
}
