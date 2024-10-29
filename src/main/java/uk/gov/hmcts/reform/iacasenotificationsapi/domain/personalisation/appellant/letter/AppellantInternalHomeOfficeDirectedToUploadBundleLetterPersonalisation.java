package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.letter;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.LetterNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

@Service
public class AppellantInternalHomeOfficeDirectedToUploadBundleLetterPersonalisation implements LetterNotificationPersonalisation {
    private final String appellantInternalHomeOfficeUploadBundleLetter;
    private final CustomerServicesProvider customerServicesProvider;
    private final DirectionFinder directionFinder;

    public AppellantInternalHomeOfficeDirectedToUploadBundleLetterPersonalisation(
        @Value("${govnotify.template.requestRespondentEvidenceDirection.appellant.letter}") String appellantInternalHomeOfficeUploadBundleLetter,
        CustomerServicesProvider customerServicesProvider,
        DirectionFinder directionFinder

    ) {
        this.appellantInternalHomeOfficeUploadBundleLetter = appellantInternalHomeOfficeUploadBundleLetter;
        this.customerServicesProvider = customerServicesProvider;
        this.directionFinder = directionFinder;

    }

    @Override
    public String getTemplateId() {
        return appellantInternalHomeOfficeUploadBundleLetter;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return hasBeenSubmittedByAppellantInternalCase(asylumCase) ?
            getAppellantAddressInCountryOrOoc(asylumCase) : getLegalRepAddressInCountryOrOoc(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        List<String> address =  getAppellantOrLegalRepAddressLetterPersonalisation(asylumCase);

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        final String dueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        ImmutableMap.Builder<String, String> personalizationBuilder = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("directionDueDate", dueDate);

        for (int i = 0; i < address.size(); i++) {
            personalizationBuilder.put("address_line_" + (i + 1), address.get(i));
        }
        return personalizationBuilder.build();
    }
}
