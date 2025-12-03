/* (C) 2024 */
package com.bot.util.text;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.bot.util.files.TextFileUtil;
import com.bot.util.format.FormatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class FormatData {


    private FormatData() {
        // YOU SHOULD USE @Autowired ,NOT new ErrUtil()
    }

    @Autowired
    private FormatUtil formatUtil;
    @Autowired
    private TextFileUtil textFileUtil;
    private DecimalFormat df;

    private final String STR_NULL = "null";
    private final String STR_0 = "0";

    private final String STR_MINUS = "-";
    private final String STR_PLUS = "+";

    /**
     * 數字格式化
     *
     * @param value    需要格式化的值
     * @param _int     整數位
     * @param _decimal 小數位<br>
     *                 EX1: decimalFormat(123,5.2) = > 00123.00 <br>
     */
    public String decimalFormat(Object value, int _int, int _decimal) {

        if (value.toString().trim().isEmpty()) {
            value = " ";
        }

        df = createBigDecimalFormat(_int, _decimal);

        if (value instanceof Short) {
        } else if (value instanceof Integer) {
        } else if (value instanceof Long) {
        } else if (value instanceof String) {
            String val = (String) value;
            if (!val.matches("-?\\d+(,\\d{3})*(\\.\\d+)?")) {
                if (STR_NULL.equalsIgnoreCase(val)) {
                    value = STR_0;
                } else {
                }
            }
        } else if (value instanceof BigDecimal) {
        } else {
            return (String) value;
        }

        return df.format(value);
    }

    /**
     * 數字格式化2
     *
     * @param value  需要格式化的值
     * @param format 格式 EX1: decimalFormat2(123,00000.00) = > 00123.00 <br>
     */
    public String decimalFormat2(Object value, String format) {
        if (value.toString().trim().isEmpty()) {
            value = STR_0;
        }
        // TODO 判斷有-------9.99 為補滿負號數字
        // 先判斷正負號 決定要空白或是-號碼
        // 長度 判斷長度總共要幾位 建立一個 剛好長度的字串位置
        //

        // 判斷有無正負號
        if (STR_MINUS.equals(format.substring(0, 1)) || STR_PLUS.equals(format.substring(0, 1))) {
            format = STR_PLUS + format.substring(1) + ";" + format;
        }

        // 正常只會有數字會需要設定格式
        if (value.toString().length() > format.length()) {
            int f = format.length();
            value = value.toString().substring(0, f);
        }

        df = new DecimalFormat(format);

        if (value instanceof Short) {
        } else if (value instanceof Integer) {
        } else if (value instanceof Long) {
        } else if (value instanceof String) {
            String val = (String) value;
            if (!val.matches("-?\\d+(,\\d{3})*(\\.\\d+)?")) {
                if (STR_NULL.equalsIgnoreCase(val)) {
                    value = STR_0;
                } else if (STR_PLUS.equals(val.substring(0, 1))) {
                    value = val.substring(1);
                } else {
                }
            }
        } else if (value instanceof BigDecimal) {
        } else {
            return (String) value;
        }

        // 若直接用字串的數字接format會出錯，統一最後用bigdecimal接
        BigDecimal bgValue = new BigDecimal(String.valueOf(value));

        return df.format(bgValue);
    }

    private DecimalFormat createBigDecimalFormat(int _int, int _decimal) {

        String formatText = "";
        if (_int > 0) {

            for (int i = 1; i <= _int; i++) {
                formatText = formatText + STR_0;
            }
        }

        if (_decimal > 0) {
            formatText = formatText + ".";
            for (int i = 1; i <= _decimal; i++) {
                formatText = formatText + STR_0;
            }
        }


        return new DecimalFormat(formatText);
    }

    /**
     * 字串格式化(字串靠左)
     *
     * @param value 需要格式化的值
     * @param count 總共位數(以空白補足)
     */
    public String stringFormatL(String value, int count) {
        if (STR_NULL.equalsIgnoreCase(value)) value = " ";

        return formatUtil.padX(value, count);
    }

    /**
     * 字串格式化(字串靠右)
     *
     * @param value 需要格式化的值
     * @param count 總共位數(以空白補足)
     */
    public String stringFormatR(String value, int count) {
        if (STR_NULL.equalsIgnoreCase(value)) value = " ";
        return formatUtil.padLeft(value, count);
    }


    /**
     * 中文替換成*、全型空白補2個半型空白
     */
    public String getMakedValue(String str, String replaceTxt) {

        String t = "";

        for (char c : str.toCharArray()) {
            if (isChinese(c)) {
                t = t + replaceTxt + replaceTxt;
            } else if (isFullWidthSpace(c)) {
                t = t + "  ";
            } else if (!" ".equals(String.valueOf(c))) {
                t = t + replaceTxt;
            } else {
                t = t + c;
            }
        }
        return t;
    }

    /**
     * 計算字串的「顯示寬度」，中文字和全形空白算 2，其他字元算 1
     */
    public int getDisplayWidth(String str) {
        int length = 0;
        for (char c : str.toCharArray()) {
            if (isChinese(c) || isFullWidthSpace(c) || isFullWidthChar(c) || isSpecialWidthChar(c)) {
                length += 2;
            } else {
                length += 1;
            }
        }
        return length;
    }

    /**
     * 中文替換成2個replaceTxt、全形空白補2個半形空白、
     * 其他全形字元也補2個replaceTxt，半形補1個replaceTxt
     */
    public String getReplaceValue(String str, String replaceTxt) {
        StringBuilder result = new StringBuilder();

        for (char c : str.toCharArray()) {
            if (isChinese(c)) {
                // 中文佔兩位
                result.append(replaceTxt).append(replaceTxt);
            } else if (isFullWidthSpace(c)) {
                // 全形空白補兩個半形空白
                result.append(replaceTxt).append(replaceTxt);
            } else if ( isSpecialWidthChar(c)) {
                // 特殊字體補兩個半行空白
                result.append(replaceTxt).append(replaceTxt);
            } else if (isFullWidthChar(c)) {
                // 其他全形字元替換1個字
                result.append(replaceTxt).append(replaceTxt);
            } else {
                // 其他半形字元替換1個字
                result.append(replaceTxt);
            }
        }

        return result.toString();
    }

    public String getReplaceSpace(String str, String replaceTxt) {
        StringBuilder result = new StringBuilder();
        for (char c : str.toCharArray()) {

            if (isChinese(c)) {
                // 中文佔兩位
                result.append(c);
            } else if ( isSpecialWidthChar(c)) {
                // 特殊字體補兩個半行空白
                result.append(replaceTxt).append(replaceTxt);
            } else if (isFullWidthSpace(c)) {
                // 全形空白補兩個半形空白
                result.append(replaceTxt).append(replaceTxt);
            } else  if (isFullWidthChar(c)) {
                // 其他全形字元替換1個字
                result.append(c);
            } else {
                // 其他半形字元替換1個字
                result.append(c);
            }
        }

        return result.toString();
    }

    // 判斷是否為中文
    public boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT
                || ub == Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS;
    }

    /** 判斷是否為全形空白（Unicode: \u3000） */
    private boolean isFullWidthSpace(char c) {
        return c == '\u3000';
    }

    /** 判斷是否為全形字元（包含全形英數與符號） */
    private boolean isFullWidthChar(char c) {
        // 中日韓統一表意文字 (中文)
        if (c >= 0x4E00 && c <= 0x9FFF) return true;

        // 全形 ASCII、全形標點
        if (c >= 0xFF01 && c <= 0xFF60) return true;
        if (c >= 0xFFE0 && c <= 0xFFE6) return true;

        // 注音符號 ㄅㄆㄇㄈ ㄧㄨㄩ
        if (c >= 0x3100 && c <= 0x312F) return true;

        // 注音擴展
        if (c >= 0x31A0 && c <= 0x31BF) return true;

        return false;
    }
    /** 亂碼 / 特殊字，視覺寬度通常 = 2 */
    private boolean isSpecialWidthChar(char c){
        return  c >= 0xE000 && c <= 0xF8FF;
    }
}
