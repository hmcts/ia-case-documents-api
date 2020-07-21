package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer.editdocument;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADDENDUM_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ADDITIONAL_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DRAFT_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FINAL_DECISION_AND_REASONS_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_RECORDING_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.RESPONDENT_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_DOCUMENTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HasDocument;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Service
public class EditDocumentService {

    public FormattedDocumentList getFormattedDocumentsGivenCaseAndDocIds(AsylumCase asylumCase,
                                                                         List<String> docIdsMatch) {
        List<FormattedDocument> formattedDocList = new ArrayList<>();
        getListOfDocumentFields().forEach(fieldDefinition -> {
            Optional<List<IdValue<HasDocument>>> fieldOptional = asylumCase.read(fieldDefinition);
            if (fieldOptional.isPresent()) {
                List<IdValue<HasDocument>> docs = fieldOptional.get();
                docs.forEach(doc -> addToListIfMatch(docIdsMatch, formattedDocList, doc.getValue()));
            }
        });
        return new FormattedDocumentList(formattedDocList);
    }

    private void addToListIfMatch(List<String> docIds, List<FormattedDocument> formattedDocList, HasDocument doc) {
        String docId = getIdFromDocUrl(doc.getDocument().getDocumentUrl());
        if (docIds.contains(docId)) {
            FormattedDocument formattedDocument = new FormattedDocument(doc.getDocument().getDocumentFilename(),
                doc.getDescription());
            formattedDocList.add(formattedDocument);
        }
    }

    public static String getIdFromDocUrl(String documentUrl) {
        String regexToGetStringFromTheLastForwardSlash = "([^/]+$)";
        Pattern pattern = Pattern.compile(regexToGetStringFromTheLastForwardSlash);
        Matcher matcher = pattern.matcher(documentUrl);
        if (matcher.find()) {
            return matcher.group();
        }
        return documentUrl;
    }

    private List<AsylumCaseDefinition> getListOfDocumentFields() {
        return Arrays.asList(
            ADDITIONAL_EVIDENCE_DOCUMENTS,
            TRIBUNAL_DOCUMENTS,
            HEARING_DOCUMENTS,
            LEGAL_REPRESENTATIVE_DOCUMENTS,
            ADDENDUM_EVIDENCE_DOCUMENTS,
            RESPONDENT_DOCUMENTS,
            DRAFT_DECISION_AND_REASONS_DOCUMENTS,
            FINAL_DECISION_AND_REASONS_DOCUMENTS,
            HEARING_RECORDING_DOCUMENTS);
    }
}
