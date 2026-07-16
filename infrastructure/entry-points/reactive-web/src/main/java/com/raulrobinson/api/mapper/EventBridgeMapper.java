package com.raulrobinson.api.mapper;

import com.raulrobinson.api.dto.*;
import com.raulrobinson.model.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventBridgeMapper {

    EventBusResponse toBusResponse(EventBusItem model);

    EventBusesResponse toBusesResponse(EventBusesResult model);

    EventRuleResponse toRuleResponse(EventRuleItem model);

    EventRulesResponse toRulesResponse(EventRulesResult model);

    EventRuleTargetResponse toTargetResponse(EventRuleTarget model);

    EventRuleDetailResponse toRuleDetailResponse(EventRuleDetail model);

    EventPutEntryResponse toPutEntryResponse(EventPutEntry model);

    EventPutResponse toPutResponse(EventPutResult model);
}
