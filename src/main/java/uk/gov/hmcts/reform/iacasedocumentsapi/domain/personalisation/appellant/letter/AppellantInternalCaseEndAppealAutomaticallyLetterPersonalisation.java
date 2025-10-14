package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class AppellantInternalCaseEndAppealAutomaticallyLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalCaseEndAppealAutomaticallyLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;
    private final int daysToReinstateAppeal;


    public AppellantInternalCaseEndAppealAutomaticallyLetterPersonalisation(
        @Value("${govnotify.template.endAppealAutomatically.appellant.letter}") String appellantInternalCaseEndAppealAutomaticallyLetterTemplateId,
        @Value("${appellantDaysToWait.letter.toReinstateAppeal}") int daysToReinstateAppeal,
        CustomerServicesProvider customerServicesProvider,
        SystemDateProvider systemDateProvider
    ) {
        this.appellantInternalCaseEndAppealAutomaticallyLetterTemplateId = appellantInternalCaseEndAppealAutomaticallyLetterTemplateId;
        this.daysToReinstateAppeal = daysToReinstateAppeal;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalCaseEndAppealAutomaticallyLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
            getAppellantAddressInCountryOrOoc(asylumCase) : getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_END_APPEAL_AUTOMATICALLY_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final String dueDate = systemDateProvider.dueDate(daysToReinstateAppeal);

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("reinstateDeadlineDate", dueDate)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""));

        List<String> address =  getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}
