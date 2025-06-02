package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities;

import static junit.framework.TestCase.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DynamicListTest {

    private final Value codeValue1 = new Value("code123", "code123");
    private final Value codeValue2 = new Value("code456", "code456");
    private final Value codeValue3 = new Value("code789", "code789");

    private final List<Value> accountsFromOrg = new ArrayList<>();

    @Test
    void should_hold_onto_values() {

        accountsFromOrg.add(codeValue1);
        accountsFromOrg.add(codeValue2);
        accountsFromOrg.add(codeValue3);

        DynamicList accountList = new DynamicList(accountsFromOrg.get(0), accountsFromOrg);

        Assertions.assertEquals(codeValue1, accountList.getValue());
        Assertions.assertEquals(codeValue1, accountList.getListItems().get(0));
        Assertions.assertEquals(codeValue2, accountList.getListItems().get(1));
        Assertions.assertEquals(codeValue3, accountList.getListItems().get(2));

        Assertions.assertEquals(accountsFromOrg, accountList.getListItems());
    }

}
