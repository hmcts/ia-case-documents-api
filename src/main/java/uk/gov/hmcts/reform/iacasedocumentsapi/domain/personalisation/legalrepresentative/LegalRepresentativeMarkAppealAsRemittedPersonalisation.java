package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import java.util.Map;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.SourceOfRemittal;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeMarkAppealAsRemittedPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeMarkAppealAsRemittedTemplateId;
    private final CustomerServicesProvider customerServicesProvider;


    public LegalRepresentativeMarkAppealAsRemittedPersonalisation(
        @Value("${govnotify.template.markAppealAsRemitted.legalRep.email}")
        String legalRepresentativeMarkAppealAsRemittedTemplateId,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepresentativeMarkAppealAsRemittedTemplateId =
            legalRepresentativeMarkAppealAsRemittedTemplateId;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return legalRepresentativeMarkAppealAsRemittedTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REP_MARK_APPEAL_AS_REMITTED";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String listingReferenceLine = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
            .map(ref -> "\nListing reference: " + ref)
            .orElse("");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", listingReferenceLine)
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("remittalSource", asylumCase.read(AsylumCaseDefinition.SOURCE_OF_REMITTAL, SourceOfRemittal.class)
                    .orElseThrow(() -> new IllegalStateException("sourceOfRemittal is not present"))
                    .getValue());

        return listCaseFields.build();
    }
}
