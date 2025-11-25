package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer.editdocument;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;

@Service
public class CaseOfficerEditDocumentsPersonalisation implements EmailNotificationPersonalisation {

    private final String appealDocumentDeletedCaseOfficerBeforeListingTemplateId;
    private final String appealDocumentDeletedCaseOfficerAfterListingTemplateId;
    private final EmailAddressFinder emailAddressFinder;
    private final EditDocumentService editDocumentService;
    private final String iaExUiFrontendUrl;
    private final AppealService appealService;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerEditDocumentsPersonalisation(
            @NotNull(message = "appealDocumentDeletedCaseOfficerBeforeListingTemplateId cannot be null")
            @Value("${govnotify.template.appealDocumentDeletedBeforeListing.caseOfficer.email}")
                    String appealDocumentDeletedCaseOfficerBeforeListingTemplateId,
            @NotNull(message = "appealDocumentDeletedCaseOfficerAfterListingTemplateId cannot be null")
            @Value("${govnotify.template.appealDocumentDeletedAfterListing.caseOfficer.email}")
                    String appealDocumentDeletedCaseOfficerAfterListingTemplateId,
            EmailAddressFinder emailAddressFinder,
            EditDocumentService editDocumentService,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            AppealService appealService, FeatureToggler featureToggler) {

        this.appealDocumentDeletedCaseOfficerBeforeListingTemplateId = appealDocumentDeletedCaseOfficerBeforeListingTemplateId;
        this.appealDocumentDeletedCaseOfficerAfterListingTemplateId = appealDocumentDeletedCaseOfficerAfterListingTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.editDocumentService = editDocumentService;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appealService = appealService;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_DOCUMENT_DELETED_CASE_OFFICER";
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase) ? appealDocumentDeletedCaseOfficerAfterListingTemplateId
            : appealDocumentDeletedCaseOfficerBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", false)
            ? Collections.singleton(emailAddressFinder.getHearingCentreEmailAddress(asylumCase))
            : Collections.emptySet();
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        return ImmutableMap.<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(
                AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(StringUtils.EMPTY))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(StringUtils.EMPTY))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(StringUtils.EMPTY))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("reasonForEditingOrDeletingDocuments", getReasonFromCaseNoteDescription(asylumCase))
            .put("editedOrDeletedDocumentList",
                getEditedOrDeletedDocumentList(asylumCase, callback.getCaseDetailsBefore().orElse(null)))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }

    private String getEditedOrDeletedDocumentList(AsylumCase asylumCase, CaseDetails<AsylumCase> caseDetailsBefore) {
        if (caseDetailsBefore == null) {
            return StringUtils.EMPTY;
        }
        return editDocumentService.getFormattedDocumentsGivenCaseAndDocNames(caseDetailsBefore.getCaseData(),
            getDocNamesFromCaseNote(asylumCase)).toString();
    }

    private List<String> getDocNamesFromCaseNote(AsylumCase asylumCase) {
        String caseNoteDescription = getCaseNoteDescriptionFromCaseNote(asylumCase);
        String[] temp = StringUtils.substringBetween(caseNoteDescription, "Document names: [", "]")
            .split(",");
        return Stream.of(temp).map(String::trim).collect(Collectors.toList());
    }

    private String getCaseNoteDescriptionFromCaseNote(AsylumCase asylumCase) {
        Optional<List<IdValue<CaseNote>>> caseNotesOptional = asylumCase.read(CASE_NOTES);
        if (caseNotesOptional.isPresent()) {
            List<IdValue<CaseNote>> caseNotes = caseNotesOptional.get();
            int latestCaseNote = 0;
            return caseNotes.get(latestCaseNote).getValue().getCaseNoteDescription();
        }
        return StringUtils.EMPTY;
    }

    private String getReasonFromCaseNoteDescription(AsylumCase asylumCase) {
        String caseNoteDescription = getCaseNoteDescriptionFromCaseNote(asylumCase);
        return StringUtils.substringAfter(caseNoteDescription, "reason:").trim();
    }

}
