/* (C) 2024 */
package com.bot.mis.util.page;

import com.bot.txcontrol.buffer.TxCom;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import com.bot.txcontrol.util.parse.Parse;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class PageQueryUtil {

    @Autowired private Parse parse;

    private Map<String, String> attrMap;

    @Getter private int pageNo;

    @Getter private int pageSize;

    @Getter private boolean isBIS;

    private PageQueryUtil() {
        // YOU SHOULD USE @Autowired ,NOT new PageQueryUtil()
    }

    private void setPageNo(Map<String, String> rqAttrMap) {
        pageNo = 0;
        String rqPageNo = "";
        String rqPageSize = "";
        if (rqAttrMap.containsKey("LAZYQUERYTOKENID")
                && !rqAttrMap.get("LAZYQUERYTOKENID").isEmpty()) {
            isBIS = true;
            rqPageNo = rqAttrMap.get("PAGENO");
            if (!Objects.isNull(rqPageNo) && parse.isNumeric(rqPageNo)) {
                pageNo = Math.max(parse.string2Integer(rqPageNo) - 1, 0);
            }
            rqPageSize = rqAttrMap.get("PAGESIZE");
            if (!Objects.isNull(rqPageSize) && parse.isNumeric(rqPageSize)) {
                pageSize = parse.string2Integer(rqPageSize);
                if (pageSize > 500) {
                    pageSize = 500;
                }
                if (pageSize <= 0) {
                    pageSize = 1;
                }
            }
        } else {
            isBIS = false;
        }
        ApLogHelper.info(
                log, false, LogType.APLOG.getCode(), "rqPageNo={},pageNo={}", rqPageNo, pageNo);
        ApLogHelper.info(
                log,
                false,
                LogType.APLOG.getCode(),
                "rqPageSize={},pageSize={}",
                rqPageSize,
                pageSize);
    }

    public void setMaxCountToTxCom(TxCom txCom, Long maxCount) {
        int totalCount = 0;
        try {
            totalCount = Math.toIntExact(maxCount);
        } catch (ArithmeticException e) {
            // 整數溢出 改用字串截斷
            String totalCountInString = String.valueOf(maxCount);
            totalCountInString = totalCountInString.substring(totalCountInString.length() - 8);
            totalCount = parse.string2Integer(totalCountInString);
        }
        txCom.setTotalCount(totalCount);
    }

    public void init(Map<String, String> rqAttrMap) {
        this.setPageNo(rqAttrMap);
    }
}
