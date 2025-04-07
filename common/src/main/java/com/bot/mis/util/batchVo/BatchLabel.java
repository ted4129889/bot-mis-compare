/* (C) 2024 */
package com.bot.mis.util.batchVo;

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
@Component("BatchLabel")
@Scope("prototype")
public class BatchLabel extends RequestBaseSvc {

    @CustomSchema(
            order = 68,
            schema = @Schema(description = "批次指定日期", maxLength = 8, type = "NUMBER"),
            decimal = 0,
            align = "RIGHT",
            pad = "ZERO")
    @NotEmpty(message = "{INPUT_DATE}")
    @JsonProperty("INPUT_DATE")
    private String inputDate;
}
