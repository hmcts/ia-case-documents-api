package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import static java.util.Objects.requireNonNull;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantMarkAppealAsRemittedPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantMarkAppealAsRemittedTemplateId;
    private final RecipientsFinder recipientsFinder;


    public AppellantMarkAppealAsRemittedPersonalisationSms(
        @Value("${govnotify.template.markAppealAsRemitted.appellant.sms}")
        String appellantMarkAppealAsRemittedTemplateId,
        RecipientsFinder recipientsFinder
    ) {
        this.appellantMarkAppealAsRemittedTemplateId = appellantMarkAppealAsRemittedTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        boolean isAip = asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);

        return isAip ? recipientsFinder.findAll(asylumCase, NotificationType.SMS) : Collections.emptySet();
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantMarkAppealAsRemittedTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_MARK_APPEAL_AS_REMITTED_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("remittalSource", asylumCase.read(AsylumCaseDefinition.SOURCE_OF_REMITTAL, SourceOfRemittal.class)
                    .orElseThrow(() -> new IllegalStateException("sourceOfRemittal is not present"))
                    .getValue());

        return listCaseFields.build();
    }

}
