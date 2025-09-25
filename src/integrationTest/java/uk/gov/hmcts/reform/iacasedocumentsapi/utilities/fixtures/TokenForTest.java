package uk.gov.hmcts.reform.iacasedocumentsapi.utilities.fixtures;


import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.idam.Token;

public class TokenForTest extends Token {

    public static Token generateValid() {
        return new Token("eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre", "openid roles profile");
    }

}
