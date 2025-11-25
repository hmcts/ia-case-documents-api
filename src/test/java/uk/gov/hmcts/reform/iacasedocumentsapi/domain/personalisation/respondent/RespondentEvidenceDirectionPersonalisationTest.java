package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RespondentEvidenceDirectionPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock DirectionFinder directionFinder;
    @Mock Direction direction;
    @Mock CustomerServicesProvider customerServicesProvider;

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String ejpTemplateId = "someEjpTemplateId";
    private final String detentionTemplateId = "someDetentionTemplateId";
    private final String respondentReviewEmailAddress = "respondentReview@example.com";

    private final String expectedDirectionDueDate = "27 Aug 2019";
    private final String companyName = "Legal Rep Company Name";
    private final String companyAddress = "45 Lunar House Spa Road London SE1 3HP";
    private final String legalRepName = "Legal Rep Name";
    private final String legalRepFamilyName = "Legal Rep Family Name";
    private final String legalRepReference = "Legal Rep Reference";
    private final String legalRepEmail = "Legal Rep Email";
    private final String legalRepEjpGivenName = "Given Name";
    private final String legalRepEjpFamilyName = "Family Name";

    private final String legalRepEjpReference = "Legal Rep Reference Ejp";
    private final String legalRepEjpEmail = "Legal Rep Email Ejp";
    private final String legalRepEjpCompanyName = "Legal Rep Company Name Ejp";

    private final String appealReferenceNumber = "someReferenceNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private final AddressUk legalRepCompanyAddress = new AddressUk("45 Lunar House",
            "Spa Road",
            "Woolworth",
            "London",
            "London",
            "SE1 3HP", "UK");

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";

    private RespondentEvidenceDirectionPersonalisation respondentEvidenceDirectionPersonalisation;


    @BeforeEach
    public void setup() {
        String directionDueDate = "2019-08-27";
        when((direction.getDateDue())).thenReturn(directionDueDate);
        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)).thenReturn(Optional.of(direction));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));

        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.of(legalRepFamilyName));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmail));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReference));
        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.of(companyName));
        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(legalRepCompanyAddress));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        String iaExUiFrontendUrl = "http://somefrontendurl";
        respondentEvidenceDirectionPersonalisation = new RespondentEvidenceDirectionPersonalisation(
            templateId,
            ejpTemplateId,
            detentionTemplateId,
            respondentReviewEmailAddress,
                iaExUiFrontendUrl,
            directionFinder,
            customerServicesProvider);
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(LEGAL_REP_GIVEN_NAME_EJP, String.class)).thenReturn(Optional.of(legalRepEjpGivenName));
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME_EJP, String.class)).thenReturn(Optional.of(legalRepEjpFamilyName));
        when(asylumCase.read(LEGAL_REP_EMAIL_EJP, String.class)).thenReturn(Optional.of(legalRepEjpEmail));
        when(asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class)).thenReturn(Optional.of(legalRepEjpReference));
        when(asylumCase.read(LEGAL_REP_COMPANY_EJP, String.class)).thenReturn(Optional.of(legalRepEjpCompanyName));
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, respondentEvidenceDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_ejp_template_id() {
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        assertEquals(ejpTemplateId, respondentEvidenceDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_given_template_id_for_non_detention() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals(templateId, respondentEvidenceDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_given_template_id_for_missing_detention() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());

        assertEquals(templateId, respondentEvidenceDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_given_template_id_for_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals(detentionTemplateId, respondentEvidenceDirectionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPONDENT_EVIDENCE_DIRECTION", respondentEvidenceDirectionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(respondentEvidenceDirectionPersonalisation.getRecipientsList(asylumCase).contains(respondentReviewEmailAddress));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> respondentEvidenceDirectionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentEvidenceDirectionPersonalisation);
        Map<String, String> personalisation = respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(companyName, personalisation.get("companyName"));
        assertEquals(companyAddress, personalisation.get("companyAddress"));
        assertEquals(legalRepName + " " + legalRepFamilyName, personalisation.get("legalRepName"));
        assertEquals(legalRepEmail, personalisation.get("legalRepEmail"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given_icc_lr(YesOrNo isAda) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentEvidenceDirectionPersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));

        when(asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME_PAPER_J, String.class)).thenReturn(Optional.of(legalRepFamilyName));

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.of(legalRepEmail));

        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(legalRepReference));

        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_COMPANY_PAPER_J, String.class)).thenReturn(Optional.of(companyName));

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.of(legalRepCompanyAddress));

        Map<String, String> personalisation = respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(companyName, personalisation.get("companyName"));
        assertEquals(companyAddress, personalisation.get("companyAddress"));
        assertEquals(legalRepName + " " + legalRepFamilyName, personalisation.get("legalRepName"));
        assertEquals(legalRepEmail, personalisation.get("legalRepEmail"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given_icc_aip(YesOrNo isAda) {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentEvidenceDirectionPersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));

        when(asylumCase.read(LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.of(legalRepFamilyName));
        when(asylumCase.read(LEGAL_REP_FAMILY_NAME_PAPER_J, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmail));
        when(asylumCase.read(LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReference));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.of(companyName));
        when(asylumCase.read(LEGAL_REP_COMPANY_PAPER_J, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(legalRepCompanyAddress));
        when(asylumCase.read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(companyName, personalisation.get("companyName"));
        assertEquals(companyAddress, personalisation.get("companyAddress"));
        assertEquals(legalRepName + " " + legalRepFamilyName, personalisation.get("legalRepName"));
        assertEquals(legalRepEmail, personalisation.get("legalRepEmail"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given_detained_appeal() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YES));

        initializePrefixes(respondentEvidenceDirectionPersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(companyName, personalisation.get("companyName"));
        assertEquals(companyAddress, personalisation.get("companyAddress"));
        assertEquals(legalRepName + " " + legalRepFamilyName, personalisation.get("legalRepName"));
        assertEquals(legalRepEmail, personalisation.get("legalRepEmail"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_empty_company_details_for_notice_of_change_found(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentEvidenceDirectionPersonalisation);
        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.of(
                new ChangeOrganisationRequest(
                    null,
                    null,
                    null
                )
            ));
        Map<String, String> personalisation = respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("companyName"));
        assertEquals("", personalisation.get("companyAddress"));
        assertEquals("", personalisation.get("legalRepName"));
        assertEquals("", personalisation.get("legalRepEmail"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_company_details_for_notice_of_change_found_with_valid_case_role(YesOrNo isAda) {

        Value caseRole =
            new Value("[LEGALREPRESENTATIVE]", "Legal Representative");

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentEvidenceDirectionPersonalisation);

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.of(
                new ChangeOrganisationRequest(
                    new DynamicList(caseRole, newArrayList(caseRole)),
                    LocalDateTime.now().toString(),
                    "1"
                )
            ));
        Map<String, String> personalisation = respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(companyName, personalisation.get("companyName"));
        assertEquals(companyAddress, personalisation.get("companyAddress"));
        assertEquals(legalRepName + " " + legalRepFamilyName, personalisation.get("legalRepName"));
        assertEquals(legalRepEmail, personalisation.get("legalRepEmail"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_direction_is_empty() {

        when(directionFinder.findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("direction 'respondentEvidence' is not present");
    }

    @Test
    public void should_return_ejp_personalisation_when_all_information_given() {

        initializePrefixes(respondentEvidenceDirectionPersonalisation);
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        Map<String, String> personalisation = respondentEvidenceDirectionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(legalRepEjpCompanyName, personalisation.get("companyName"));
        assertEquals(legalRepEjpEmail, personalisation.get("legalRepEmail"));
        assertEquals(legalRepEjpReference, personalisation.get("legalRepReference"));
        assertEquals(legalRepEjpGivenName +  " " + legalRepEjpFamilyName, personalisation.get("legalRepName"));

        assertEquals(expectedDirectionDueDate, personalisation.get("dueDate"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals("Immigration and Asylum appeal", personalisation.get("subjectPrefix"));

    }
}
