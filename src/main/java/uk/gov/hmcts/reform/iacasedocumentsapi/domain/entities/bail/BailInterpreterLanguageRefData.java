package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.bail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BailInterpreterLanguageRefData {

    private DynamicList languageRefData;
    private String languageManualEntry;
    private String languageManualEntryDescription;

}
