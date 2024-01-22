package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterLanguageRefData {

    private DynamicList languageRefData;
    private String languageManualEntry;
    private String languageManualEntryDescription;

}
