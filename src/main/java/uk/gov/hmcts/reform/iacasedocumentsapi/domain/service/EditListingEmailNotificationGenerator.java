package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.ApplicationContextProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice.HomeOfficeEditListingNoChangePersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative.LegalRepresentativeEditListingNoChangePersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

/*
 * This class extends EmailNotificationGenerator to filter the editListing personalisation list based on the content difference.
 */
public class EditListingEmailNotificationGenerator extends EmailNotificationGenerator {


    public EditListingEmailNotificationGenerator(
        List<EmailNotificationPersonalisation> repPersonalisationList,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        super(repPersonalisationList, notificationSender, notificationIdAppender);
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        final CaseDetails<AsylumCase> asylumCaseCaseDetailsBefore = callback.getCaseDetailsBefore().orElse(null);

        ApplicationContextProvider.getApplicationContext().getBean(CustomerServicesProvider.class)
            .setCorrectEmail(asylumCase);

        AsylumCase asylumCaseBefore = null;
        if (asylumCaseCaseDetailsBefore != null) {
            asylumCaseBefore = asylumCaseCaseDetailsBefore.getCaseData();
        }

        final List<EmailNotificationPersonalisation> noChangePersonalisationList = personalisationList.stream().filter(
            personalisation -> personalisation instanceof LegalRepresentativeEditListingNoChangePersonalisation || personalisation instanceof HomeOfficeEditListingNoChangePersonalisation
        ).collect(Collectors.toList());

        final List<EmailNotificationPersonalisation> withChangePersonalisationList = personalisationList.stream().filter(
            personalisation -> ! (personalisation instanceof LegalRepresentativeEditListingNoChangePersonalisation || personalisation instanceof HomeOfficeEditListingNoChangePersonalisation))
            .collect(Collectors.toList());


        final boolean editListingContentUnchanged = isEditListingContentUnchanged(asylumCase, asylumCaseBefore);

        if (editListingContentUnchanged) {
            noChangePersonalisationList.forEach(personalisation -> {
                String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
                List<String> notificationIds = createEmail(personalisation, asylumCase, referenceId, callback);
                notificationIdAppender.appendAll(asylumCase, referenceId, notificationIds);
            });
        } else {
            withChangePersonalisationList.forEach(personalisation -> {
                String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
                List<String> notificationIds = createEmail(personalisation, asylumCase, referenceId, callback);
                notificationIdAppender.appendAll(asylumCase, referenceId, notificationIds);
            });

        }
    }

    private boolean isEditListingContentUnchanged(AsylumCase caseData, AsylumCase caseDataBefore) {

        return
            isDateOfHearingUnchanged(caseData, caseDataBefore) && isHearingCentreUnchanged(caseData, caseDataBefore)
                && isHearingChannelUnchanged(caseData, caseDataBefore);
    }

    private boolean isHearingCentreUnchanged(AsylumCase caseData, AsylumCase caseDataBefore) {

        // HearingCentre should always have values here. If not something is really wrong. This is assuming it will always be there.
        return
            caseData.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(null)
                == caseDataBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class).orElse(null);

    }

    private boolean isDateOfHearingUnchanged(AsylumCase caseData, AsylumCase caseDataBefore) {

        // Date string value comparision is good enough here.
        return
            caseData.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class).orElse("")
                .equalsIgnoreCase(caseDataBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class).orElse(""));

    }

    private boolean isHearingChannelUnchanged(AsylumCase caseData, AsylumCase caseDataBefore) {

        return
            caseData.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class).orElse(null)
                == caseDataBefore.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class).orElse(null);

    }

}
