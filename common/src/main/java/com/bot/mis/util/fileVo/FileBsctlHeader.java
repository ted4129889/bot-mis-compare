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
@Component("FileBsctlHeader")
@Scope("prototype")
public class FileBsctlHeader extends RequestBaseSvc {

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
            schema = @Schema(description = "CODE", maxLength = 7, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CODE}")
    @JsonProperty("CODE")
    private String code;

    @CustomSchema(
            order = 70,
            schema = @Schema(description = "MTYPE", maxLength = 3, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{MTYPE}")
    @JsonProperty("MTYPE")
    private String mtype;

    @CustomSchema(
            order = 71,
            schema = @Schema(description = "ENTPNO", maxLength = 10, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{ENTPNO}")
    @JsonProperty("ENTPNO")
    private String entpno;

    @CustomSchema(
            order = 72,
            schema = @Schema(description = "RDAY", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{RDAY}")
    @JsonProperty("RDAY")
    private String rday;

    @CustomSchema(
            order = 73,
            schema = @Schema(description = "CDFLG", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CDFLG}")
    @JsonProperty("CDFLG")
    private String cdflg;

    @CustomSchema(
            order = 74,
            schema = @Schema(description = "REMARK", maxLength = 26, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{REMARK}")
    @JsonProperty("REMARK")
    private String remark;

    @CustomSchema(
            order = 75,
            schema = @Schema(description = "CURCD", maxLength = 2, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CURCD}")
    @JsonProperty("CURCD")
    private String curcd;

    @CustomSchema(
            order = 76,
            schema = @Schema(description = "ADTOTCNT", maxLength = 6, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ADTOTCNT}")
    @JsonProperty("ADTOTCNT")
    private String adtotcnt;

    @CustomSchema(
            order = 77,
            schema = @Schema(description = "ADTOTAMT", maxLength = 11, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ADTOTAMT}")
    @JsonProperty("ADTOTAMT")
    private String adtotamt;

    @CustomSchema(
            order = 78,
            schema = @Schema(description = "ACTOTCNT", maxLength = 6, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ACTOTCNT}")
    @JsonProperty("ACTOTCNT")
    private String actotcnt;

    @CustomSchema(
            order = 79,
            schema = @Schema(description = "ACTOTAMT", maxLength = 11, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ACTOTAMT}")
    @JsonProperty("ACTOTAMT")
    private String actotamt;

    @CustomSchema(
            order = 80,
            schema = @Schema(description = "FDTOTCNT", maxLength = 6, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{FDTOTCNT}")
    @JsonProperty("FDTOTCNT")
    private String fdtotcnt;

    @CustomSchema(
            order = 81,
            schema = @Schema(description = "FDTOTAMT", maxLength = 11, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{FDTOTAMT}")
    @JsonProperty("FDTOTAMT")
    private String fdtotamt;

    @CustomSchema(
            order = 82,
            schema = @Schema(description = "FCTOTCNT", maxLength = 6, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{FCTOTCNT}")
    @JsonProperty("FCTOTCNT")
    private String fctotcnt;

    @CustomSchema(
            order = 83,
            schema = @Schema(description = "FCTOTAMT", maxLength = 11, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{FCTOTAMT}")
    @JsonProperty("FCTOTAMT")
    private String fctotamt;

    @CustomSchema(
            order = 84,
            schema = @Schema(description = "STATUS", maxLength = 1, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{STATUS}")
    @JsonProperty("STATUS")
    private String status;

    @CustomSchema(
            order = 85,
            schema = @Schema(description = "USERDATA", maxLength = 48, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{USERDATA}")
    @JsonProperty("USERDATA")
    private String userdata;

    @CustomSchema(
            order = 86,
            schema = @Schema(description = "UPLBHSEQ", maxLength = 13, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{UPLBHSEQ}")
    @JsonProperty("UPLBHSEQ")
    private String uplbhseq;

    @CustomSchema(
            order = 87,
            schema = @Schema(description = "MDTYPE", maxLength = 1, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{MDTYPE}")
    @JsonProperty("MDTYPE")
    private String mdtype;

    @CustomSchema(
            order = 88,
            schema = @Schema(description = "CHANNEL", maxLength = 1, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CHANNEL}")
    @JsonProperty("CHANNEL")
    private String channel;

    @CustomSchema(
            order = 89,
            schema = @Schema(description = "CHKFG", maxLength = 1, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CHKFG}")
    @JsonProperty("CHKFG")
    private String chkfg;

    @CustomSchema(
            order = 90,
            schema = @Schema(description = "FILLER", maxLength = 2, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{FILLER}")
    @JsonProperty("FILLER")
    private String filler;
}
