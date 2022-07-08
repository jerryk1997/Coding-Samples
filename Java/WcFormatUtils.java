package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;

public final class WcFormatUtils {
    private WcFormatUtils() {

    }

    public static String formatValue(String value) {
        return value.isEmpty() ? "" : (CHAR_TAB + value);
    }

    public static String formatValue(Long value) {
        return CHAR_TAB + Long.toString(value);
    }

    /**
     * Returns formatted StringBuilder of count results
     *
     * @param isBytes Boolean option to include count of bytes
     * @param isLines Boolean option to include count of lines
     * @param isWords Boolean option to include count of words
     * @param count   Count result from getCountReport
     */
    public static StringBuilder formatCountResult(Boolean isBytes, Boolean isLines, Boolean isWords, long[] count) { //NOPMD - suppressed UseVarargs - need all three boolean names
        StringBuilder sb = new StringBuilder(); //NOPMD - suppressed ShortVariable - unnecessary variable name is clear
        if (isLines) {
            sb.append(formatValue(count[0]));
        }
        if (isWords) {
            sb.append(formatValue(count[1]));
        }
        if (isBytes) {
            sb.append(formatValue(count[2]));
        }
        return sb;
    }
}
