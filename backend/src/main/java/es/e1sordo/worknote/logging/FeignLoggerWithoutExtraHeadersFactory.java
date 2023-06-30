package es.e1sordo.worknote.logging;

import feign.Logger;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignLoggerFactory;

import java.util.List;

@RequiredArgsConstructor
public class FeignLoggerWithoutExtraHeadersFactory implements FeignLoggerFactory {

    private final boolean printAllHeaders;
    private final List<String> printableHeaders;
    private final List<String> secretHeaders;

    @Override
    public Logger create(Class<?> type) {
        return new FeignLoggerWithoutExtraHeaders(
                LoggerFactory.getLogger(type),
                printAllHeaders,
                printableHeaders,
                secretHeaders
        );
    }
}
