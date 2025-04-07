/* (C) 2024 */
package com.bot.mis.util.xml;

import com.bot.mis.code.CurrencyCode;
import com.bot.mis.util.files.TextFileUtil;
import com.bot.mis.util.text.FormatData;
import com.bot.mis.util.xml.vo.FieldProperties;
import com.bot.mis.util.xml.vo.XmlField;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class DataProcess {

    @Autowired private EntityManager entityManager;
    @Autowired private TextFileUtil textFileUtil;
    @Autowired private FormatData fd;

    private Map<String, String> tmpMap = new HashMap<>();

    int batchDate = 0;
    private final String STR_STRING = "string";
    private final String STR_0 = "0";
    private final String SCIENTIFICNOTATION = "e";
    private final String SLASH = "/";
    private final String STR_YYYYMMDD = "YYYY/MM/DD";

    private final String STR_LEFT = "left";

    private final int BUFFER_CAPACITY = 2000;

    /**
     * 執行 資料與設定檔處理
     *
     * @param sqlDatalist 資料庫資料
     * @param xmlList xml欄位
     * @return List
     */
    protected List<String> exec(
            List<Map<String, String>> sqlDatalist,
            List<XmlField> xmlList,
            boolean isUseSqlData,
            String separator,
            int batchDate) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "DataProcess exec...");
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "xmlList ={}", xmlList.toString());

        this.batchDate = batchDate;

        StringBuilder result;
        List<String> fileContents = new ArrayList<>();
        if (isUseSqlData) {
            for (Map<String, String> r : sqlDatalist) {
                String value = "";
                this.tmpMap.putAll(r);
                result = new StringBuilder(BUFFER_CAPACITY);
                for (XmlField f : xmlList) {

                    FieldProperties properties = new FieldProperties();
                    properties.setFieldName(f.getFieldName());
                    properties.setFieldType(f.getFieldType());
                    properties.setFormat(f.getFormat());
                    properties.setLength(Integer.parseInt(f.getLength()));
                    properties.setAlign(f.getAlign());
                    properties.setOTableName(f.getOTableName());
                    properties.setOFieldName(f.getOFieldName());

                    String getFieldValue = "";
                    if (!Objects.isNull(r.get(f.getFieldName()))) {
                        getFieldValue = r.get(f.getFieldName()).trim();
                    } else if (!Objects.isNull(r.get(f.getFieldName().toUpperCase()))) {
                        getFieldValue = r.get(f.getFieldName().toUpperCase()).trim();
                    } else {
                        getFieldValue = "";
                    }

                    value = formatValue(getFieldValue, properties, separator); // 格式化值

                    ApLogHelper.info(
                            log, false, LogType.NORMAL.getCode(), "After format value = {}", value);
                    result.append(value); // 按順序拼接值
                }

                fileContents.add(result.toString());
            }
        } else {
            result = new StringBuilder(BUFFER_CAPACITY);
            String value = "";
            for (XmlField f : xmlList) {

                FieldProperties properties = new FieldProperties();
                properties.setFieldName(f.getFieldName());
                properties.setFieldType(f.getFieldType());
                properties.setFormat(f.getFormat());
                properties.setLength(Integer.parseInt(f.getLength()));
                properties.setAlign(f.getAlign());
                properties.setOTableName(f.getOTableName());
                properties.setOFieldName(f.getOFieldName());

                value = formatValue(f.getFieldName().trim(), properties, separator);

                ApLogHelper.info(
                        log, false, LogType.NORMAL.getCode(), "After format value = {}", value);
                result.append(value); // 按順序拼接值
            }

            fileContents.add(result.toString());
        }

        return fileContents;
    }

    /**
     * 格式化欄位
     *
     * @param value 值
     * @param properties 稱欄設定
     * @return String
     */
    private String formatValue(String value, FieldProperties properties, String separator) {
        normalLog("before format value = {}", value);
        normalLog("xml.getFieldName = {}", properties.getFieldName());
        normalLog("xml.fieldType = {}", properties.getFieldType());
        normalLog("xml.format = {}", properties.getFormat());
        normalLog("xml.length = {}", properties.getLength());
        normalLog("xml.align = {}", properties.getAlign());
        normalLog("xml.oTableName = {}", properties.getOTableName());
        normalLog("xml.ofieldName = {}", properties.getOFieldName());

        String text = xmlFieldNameProcess(value, properties, separator);

        // 字串型態(因是要輸出TXT，基本上SQL的值最後轉成字串比較好處理
        if (STR_STRING.equalsIgnoreCase(properties.getFieldType())) {
            // 含0表示是數字(金額、利率...)
            if (properties.getFormat().contains(STR_0)) {
                // 數字如果用DECIMAL FORMAT處理都會是往左補0，此段暫時不分左右
                text = parseDecimalFormat(text, properties.getFormat());
            } else {
                text = parseStringFormat(text, properties);
            }
        } else {
            if (properties.getFormat().contains(STR_0)) {
                text = parseDecimalFormat(text, properties.getFormat());
            }
        }
        return text;
    }

    /**
     * XML fieldName 特殊字眼處理
     *
     * @param sValue 欄位名稱
     * @param properties 稱欄設定
     * @param separator 區隔符號
     * @return String
     */
    private String xmlFieldNameProcess(
            String sValue, FieldProperties properties, String separator) {
        String value = "";
        if ("batchdate".equalsIgnoreCase(sValue)) {
            value = String.valueOf(batchDate);
            // 固定值(第一個字元跟最後一個字元為單引號表示為固定值
        } else if (properties.getFieldName().startsWith("'")
                && properties.getFieldName().endsWith("'")) {
            value = properties.getFieldName().substring(1, properties.getFieldName().length() - 1);
            // 空白
        } else if (properties.getFieldName().toLowerCase().contains("space")) {
            value = "";
            // 區隔符號
        } else if (properties.getFieldName().toLowerCase().contains("separator")) {
            value = separator;
            // 雙引號
        } else if (properties.getFieldName().toLowerCase().contains("dquo")) {
            value = "\"";
        } else if (properties.getFieldName().toLowerCase().contains("currency")) {
            value = getCurrency(properties.getFieldName());
        } else if ("".equalsIgnoreCase(sValue)) {
            value = "";
        } else {
            value = sValue;
        }
        return value;
    }

    /**
     * 處理數字(含科學記號)
     *
     * @param text 值(資料)
     * @param format 格式
     * @return String 字串
     */
    private String parseDecimalFormat(String text, String format) {
        ApLogHelper.info(
                log, false, LogType.NORMAL.getCode(), " parseDecimalFormat.value = {}", text);
        ApLogHelper.info(
                log,
                false,
                LogType.NORMAL.getCode(),
                " parseDecimalFormat.formatType = {}",
                format);
        String result = "";
        if (text.toLowerCase().contains(SCIENTIFICNOTATION)) {
            BigDecimal number = new BigDecimal(text.trim());
            // 數字如果用DECIMAL FORMAT處理都會是往左補0，此段暫時不分左右
            result = fd.decimalFormat2(number, format);
        } else {
            result = fd.decimalFormat2(text, format);
        }

        return result;
    }

    /**
     * 處理文字(字串、日期)
     *
     * @param text 值(資料)
     * @param properties 格式設定
     * @return String 字串
     */
    private String parseStringFormat(String text, FieldProperties properties) {
        ApLogHelper.info(
                log, false, LogType.NORMAL.getCode(), " parseStringFormat.value = {}", text);
        ApLogHelper.info(
                log,
                false,
                LogType.NORMAL.getCode(),
                " parseStringFormat.formatType = {}",
                properties.getFormat());
        String result = "";
        if (STR_YYYYMMDD.equalsIgnoreCase(properties.getFormat())) {

            if (text.trim().length() == 8) {
                result =
                        text.substring(0, 4)
                                + SLASH
                                + text.substring(4, 6)
                                + SLASH
                                + text.substring(6, 8);
            } else {
                text = String.valueOf(this.batchDate);
                result =
                        text.substring(0, 4)
                                + SLASH
                                + text.substring(4, 6)
                                + SLASH
                                + text.substring(6, 8);
            }
        }
        if ("".equalsIgnoreCase(properties.getFormat())) {
            if (STR_LEFT.equalsIgnoreCase(properties.getAlign())) {
                result = fd.stringFormatL(text, properties.getLength());
            } else {
                result = fd.stringFormatR(text, properties.getLength());
            }
        }
        return result;
    }

    /** 幣別資料處理 */
    private String getCurrency(String fieldName) {
        String result = "";

        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(fieldName);

        String dbColName = "";
        if (matcher.find()) {
            // 取得第一個括號內的值
            dbColName = matcher.group(1);
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "dbColName = {}", dbColName);
        } else {
            ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "未找到括號內的值");
        }

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "curcd = {}", dbColName);
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "currencyColName = {}", fieldName);

        // 幣別全名
        if (fieldName.toLowerCase().contains("currencyname")) {
            result = CurrencyCode.setCurcd(Integer.parseInt(tmpMap.get(dbColName))).getName();
            ApLogHelper.info(
                    log, false, LogType.NORMAL.getCode(), "CurrencyCode getName = {}", result);
        }
        // 幣別簡稱
        if (fieldName.toLowerCase().contains("currencyswname")) {
            result = CurrencyCode.setCurcd(Integer.parseInt(tmpMap.get(dbColName))).getSwName();
            ApLogHelper.info(
                    log, false, LogType.NORMAL.getCode(), "CurrencyCode getSwName = {}", result);
        }
        // 幣別單位符號
        if (fieldName.toLowerCase().contains("currencysymbol")) {
            result = CurrencyCode.setCurcd(Integer.parseInt(tmpMap.get(dbColName))).getSymbol();
            ApLogHelper.info(
                    log, false, LogType.NORMAL.getCode(), "CurrencyCode getSymbol = {}", result);
        }

        return result;
    }

    private void normalLog(String key, Object value) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), key, value);
    }
}
