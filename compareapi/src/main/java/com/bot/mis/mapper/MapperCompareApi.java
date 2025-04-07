/* (C) 2025 */
package com.bot.mis.mapper;

import com.bot.mis.adapter.in.api.CL_BATCH_I;
import com.bot.mis.adapter.in.svc.CompareService_I;
import com.bot.txcontrol.mapper.MapperCase;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface MapperCompareApi extends MapperCase {

    CompareService_I mapperCompareService(CL_BATCH_I cL_BATCH_I);
}
