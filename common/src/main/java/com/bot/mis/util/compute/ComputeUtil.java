/* (C) 2024 */
package com.bot.mis.util.compute;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class ComputeUtil {

    private static final int SQRT_COMPUTING_SCALE = 100;

    private ComputeUtil() {
        // YOU SHOULD USE @Autowired ,NOT new ComputeUtil()
    }

    public BigDecimal sqrt(BigDecimal value) {
        BigDecimal x0 = BigDecimal.ZERO;
        BigDecimal x1 = BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
        while (!x0.equals(x1)) {
            x0 = x1;
            x1 = value.divide(x0, SQRT_COMPUTING_SCALE, RoundingMode.HALF_UP);
            x1 = x1.add(x0);
            x1 = x1.divide(BigDecimal.valueOf(2), SQRT_COMPUTING_SCALE, RoundingMode.HALF_UP);
        }
        return x1;
    }
}
