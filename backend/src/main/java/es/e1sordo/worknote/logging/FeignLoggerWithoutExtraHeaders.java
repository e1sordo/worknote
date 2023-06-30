package es.e1sordo.worknote.logging;

import feign.Request;
import feign.Response;
import feign.Util;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static feign.Util.UTF_8;
import static feign.Util.decodeOrDefault;
import static feign.Util.valuesOrEmpty;

@RequiredArgsConstructor
public class FeignLoggerWithoutExtraHeaders extends feign.Logger {

    private static final String IN_PREFIX = "<<<---";
    private static final String OUT_PREFIX = "--->>>";

    private final Logger log;
    private final boolean printAllHeaders;
    private final List<String> printableHeaders;
    private final List<String> secretHeaders;

    @Override
    protected void log(String configKey, String formatPattern, Object... args) {
        final var templateMessage = "%s%s".formatted(extractMethodTag(configKey), formatPattern);
        log.info(templateMessage, args);
    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        final var prefix = OUT_PREFIX;

        log(configKey, "{} {} {} HTTP/1.1", prefix, request.httpMethod().name(), request.url());

        logPrintableHeadersOnly(configKey, prefix, request.headers());

        final var requestBody = request.body();
        if ((requestBody != null) && (logLevel.ordinal() >= Level.FULL.ordinal())) {
            logBodyInSingleLine(configKey, prefix, new String(requestBody));
        }
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
            throws IOException {
        final var prefix = IN_PREFIX;

        final String reason = response.reason() != null && logLevel.compareTo(Level.NONE) > 0
                ? " " + response.reason()
                : "";
        final int status = response.status();
        log(configKey, "{} HTTP/1.1 {}{} ({}ms)", prefix, status, reason, elapsedTime);

        logPrintableHeadersOnly(configKey, prefix, response.headers());

        if (response.body() == null || (status == 204 || status == 205)) {
            // HTTP 204 No Content "...response MUST NOT include a message-body"
            // HTTP 205 Reset Content "...response MUST NOT include an entity"
            log(configKey, "{} END HTTP (empty body)", prefix);
            return response;
        }

        byte[] bodyData = Util.toByteArray(response.body().asInputStream());
        final var bodyLength = bodyData.length;
        if (logLevel.ordinal() >= Level.FULL.ordinal() && bodyLength > 0) {
            final var decodedString = decodeOrDefault(bodyData, UTF_8, "Binary data");
            logBodyInSingleLine(configKey, prefix, decodedString);
        }

        return response.toBuilder().body(bodyData).build();
    }

    @Override
    protected IOException logIOException(String configKey, Level logLevel, IOException ioe, long elapsedTime) {
        log(configKey, "<--- ERROR {}: {} ({}ms)", ioe.getClass().getSimpleName(), ioe.getMessage(), elapsedTime);
        if (logLevel.ordinal() >= Level.FULL.ordinal()) {
            StringWriter sw = new StringWriter();
            ioe.printStackTrace(new PrintWriter(sw));
            log(configKey, "{}", sw);
            log(configKey, "<--- END ERROR");
        }
        return ioe;
    }

    private void logPrintableHeadersOnly(String configKey, String prefix, Map<String, Collection<String>> headers) {
        final var headersString = new StringBuilder();
        for (String field : headers.keySet())
            if (printAllHeaders || printableHeaders.contains(field))
                for (String pureValue : valuesOrEmpty(headers, field)) {
                    var value = secretHeaders.contains(field) ? "[MASKED]" : pureValue;
                    headersString.append("%s: %s, ".formatted(field, value));
                }

        if (headersString.length() > 0)
            log(configKey, "{} {}", prefix, headersString.toString());
    }

    private void logBodyInSingleLine(String configKey, String prefix, String bodyString) {
        final var singleLineBody = Arrays.stream(bodyString.split("\n"))
                .map(String::trim)
                .reduce(String::concat)
                .orElse(bodyString);
        log(configKey, "{} {}.", prefix, singleLineBody);
    }

    private String extractMethodTag(String configKey) {
        return "[🌐 " + configKey.substring(0, configKey.indexOf('(')).split("#")[1] + "] ";
    }
}
