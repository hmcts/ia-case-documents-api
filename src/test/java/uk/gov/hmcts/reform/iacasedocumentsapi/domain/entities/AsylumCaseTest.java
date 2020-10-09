package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CASE_ARGUMENT_EVIDENCE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

public class AsylumCaseTest {

    private final String caseData = "{\"appealType\": \"protection\", \"directions\": [{\"id\": \"2\", \"value\": {\"tag\": \"buildCase\", \"dateDue\": \"2019-06-06\", \"parties\": \"legalRepresentative\", \"dateSent\": \"2019-05-09\", \"explanation\": \"You must now build your case by uploading your appeal argument and evidence.\\n\\nAdvice on writing an appeal argument\\nYou must write a full argument that references:\\n- all the evidence you have or plan to rely on, including any witness statements\\n- the grounds and issues of the case\\n- any new matters\\n- any legal authorities you plan to rely on and why they are applicable to your case\\n\\nYour argument must explain why you believe the respondent's decision is wrong. You must provide all the information for the Home Office to conduct a thorough review of their decision at this stage.\\n\\nNext steps\\nOnce you have uploaded your appeal argument and all evidence, submit your case. The case officer will then review everything you've added. If your case looks ready, the case officer will send it to the respondent for their review. The respondent then has 14 days to respond.\"}}, {\"id\": \"1\", \"value\": {\"tag\": \"respondentEvidence\", \"dateDue\": \"2019-05-23\", \"parties\": \"respondent\", \"dateSent\": \"2019-05-09\", \"explanation\": \"A notice of appeal has been lodged against this asylum decision.\\n\\nYou must now send all documents to the case officer. The case officer will send them to the other party. You have 14 days to supply these documents.\\n\\nYou must include:\\n- the notice of decision\\n- any other document provided to the appellant giving reasons for that decision\\n- any statements of evidence\\n- the application form\\n- any record of interview with the appellant in relation to the decision being appealed\\n- any other unpublished documents on which you rely\\n- the notice of any other appealable decision made in relation to the appellant\"}}], \"hasNewMatters\": \"No\", \"hearingCentre\": \"taylorHouse\", \"appellantTitle\": \"MR\", \"hasOtherAppeals\": \"No\", \"notificationsSent\": [{\"id\": \"1557411655106767_APPEAL_SUBMITTED_CASE_OFFICER\", \"value\": \"c959bba5-f808-4c84-be35-3c4a59f5e71a\"}, {\"id\": \"1557411655106767_RESPONDENT_EVIDENCE_DIRECTION\", \"value\": \"c1c3c96c-326d-4916-b63c-7d49277121fe\"}, {\"id\": \"1557411655106767_BUILD_CASE_DIRECTION\", \"value\": \"c54a67fd-eede-4ad1-aa39-2c05400bf201\"}], \"caseArgumentEvidence\": [{\"id\": \"d019091d-806c-49cf-af64-669fb3d21361\", \"value\": {\"document\": {\"document_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2\", \"document_filename\": \"test.doc\", \"document_binary_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2/binary\"}, \"description\": \"desc\"}}], \"appellantFamilyName\": \"Mortimer\", \"appellantGivenNames\": \"Antony\", \"respondentDocuments\": [{\"id\": \"1\", \"value\": {\"tag\": \"respondentEvidence\", \"document\": {\"document_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2\", \"document_filename\": \"test.doc\", \"document_binary_url\": \"http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2/binary\"}, \"description\": \"desc\", \"dateUploaded\": \"2019-05-09\"}}], \"submissionOutOfTime\": \"Yes\", \"appellantDateOfBirth\": \"1980-04-12\", \"appealReferenceNumber\": \"PA/50222/2019\", \"appellantNationalities\": [{\"id\": \"44ac652f-4c79-4bf5-95f2-3f670990a900\", \"value\": {\"code\": \"AF\"}}], \"homeOfficeDecisionDate\": \"1980-04-12\", \"appealGroundsForDisplay\": [\"protectionRefugeeConvention\", \"protectionHumanRights\"], \"appealGroundsProtection\": {\"values\": [\"protectionRefugeeConvention\"]}, \"appellantNameForDisplay\": \"Antony Mortimer\", \"legalRepresentativeName\": \"A Legal Rep\", \"appealGroundsHumanRights\": {\"values\": [\"protectionHumanRights\"]}, \"appellantHasFixedAddress\": \"No\", \"homeOfficeReferenceNumber\": \"A1234567\", \"sendDirectionActionAvailable\": \"Yes\", \"caseBuildingReadyForSubmission\": \"No\", \"legalRepresentativeEmailAddress\": \"ia-law-firm-a@fake.hmcts.net\", \"currentCaseStateVisibleToCaseOfficer\": \"caseBuilding\", \"changeDirectionDueDateActionAvailable\": \"Yes\", \"uploadAdditionalEvidenceActionAvailable\": \"No\", \"currentCaseStateVisibleToLegalRepresentative\": \"caseBuilding\"}";
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AsylumCase asylumCase;

    @BeforeEach
    public void setUp() throws Exception {
        asylumCase = objectMapper.readValue(caseData, AsylumCase.class);
    }

    @Test
    public void reads_simple_type_with_target_type_generics() {

        Optional<String> maybeAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        assertEquals(maybeAppealReferenceNumber.get(), "PA/50222/2019");
    }

    @Test
    public void reads_complex_type_with_target_type_generics() {

        Optional<List<IdValue<DocumentWithDescription>>> maybeCaseArgumentEvidence = asylumCase.read(AsylumCaseDefinition.CASE_ARGUMENT_EVIDENCE);

        IdValue<DocumentWithDescription> documentWithDescriptionIdValue = maybeCaseArgumentEvidence.get().get(0);

        assertEquals(documentWithDescriptionIdValue.getId(), "d019091d-806c-49cf-af64-669fb3d21361");
        assertEquals(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentUrl(),
                "http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2");
        assertEquals(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentBinaryUrl(),
                "http://dm-store:8080/documents/7a45c5cb-7b8f-47e0-983b-815b613cdce2/binary");
        assertEquals(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentFilename(), "test.doc");
        assertEquals(documentWithDescriptionIdValue.getValue().getDescription().get(),"desc");
    }

    @Test
    public void reads_simple_type_with_parameter_type_generics() {

        assertEquals(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get(),"PA/50222/2019");
    }

    @Test
    public void writes_simple_type() {
        asylumCase.write(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number");

        assertEquals(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).get(),"some-appeal-reference-number");
    }

    @Test
    public void writes_complex_type() {

        IdValue<DocumentWithDescription> idValue = new IdValue<>(
                "some-id",
                new DocumentWithDescription(
                        new Document(
                                "some-doc-url",
                                "some-doc-binary-url",
                                "some-doc-filename"),
                        "some-description"));

        asylumCase.write(CASE_ARGUMENT_EVIDENCE, asList(idValue));


        Optional<List<IdValue<DocumentWithDescription>>> maybeCaseArgumentEvidence = asylumCase.read(CASE_ARGUMENT_EVIDENCE);

        IdValue<DocumentWithDescription> documentWithDescriptionIdValue = maybeCaseArgumentEvidence.get().get(0);


        assertEquals(maybeCaseArgumentEvidence.get().size(),1);
        assertEquals(documentWithDescriptionIdValue.getId(),"some-id");
        assertEquals(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentUrl(),"some-doc-url");
        assertEquals(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentBinaryUrl(),"some-doc-binary-url");
        assertEquals(documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentFilename(),"some-doc-filename");
        assertEquals(documentWithDescriptionIdValue.getValue().getDescription().get(),"some-description");
    }

    @Test
    public void clears_value() {
        asylumCase.clear(APPEAL_REFERENCE_NUMBER);

        assertEquals(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class), Optional.empty());
    }

}
