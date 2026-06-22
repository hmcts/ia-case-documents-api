package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CallbackRequestAdapterTest {

    @Mock
    private MethodParameter methodParameter;

    @Mock
    private Type type;

    @InjectMocks
    private CallbackRequestAdapter callbackRequestAdapter;

    @BeforeEach
    void setUp() {

    }

    @Test
    void should_return_true() {
        assertEquals(true, callbackRequestAdapter.supports(methodParameter, type, null));
    }

}
