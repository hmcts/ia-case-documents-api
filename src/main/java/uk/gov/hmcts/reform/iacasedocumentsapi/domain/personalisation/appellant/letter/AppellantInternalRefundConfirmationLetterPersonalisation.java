package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.*;

@Service
public class AppellantInternalRefundConfirmationLetterPersonalisation implements LetterNotificationPersonalisation {

    private final String appellantInternalRefundConfirmationLetterTemplateId;
    private final CustomerServicesProvider customerServicesProvider;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterNotificationSent;

    public AppellantInternalRefundConfirmationLetterPersonalisation(
            @Value("${govnotify.template.refundConfirmation.appellant.letter}") String appellantInternalRefundConfirmationLetterTemplateId,
            @Value("${appellantDaysToWait.letter.afterManageFeeEvent}") int daysAfterNotificationSent,
            CustomerServicesProvider customerServicesProvider,
            SystemDateProvider systemDateProvider
    ) {
        this.appellantInternalRefundConfirmationLetterTemplateId = appellantInternalRefundConfirmationLetterTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterNotificationSent = daysAfterNotificationSent;
    }

    @Override
    public String getTemplateId() {
        return appellantInternalRefundConfirmationLetterTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
                getAppellantAddressInCountryOrOoc(asylumCase) : getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_REFUND_CONFIRMATION_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        final String dueDate = systemDateProvider.dueDate(daysAfterNotificationSent);

        List<String> address = getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("previousDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(PREVIOUS_DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
                .put("updatedDecisionHearingFeeOption", normalizeDecisionHearingOptionText(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class).orElse("")))
                .put("newFee", convertAsylumCaseFeeValue(asylumCase.read(NEW_FEE_AMOUNT, String.class).orElse("")))
                .put("onlineCaseReferenceNumber", asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
                .put("dueDate", dueDate);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}
