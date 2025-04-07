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
@Component("FileC012")
@Scope("prototype")
public class FileC012 extends RequestBaseSvc {

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
            schema = @Schema(description = "主辦分行", maxLength = 3, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{PBRNO}")
    @JsonProperty("PBRNO")
    private String pbrno;

    @CustomSchema(
            order = 70,
            schema = @Schema(description = "單位中文名", maxLength = 60, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{CNAME}")
    @JsonProperty("CNAME")
    private String cname;

    @CustomSchema(
            order = 71,
            schema = @Schema(description = "營利事業編號", maxLength = 15, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{ENTPNO}")
    @JsonProperty("ENTPNO")
    private String entpno;

    @CustomSchema(
            order = 72,
            schema = @Schema(description = "總公司統編", maxLength = 8, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{HENTPNO}")
    @JsonProperty("HENTPNO")
    private String hentpno;

    @CustomSchema(
            order = 73,
            schema = @Schema(description = "代收狀態（異動前）", maxLength = 1, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{STOP}")
    @JsonProperty("STOP")
    private String stop;

    @CustomSchema(
            order = 74,
            schema = @Schema(description = "建檔日", maxLength = 8, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{DATE}")
    @JsonProperty("DATE")
    private String date;

    @CustomSchema(
            order = 75,
            schema = @Schema(description = "存入戶號", maxLength = 12, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ACTNO}")
    @JsonProperty("ACTNO")
    private String actno;

    @CustomSchema(
            order = 76,
            schema = @Schema(description = "主辦分行中文", maxLength = 30, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{PBRNONM}")
    @JsonProperty("PBRNONM")
    private String pbrnonm;

    @CustomSchema(
            order = 77,
            schema = @Schema(description = "保留", maxLength = 138, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{FILLER}")
    @JsonProperty("FILLER")
    private String filler;
}
