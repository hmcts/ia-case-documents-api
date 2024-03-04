package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.applicant.ApplicantBailSmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;

@Service
public class ApplicantBailCaseListingPersonalisationSms implements ApplicantBailSmsNotificationPersonalisation {

    private final String caseListingInitialApplicantSmsTemplateId;
    private final String caseListingRelistingApplicantSmsTemplateId;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DateTimeExtractor dateTimeExtractor;

    public ApplicantBailCaseListingPersonalisationSms(
        @Value("${govnotify.bail.template.caseListing.initial.sms}") String caseListingInitialApplicantSmsTemplateId,
        @Value("${govnotify.bail.template.caseListing.relisting.sms}") String caseListingRelistingApplicantSmsTemplateId,
        HearingDetailsFinder hearingDetailsFinder,
        DateTimeExtractor dateTimeExtractor) {
        this.caseListingInitialApplicantSmsTemplateId = caseListingInitialApplicantSmsTemplateId;
        this.caseListingRelistingApplicantSmsTemplateId = caseListingRelistingApplicantSmsTemplateId;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    @Override
    public String getTemplateId(BailCase bailCase) {

        ListingEvent listingEvent = bailCase.read(LISTING_EVENT, ListingEvent.class)
            .orElseThrow(() -> new IllegalStateException("Listing Event is not present"));

        return switch (listingEvent) {
            case INITIAL -> caseListingInitialApplicantSmsTemplateId;
            case RELISTING -> caseListingRelistingApplicantSmsTemplateId;
        };
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_CASE_LISTING_APPLICANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getBailHearingDateTime(bailCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getBailHearingDateTime(bailCase)))
            .put("hearingCentre", hearingDetailsFinder.getBailHearingCentreAddress(bailCase))
            .build();
    }
}
