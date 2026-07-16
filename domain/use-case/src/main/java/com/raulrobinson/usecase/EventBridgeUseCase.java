package com.raulrobinson.usecase;

import com.raulrobinson.model.*;
import com.raulrobinson.ports.in.IEventBridgeUseCase;
import com.raulrobinson.ports.out.EventBridgeGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventBridgeUseCase implements IEventBridgeUseCase {

    private final EventBridgeGateway eventBridge;

    @Override
    public Mono<EventBusesResult> listBuses(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region) {
        return eventBridge.listBuses(accessKeyId, secretAccessKey, sessionToken, region);
    }

    @Override
    public Mono<EventRulesResult> listRules(String accessKeyId, String secretAccessKey,
                                            String sessionToken, String region, String busName) {
        return eventBridge.listRules(accessKeyId, secretAccessKey, sessionToken, region, busName);
    }

    @Override
    public Mono<EventRuleDetail> getRuleDetail(String accessKeyId, String secretAccessKey,
                                               String sessionToken, String region,
                                               String busName, String ruleName) {
        return eventBridge.getRuleDetail(accessKeyId, secretAccessKey, sessionToken, region, busName, ruleName);
    }

    @Override
    public Mono<EventPutResult> putEvent(String accessKeyId, String secretAccessKey,
                                         String sessionToken, String region,
                                         String busName, String source,
                                         String detailType, String detail,
                                         List<String> resources) {
        return eventBridge.putEvent(accessKeyId, secretAccessKey, sessionToken, region,
                busName, source, detailType, detail, resources);
    }
}
