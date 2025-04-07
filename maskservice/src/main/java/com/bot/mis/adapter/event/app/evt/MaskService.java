/* (C) 2025 */
package com.bot.mis.adapter.event.app.evt;

import com.bot.mis.adapter.in.svc.MaskService_I;
import com.bot.txcontrol.adapter.RequestSvcCase;
import com.bot.txcontrol.adapter.event.TradeEventCase;
import com.bot.txcontrol.buffer.AggregateBuffer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Slf4j
@Component("MaskService")
@Scope("prototype")
public class MaskService extends TradeEventCase<RequestSvcCase> {

    private MaskService_I maskservice_I;

    public MaskService(MaskService_I source, AggregateBuffer aggregateBuffer) {
        super(source, aggregateBuffer);
        this.maskservice_I = source;
        this.setAggregateBuffer(aggregateBuffer);
    }
}
