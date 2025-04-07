/* (C) 2024 */
package com.bot.mis.code;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public enum CurrencyCode {
    TWD(0, "NT$", "TWD", "New Taiwan Dollar"),
    USD(1, "US$", "USD", "US Dollar"),
    HKD(2, "HK$", "HKD", "Hong Kong Dollar"),
    MYR(3, "M$", "MYR", "Riggit"),
    GBP(4, "STG", "GBP", "Pound Sterling"),
    AUD(5, "A$", "AUD", "Australian Dollar"),
    CAD(6, "C$", "CAD", "Canadian Dollar"),
    FRF(7, "F.FR", "FRF", "Franc"),
    DEM(8, "DM", "DEM", "Deutsche Mark"),

    ITL(9, "LIRA", "ITL", "Italian Lira"),

    SGD(10, "S$", "SGD", "Singapore Dollar"),

    CHF(11, "S.FR", "CHF", "Swiss Franc"),

    BEF(12, "B.FR", "BEF", "Common Belgian Franc"),

    JPY(13, "YEN", "JPY", "Japanese Yen"),

    ATS(14, "AS", "ATS", "Austrian Schilling"),

    NLG(15, "DFL", "NLG", "Netherlands Guilder"),

    ZAR(16, "RAND", "ZAR", "South African Rand"),

    SEK(17, "SW.KR", "SEK", "Swedish Krona"),

    NZD(18, "NZ$", "NZD", "New Zealand Dollar"),

    THB(19, "BAHT", "THB", "Thai Baht"),

    PHP(20, "P.", "PHP", "Philippine Peso"),

    IDR(21, "RP.", "IDR", "Indonesian Rupiah"),

    EUR(22, "EURO", "EUR", "EURO"),

    ESP(23, "PTA", "ESP", "Spanish Peseta"),

    KRW(24, "WON", "KRW", "WON"),

    CNY(25, "YUAN", "CNY", "YUAN"),

    VND(26, "DONG", "VND", "DONG");

    private final int curcd;
    private final String symbol;
    private final String swname;
    private final String name;

    // Enum 的構造方法
    CurrencyCode(int curcd, String symbol, String swname, String name) {
        this.curcd = curcd;
        this.symbol = symbol;
        this.swname = swname;
        this.name = name;
    }

    // Getter 方法
    public int getCurcd() {
        return curcd;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getSwName() {
        return swname;
    }

    public String getName() {
        return name;
    }

    // 根據序號查找貨幣
    public static CurrencyCode setCurcd(int curcd) {
        for (CurrencyCode currency : values()) {
            if (currency.curcd == curcd) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Invalid currency curcd: " + curcd);
    }
}
