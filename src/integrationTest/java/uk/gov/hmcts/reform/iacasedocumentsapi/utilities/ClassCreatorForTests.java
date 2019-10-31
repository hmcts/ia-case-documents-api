package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unchecked")
public class ClassCreatorForTests {

    //Instantiate classes with null values through a private no-args constructor
    public <T> Constructor<T> findPrivateNoArgsConstructor(Class<T> clazz) {

        return (Constructor<T>)
            Arrays.stream(clazz.getDeclaredConstructors())
                .filter(constructor ->
                    Modifier.isPrivate(constructor.getModifiers()) && constructor.getParameterCount() == 0)
                .peek(constructor -> constructor.setAccessible(true))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find private no-args constructor"));
    }

}
