package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class AsylumCaseFileNameQualifierTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private String appealReferenceNumber = "PA/12345/2020";
    private String appellantFamilyName = "González";
    private String unqualifiedFileName = "appeal-form";
    private String expectedFileName = "PA 12345 2020-González-appeal-form";

    private AsylumCaseFileNameQualifier asylumCaseFileNameQualifier;

    @BeforeEach
    public void setUp() {

        asylumCaseFileNameQualifier =
            new AsylumCaseFileNameQualifier();
    }

    @Test
    public void should_qualify_file_name_using_case_data() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        String qualifiedFileName = asylumCaseFileNameQualifier.get(unqualifiedFileName, caseDetails);

        verify(asylumCase, times(1)).read(APPEAL_REFERENCE_NUMBER, String.class);
        verify(asylumCase, times(1)).read(APPELLANT_FAMILY_NAME, String.class);

        assertEquals(expectedFileName, qualifiedFileName);
    }

    @Test
    public void should_throw_when_appeal_reference_number_is_not_present() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asylumCaseFileNameQualifier.get(unqualifiedFileName, caseDetails))
            .hasMessage("appealReferenceNumber is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_appellant_family_name_is_not_present() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asylumCaseFileNameQualifier.get(unqualifiedFileName, caseDetails))
            .hasMessage("appellantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
