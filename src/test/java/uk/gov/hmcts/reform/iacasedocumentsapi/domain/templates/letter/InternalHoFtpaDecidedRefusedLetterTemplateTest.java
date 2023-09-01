package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalHoFtpaDecidedRefusedLetterTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String telephoneNumber = "0300 123 1711";
    private final String email = "IAC-ADA-HW@justice.gov.uk";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private final String templateName = "INTERNAL_HO_FTPA_DECIDED_REFUSED_NOTICE_TEMPLATE.docx";
    private final String logo = "[userImage:hmcts.png]";
    private final String refused = "refused";
    private final String notAdmitted = "not admitted";
    private InternalHoFtpaDecidedRefusedLetterTemplate internalHoFtpaDecidedRefusedLetterTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        internalHoFtpaDecidedRefusedLetterTemplate =
                new InternalHoFtpaDecidedRefusedLetterTemplate(templateName, customerServicesProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalHoFtpaDecidedRefusedLetterTemplate.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(email);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));
    }

    @ParameterizedTest
    @EnumSource(value = FtpaDecisionOutcomeType.class, names = {"FTPA_REFUSED", "FTPA_NOT_ADMITTED"})
    void should_populate_template(FtpaDecisionOutcomeType ftpaDecisionOutcomeType) {
        dataSetup();

        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(ftpaDecisionOutcomeType));

        fieldValuesMap = internalHoFtpaDecidedRefusedLetterTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(ftpaDecisionOutcomeType.equals(FtpaDecisionOutcomeType.FTPA_REFUSED)
                ? refused
                : notAdmitted, fieldValuesMap.get("refused"));
    }

    @Test
    void should_throw_if_refused_decision_is_not_present() {
        dataSetup();
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalHoFtpaDecidedRefusedLetterTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Judge decision 'refused' or 'not admitted' must be present");
    }
}