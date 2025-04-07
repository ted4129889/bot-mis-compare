/* (C) 2024 */
package com.bot.mis.util.xml.mask;

import com.bot.mis.util.xml.mask.xmltag.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class DataMasker {

    // maskType
    private static final String ID_NUMBER = "1"; // 身分證字號或統一(居留)證號
    private static final String BANK_ACCOUNT_NUMBER = "2"; // 銀行帳戶之號碼 (不遮)
    private static final String CREDIT_CARD_NUMBER = "3"; // 信用卡或簽帳卡之號碼
    private static final String NAME = "4"; // 姓名
    private static final String ADDRESS = "5"; // 地址
    private static final String EMAIL_ADDRESS = "6"; // 電子郵遞地址
    private static final String PHONE_NUMBER = "7"; // 電話號碼
    private static final String BIRTHDAY = "8"; // 生日
    private static final String JOB_TITLE = "9"; // 職稱
    private static final String FINGERPRINT_PHOTO = "10"; // 指紋、相片
    private static final String PLACE_OF_BIRTH = "11"; // 出生地
    private static final String EDUCATION = "12"; // 學歷
    private static final String WORK_EXPERIENCE = "13"; // 經歷
    private static final String OCCUPATION = "14"; // 職業
    private static final String NICKNAME = "15"; // 暱稱
    private static final String MARITAL_STATUS = "16"; // 婚姻狀態
    private static final String SERVICE_UNIT = "17"; // 服務單位
    private static final String PASSPORT_NUMBER = "18"; // 護照號碼

    private static final String OBU = "OBU";
    private static final String ID = "ID";
    private static final String UNIFIED_NUMBER = "UNIFIED_NUMBER";

    @Autowired private IdMapping idMapping;

    //    @Autowired private IdConverterByYaml idConverterByYaml;

    /**
     * Masks sensitive data in the provided list of SQL data rows based on the specified field list.
     *
     * @param sqlData the list of SQL data rows to be masked
     * @param fieldList the list of fields that define the masking rules
     */
    public void maskData(List<Map<String, Object>> sqlData, List<Field> fieldList) {
        for (Map<String, Object> row : sqlData) {
            for (Field field : fieldList) {
                String fieldName = field.getFieldName();
                String maskType = field.getMaskType();
                if (row.containsKey(fieldName)) {
                    Map<String, Object> columnInfo = (Map<String, Object>) row.get(fieldName);
                    Object value = columnInfo.get("value");
                    columnInfo.put(
                            "value", applyMask(value != null ? value.toString() : null, maskType));
                }
            }
        }
    }

    /**
     * Applies the specified mask type to the given value.
     *
     * @param value the value to be masked
     * @param maskType the type of masking to apply
     * @return the masked value
     */
    public String applyMask(String value, String maskType) {
        // 如果value為空則直接return
        if (value == null || value.isEmpty() || value.isBlank()) return value;

        return switch (maskType) {
            case ID_NUMBER -> maskId(value);
            case CREDIT_CARD_NUMBER -> maskCreditCardNumber(value);
            case NAME,
                    PLACE_OF_BIRTH,
                    EDUCATION,
                    WORK_EXPERIENCE,
                    OCCUPATION,
                    NICKNAME,
                    SERVICE_UNIT -> maskAllButFirst(value);
            case ADDRESS -> maskAddress(value);
            case EMAIL_ADDRESS -> maskEmail(value);
            case PHONE_NUMBER -> maskPhoneNumber(value);
            case BIRTHDAY -> maskBirthDay(value);
            case JOB_TITLE -> maskJobTitle(value);
            case MARITAL_STATUS -> maskMaritalStatus(value);
            case PASSPORT_NUMBER -> maskPassportNumber(value);
            default -> value;
        };
    }

    private static String determineIdType(String value) {
        return switch (value.length()) {
                // 長度 = 10 判斷OBU || ID
            case 10 -> value.startsWith(OBU) ? OBU : ID;
                // 長度 = 8 為統編
            case 8 -> UNIFIED_NUMBER;
            default -> "UNKNOWN";
        };
    }

    /**
     * Masks the given ID based on its type.
     *
     * @param value the ID to be masked
     * @return the masked ID
     */
    private String maskId(String value) {
        return switch (determineIdType(value)) {
                // OBU前三碼不置換，後7碼置換
            case OBU -> OBU + generateRandomString(value.substring(3), OBU);
                // 本國ID後8碼置換
            case ID -> value.substring(0, 2) + generateRandomString(value.substring(2), ID);
                // 統編全部8碼置換
            case UNIFIED_NUMBER -> generateRandomString(value, UNIFIED_NUMBER);
            default -> value;
        };
    }

    //    private String convertByYaml(String value, String idType) {
    //        return UNIFIED_NUMBER.equals(idType)
    //                ? idConverterByYaml.maskUnifiedDigits(value)
    //                : idConverterByYaml.maskIdDigits(value);
    //    }

    private String generateRandomString(String value, String idType) {
        //        String characters =
        //
        // "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
        //        SecureRandom random = new SecureRandom();
        //        StringBuilder sb = new StringBuilder(length);
        //
        //        for (int i = 0; i < length; i++) {
        //            int index = random.nextInt(characters.length());
        //            sb.append(characters.charAt(index));
        //        }
        //
        //        return sb.toString();
        StringBuilder maskedString = new StringBuilder(value.length());
        HashMap<Integer, String> mapping = idMapping.getMapping(idType);

        for (char c : value.toCharArray()) {
            int digit = Character.getNumericValue(c);
            maskedString.append(mapping.get(digit));
        }

        return maskedString.toString();
    }

    /**
     * Masks the given credit card number.
     *
     * @param value the credit card number to be masked
     * @return the masked credit card number
     */
    private String maskCreditCardNumber(String value) {
        // 前6 後4，中間以*代替
        return value.length() > 10
                ? value.substring(0, 6)
                        + "*".repeat(value.length() - 10)
                        + value.substring(value.length() - 4)
                : value;
    }

    /**
     * Masks all characters except the first in the given value.
     *
     * @param value the value to be masked
     * @return the masked value
     */
    private String maskAllButFirst(String value) {
        // 第一個字不遮蔽，其餘以*代替
        return value.length() > 1 ? value.charAt(0) + "*".repeat(value.length() - 1) : value;
    }

    /**
     * Masks the given address.
     *
     * @param value the address to be masked
     * @return the masked address
     */
    private String maskAddress(String value) {
        // 前六個字不遮蔽，其餘以*代替
        return value.length() > 6 ? value.substring(0, 6) + "*".repeat(value.length() - 6) : value;
    }

    /**
     * Masks the given email address.
     *
     * @param value the email address to be masked
     * @return the masked email address
     */
    private String maskEmail(String value) {
        // 前二個字不遮蔽、其餘以@COM 代替
        return value.length() > 2 ? value.substring(0, 2) + "@COM" : value;
    }

    /**
     * Masks the given phone number.
     *
     * @param value the phone number to be masked
     * @return the masked phone number
     */
    private String maskPhoneNumber(String value) {
        // 前四個字不遮蔽，*遮五位，最後用9代替
        return value.length() > 4
                ? value.substring(0, 4) + "*".repeat(value.length() - 5) + "9"
                : value;
    }

    /**
     * Masks the given birthday.
     *
     * @param value the birthday to be masked
     * @return the masked birthday
     */
    private String maskBirthDay(String value) {
        // 前六位固定設為199001，後二位不遮
        return "199001" + (value.length() > 8 ? value.substring(6) : "");
    }

    /**
     * Masks the given job title.
     *
     * @param value the job title to be masked
     * @return the masked job title
     */
    private String maskJobTitle(String value) {
        // 以統一職稱代替IdConverterByYaml
        return "統一職稱";
    }

    /**
     * Masks the given marital status.
     *
     * @param value the marital status to be masked
     * @return the masked marital status
     */
    private String maskMaritalStatus(String value) {
        // 以一個*代替
        return "*";
    }

    /**
     * Masks the given passport number.
     *
     * @param value the passport number to be masked
     * @return the masked passport number
     */
    private String maskPassportNumber(String value) {
        // 前五個字不遮蔽，其餘以9 代替
        return value.length() > 5 ? value.substring(0, 5) + "9".repeat(value.length() - 5) : value;
    }
}
