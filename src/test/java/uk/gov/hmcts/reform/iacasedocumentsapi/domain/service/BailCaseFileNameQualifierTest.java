package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
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
public class BailCaseFileNameQualifierTest {
    private final String unqualifiedFileName = "bail-application-summary";
    private final String expectedFileName = "Doe-bail-application-summary";
    private final String familyName = "Doe";

    @Mock
    private CaseDetails<BailCase> caseDetails;

    @Mock
    private BailCase bailCase;

    private BailCaseFileNameQualifier bailCaseFileNameQualifier;

    @BeforeEach
    public void setUp() {
        bailCaseFileNameQualifier = new BailCaseFileNameQualifier();
    }

    @Test
    public void should_generate_valid_file_name() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(familyName));

        String fileName = bailCaseFileNameQualifier.get(unqualifiedFileName, caseDetails);

        verify(bailCase, times(1)).read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class);
        assertEquals(expectedFileName, fileName);
    }

    @Test
    public void should_throw_exception_when_family_name_is_empty() {
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bailCaseFileNameQualifier.get(unqualifiedFileName, caseDetails))
            .isExactlyInstanceOf(RequiredFieldMissingException.class)
            .hasMessage("Applicant Family Name is not present");
    }
}
