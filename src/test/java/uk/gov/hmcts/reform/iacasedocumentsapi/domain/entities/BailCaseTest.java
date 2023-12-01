package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Collections.singletonList;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DETAINED_LOC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.APPLICANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.UPLOAD_THE_BAIL_EVIDENCE_DOCS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;

public class BailCaseTest {
    private final String caseData = "{\"applicantFamilyName\": \"family\", \"applicantGivenNames\": \"test\", \"applicantDateOfBirth\": \"2000-02-02\", \"applicantDetainedLoc\": \"immigrationRemovalCentre\", \"applicantNationality\": \"HAS_NATIONALITY\", \"applicantNationalities\": [{\"id\": \"f90f10db-1a34-43d9-a8f8-1cc9fac91891\", \"value\": {\"code\": \"Algerian\"}}], \"applicantPrisonDetails\": null, \"hasAppealHearingPending\": \"DontKnow\", \"applicantArrivalInUKDate\": \"2021-12-12\"}";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private BailCase bailCase;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        bailCase = objectMapper.readValue(caseData, BailCase.class);
    }

    @Test
    void should_read_simple_type_with_parameter_type() {
        assertEquals("family", bailCase.read(APPLICANT_FAMILY_NAME, String.class).get());
        assertEquals("immigrationRemovalCentre", bailCase.read(APPLICANT_DETAINED_LOC, String.class).get());
    }

    @Test
    void should_read_complex_type_with_target_parameter_type() {
        Optional<List<IdValue<NationalityFieldValue>>> mayBeNationalities = bailCase.read(APPLICANT_NATIONALITIES);
        List<IdValue<NationalityFieldValue>> nationalityList = mayBeNationalities.get();
        NationalityFieldValue nationality = nationalityList.get(0).getValue();
        assertEquals(1, nationalityList.size());
        assertEquals("Algerian", nationality.getCode());
    }

    @Test
    void should_write_simple_types() {
        bailCase.write(APPLICANT_GIVEN_NAMES, "John");
        bailCase.write(APPLICANT_FAMILY_NAME, "Doe");
        assertEquals("John", bailCase.read(APPLICANT_GIVEN_NAMES).get());
        assertEquals("Doe", bailCase.read(APPLICANT_FAMILY_NAME).get());
    }

    @Test
    void should_write_complex_type() {
        IdValue<DocumentWithDescription> idValue = new IdValue<>(
            "some-id",
            new DocumentWithDescription(
                new Document(
                    "some-doc-url",
                    "some-doc-binary-url",
                    "some-doc-filename",
                        "some-hash"
                ),
                "some-description"));
        bailCase.write(UPLOAD_THE_BAIL_EVIDENCE_DOCS, singletonList(idValue));
        Optional<List<IdValue<DocumentWithDescription>>> maybeEvidence = bailCase.read(UPLOAD_THE_BAIL_EVIDENCE_DOCS);
        IdValue<DocumentWithDescription> documentWithDescriptionIdValue = maybeEvidence.get().get(0);

        Assertions.assertEquals(1, maybeEvidence.get().size());
        Assertions.assertEquals("some-id", documentWithDescriptionIdValue.getId());
        Assertions.assertEquals("some-doc-url", documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentUrl());
        Assertions.assertEquals("some-doc-binary-url", documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentBinaryUrl());
        Assertions.assertEquals("some-doc-filename", documentWithDescriptionIdValue.getValue().getDocument().get().getDocumentFilename());
        Assertions.assertEquals("some-description", documentWithDescriptionIdValue.getValue().getDescription().get());
    }

    @Test
    void should_clear_field() {
        bailCase.clear(APPLICANT_FAMILY_NAME);
        assertEquals(Optional.empty(), bailCase.read(APPLICANT_FAMILY_NAME));
    }
}
