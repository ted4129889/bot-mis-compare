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
@Component("FileBsctlDetail")
@Scope("prototype")
public class FileBsctlDetail extends RequestBaseSvc {

    @CustomSchema(
            order = 68,
            schema = @Schema(description = "DATYPE", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{DATYPE}")
    @JsonProperty("DATYPE")
    private String datype;

    @CustomSchema(
            order = 69,
            schema = @Schema(description = "SEQNO", maxLength = 6, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{SEQNO}")
    @JsonProperty("SEQNO")
    private String seqno;

    @CustomSchema(
            order = 70,
            schema = @Schema(description = "FILLER1", maxLength = 4, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{FILLER1}")
    @JsonProperty("FILLER1")
    private String filler1;

    @CustomSchema(
            order = 71,
            schema = @Schema(description = "ACTNO1", maxLength = 16, type = "CHAR"),
            decimal = 0,
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{ACTNO1}")
    @JsonProperty("ACTNO1")
    private String actno1;

    @CustomSchema(
            order = 72,
            schema = @Schema(description = "CRDB", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CRDB}")
    @JsonProperty("CRDB")
    private String crdb;

    @CustomSchema(
            order = 73,
            schema = @Schema(description = "TXAMT", maxLength = 11, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{TXAMT}")
    @JsonProperty("TXAMT")
    private String txamt;

    @CustomSchema(
            order = 74,
            schema = @Schema(description = "RDAY1", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{RDAY1}")
    @JsonProperty("RDAY1")
    private String rday1;

    @CustomSchema(
            order = 75,
            schema = @Schema(description = "ERDAY1", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ERDAY1}")
    @JsonProperty("ERDAY1")
    private String erday1;

    @CustomSchema(
            order = 76,
            schema = @Schema(description = "REMARK1", maxLength = 26, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{REMARK1}")
    @JsonProperty("REMARK1")
    private String remark1;

    @CustomSchema(
            order = 77,
            schema = @Schema(description = "ID", maxLength = 10, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{ID}")
    @JsonProperty("ID")
    private String id;

    @CustomSchema(
            order = 78,
            schema = @Schema(description = "STCD2", maxLength = 2, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{STCD2}")
    @JsonProperty("STCD2")
    private String stcd2;

    @CustomSchema(
            order = 79,
            schema = @Schema(description = "PNO1", maxLength = 20, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{PNO1}")
    @JsonProperty("PNO1")
    private String pno1;

    @CustomSchema(
            order = 80,
            schema = @Schema(description = "PNO2", maxLength = 20, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{PNO2}")
    @JsonProperty("PNO2")
    private String pno2;

    @CustomSchema(
            order = 81,
            schema = @Schema(description = "USRDATA", maxLength = 60, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{USRDATA}")
    @JsonProperty("USRDATA")
    private String usrdata;

    @CustomSchema(
            order = 82,
            schema = @Schema(description = "CHKFG1", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CHKFG1}")
    @JsonProperty("CHKFG1")
    private String chkfg1;

    @CustomSchema(
            order = 83,
            schema = @Schema(description = "CHKID", maxLength = 1, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CHKID}")
    @JsonProperty("CHKID")
    private String chkid;

    @CustomSchema(
            order = 84,
            schema = @Schema(description = "FILLER2", maxLength = 5, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{FILLER2}")
    @JsonProperty("FILLER2")
    private String filler2;
}
