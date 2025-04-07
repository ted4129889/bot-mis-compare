/* (C) 2024 */
package com.bot.mis.util.text;

import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.exception.LogicException;
import com.bot.txcontrol.util.parse.Parse;
import com.bot.txcontrol.util.text.format.FormatUtil;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class FormatData {

    @Autowired private Parse parse;

    @Autowired private FormatUtil formatUtil;

    private FormatData() {
        // YOU SHOULD USE @Autowired ,NOT new ErrUtil()
    }

    private DecimalFormat df;

    private final String STR_NULL = "null";
    private final String STR_0 = "0";

    private final String STR_MINUS = "-";
    private final String STR_PLUS = "+";

    /**
     * 數字格式化
     *
     * @param value 需要格式化的值
     * @param _int 整數位
     * @param _decimal 小數位<br>
     *     EX1: decimalFormat(123,5.2) = > 00123.00 <br>
     */
    public String decimalFormat(Object value, int _int, int _decimal) {

        if (value.toString().trim().isEmpty()) {
            value = " ";
        }

        df = createBigDecimalFormat(_int, _decimal);

        if (value instanceof Short) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "Short");
        } else if (value instanceof Integer) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "Integer");
        } else if (value instanceof Long) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "Long");
        } else if (value instanceof String) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "String");
            String val = (String) value;
            if (!val.matches("-?\\d+(,\\d{3})*(\\.\\d+)?")) {
                if (STR_NULL.equalsIgnoreCase(val)) {
                    value = STR_0;
                } else {
                    throw new LogicException("", "不是有效的數字");
                }
            }
        } else if (value instanceof BigDecimal) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "BigDecimal");
        } else {
            return (String) value;
        }

        return df.format(value);
    }

    /**
     * 數字格式化2
     *
     * @param value 需要格式化的值
     * @param format 格式 EX1: decimalFormat2(123,00000.00) = > 00123.00 <br>
     */
    public String decimalFormat2(Object value, String format) {
        if (value.toString().trim().isEmpty()) {
            value = STR_0;
        }
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
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "Short");
        } else if (value instanceof Integer) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "Integer");
        } else if (value instanceof Long) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "Long");
        } else if (value instanceof String) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "String");
            String val = (String) value;
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), val);
            if (!val.matches("-?\\d+(,\\d{3})*(\\.\\d+)?")) {
                if (STR_NULL.equalsIgnoreCase(val)) {
                    value = STR_0;
                } else if (STR_PLUS.equals(val.substring(0, 1))) {
                    value = val.substring(1);
                } else {
                    throw new LogicException("", "不是有效的數字");
                }
            }
        } else if (value instanceof BigDecimal) {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "BigDecimal");
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

        ApLogHelper.info(
                log,
                false,
                LogType.NORMAL.getCode(),
                "formatText = new DecimalFormat(\" {} \")",
                formatText);

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
}
