package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import static java.util.Objects.requireNonNull;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class HomeOfficeMarkAppealAsRemittedPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeMarkAppealAsRemittedTemplateId;
    private final String upperTribunalNoticesEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;

    public HomeOfficeMarkAppealAsRemittedPersonalisation(
        @Value("${upperTribunalNoticesEmailAddress}") String upperTribunalNoticesEmailAddress,
        @Value("${govnotify.template.markAppealAsRemitted.homeOffice.email}") String homeOfficeMarkAppealAsRemittedTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.upperTribunalNoticesEmailAddress = upperTribunalNoticesEmailAddress;
        this.homeOfficeMarkAppealAsRemittedTemplateId = homeOfficeMarkAppealAsRemittedTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeMarkAppealAsRemittedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(upperTribunalNoticesEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HOME_OFFICE_MARK_APPEAL_AS_REMITTED";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("remittalSource", asylumCase.read(AsylumCaseDefinition.SOURCE_OF_REMITTAL, SourceOfRemittal.class)
                    .orElseThrow(() -> new IllegalStateException("sourceOfRemittal is not present"))
                    .getValue())
            .build();
    }

}
