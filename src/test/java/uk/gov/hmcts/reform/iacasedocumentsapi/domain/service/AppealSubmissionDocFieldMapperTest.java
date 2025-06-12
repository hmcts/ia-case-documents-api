package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HasOtherAppeals;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.BailApplicationStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_TITLE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPLICATION_OUT_OF_TIME_DOCUMENT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPLICATION_OUT_OF_TIME_EXPLANATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_ADDRESS_LINES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_BUILDING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DETENTION_POSTCODE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_OTHER_APPEALS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_PENDING_BAIL_APPLICATIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_COMPANY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.OTHER_APPEALS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMOVAL_ORDER_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMOVAL_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppealSubmissionDocFieldMapperTest {

    private static final Logger log = LoggerFactory.getLogger(AppealSubmissionDocFieldMapperTest.class);

    @Mock
    private StringProvider stringProvider;
    private AppealSubmissionDocFieldMapper appealSubmissionDocFieldMapper;
    private CaseDetails<AsylumCase> caseDetails;
    private AsylumCase asylumCase;
    private LocalDateTime createdDate = LocalDateTime.parse("2025-05-08T12:34:56");
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        appealSubmissionDocFieldMapper = new AppealSubmissionDocFieldMapper(stringProvider);
        asylumCase = new AsylumCase();
        caseDetails = new CaseDetails<>(
                123L,
                "IA",
                State.APPEAL_STARTED,
                asylumCase,
                createdDate
        );
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Set up the asylum case with the provided JSON data
        asylumCase.write(APPEAL_SUBMISSION_DATE, "2025-05-08");
        asylumCase.write(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, "ialegalreporgcreator12@mailnesia.com");
        asylumCase.write(LEGAL_REP_NAME, "Legal");
        asylumCase.write(LEGAL_REP_FAMILY_NAME, "Rep");
        asylumCase.write(LEGAL_REP_COMPANY, "ia-legal-rep-org777");
        asylumCase.write(LEGAL_REP_REFERENCE_NUMBER, "1234");
        asylumCase.write(APPELLANT_DATE_OF_BIRTH, "1991-01-01");
        asylumCase.write(APPELLANT_TITLE, "Mr");
        asylumCase.write(APPELLANT_GIVEN_NAMES, "Terry");
        asylumCase.write(APPELLANT_FAMILY_NAME, "Jerry");
        asylumCase.write(APPEAL_OUT_OF_COUNTRY, YesOrNo.NO);
        asylumCase.write(IS_ADMIN, YesOrNo.NO);
        asylumCase.write(APPELLANT_IN_DETENTION, YesOrNo.YES);
        asylumCase.write(DETENTION_FACILITY, "other");
        asylumCase.put("otherDetentionFacilityName", "Testing Facility");
        asylumCase.write(DETENTION_BUILDING, "someDetentionBuilding");
        asylumCase.write(DETENTION_ADDRESS_LINES, "someDetentionAddressLines");
        asylumCase.write(DETENTION_POSTCODE, "SW4 7TX");
        asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
        asylumCase.write(HAS_PENDING_BAIL_APPLICATIONS, BailApplicationStatus.NO);
        asylumCase.write(REMOVAL_ORDER_OPTIONS, YesOrNo.YES);
        asylumCase.write(REMOVAL_ORDER_DATE, "2026-04-15T10:00:00.000");
        asylumCase.write(SUBMISSION_OUT_OF_TIME, YesOrNo.YES);
        asylumCase.write(APPLICATION_OUT_OF_TIME_EXPLANATION, "Testing");
        asylumCase.write(HAS_OTHER_APPEALS, HasOtherAppeals.YES);

        // Set up address
        AddressUk address = new AddressUk(
                "Apartment 1",
                "Westside One",
                "22 Suffolk Street Queensway",
                "Birmingham",
                "",
                "B1 1LS",
                "United Kingdom"
        );
        asylumCase.write(APPELLANT_ADDRESS, address);

        // Set up nationalities
        List<IdValue<Map<String, String>>> nationalities = Arrays.asList(
                new IdValue<>("1", ImmutableMap.of("code", "ZZ"))
        );
        asylumCase.write(APPELLANT_NATIONALITIES, nationalities);
        when(stringProvider.get("isoCountries", "ZZ")).thenReturn(Optional.of("Unknown Country"));

        // Set up other appeals
        List<IdValue<Map<String, String>>> otherAppeals = Arrays.asList(
                new IdValue<>("1", ImmutableMap.of("value", "EU/50003/2025"))
        );
        asylumCase.write(OTHER_APPEALS, otherAppeals);

        // Set up out of time document
        Document document = new Document(
                "http://dm-store-aat.service.core-compute-aat.internal/documents/253c9ce3-05d0-4d97-a86f-30a0aa206abb",
                "Supporting document - Test PDF.pdf",
                "http://dm-store-aat.service.core-compute-aat.internal/documents/253c9ce3-05d0-4d97-a86f-30a0aa206abb/binary"
        );
        asylumCase.write(APPLICATION_OUT_OF_TIME_DOCUMENT, document);
    }

    @Test
    void should_map_basic_case_details() throws Exception {
        // When
        Map<String, Object> result = appealSubmissionDocFieldMapper.mapFieldValues(caseDetails);
        log.info("Mapped field values as JSON: {}", objectMapper.writeValueAsString(result));

        // Then
        assertEquals("08052025", result.get("CREATED_DATE"));
        assertEquals("08052025", result.get("appealSubmissionDate"));
        assertEquals("ialegalreporgcreator12@mailnesia.com", result.get("legalRepresentativeEmailAddress"));
        assertEquals("Legal Rep", result.get("legalRepName"));
        assertEquals("ia-legal-rep-org777", result.get("legalRepCompany"));
        assertEquals("1234", result.get("legalRepReferenceNumber"));
        assertEquals("01011991", result.get("appellantDateOfBirth"));
        assertEquals("Mr", result.get("appellantTitle"));
        assertEquals(YesOrNo.NO, result.get("appealOutOfCountry"));
        assertEquals(YesOrNo.NO, result.get("isAdmin"));
    }

    @Test
    void should_map_detained_appellant_details() throws Exception {
        // When
        Map<String, Object> result = appealSubmissionDocFieldMapper.mapFieldValues(caseDetails);
        log.info("Mapped field values as JSON: {}", objectMapper.writeValueAsString(result));

        // Then
        assertEquals(Optional.of(YesOrNo.YES), result.get("appellantInDetention"));
        assertEquals("Detained", result.get("detentionStatus"));
        assertEquals("Other", result.get("detentionFacility"));
        assertEquals("Testing Facility", result.get("detentionFacilityName"));
        assertEquals("someDetentionBuilding", result.get("detentionBuilding"));
        assertEquals("someDetentionAddressLines", result.get("detentionAddressLines"));
        assertEquals("SW4 7TX", result.get("detentionPostcode"));
        assertEquals(YesOrNo.NO, result.get("isAcceleratedDetainedAppeal"));
        assertEquals(BailApplicationStatus.NO, result.get("hasPendingBailApplication"));
    }

    @Test
    void should_map_appellant_address() throws Exception {
        // When
        Map<String, Object> result = appealSubmissionDocFieldMapper.mapFieldValues(caseDetails);
        log.info("Mapped field values as JSON: {}", objectMapper.writeValueAsString(result));

        // Then
        Map<String, String> addressMap = (Map<String, String>) result.get("appellantAddress");
        assertEquals("Apartment 1", addressMap.get("appellantAddressLine1"));
        assertEquals("Westside One", addressMap.get("appellantAddressLine2"));
        assertEquals("22 Suffolk Street Queensway", addressMap.get("appellantAddressLine3"));
        assertEquals("Birmingham", addressMap.get("appellantAddressPostTown"));
        assertEquals("", addressMap.get("appellantAddressCounty"));
        assertEquals("B1 1LS", addressMap.get("appellantAddressPostCode"));
        assertEquals("United Kingdom", addressMap.get("appellantAddressCountry"));
    }

    @Test
    void should_map_nationalities() throws Exception {
        // When
        Map<String, Object> result = appealSubmissionDocFieldMapper.mapFieldValues(caseDetails);
        log.info("Mapped field values as JSON: {}", objectMapper.writeValueAsString(result));

        // Then
        List<Map<String, String>> nationalityList = (List<Map<String, String>>) result.get("appellantNationalities");
        assertEquals(1, nationalityList.size());
        assertEquals("Unknown Country", nationalityList.get(0).get("nationality"));
    }

    @Test
    void should_map_out_of_time_details() throws Exception {
        // When
        Map<String, Object> result = appealSubmissionDocFieldMapper.mapFieldValues(caseDetails);
        log.info("Mapped field values as JSON: {}", objectMapper.writeValueAsString(result));

        // Then
        assertEquals("Testing", result.get("applicationOutOfTimeExplanation"));
        assertEquals(YesOrNo.YES, result.get("submissionOutOfTime"));
        assertEquals("http://dm-store-aat.service.core-compute-aat.internal/documents/253c9ce3-05d0-4d97-a86f-30a0aa206abb/binary", result.get("applicationOutOfTimeDocumentName"));
    }

    @Test
    void should_map_removal_order_details() throws Exception {
        // When
        Map<String, Object> result = appealSubmissionDocFieldMapper.mapFieldValues(caseDetails);
        log.info("Mapped field values as JSON: {}", objectMapper.writeValueAsString(result));

        // Then
        assertEquals(YesOrNo.YES, result.get("removalOrderOption"));
        assertEquals("2026-04-15T10:00:00.000", result.get("removalOrderDate"));
    }

    @Test
    void should_map_other_appeals() throws Exception {
        // When
        Map<String, Object> result = appealSubmissionDocFieldMapper.mapFieldValues(caseDetails);
        log.info("Mapped field values as JSON: {}", objectMapper.writeValueAsString(result));

        // Then
        assertEquals("EU/50003/2025", result.get("otherAppeals"));
        assertEquals(YesOrNo.YES, result.get("hasOtherAppeals"));
    }
} 