/* (C) 2024 */
package com.bot.mis.util.err;

import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.util.dump.ExceptionDump;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class ErrUtil {

    private ErrUtil() {
        // YOU SHOULD USE @Autowired ,NOT new ErrUtil()
    }

    public String printErrorLog(String errorCode, String errorMsg) {
        ApLogHelper.error(
                log,
                false,
                LogType.NORMAL.getCode(),
                "ErrorCode={},ErrorMsg={}.",
                errorCode,
                ExceptionDump.exception2String(new Throwable(errorMsg)));
        return errorMsg;
    }
}
