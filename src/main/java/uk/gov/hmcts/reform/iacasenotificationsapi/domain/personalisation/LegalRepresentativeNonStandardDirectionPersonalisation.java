package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.BasePersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Service
public class LegalRepresentativeNonStandardDirectionPersonalisation implements NotificationPersonalisation {

    private static final String legalRepNonStandardDirectionSuffix = "_LEGAL_REP_NON_STANDARD_DIRECTION";

    private final GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    private final BasePersonalisationProvider basePersonalisationProvider;
    private final EmailAddressFinder emailAddressFinder;


    public LegalRepresentativeNonStandardDirectionPersonalisation(
            GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
            BasePersonalisationProvider basePersonalisationProvider,
            EmailAddressFinder emailAddressFinder) {

        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.basePersonalisationProvider = basePersonalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return govNotifyTemplateIdConfiguration.getLegalRepresentativeNonStandardDirectionTemplateId();
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return emailAddressFinder.getLegalRepEmailAddress(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + legalRepNonStandardDirectionSuffix;
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return basePersonalisationProvider.getNonStandardDirectionPersonalisation(asylumCase);
    }
}
