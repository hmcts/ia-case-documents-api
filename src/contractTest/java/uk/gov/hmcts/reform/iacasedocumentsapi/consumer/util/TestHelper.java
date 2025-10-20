package uk.gov.hmcts.reform.iacasedocumentsapi.consumer.util;

import java.util.concurrent.Callable;

public class TestHelper<T> {
    public T executeWithRetry(Callable<T> func, int maxAttempts) {
        T response = null;
        int attempts = 0;
        while (attempts < maxAttempts) {
            try {
                response = func.call();
                break;
            } catch (Exception e) {
                attempts++;
                if (attempts == maxAttempts) {
                    break;
                }
            }
        }
        return response;
    }
}
