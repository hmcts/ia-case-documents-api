package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType.AIP;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantMarkAppealAsRemittedPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantMarkAppealAsRemittedTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;


    public AppellantMarkAppealAsRemittedPersonalisationEmail(
        @Value("${govnotify.template.markAppealAsRemitted.appellant.email}")
        String appellantMarkAppealAsRemittedTemplateId,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder
    ) {
        this.appellantMarkAppealAsRemittedTemplateId = appellantMarkAppealAsRemittedTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        boolean isAip = asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);

        return isAip ? recipientsFinder.findAll(asylumCase, NotificationType.EMAIL) : Collections.emptySet();
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantMarkAppealAsRemittedTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_MARK_APPEAL_AS_REMITTED_NOTIFICATION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("remittalSource", asylumCase.read(AsylumCaseDefinition.SOURCE_OF_REMITTAL, SourceOfRemittal.class)
                    .orElseThrow(() -> new IllegalStateException("sourceOfRemittal is not present"))
                    .getValue());

        return listCaseFields.build();
    }
}
