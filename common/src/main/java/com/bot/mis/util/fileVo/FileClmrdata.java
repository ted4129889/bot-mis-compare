/* (C) 2024 */
package com.bot.mis.util.fileVo;

import com.bot.txcontrol.adapter.in.RequestBaseSvc;
import com.bot.txcontrol.config.CustomSchema;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Component("FileClmrdata")
@Scope("prototype")
public class FileClmrdata extends RequestBaseSvc {

    @CustomSchema(
            order = 68,
            schema = @Schema(description = "代收類別", maxLength = 6, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CODE}")
    @JsonProperty("CODE")
    private String code;

    @CustomSchema(
            order = 69,
            schema = @Schema(description = "主辦行", maxLength = 3, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{PBRNO}")
    @JsonProperty("PBRNO")
    private String pbrno;

    @CustomSchema(
            order = 70,
            schema = @Schema(description = "ATM繳款代碼", maxLength = 3, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ATMCODE}")
    @JsonProperty("ATMCODE")
    private String atmcode;

    @CustomSchema(
            order = 71,
            schema = @Schema(description = "虛擬帳號繳款代碼", maxLength = 4, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{VRCODE}")
    @JsonProperty("VRCODE")
    private String vrcode;

    @CustomSchema(
            order = 72,
            schema = @Schema(description = "銷帳編號重覆記號", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{RIDDUP}")
    @JsonProperty("RIDDUP")
    private String riddup;

    @CustomSchema(
            order = 73,
            schema = @Schema(description = "存入戶號", maxLength = 12, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ACTNO}")
    @JsonProperty("ACTNO")
    private String actno;

    @CustomSchema(
            order = 74,
            schema = @Schema(description = "整批入扣記號", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{MSG1}")
    @JsonProperty("MSG1")
    private String msg1;

    @CustomSchema(
            order = 75,
            schema = @Schema(description = "傳送空檔記號", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{MSG2}")
    @JsonProperty("MSG2")
    private String msg2;

    @CustomSchema(
            order = 76,
            schema = @Schema(description = "事業單位每筆給付費用", maxLength = 6, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CFEE1}")
    @JsonProperty("CFEE1")
    private String cfee1;

    @CustomSchema(
            order = 77,
            schema = @Schema(description = "主辦行每筆給付代收行費用", maxLength = 5, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CFEE2}")
    @JsonProperty("CFEE2")
    private String cfee2;

    @CustomSchema(
            order = 78,
            schema = @Schema(description = "資料回應方式", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{PUTTIME}")
    @JsonProperty("PUTTIME")
    private String puttime;

    @CustomSchema(
            order = 79,
            schema = @Schema(description = "資料檢查方式", maxLength = 1, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CHKTYPE}")
    @JsonProperty("CHKTYPE")
    private String chktype;

    @CustomSchema(
            order = 80,
            schema = @Schema(description = "金額檢查方式", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CHKAMT}")
    @JsonProperty("CHKAMT")
    private String chkamt;

    @CustomSchema(
            order = 81,
            schema = @Schema(description = "單位金額(每股金額)", maxLength = 10, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{UNIT}")
    @JsonProperty("UNIT")
    private String unit;

    @CustomSchema(
            order = 82,
            schema = @Schema(description = "額度控管", maxLength = 13, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{AMT}")
    @JsonProperty("AMT")
    private String amt;

    @CustomSchema(
            order = 83,
            schema = @Schema(description = "代收付累計金額", maxLength = 13, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{TOTAMT}")
    @JsonProperty("TOTAMT")
    private String totamt;

    @CustomSchema(
            order = 84,
            schema = @Schema(description = "累計筆數", maxLength = 8, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{TOTCNT}")
    @JsonProperty("TOTCNT")
    private String totcnt;

    @CustomSchema(
            order = 85,
            schema = @Schema(description = "營利事業編號或個人身分証統一編號", maxLength = 10, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{ENTPNO}")
    @JsonProperty("ENTPNO")
    private String entpno;

    @CustomSchema(
            order = 86,
            schema = @Schema(description = "營利事業編號(總公司)", maxLength = 8, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{HENTPNO}")
    @JsonProperty("HENTPNO")
    private String hentpno;

    @CustomSchema(
            order = 87,
            schema = @Schema(description = "單位中文名", maxLength = 40, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CNAME}")
    @JsonProperty("CNAME")
    private String cname;

    @CustomSchema(
            order = 88,
            schema = @Schema(description = "簡稱", maxLength = 10, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{SCNAME}")
    @JsonProperty("SCNAME")
    private String scname;

    @CustomSchema(
            order = 89,
            schema = @Schema(description = "檢查扣繳項目記號", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CDATA}")
    @JsonProperty("CDATA")
    private String cdata;

    @CustomSchema(
            order = 90,
            schema = @Schema(description = "手續費種類-依繳款金額", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{FKD}")
    @JsonProperty("FKD")
    private String fkd;

    @CustomSchema(
            order = 91,
            schema = @Schema(description = "給付中華電信每筆中介費用", maxLength = 5, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{MFEE}")
    @JsonProperty("MFEE")
    private String mfee;

    @CustomSchema(
            order = 92,
            schema = @Schema(description = "代收狀態記號", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{STOP}")
    @JsonProperty("STOP")
    private String stop;

    @CustomSchema(
            order = 93,
            schema = @Schema(description = "軋帳記號", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{AFCBV}")
    @JsonProperty("AFCBV")
    private String afcbv;

    @CustomSchema(
            order = 94,
            schema = @Schema(description = "銷帳媒體產生週期", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CYCK1}")
    @JsonProperty("CYCK1")
    private String cyck1;

    @CustomSchema(
            order = 95,
            schema = @Schema(description = "銷帳媒體產生週期日", maxLength = 2, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CYCNO1}")
    @JsonProperty("CYCNO1")
    private String cycno1;

    @CustomSchema(
            order = 96,
            schema = @Schema(description = "對帳媒體產生週期", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CYCK2}")
    @JsonProperty("CYCK2")
    private String cyck2;

    @CustomSchema(
            order = 97,
            schema = @Schema(description = "對帳媒體產生週期日", maxLength = 2, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CYCNO2}")
    @JsonProperty("CYCNO2")
    private String cycno2;

    @CustomSchema(
            order = 98,
            schema = @Schema(description = "媒體檔名", maxLength = 8, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{PUTNAME}")
    @JsonProperty("PUTNAME")
    private String putname;

    @CustomSchema(
            order = 99,
            schema = @Schema(description = "媒體種類", maxLength = 2, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{PUTTYPE}")
    @JsonProperty("PUTTYPE")
    private String puttype;

    @CustomSchema(
            order = 100,
            schema = @Schema(description = "媒體給付住址", maxLength = 40, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{PUTADDR}")
    @JsonProperty("PUTADDR")
    private String putaddr;

    @CustomSchema(
            order = 101,
            schema = @Schema(description = "即時傳輸平台代號或稅費解繳日期起迄控制", maxLength = 40, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{NETINFO}")
    @JsonProperty("NETINFO")
    private String netinfo;

    @CustomSchema(
            order = 102,
            schema = @Schema(description = "單位申請日期", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{APPDT}")
    @JsonProperty("APPDT")
    private String appdt;

    @CustomSchema(
            order = 103,
            schema = @Schema(description = "分行異動日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{UPDDT}")
    @JsonProperty("UPDDT")
    private String upddt;

    @CustomSchema(
            order = 104,
            schema = @Schema(description = "指定挑檔日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{PUTDT}")
    @JsonProperty("PUTDT")
    private String putdt;

    @CustomSchema(
            order = 105,
            schema = @Schema(description = "臨時挑檔日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{TPUTDT}")
    @JsonProperty("TPUTDT")
    private String tputdt;

    @CustomSchema(
            order = 106,
            schema = @Schema(description = "上次CYC1挑檔日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{LPUTDT}")
    @JsonProperty("LPUTDT")
    private String lputdt;

    @CustomSchema(
            order = 107,
            schema = @Schema(description = "上上次CYC1挑檔日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{LLPUTDT}")
    @JsonProperty("LLPUTDT")
    private String llputdt;

    @CustomSchema(
            order = 108,
            schema = @Schema(description = "上次CYC2挑檔日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ULPUTDT}")
    @JsonProperty("ULPUTDT")
    private String ulputdt;

    @CustomSchema(
            order = 109,
            schema = @Schema(description = "上上次CYC2挑檔日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ULLPUTDT}")
    @JsonProperty("ULLPUTDT")
    private String ullputdt;

    @CustomSchema(
            order = 110,
            schema = @Schema(description = "科目", maxLength = 14, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CLSACNO}")
    @JsonProperty("CLSACNO")
    private String clsacno;

    @CustomSchema(
            order = 111,
            schema = @Schema(description = "收付截止日期", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{STOPDATE}")
    @JsonProperty("STOPDATE")
    private String stopdate;

    @CustomSchema(
            order = 112,
            schema = @Schema(description = "收付截止時分秒", maxLength = 6, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{STOPTIME}")
    @JsonProperty("STOPTIME")
    private String stoptime;

    @CustomSchema(
            order = 113,
            schema = @Schema(description = "收付記號", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CRDB}")
    @JsonProperty("CRDB")
    private String crdb;

    @CustomSchema(
            order = 114,
            schema = @Schema(description = "臨櫃手續費用", maxLength = 6, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CFEE3}")
    @JsonProperty("CFEE3")
    private String cfee3;

    @CustomSchema(
            order = 115,
            schema = @Schema(description = "事業單位給付主辦行每月費用", maxLength = 12, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CFEE4}")
    @JsonProperty("CFEE4")
    private String cfee4;

    @CustomSchema(
            order = 116,
            schema = @Schema(description = "全繳手續費", maxLength = 5, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CFEEEB}")
    @JsonProperty("CFEEEB")
    private String cfeeeb;

    @CustomSchema(
            order = 117,
            schema = @Schema(description = "全國繳費稅管道", maxLength = 10, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{EBTYPE}")
    @JsonProperty("EBTYPE")
    private String ebtype;

    @CustomSchema(
            order = 118,
            schema = @Schema(description = "全繳連動代收類別", maxLength = 6, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{LKCODE}")
    @JsonProperty("LKCODE")
    private String lkcode;

    @CustomSchema(
            order = 119,
            schema = @Schema(description = "全繳連動代收類別", maxLength = 3, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{PWTYPE}")
    @JsonProperty("PWTYPE")
    private String pwtype;
}
