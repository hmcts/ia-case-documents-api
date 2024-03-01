package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InterpreterLanguageRefData {

    private DynamicList languageRefData;
    private List<String> languageManualEntry;
    private String languageManualEntryDescription;

}