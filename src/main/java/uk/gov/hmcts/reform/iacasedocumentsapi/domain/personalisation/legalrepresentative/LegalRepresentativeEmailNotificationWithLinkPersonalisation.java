package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CHANGE_ORGANISATION_REQUEST_FIELD;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;

import java.util.Collections;
import java.util.Set;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailWithLinkNotificationPersonalisation;

public interface LegalRepresentativeEmailNotificationWithLinkPersonalisation extends EmailWithLinkNotificationPersonalisation {
    @Override
    default Set<String> getRecipientsList(AsylumCase asylumCase) {

        if (asylumCase.read(CHANGE_ORGANISATION_REQUEST_FIELD, ChangeOrganisationRequest.class)
                .map(it -> it.getCaseRoleId() == null)
                .orElse(false)) {

            return Collections.emptySet();
        } else {
            return Collections.singleton(getLegalRepEmailInternalOrLegalRepJourney(asylumCase));
        }
    }
}
