/* (C) 2025 */
package com.bot.mis.adapter.event.app.evt;

import com.bot.mis.adapter.in.svc.CompareService_I;
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
@Component("CompareService")
@Scope("prototype")
public class CompareService extends TradeEventCase<RequestSvcCase> {

    private CompareService_I compareservice_I;

    public CompareService(CompareService_I source, AggregateBuffer aggregateBuffer) {
        super(source, aggregateBuffer);
        this.compareservice_I = source;
        this.setAggregateBuffer(aggregateBuffer);
    }
}
