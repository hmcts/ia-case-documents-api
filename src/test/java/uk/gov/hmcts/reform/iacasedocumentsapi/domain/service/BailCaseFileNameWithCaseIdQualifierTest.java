package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@ExtendWith(MockitoExtension.class)
public class BailCaseFileNameWithCaseIdQualifierTest {
    private final String unqualifiedFileName = "bail-application-summary";

    @Mock
    private CaseDetails<BailCase> caseDetails;

    @Mock
    private BailCase bailCase;

    private BailCaseFileNameWithCaseIdQualifier fileNameQualifier;

    @BeforeEach
    public void setUp() {
        fileNameQualifier = new BailCaseFileNameWithCaseIdQualifier();
    }

    @Test
    public void should_generate_valid_file_name() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));
        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("1111"));

        String fileName = fileNameQualifier.get(unqualifiedFileName, caseDetails);

        verify(bailCase, times(1)).read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class);
        assertEquals("1111-Doe-bail-application-summary", fileName);
    }

    @Test
    public void should_throw_exception_when_family_name_is_empty() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileNameQualifier.get(unqualifiedFileName, caseDetails))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Applicant Family Name is not present");
    }

    @Test
    public void should_throw_exception_when_reference_number_is_empty() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("Doe"));
        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileNameQualifier.get(unqualifiedFileName, caseDetails))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Bail reference number is not present");
    }
}
