/* (C) 2024 */
package com.bot.mis.util.report;

import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.util.text.format.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class ReportUtil {

    @Autowired private FormatUtil formatUtil;

    private String leftData;
    private String rightData;
    private String leftPattern;
    private String rightPattern;

    private ReportUtil() {
        // YOU SHOULD USE @Autowired ,NOT new ReportUtil()
    }

    /**
     * Generates a string consisting of a single character repeated {@code n} times. This method
     * utilizes the {@code String.repeat(int)} method for an efficient way to create a string with
     * the character repeated the specified number of times.
     *
     * @param character The character to be repeated in the generated string. This should be a
     *     non-null single character string. The behavior is undefined if the string contains more
     *     than one character.
     * @param n The number of times the character should be repeated in the generated string. If
     *     {@code n} is less than 0, the repetition is adjusted to 0 times, effectively returning an
     *     empty string.
     * @return A string made by repeating the specified {@code character} {@code n} times. If {@code
     *     n} is less than or equal to 0, the method returns an empty string.
     */
    public String makeGate(String character, int n) {
        ApLogHelper.info(
                log,
                false,
                LogType.NORMAL.getCode(),
                "makeGate() , character = {} , n = {}",
                character,
                n);
        return character.repeat(Math.max(0, n));
    }

    /**
     * Formats a given string according to a COBOL-like pattern, with special handling for leading
     * zeros.<br>
     * ex1: Z99/99/99<br>
     * ex2: ZZZ,ZZZ,ZZ9.99<br>
     * ex3: ZZ9.BB
     *
     * @param data The string data to format, expected to be a numeric string.
     * @param pattern The format pattern, where '9' represents a digit, 'Z' is a digit that may be
     *     replaced with a space if it's a leading zero, and '/' is a literal character.
     * @return The formatted string according to the pattern.
     */
    public String customFormat(String data, String pattern) {
        ApLogHelper.info(
                log,
                false,
                LogType.NORMAL.getCode(),
                "customFormat() , data = {} , pattern = {}",
                data,
                pattern);
        pattern = pattern.toUpperCase();
        cutData(data.trim());
        cutPattern(pattern);
        pad9ByPattern();
        leftFormat();
        rightFormat();
        ApLogHelper.info(
                log,
                false,
                LogType.NORMAL.getCode(),
                "customFormat() after , leftData = {} , rightData = {}",
                leftData,
                rightData);
        return leftData + rightData;
    }

    private void cutData(String data) {
        if (data.contains(".")) {
            leftData = data.substring(0, data.indexOf("."));
            rightData =
                    data.length() > data.indexOf(".") + 1
                            ? data.substring(data.indexOf(".") + 1)
                            : "";
        } else {
            leftData = data;
            rightData = "";
        }
    }

    private void cutPattern(String pattern) {
        if (pattern.contains(".")) {
            leftPattern = pattern.substring(0, pattern.indexOf("."));
            rightPattern =
                    pattern.length() > pattern.indexOf(".") + 1
                            ? pattern.substring(pattern.indexOf(".") + 1)
                            : "";
        } else {
            leftPattern = pattern;
            rightPattern = "";
        }
    }

    private void pad9ByPattern() {
        leftData = formatUtil.pad9(leftData, leftPattern.replaceAll("[/,.:]", "").length());
        rightData =
                rightPattern.isEmpty()
                        ? ""
                        : formatUtil.rightPad9(
                                rightData, rightPattern.replaceAll("[/,.:]", "").length());
    }

    private void leftFormat() {
        StringBuilder formatted = new StringBuilder();
        int dataIndex = 0;
        boolean startWithZero = true;
        for (int i = 0; i < leftPattern.length(); i++) {
            char patternChar = leftPattern.charAt(i);
            switch (patternChar) {
                case '9', 'Z':
                    if (dataIndex < leftData.length()) {
                        char dataChar = leftData.charAt(dataIndex);
                        if (startWithZero && patternChar == 'Z' && dataChar == '0') {
                            formatted.append(' ');
                        } else {
                            startWithZero = false;
                            formatted.append(dataChar);
                        }
                        dataIndex++;
                    }
                    break;
                case '-':
                    if (i == 0 && dataIndex < leftData.length()) {
                        char dataChar = leftData.charAt(dataIndex);
                        if (dataChar == '-') {
                            formatted.append(dataChar);
                            dataIndex++;
                        } else {
                            formatted.append(' ');
                            dataIndex++;
                        }
                    }
                    break;
                case '/', ',', ':':
                    if (startWithZero) {
                        formatted.append(' ');
                    } else {
                        formatted.append(patternChar);
                    }
                    break;
                default:
                    ApLogHelper.error(
                            log,
                            false,
                            LogType.NORMAL.getCode(),
                            "unexpected patternChar = {}",
                            patternChar);
                    break;
            }
        }
        leftData = formatted.toString();
    }

    private void rightFormat() {
        StringBuilder formatted = new StringBuilder();
        int dataIndex = rightPattern.length() - 1;
        boolean startWithZero = true;
        for (int i = rightPattern.length() - 1; i > -1; i--) {
            char patternChar = rightPattern.charAt(i);
            switch (patternChar) {
                case '9', 'Z':
                    if (dataIndex < rightData.length()) {
                        char dataChar = rightData.charAt(dataIndex);
                        if (startWithZero && patternChar == 'Z' && dataChar == '0') {
                            formatted.append(' ');
                        } else {
                            startWithZero = false;
                            formatted.append(dataChar);
                        }
                        dataIndex--;
                    }
                    break;
                case 'B':
                    formatted.append(' ');
                    dataIndex--;
                    break;
                default:
                    ApLogHelper.error(
                            log,
                            false,
                            LogType.NORMAL.getCode(),
                            "unexpected patternChar = {}",
                            patternChar);
                    break;
            }
        }
        rightData = formatted.isEmpty() ? "" : "." + formatted.toString();
    }
}
