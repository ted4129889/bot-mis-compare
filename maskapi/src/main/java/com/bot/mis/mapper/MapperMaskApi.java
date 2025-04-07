/* (C) 2025 */
package com.bot.mis.mapper;

import com.bot.mis.adapter.in.api.CL_BATCH_I;
import com.bot.mis.adapter.in.svc.MaskService_I;
import com.bot.txcontrol.mapper.MapperCase;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface MapperMaskApi extends MapperCase {

    MaskService_I mapperMaskService(CL_BATCH_I cL_BATCH_I);
}
