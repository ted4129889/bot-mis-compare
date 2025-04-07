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
@Component("FileC016")
@Scope("prototype")
public class FileC016 extends RequestBaseSvc {

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
            schema = @Schema(description = "代收日", maxLength = 7, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{EDATE}")
    @JsonProperty("EDATE")
    private String edate;

    @CustomSchema(
            order = 70,
            schema = @Schema(description = "繳費金額", maxLength = 60, type = "CHAR"),
            align = "LEFT",
            pad = "SPACE")
    @NotEmpty(message = "{AMT}")
    @JsonProperty("AMT")
    private String amt;
}
