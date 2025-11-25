package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_EJP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_EMAIL_EJP;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeEmailNotificationPersonalisationTest {

    private final String legalRepEmailAddress = "legalrep@example.com";
    private final String ejpLegalRepEmailAddress = "ejplegalrep@example.com";
    private String iaExUiFrontendUrl = "http://localhost";
    private String afterListingTemplateId = "afterListingTemplateId";
    private String beforeListingTemplateId = "beforeListingTemplateId";

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    PersonalisationProvider personalisationProvider;

    private LegalRepresentativeChangeDirectionDueDatePersonalisation personalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        personalisation = new LegalRepresentativeChangeDirectionDueDatePersonalisation(beforeListingTemplateId,
            afterListingTemplateId, iaExUiFrontendUrl, personalisationProvider, customerServicesProvider);
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)).thenReturn(Optional.empty());

        assertTrue(personalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_return_given_ejp_lr_email_address_from_asylum_case_if_it_is_ejp() {
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(LEGAL_REP_EMAIL_EJP, String.class))
            .thenReturn(Optional.of(ejpLegalRepEmailAddress));

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)).thenReturn(Optional.empty());

        assertTrue(personalisation.getRecipientsList(asylumCase)
            .contains(ejpLegalRepEmailAddress));
    }

    @Test
    public void should_return_given_email_address_for_change_org_request_field_and_field() {

        Value caseRole =
            new Value("[LEGALREPRESENTATIVE]", "Legal Representative");

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.of(
                new ChangeOrganisationRequest(
                    new DynamicList(caseRole, newArrayList(caseRole)),
                    LocalDateTime.now().toString(),
                    "1"
                )
            ));

        assertTrue(personalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_return_given_email_address_for_empty_change_org_request_field() {

        when(asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class))
            .thenReturn(Optional.of(
                new ChangeOrganisationRequest(
                    null,
                    null,
                    null
                )
            ));

        assertTrue(personalisation.getRecipientsList(asylumCase).isEmpty());
    }

}
