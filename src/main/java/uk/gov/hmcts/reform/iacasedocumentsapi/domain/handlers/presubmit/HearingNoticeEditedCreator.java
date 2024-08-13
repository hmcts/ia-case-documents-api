package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Component
public class HearingNoticeEditedCreator implements PreSubmitCallbackHandler<AsylumCase> {

    private final DocumentCreator<AsylumCase> hearingNoticeUpdatedRequirementsDocumentCreator;
    private final DocumentCreator<AsylumCase> hearingNoticeUpdatedDetailsDocumentCreator;
    private final DocumentCreator<AsylumCase> remoteHearingNoticeUpdatedDetailsDocumentCreator;
    private final DocumentCreator<AsylumCase> adaHearingNoticeUpdatedDetailsDocumentCreator;
    private final DocumentHandler documentHandler;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final FeatureToggler featureToggler;
    private final DocumentReceiver documentReceiver;
    private final DocumentsAppender documentsAppender;
    private final Appender<ReheardHearingDocuments> reheardHearingAppender;

    public HearingNoticeEditedCreator(
        @Qualifier("hearingNoticeUpdatedRequirements") DocumentCreator<AsylumCase> hearingNoticeUpdatedRequirementsDocumentCreator,
        @Qualifier("hearingNoticeUpdatedDetails") DocumentCreator<AsylumCase> hearingNoticeUpdatedDetailsDocumentCreator,
        @Qualifier("remoteHearingNoticeUpdatedDetails") DocumentCreator<AsylumCase> remoteHearingNoticeUpdatedDetailsDocumentCreator,
        @Qualifier("adaHearingNoticeUpdatedDetails") DocumentCreator<AsylumCase> adaHearingNoticeUpdatedDetailsDocumentCreator,
        DocumentHandler documentHandler,
        HearingDetailsFinder hearingDetailsFinder,
        FeatureToggler featureToggler,
        DocumentReceiver documentReceiver,
        DocumentsAppender documentsAppender,
        Appender<ReheardHearingDocuments> reheardHearingAppender
    ) {
        this.hearingNoticeUpdatedRequirementsDocumentCreator = hearingNoticeUpdatedRequirementsDocumentCreator;
        this.hearingNoticeUpdatedDetailsDocumentCreator = hearingNoticeUpdatedDetailsDocumentCreator;
        this.remoteHearingNoticeUpdatedDetailsDocumentCreator = remoteHearingNoticeUpdatedDetailsDocumentCreator;
        this.adaHearingNoticeUpdatedDetailsDocumentCreator = adaHearingNoticeUpdatedDetailsDocumentCreator;
        this.documentHandler = documentHandler;
        this.hearingDetailsFinder = hearingDetailsFinder;
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
            && Event.EDIT_CASE_LISTING == callback.getEvent();
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
        final Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();
        final String listCaseHearingCentre = hearingDetailsFinder.getHearingCentreName(caseDetails.getCaseData());
        final String hearingDate = hearingDetailsFinder.getHearingDateTime(caseDetails.getCaseData());


        if (caseDetailsBefore.isPresent()) {

            final String hearingCentreNameBefore =
                hearingDetailsFinder.getHearingCentreName(caseDetailsBefore.get().getCaseData());

            final String oldHearingDate =
                hearingDetailsFinder.getHearingDateTime(caseDetailsBefore.get().getCaseData());

            boolean isAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO) == YES;
            boolean isCaseUsingLocationRefData = asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)
                .orElse(YesOrNo.NO).equals(YesOrNo.YES);

            //prevent the existing case with previous selected remote hearing when the ref data feature is on with different hearing centre
            //IS_REMOTE_HEARING is used for the case ref data
            if ((!isCaseUsingLocationRefData && asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class).equals(Optional.of(HearingCentre.REMOTE_HEARING)))
                || (isCaseUsingLocationRefData && asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES))) {
                generateDocument(caseDetails, asylumCase, caseDetailsBefore, remoteHearingNoticeUpdatedDetailsDocumentCreator);
            } else if (hearingCentreNameBefore.equals(listCaseHearingCentre) && oldHearingDate.equals(hearingDate)) {
                if (isAda) {
                    generateDocument(caseDetails, asylumCase, caseDetailsBefore, adaHearingNoticeUpdatedDetailsDocumentCreator);
                } else {
                    generateDocument(caseDetails, asylumCase, caseDetailsBefore, hearingNoticeUpdatedRequirementsDocumentCreator);
                }
            } else {
                if (isAda) {
                    generateDocument(caseDetails, asylumCase, caseDetailsBefore, adaHearingNoticeUpdatedDetailsDocumentCreator);
                } else {
                    generateDocument(caseDetails, asylumCase, caseDetailsBefore, hearingNoticeUpdatedDetailsDocumentCreator);
                }
            }
        } else {
            throw new IllegalStateException("previous case data is not present");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void generateDocument(CaseDetails<AsylumCase> caseDetails,
                                  AsylumCase asylumCase,
                                  Optional<CaseDetails<AsylumCase>> caseDetailsBefore,
                                  DocumentCreator<AsylumCase> hearingNoticeEditedDocumentCreator) {

        Document hearingNoticeEdited =
            hearingNoticeEditedDocumentCreator.create(
                caseDetails,
                caseDetailsBefore
                    .orElseThrow(() -> new IllegalStateException("previous case data is not present")));

        if ((asylumCase.read(AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED, YesOrNo.class).equals(Optional.of(YesOrNo.YES))
            && (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)))) {
            if (featureToggler.getValue("dlrm-remitted-feature-flag", false)) {
                appendReheardHearingDocuments(asylumCase, hearingNoticeEdited);
            } else {
                documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                    asylumCase,
                    hearingNoticeEdited,
                    REHEARD_HEARING_DOCUMENTS,
                    DocumentTag.REHEARD_HEARING_NOTICE_RELISTED
                );
            }
        } else {
            documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
                asylumCase,
                hearingNoticeEdited,
                HEARING_DOCUMENTS,
                DocumentTag.HEARING_NOTICE_RELISTED
            );
        }
    }

    private void appendReheardHearingDocuments(AsylumCase asylumCase, Document hearingNotice) {
        DocumentWithMetadata documentWithMetadata =
            documentReceiver.receive(
                hearingNotice,
                "",
                DocumentTag.REHEARD_HEARING_NOTICE_RELISTED
            );

        Optional<List<IdValue<ReheardHearingDocuments>>> maybeExistingReheardDocuments =
            asylumCase.read(REHEARD_HEARING_DOCUMENTS_COLLECTION);
        List<IdValue<ReheardHearingDocuments>> existingReheardDocuments = maybeExistingReheardDocuments.orElse(emptyList());

        if (!existingReheardDocuments.isEmpty()) {
            IdValue<ReheardHearingDocuments> latestReheardHearingDocument = existingReheardDocuments.get(0);
            List<IdValue<DocumentWithMetadata>> allDocuments = documentsAppender.append(
                latestReheardHearingDocument.getValue().getReheardHearingDocs(),
                Collections.singletonList(documentWithMetadata)
            );
            IdValue<ReheardHearingDocuments> updatedReheardHearingDocuments = new IdValue<>(
                latestReheardHearingDocument.getId(),
                new ReheardHearingDocuments(allDocuments)
            );
            ArrayList<IdValue<ReheardHearingDocuments>> newReheardCollection = new ArrayList<>(existingReheardDocuments);
            newReheardCollection.set(0, updatedReheardHearingDocuments);
            existingReheardDocuments = newReheardCollection;
        } else {
            List<IdValue<DocumentWithMetadata>> allDocuments = documentsAppender.append(
                emptyList(),
                Collections.singletonList(documentWithMetadata)
            );
            ReheardHearingDocuments newReheardDocuments = new ReheardHearingDocuments(allDocuments);
            existingReheardDocuments = reheardHearingAppender.append(newReheardDocuments, existingReheardDocuments);
        }

        asylumCase.write(REHEARD_HEARING_DOCUMENTS_COLLECTION, existingReheardDocuments);
    }
}
