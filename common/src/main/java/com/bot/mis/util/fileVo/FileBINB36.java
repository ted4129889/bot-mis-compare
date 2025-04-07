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
@Component("FileBINB36")
@Scope("prototype")
public class FileBINB36 extends RequestBaseSvc {

    @CustomSchema(
            order = 68,
            schema = @Schema(description = "代收日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{TBSDY}")
    @JsonProperty("TBSDY")
    private String tbsdy;

    @CustomSchema(
            order = 69,
            schema = @Schema(description = "主辦行", maxLength = 3, type = "NUMBER"),
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{PBRNO}")
    @JsonProperty("PBRNO")
    private String pbrno;

    @CustomSchema(
            order = 70,
            schema = @Schema(description = "幣別代號", maxLength = 2, type = "NUMBER"),
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CURCD}")
    @JsonProperty("CURCD")
    private String curcd;

    @CustomSchema(
            order = 71,
            schema = @Schema(description = "ONAPNO", maxLength = 2, type = "NUMBER"),
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{ONAPNO}")
    @JsonProperty("ONAPNO")
    private String onapno;

    @CustomSchema(
            order = 72,
            schema = @Schema(description = "累計代收筆數", maxLength = 5, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{DBCNT}")
    @JsonProperty("DBCNT")
    private String dbcnt;

    @CustomSchema(
            order = 73,
            schema = @Schema(description = "累計代收手續費", maxLength = 15, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{DBAMT}")
    @JsonProperty("DBAMT")
    private String dbamt;

    @CustomSchema(
            order = 74,
            schema = @Schema(description = "累計代收筆數", maxLength = 5, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CRCNT}")
    @JsonProperty("CRCNT")
    private String crcnt;

    @CustomSchema(
            order = 75,
            schema = @Schema(description = "累計代收手續費", maxLength = 15, type = "NUMBER"),
            decimal = 2,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{CRAMT}")
    @JsonProperty("CRAMT")
    private String cramt;
}
