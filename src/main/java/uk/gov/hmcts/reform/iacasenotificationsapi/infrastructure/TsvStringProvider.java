package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Service
public class TsvStringProvider implements StringProvider {

    private final String tsvStringsFileLocation;

    private final Map<String, Map<String, String>> stringsByGroup = new HashMap<>();

    public TsvStringProvider(
        @Value("${tsvStringsFileLocation}") String tsvStringsFileLocation
    ) {
        this.tsvStringsFileLocation = tsvStringsFileLocation;
    }

    public Optional<String> get(
        String group,
        String code
    ) {
        requireNonNull(group, "group must not be null");
        requireNonNull(code, "code must not be null");

        tryLoadTsv();

        return Optional.ofNullable(
            stringsByGroup
                .getOrDefault(group, Collections.emptyMap())
                .get(code)
        );
    }

    private synchronized void tryLoadTsv() {

        if (!stringsByGroup.isEmpty()) {
            return;
        }

        try {

            String tsv =
                StreamUtils.copyToString(
                    new ClassPathResource(tsvStringsFileLocation).getInputStream(),
                    Charset.defaultCharset()
                );

            String[] lines = tsv.split("\\r?\\n");

            Stream
                .of(lines)
                .map(line -> line.split("\t"))
                .forEach(fields ->
                    stringsByGroup.computeIfAbsent(
                        fields[0],
                        k -> new HashMap<>()
                    ).put(
                        fields[1],
                        fields[2]
                    )
                );

        } catch (IOException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalStateException("Cannot load TSV strings from file: " + tsvStringsFileLocation);
        }
    }
}
