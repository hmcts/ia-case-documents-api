package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.caselinking.CaseLink;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.caselinking.ReasonForLink;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class InternalMaintainCaseLinkAppealTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String telephoneNumber = "0300 123 1711";
    private final String adaEmail = "IAC-ADA-HW@justice.gov.uk";
    private String appellantGivenNames = "John";
    private String appellantFamilyName = "Smith";
    private String homeOfficeReferenceNumber = "123654";
    private String appealReferenceNumber = "HU/11111/2022";
    private final String templateName = "INTERNAL_DETAINED_MAINTAIN_CASE_LINK_APPEAL_TEMPLATE.docx";
    private final String logo = "[userImage:hmcts.png]";
    private InternalMaintainCaseLinkAppealTemplate internalMaintainCaseLinkAppealTemplate;
    private Map<String, Object> fieldValuesMap;
    private final List<AbstractMap.SimpleEntry<String, String>> pairList = List.of(
            new AbstractMap.SimpleEntry<>("reason", "Same Party"),
            new AbstractMap.SimpleEntry<>("reason", "Same child/ren")
    );

    @BeforeEach
    public void setUp() {
        internalMaintainCaseLinkAppealTemplate =
                new InternalMaintainCaseLinkAppealTemplate(templateName, customerServicesProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalMaintainCaseLinkAppealTemplate.getName());
    }

    void dataSetup() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(telephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(adaEmail);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(CASE_LINKS)).thenReturn(Optional.of(createCaseLinksFixtures()));
    }

    @Test
    void should_populate_template_correctly() {
        dataSetup();
        fieldValuesMap = internalMaintainCaseLinkAppealTemplate.mapFieldValues(caseDetails);
        assertEquals(logo, fieldValuesMap.get("hmcts"));
        assertEquals(appealReferenceNumber, fieldValuesMap.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, fieldValuesMap.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, fieldValuesMap.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, fieldValuesMap.get("homeOfficeReferenceNumber"));
        assertEquals(telephoneNumber, fieldValuesMap.get("customerServicesTelephone"));
        assertEquals(pairList, fieldValuesMap.get("reason"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")), fieldValuesMap.get("dateLetterSent"));
    }

    @Test
    void should_throw_when_case_links_are_not_present() {
        dataSetup();
        when(asylumCase.read(CASE_LINKS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalMaintainCaseLinkAppealTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("caseLinks are not present");

    }

    @Test
    void should_resolve_reasons_from_latest_create_link_event() {
        dataSetup();
        fieldValuesMap = internalMaintainCaseLinkAppealTemplate.mapFieldValues(caseDetails);
        assertEquals(pairList, fieldValuesMap.get("reason"));
    }

    private List<IdValue<CaseLink>> createCaseLinksFixtures() {
        return List.of(
                new IdValue<>("1",
                        new CaseLink(
                                "1",
                                "Asylum",
                                LocalDateTime.now().minusMinutes(10),
                                List.of(
                                        new IdValue<>("1",
                                                new ReasonForLink("CLRC001")
                                        ),
                                        new IdValue<>("2",
                                                new ReasonForLink("CLRC002")
                                        )
                                )
                        )),
                new IdValue<>("2",
                        new CaseLink(
                                "2",
                                "Asylum",
                                LocalDateTime.now().minusMinutes(5),
                                List.of(
                                        new IdValue<>("1",
                                                new ReasonForLink("CLRC003")
                                        ),
                                        new IdValue<>("2",
                                                new ReasonForLink("CLRC004")
                                        )
                                )
                        )));
    }
}