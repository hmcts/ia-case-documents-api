package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.homeoffice.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.BailCaseUtils.isBailConditionalGrant;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_LEGALLY_REPRESENTED_FOR_FLAG;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@Service
public class HomeOfficeBailCaseListingPersonalisation implements BailEmailNotificationPersonalisation {

    private final String homeOfficeCaseListingInitialWithLegalRepPersonalisationTemplateId;
    private final String homeOfficeCaseListingInitialWithoutLegalRepPersonalisationTemplateId;
    private final String homeOfficeCaseListingRelistingWithLegalRepPersonalisationTemplateId;
    private final String homeOfficeCaseListingRelistingWithoutLegalRepPersonalisationTemplateId;
    private final String homeOfficeCaseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId;
    private final String homeOfficeCaseListingConditionalBailRelistingWithoutLegalRepPersonalisationTemplateId;
    private final String bailHomeOfficeEmailAddress;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DateTimeExtractor dateTimeExtractor;

    public HomeOfficeBailCaseListingPersonalisation(
        @NotNull(message = "homeOfficeCaseListingPersonalisationTemplateId cannot be null")
        @Value("${govnotify.bail.template.caseListing.initial.withLegalRep.email}") String homeOfficeCaseListingInitialWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.caseListing.initial.withoutLegalRep.email}") String homeOfficeCaseListingInitialWithoutLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.caseListing.relisting.withLegalRep.email}") String homeOfficeCaseListingRelistingWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.caseListing.relisting.withoutLegalRep.email}") String homeOfficeCaseListingRelistingWithoutLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.caseListing.conditionalBailRelisting.withLegalRep.email}") String homeOfficeCaseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId,
        @Value("${govnotify.bail.template.caseListing.conditionalBailRelisting.withoutLegalRep.email}") String homeOfficeCaseListingConditionalBailRelistingWithoutLegalRepPersonalisationTemplateId,
        @Value("${bailHomeOfficeEmailAddress}") String bailHomeOfficeEmailAddress,
        HearingDetailsFinder hearingDetailsFinder,
        DateTimeExtractor dateTimeExtractor
    ) {
        this.homeOfficeCaseListingInitialWithLegalRepPersonalisationTemplateId = homeOfficeCaseListingInitialWithLegalRepPersonalisationTemplateId;
        this.homeOfficeCaseListingInitialWithoutLegalRepPersonalisationTemplateId = homeOfficeCaseListingInitialWithoutLegalRepPersonalisationTemplateId;
        this.homeOfficeCaseListingRelistingWithLegalRepPersonalisationTemplateId = homeOfficeCaseListingRelistingWithLegalRepPersonalisationTemplateId;
        this.homeOfficeCaseListingRelistingWithoutLegalRepPersonalisationTemplateId = homeOfficeCaseListingRelistingWithoutLegalRepPersonalisationTemplateId;
        this.homeOfficeCaseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId = homeOfficeCaseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId;
        this.homeOfficeCaseListingConditionalBailRelistingWithoutLegalRepPersonalisationTemplateId = homeOfficeCaseListingConditionalBailRelistingWithoutLegalRepPersonalisationTemplateId;
        this.bailHomeOfficeEmailAddress = bailHomeOfficeEmailAddress;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_BAIL_APPLICATION_CASE_LISTING_HOME_OFFICE";
    }

    @Override
    public String getTemplateId(BailCase bailCase) {

        YesOrNo isLegallyRepresented = bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class).orElse(YesOrNo.NO);

        ListingEvent listingEvent = bailCase.read(LISTING_EVENT, ListingEvent.class)
            .orElseThrow(() -> new IllegalStateException("Listing Event is not present"));

        return switch (isLegallyRepresented) {
            case YES -> switch (listingEvent) {
                case INITIAL -> homeOfficeCaseListingInitialWithLegalRepPersonalisationTemplateId;
                case RELISTING -> isBailConditionalGrant(bailCase) ?
                    homeOfficeCaseListingConditionalBailRelistingWithLegalRepPersonalisationTemplateId :
                    homeOfficeCaseListingRelistingWithLegalRepPersonalisationTemplateId;
            };
            case NO -> switch (listingEvent) {
                case INITIAL -> homeOfficeCaseListingInitialWithoutLegalRepPersonalisationTemplateId;
                case RELISTING -> isBailConditionalGrant(bailCase) ?
                    homeOfficeCaseListingConditionalBailRelistingWithoutLegalRepPersonalisationTemplateId :
                    homeOfficeCaseListingRelistingWithoutLegalRepPersonalisationTemplateId;
            };
        };
    }

    @Override
    public Set<String> getRecipientsList(BailCase bailCase) {
        return Collections.singleton(bailHomeOfficeEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(BailCase bailCase) {
        requireNonNull(bailCase, "bailCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("bailReferenceNumber", bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("legalRepReference", bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class).orElse(""))
            .put("applicantGivenNames", bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class).orElse(""))
            .put("applicantFamilyName", bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getBailHearingDateTime(bailCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getBailHearingDateTime(bailCase)))
            .put("hearingCentre", hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase))
            .build();
    }
}
