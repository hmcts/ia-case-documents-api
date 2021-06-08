package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.helper.EndAppealTemplateHelper;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class EndAppealAppellantTemplateTest {

    private final String templateName = "END_APPEAL_NOTICE_TEMPLATE.docx";

    private final String designatedHearingCentre = "designatedHearing@justice.co.uk";

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private EndAppealTemplateHelper endAppealTemplateHelper;
    @Mock private EmailAddressFinder emailAddressFinder;

    private EndAppealAppellantTemplate endAppealAppellantTemplate;

    private final Map<String, Object> fieldValues = new HashMap<>();

    @BeforeEach
    public void setUp() {

        endAppealAppellantTemplate = new EndAppealAppellantTemplate(
            templateName, endAppealTemplateHelper, emailAddressFinder
        );
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, endAppealAppellantTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values_before_listing() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(endAppealTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValues);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());
        when(emailAddressFinder.getHearingCentreEmailAddress(caseDetails.getCaseData())).thenReturn(designatedHearingCentre);

        Map<String, Object> templateFieldValues = endAppealAppellantTemplate.mapFieldValues(caseDetails);

        assertEquals(1, templateFieldValues.size());
        assertEquals(designatedHearingCentre, templateFieldValues.get("designatedHearingCentre"));
        verify(emailAddressFinder).getHearingCentreEmailAddress(asylumCase);
        verify(emailAddressFinder, times(0)).getListCaseHearingCentreEmailAddress(asylumCase);
    }

    @Test
    public void should_map_case_data_to_template_field_values_after_listing() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(endAppealTemplateHelper.getCommonMapFieldValues(caseDetails)).thenReturn(fieldValues);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.BELFAST));
        when(emailAddressFinder.getListCaseHearingCentreEmailAddress(caseDetails.getCaseData())).thenReturn(designatedHearingCentre);

        Map<String, Object> templateFieldValues = endAppealAppellantTemplate.mapFieldValues(caseDetails);

        assertEquals(1, templateFieldValues.size());
        assertEquals(designatedHearingCentre, templateFieldValues.get("designatedHearingCentre"));
        verify(emailAddressFinder).getListCaseHearingCentreEmailAddress(asylumCase);
        verify(emailAddressFinder, times(0)).getHearingCentreEmailAddress(asylumCase);
    }

}
