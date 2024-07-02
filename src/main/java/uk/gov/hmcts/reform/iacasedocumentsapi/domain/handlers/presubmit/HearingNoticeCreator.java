package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REHEARD_HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REHEARD_HEARING_DOCUMENTS_COLLECTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isInternalNonDetainedCase;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ReheardHearingDocuments;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.Appender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentReceiver;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentsAppender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;

@Component
public class HearingNoticeCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> hearingNoticeDocumentCreator;
    private final DocumentCreator<AsylumCase> remoteHearingNoticeDocumentCreator;
    private final DocumentCreator<AsylumCase> adaHearingNoticeDocumentCreator;
    private final DocumentHandler documentHandler;
    private final FeatureToggler featureToggler;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final Appender<ReheardHearingDocuments> reheardHearingAppender;

    public HearingNoticeCreator(
        @Qualifier("hearingNotice") DocumentCreator<AsylumCase> hearingNoticeDocumentCreator,
        @Qualifier("remoteHearingNotice") DocumentCreator<AsylumCase> remoteHearingNoticeDocumentCreator,
        @Qualifier("adaHearingNotice") DocumentCreator<AsylumCase> adaHearingNoticeDocumentCreator,
        DocumentHandler documentHandler,
        FeatureToggler featureToggler,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        Appender<ReheardHearingDocuments> reheardHearingAppender
    ) {
        this.hearingNoticeDocumentCreator = hearingNoticeDocumentCreator;
        this.remoteHearingNoticeDocumentCreator = remoteHearingNoticeDocumentCreator;
        this.adaHearingNoticeDocumentCreator = adaHearingNoticeDocumentCreator;
        this.documentHandler = documentHandler;
        this.featureToggler = featureToggler;
        this.documentReceiver = documentReceiver;
        this.documentsAppender = documentsAppender;
        this.reheardHearingAppender = reheardHearingAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && List.of(Event.LIST_CASE, Event.ADJOURN_HEARING_WITHOUT_DATE).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        HearingCentre listCaseHearingCentre =
            asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(HearingCentre.TAYLOR_HOUSE);

        Document hearingNotice;
        if (listCaseHearingCentre.equals(HearingCentre.REMOTE_HEARING)) {
            hearingNotice = remoteHearingNoticeDocumentCreator.create(caseDetails);
        } else {
            boolean isAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO) == YES;
            hearingNotice = isAda ? adaHearingNoticeDocumentCreator.create(caseDetails) : hearingNoticeDocumentCreator.create(caseDetails);
        }

        if ((asylumCase.read(AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED, YesOrNo.class).equals(Optional.of(YES))
            && (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YES)).orElse(false)))) {

            if (featureToggler.getValue("dlrm-remitted-feature-flag", false)) {
                appendReheardHearingDocuments(asylumCase, hearingNotice);
            } else {
                documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    hearingNotice,
                    REHEARD_HEARING_DOCUMENTS,
                    DocumentTag.REHEARD_HEARING_NOTICE
                );
            }
        } else {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                HEARING_DOCUMENTS,
                DocumentTag.HEARING_NOTICE
            );

            if (isInternalNonDetainedCase(asylumCase)) {
                appendListedOrAdjournedLetter(asylumCase, hearingNotice, callback.getEvent());
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void appendListedOrAdjournedLetter(AsylumCase asylumCase, Document hearingNotice, Event event) {
        if (event == Event.LIST_CASE) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                LETTER_NOTIFICATION_DOCUMENTS,
                DocumentTag.INTERNAL_CASE_LISTED_LETTER
            );
        }

        if (event == Event.ADJOURN_HEARING_WITHOUT_DATE) {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNotice,
                LETTER_NOTIFICATION_DOCUMENTS,
                DocumentTag.INTERNAL_ADJOURN_WITHOUT_DATE_LETTER
            );
        }
    }

    private void appendReheardHearingDocuments(AsylumCase asylumCase, Document hearingNotice) {
        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                hearingNotice,
                "",
                DocumentTag.REHEARD_HEARING_NOTICE
            );

        List<IdValue<DocumentWithMetadata>> allDocuments =
            documentsAppender.append(
                Collections.emptyList(),
                Collections.singletonList(documentWithMetadata)
            );

        ReheardHearingDocuments newReheardDocuments = new ReheardHearingDocuments(allDocuments);

        Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
            asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
        List<IdValue<ReheardHearingDocuments>> allReheardDocuments =
            reheardHearingAppender.append(newReheardDocuments, maybeExistingReheardDocuments.orElse(emptyList()));
        asylumCase.write(REHEARD_HEARING_DOCUMENTS_COLLECTION, allReheardDocuments);
    }
}
