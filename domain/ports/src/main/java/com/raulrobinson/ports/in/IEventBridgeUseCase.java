package com.raulrobinson.ports.in;

import com.raulrobinson.model.*;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IEventBridgeUseCase {

    Mono<EventBusesResult> listBuses(String accessKeyId, String secretAccessKey,
                                     String sessionToken, String region);

    Mono<EventRulesResult> listRules(String accessKeyId, String secretAccessKey,
                                     String sessionToken, String region, String busName);

    Mono<EventRuleDetail> getRuleDetail(String accessKeyId, String secretAccessKey,
                                        String sessionToken, String region,
                                        String busName, String ruleName);

    Mono<EventPutResult> putEvent(String accessKeyId, String secretAccessKey,
                                  String sessionToken, String region,
                                  String busName, String source,
                                  String detailType, String detail,
                                  List<String> resources);
}
