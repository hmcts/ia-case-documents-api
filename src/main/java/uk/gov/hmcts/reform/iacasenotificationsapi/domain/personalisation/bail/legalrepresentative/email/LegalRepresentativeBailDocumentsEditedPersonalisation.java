package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.editbaildocuments.DocumentsEditedPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.editbaildocuments.EditBailDocumentService;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.legalrepresentative.LegalRepresentativeBailEmailNotificationPersonalisation;

@Service
public class LegalRepresentativeBailDocumentsEditedPersonalisation extends DocumentsEditedPersonalisation implements LegalRepresentativeBailEmailNotificationPersonalisation {

    private final String legalRepresentativeBailDocumentsEditedPersonalisationTemplateId;

    public LegalRepresentativeBailDocumentsEditedPersonalisation(
        @NotNull(message = "legalRepresentativeBailDocumentsEditedPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.editBailDocuments.email}") String legalRepresentativeBailDocumentsEditedPersonalisationTemplateId,
        EditBailDocumentService editBailDocumentService
    ) {
        super(editBailDocumentService);
        this.legalRepresentativeBailDocumentsEditedPersonalisationTemplateId = legalRepresentativeBailDocumentsEditedPersonalisationTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_EDITED_DOCUMENTS_LEGAL_REPRESENTATIVE";
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeBailDocumentsEditedPersonalisationTemplateId;
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
