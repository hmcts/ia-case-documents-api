package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class AsylumCaseFileNameQualifierTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private String appealReferenceNumber = "PA/12345/2020";
    private String appellantFamilyName = "González";
    private String unqualifiedFileName = "appeal-form";
    private String expectedFileName = "PA 12345 2020-González-appeal-form";

    private AsylumCaseFileNameQualifier asylumCaseFileNameQualifier;

    @Before
    public void setUp() {

        asylumCaseFileNameQualifier =
            new AsylumCaseFileNameQualifier();

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
    }

    @Test
    public void should_qualify_file_name_using_case_data() {

        String qualifiedFileName = asylumCaseFileNameQualifier.get(unqualifiedFileName, caseDetails);

        verify(asylumCase, times(1)).getAppealReferenceNumber();
        verify(asylumCase, times(1)).getAppellantFamilyName();

        assertEquals(expectedFileName, qualifiedFileName);
    }

    @Test
    public void should_throw_when_appeal_reference_number_is_not_present() {

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asylumCaseFileNameQualifier.get(unqualifiedFileName, caseDetails))
            .hasMessage("appealReferenceNumber is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_appellant_family_name_is_not_present() {

        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> asylumCaseFileNameQualifier.get(unqualifiedFileName, caseDetails))
            .hasMessage("appellantFamilyName is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
