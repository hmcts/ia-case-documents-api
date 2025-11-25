package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import java.util.Collections;
import java.util.Set;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;

public interface LegalRepresentativeBailEmailNotificationPersonalisation extends BailEmailNotificationPersonalisation {

    @Override
    default Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailCase
                .read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)
                .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }
}
