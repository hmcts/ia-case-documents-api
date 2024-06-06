package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@SuppressWarnings("unchecked")
class GenerateAmendedHearingBundlePreparerTest {

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

    @Captor
    private ArgumentCaptor<DocumentWithDescription> documentsCaptor;

    private GenerateAmendedHearingBundlePreparer generateAmendedHearingBundlePreparer;

    @BeforeEach
    void setUp() {
        generateAmendedHearingBundlePreparer =
            new GenerateAmendedHearingBundlePreparer(appender, featureToggler);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "SUITABLE", "UNSUITABLE"})
    void should_create_custom_collections(String maybeDecision) {
        when(callback.getEvent()).thenReturn(Event.GENERATE_AMENDED_HEARING_BUNDLE);
        when(asylumCase.read(SUITABILITY_REVIEW_DECISION)).thenReturn(maybeDecision.isEmpty()
                ? Optional.empty() : Optional.of(AdaSuitabilityReviewDecision.valueOf(maybeDecision)));

        List<IdValue<DocumentWithDescription>> customCollections =
            asList(new IdValue("1", createDocumentWithDescription()));
        List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.HEARING_NOTICE, "test")));

        List<IdValue<DocumentWithMetadata>> legalDocumentList = asList(
            new IdValue("1", createDocumentWithMetadata(DocumentTag.CASE_ARGUMENT, "test")),
            new IdValue("2", createDocumentWithMetadata(DocumentTag.APPEAL_SUBMISSION, "tes")),
            new IdValue("3", createDocumentWithMetadata(DocumentTag.CASE_SUMMARY, "test")));

        List<IdValue<DocumentWithMetadata>> tribunalDocumentList = asList(
                new IdValue("1", createDocumentWithMetadata(DocumentTag.ADA_SUITABILITY, "test")));

        List<IdValue<DocumentWithMetadata>> additionalEvidenceList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "test")));
        List<IdValue<DocumentWithMetadata>> respondentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.RESPONDENT_EVIDENCE, "test")));

        when(appender.append(any(DocumentWithDescription.class), anyList()))
            .thenReturn(customCollections);

        when(asylumCase.read(AsylumCaseDefinition.HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.of(legalDocumentList));

        when(asylumCase.read(AsylumCaseDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(additionalEvidenceList));

        when(asylumCase.read(AsylumCaseDefinition.RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.of(respondentList));

        when(asylumCase.read(AsylumCaseDefinition.TRIBUNAL_DOCUMENTS))
                .thenReturn(Optional.of(tribunalDocumentList));

        generateAmendedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(AsylumCaseDefinition.CUSTOM_HEARING_DOCUMENTS, customCollections);
        verify(asylumCase).write(CUSTOM_LEGAL_REP_DOCUMENTS, customCollections);
        verify(asylumCase).write(AsylumCaseDefinition.CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS, customCollections);
        verify(asylumCase).write(AsylumCaseDefinition.CUSTOM_RESPONDENT_DOCUMENTS, customCollections);
        verify(asylumCase,times(0)).write(AsylumCaseDefinition.CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS,customCollections);
        verify(asylumCase,times(0)).read(AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase,times(maybeDecision.isEmpty() ? 0 : 1))
                .write(AsylumCaseDefinition.CUSTOM_TRIBUNAL_DOCUMENTS,customCollections);
    }

    @Test
    void should_create_custom_collections_in_reheard_case() {
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(true);
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class),
            Optional.of(YesOrNo.YES));

        when(callback.getEvent()).thenReturn(Event.GENERATE_AMENDED_HEARING_BUNDLE);

        final List<IdValue<DocumentWithDescription>> customDocumentList =
            asList(new IdValue("1", createDocumentWithDescription()));

        final List<IdValue<DocumentWithMetadata>> hearingDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.REHEARD_HEARING_NOTICE, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantEvidenceDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentEvidenceDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDITIONAL_EVIDENCE, "")));
        final List<IdValue<DocumentWithMetadata>> ftpaAppellantDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.FTPA_APPELLANT, "test")));
        final List<IdValue<DocumentWithMetadata>> ftpaRespondentDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.FTPA_RESPONDENT, "test")));
        final List<IdValue<DocumentWithMetadata>> finalDecisionAndReasonsDocumentList =
            asList(new IdValue("1", createDocumentWithMetadata(DocumentTag.FINAL_DECISION_AND_REASONS_PDF, "test")));

        final List<IdValue<DocumentWithMetadata>> addendumEvidenceDocumentList = asList(
            new IdValue("3", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "")),
            new IdValue("2", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The appellant")),
            new IdValue("1", createDocumentWithMetadata(DocumentTag.ADDENDUM_EVIDENCE, "The respondent")));

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

        when(asylumCase.read(AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS))
            .thenReturn(Optional.of(finalDecisionAndReasonsDocumentList));

        when(asylumCase.read(AsylumCaseDefinition.REHEARD_HEARING_DOCUMENTS))
            .thenReturn(Optional.of(hearingDocumentList));

        when(asylumCase.read(AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.of(addendumEvidenceDocumentList));


        generateAmendedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase).write(CUSTOM_REHEARD_HEARING_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FTPA_APPELLANT_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FTPA_RESPONDENT_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_FINAL_DECISION_AND_REASONS_DOCS, customDocumentList);
        verify(asylumCase).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, customDocumentList);
        verify(asylumCase,times(1)).write(AsylumCaseDefinition.CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS,customDocumentList);
        verify(asylumCase,times(4)).read(AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS);
        verify(asylumCase, never()).read(TRIBUNAL_DOCUMENTS);
    }

    @Test
    void should_filter_legal_rep_document_with_correct_tags() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_AMENDED_HEARING_BUNDLE);

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
            new IdValue<DocumentWithMetadata>(
                "1",
                legalDocument
            ),
            new IdValue<DocumentWithMetadata>(
                "2",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename"),
                    "description",
                    "dateUploaded",
                    DocumentTag.APPEAL_SUBMISSION
                )
            ),
            new IdValue<DocumentWithMetadata>(
                "3",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename"),
                    "description",
                    "dateUploaded",
                    DocumentTag.CASE_SUMMARY
                )
            ), new IdValue<DocumentWithMetadata>(
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
            generateAmendedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

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

    @Test
    void should_filter_addendum_evidence_document_with_correct_appellant_respondent_tags() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_AMENDED_HEARING_BUNDLE);
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
            new IdValue<DocumentWithMetadata>(
                "1",
                addendumEvidenceDocuments
            ),
            new IdValue<DocumentWithMetadata>(
                "2",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename"),
                    "description",
                    "dateUploaded",
                    DocumentTag.ADDENDUM_EVIDENCE,
                    "The appellant"
                )
            ),
            new IdValue<DocumentWithMetadata>(
                "3",
                new DocumentWithMetadata(
                    new Document("documentUrl", "binaryUrl", "documentFilename"),
                    "description",
                    "dateUploaded",
                    DocumentTag.ADDENDUM_EVIDENCE,
                    "The respondent"
                )
            ),
            new IdValue<DocumentWithMetadata>(
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

        generateAmendedHearingBundlePreparer.populateCustomCollections(
            asylumCase, ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);

        verify(asylumCase, times(1)).clear(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).write(CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS, customCollections);

        generateAmendedHearingBundlePreparer.populateCustomCollections(
            asylumCase, ADDENDUM_EVIDENCE_DOCUMENTS, CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);

        verify(asylumCase, times(1)).clear(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS);
        verify(asylumCase, times(1)).write(CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS, customCollections);

        verify(appender, times(2)).append(
            documentsCaptor.capture(), eq(customCollections));

        List<DocumentWithDescription> appDocuments = documentsCaptor.getAllValues();
        assertEquals(2, appDocuments.size());
    }

    @Test
    void should_not_create_custom_collections_if_source_collections_are_empty() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_AMENDED_HEARING_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);


        when(asylumCase.read(AsylumCaseDefinition.HEARING_DOCUMENTS))
            .thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REPRESENTATIVE_DOCUMENTS))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS))
            .thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.RESPONDENT_DOCUMENTS))
            .thenReturn(Optional.empty());

        generateAmendedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, never()).write(any(), any());
    }

    @Test
    void should_not_create_custom_collections_if_source_collections_are_empty_in_reheard_case() {
        when(asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(featureToggler.getValue("reheard-feature", false)).thenReturn(false);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.GENERATE_AMENDED_HEARING_BUNDLE);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        generateAmendedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, never()).write(any(), any());
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(
            () -> generateAmendedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateAmendedHearingBundlePreparer.canHandle(callbackStage, callback);

                if (event == Event.GENERATE_AMENDED_HEARING_BUNDLE
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

        assertThatThrownBy(() -> generateAmendedHearingBundlePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateAmendedHearingBundlePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateAmendedHearingBundlePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateAmendedHearingBundlePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    private DocumentWithDescription createDocumentWithDescription() {
        return
            new DocumentWithDescription(new Document("some-url",
                "some-binary-url",
                RandomStringUtils.randomAlphabetic(20)), "test");
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
}
