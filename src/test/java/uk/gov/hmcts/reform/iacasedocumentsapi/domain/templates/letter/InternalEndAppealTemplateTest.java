package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalEndAppealTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String telephoneNumber = "0300 123 1711";
    private final String adaEmail = "IAC-ADA-HW@justice.gov.uk";
    private final String nonAdaEmail = "contactia@justice.gov.uk";
    private final String adaFormName = "IAFT-ADA4: Make an application – Accelerated detained appeal (ADA)";
    private final String nonAdaFormName = "IAFT-DE4: Make an application – Detained appeal";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private String appealEndDate = "2023-07-01";
    private String judgeName = "Mr.Test";
    private final String templateName = "INTERNAL_END_APPEAL_NOTICE_TEMPLATE.docx";
    private final String logo = "[userImage:hmcts.png]";
    private InternalEndAppealTemplate internalEndAppealTemplate;
    private Map<String, Object> fieldValuesMap;

    @BeforeEach
    public void setUp() {
        internalEndAppealTemplate =
                new InternalEndAppealTemplate(templateName, customerServicesProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalEndAppealTemplate.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(END_APPEAL_DATE, String.class)).thenReturn(Optional.of(appealEndDate));
        when(asylumCase.read(END_APPEAL_APPROVER_NAME, String.class)).thenReturn(Optional.of(judgeName));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
    }

    @ParameterizedTest
    @MethodSource("getAdaAndNonAdaArguments")
    void should_populate_template_correctly(String email, YesOrNo yesOrNo) {
        dataSetup();
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(email);
        fieldValuesMap = internalEndAppealTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
        if (yesOrNo.equals(YesOrNo.YES)) {
            assertEquals(adaEmail, fieldValuesMap.get("customerServicesEmail"));
            assertEquals(adaFormName, fieldValuesMap.get("formName"));
        } else {
            assertEquals(nonAdaEmail, fieldValuesMap.get("customerServicesEmail"));
            assertEquals(nonAdaFormName, fieldValuesMap.get("formName"));
        }
    }

    private static Stream<Arguments> getAdaAndNonAdaArguments() {

        return Stream.of(
                Arguments.of("IAC-ADA-HW@justice.gov.uk", YesOrNo.YES),
                Arguments.of("contactia@justice.gov.uk", YesOrNo.NO)
        );
    }
}