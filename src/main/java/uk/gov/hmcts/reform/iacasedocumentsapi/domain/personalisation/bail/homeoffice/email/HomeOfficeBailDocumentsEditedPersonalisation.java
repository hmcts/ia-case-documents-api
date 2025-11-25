package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.editbaildocuments.DocumentsEditedPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.editbaildocuments.EditBailDocumentService;

@Service
public class HomeOfficeBailDocumentsEditedPersonalisation extends DocumentsEditedPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeBailDocumentsEditedPersonalisationTemplateId;
    private final String homeOfficeBailDocumentsEditedWithoutLrPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;

    public HomeOfficeBailDocumentsEditedPersonalisation(
        @NotNull(message = "homeOfficeBailApplicationSubmittedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.editBailDocuments.email}") String homeOfficeBailDocumentsEditedPersonalisationTemplateId,
        @Value("${govnotify.bail.template.editBailDocumentsWithoutLr.email}") String homeOfficeBailDocumentsEditedWithoutLrPersonalisationTemplateId,
        EditBailDocumentService editBailDocumentService,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress
    ) {
        super(editBailDocumentService);
        this.homeOfficeBailDocumentsEditedPersonalisationTemplateId = homeOfficeBailDocumentsEditedPersonalisationTemplateId;
        this.homeOfficeBailDocumentsEditedWithoutLrPersonalisationTemplateId = homeOfficeBailDocumentsEditedWithoutLrPersonalisationTemplateId;

        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_EDITED_DOCUMENTS_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {
        return bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES
            ? homeOfficeBailDocumentsEditedPersonalisationTemplateId : homeOfficeBailDocumentsEditedWithoutLrPersonalisationTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<BailCase> callback) {
        requireNonNull(callback, "bailCase must not be null");
        BailCase bailCase = callback.getCaseDetails().getCaseData();
        CaseDetails<BailCase> bailCaseBefore = callback.getCaseDetailsBefore().orElse(null);

        List<String> latestModifiedDocuments = getEditedOrDeletedOrAddedDocumentList(bailCase, bailCaseBefore);
        String formattedLatestModifiedDocuments = String.join(",\n", latestModifiedDocuments);

        return isLegallyRepresented(bailCase)
            ? ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("latestModifiedDocuments", formattedLatestModifiedDocuments)
            .put("reasonForChange", getReasonFromCaseNoteDescription(bailCase))
            .build()
            : ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("latestModifiedDocuments", formattedLatestModifiedDocuments)
            .put("reasonForChange", getReasonFromCaseNoteDescription(bailCase))
            .build();
    }

}
