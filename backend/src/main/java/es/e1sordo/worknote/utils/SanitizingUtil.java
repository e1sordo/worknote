package es.e1sordo.worknote.utils;

import org.springframework.util.StringUtils;

public final class SanitizingUtil {

    public static String sanitize(String text) {
        return StringUtils.deleteAny(text, "\n\r").trim();
    }
}
