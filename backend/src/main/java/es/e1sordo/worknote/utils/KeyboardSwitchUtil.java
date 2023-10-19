package es.e1sordo.worknote.utils;

import java.util.function.Function;

public final class KeyboardSwitchUtil {

    public static String ruToEn(String str) {
        return convert(str, KeyboardSwitchUtil::ruToEn);
    }

    public static String enToRu(String str) {
        return convert(str, KeyboardSwitchUtil::enToRu);
    }

    private static String convert(String str, Function<Character, Character> fromTo) {
        if (null == str || str.isEmpty()) {
            return str;
        }
        int n = str.length();
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(fromTo.apply(str.charAt(i)));
        }
        return sb.toString();
    }

    private static char ruToEn(char c) {
        boolean upper = Character.isUpperCase(c);
        char letter = switch (Character.toLowerCase(c)) {
            case 'ф' -> 'a';
            case 'и' -> 'b';
            case 'с' -> 'c';
            case 'в' -> 'd';
            case 'у' -> 'e';
            case 'а' -> 'f';
            case 'п' -> 'g';
            case 'р' -> 'h';
            case 'ш' -> 'i';
            case 'о' -> 'j';
            case 'л' -> 'k';
            case 'д' -> 'l';
            case 'ь' -> 'm';
            case 'т' -> 'n';
            case 'щ' -> 'o';
            case 'з' -> 'p';
            case 'й' -> 'q';
            case 'к' -> 'r';
            case 'ы' -> 's';
            case 'е' -> 't';
            case 'г' -> 'u';
            case 'м' -> 'v';
            case 'ц' -> 'w';
            case 'ч' -> 'x';
            case 'н' -> 'y';
            case 'я' -> 'z';
            default -> c;
        };
        return upper ? Character.toUpperCase(letter) : letter;
    }

    private static char enToRu(char c) {
        boolean upper = Character.isUpperCase(c);
        char letter = switch (Character.toLowerCase(c)) {
            case 'f' -> 'а';
            case ',' -> 'б';
            case 'd' -> 'в';
            case 'u' -> 'г';
            case 'l' -> 'д';
            case 't' -> 'е';
            case '`' -> 'ё';
            case ';' -> 'ж';
            case 'p' -> 'з';
            case 'b' -> 'и';
            case 'q' -> 'й';
            case 'r' -> 'к';
            case 'k' -> 'л';
            case 'v' -> 'м';
            case 'y' -> 'н';
            case 'j' -> 'о';
            case 'g' -> 'п';
            case 'h' -> 'р';
            case 'c' -> 'с';
            case 'n' -> 'т';
            case 'e' -> 'у';
            case 'a' -> 'ф';
            case '[' -> 'х';
            case 'w' -> 'ц';
            case 'x' -> 'ч';
            case 'i' -> 'ш';
            case 'o' -> 'щ';
            case ']' -> 'ъ';
            case 's' -> 'ы';
            case 'm' -> 'ь';
            case '\'' -> 'э';
            case '.' -> 'ю';
            case 'z' -> 'я';
            case '<' -> 'Б';
            case '~' -> 'Ё';
            case ':' -> 'Ж';
            case '{' -> 'Х';
            case '}' -> 'Ъ';
            case '"' -> 'Э';
            case '>' -> 'Ю';
            default -> c;
        };
        return upper ? Character.toUpperCase(letter) : letter;
    }
}
