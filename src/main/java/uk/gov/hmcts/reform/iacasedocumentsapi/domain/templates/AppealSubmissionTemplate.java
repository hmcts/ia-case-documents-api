package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static java.util.stream.Collectors.joining;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class AppealSubmissionTemplate {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final String templateName;
    private final StringProvider stringProvider;

    public AppealSubmissionTemplate(
        @Value("${appealSubmissionDocument.templateName}") String templateName,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("CREATED_DATE", caseDetails.getCreatedDate().format(DOCUMENT_DATE_FORMAT));
        fieldValues.put("appealReferenceNumber", asylumCase.getAppealReferenceNumber().orElse(""));
        fieldValues.put("legalRepReferenceNumber", asylumCase.getLegalRepReferenceNumber().orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.getHomeOfficeReferenceNumber().orElse(""));
        fieldValues.put("homeOfficeDecisionDate", formatDateForRendering(asylumCase.getHomeOfficeDecisionDate().orElse("")));
        fieldValues.put("appellantGivenNames", asylumCase.getAppellantGivenNames().orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.getAppellantFamilyName().orElse(""));
        fieldValues.put("appellantDateOfBirth", formatDateForRendering(asylumCase.getAppellantDateOfBirth().orElse("")));

        Optional<String> optionalAppealType = asylumCase.getAppealType();

        if (optionalAppealType.isPresent()) {

            String appealType = optionalAppealType.get();

            fieldValues.put(
                "appealType",
                stringProvider.get("appealType", appealType).orElse("")
            );
        }

        fieldValues.put("newMatters", asylumCase.getNewMatters().orElse(""));

        if (asylumCase.getAppellantHasFixedAddress().orElse(YesOrNo.NO) == YesOrNo.YES) {

            Optional<AddressUk> optionalAppellantAddress = asylumCase.getAppellantAddress();

            if (optionalAppellantAddress.isPresent()) {

                AddressUk appellantAddress = optionalAppellantAddress.get();

                fieldValues.put(
                    "appellantAddress",
                    ImmutableMap
                        .builder()
                        .put("appellantAddressLine1", appellantAddress.getAddressLine1().orElse(""))
                        .put("appellantAddressLine2", appellantAddress.getAddressLine2().orElse(""))
                        .put("appellantAddressLine3", appellantAddress.getAddressLine3().orElse(""))
                        .put("appellantAddressPostTown", appellantAddress.getPostTown().orElse(""))
                        .put("appellantAddressCounty", appellantAddress.getCounty().orElse(""))
                        .put("appellantAddressPostCode", appellantAddress.getPostCode().orElse(""))
                        .put("appellantAddressCountry", appellantAddress.getCountry().orElse(""))
                        .build()
                );
            }
        }

        fieldValues.put(
            "appellantNationalities",
            asylumCase
                .getAppellantNationalities()
                .orElse(Collections.emptyList())
                .stream()
                .filter(idValue -> idValue.getValue().containsKey("code"))
                .map(idValue -> idValue.getValue().get("code"))
                .map(code -> stringProvider.get("isoCountries", code))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(nationality -> ImmutableMap.of("nationality", nationality))
                .collect(Collectors.toList())
        );

        fieldValues.put(
            "appealGrounds",
            asylumCase
                .getAppealGroundsForDisplay()
                .orElse(Collections.emptyList())
                .stream()
                .map(code -> stringProvider.get("appealGrounds", code))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(appealGround -> ImmutableMap.of("appealGround", appealGround))
                .collect(Collectors.toList())
        );

        fieldValues.put(
            "otherAppeals",
            asylumCase
                .getOtherAppeals()
                .orElse(Collections.emptyList())
                .stream()
                .filter(idValue -> idValue.getValue().containsKey("value"))
                .map(idValue -> idValue.getValue().get("value"))
                .collect(joining(", "))
        );

        return fieldValues;
    }

    private String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }
}
