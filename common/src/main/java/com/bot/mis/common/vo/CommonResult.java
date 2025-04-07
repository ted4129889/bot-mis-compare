/* (C) 2024 */
package com.bot.mis.common.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@Scope("prototype")
public class CommonResult {
    /** 回應代號 RTNCD 9(02) */
    private int rtncd = 0;

    /** 錯誤代號 ERRCD X(05) */
    private String errcd = "";

    /** 錯誤編號 ERRNO 9(03) */
    private int errno = 0;

    /** 錯誤訊息 ERRMSG X(20) */
    private String errmsg = "";

    // ** gs61Supmkt,gs61Newvr,錯誤記號
    private int errcdfg = 0;
}
