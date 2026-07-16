package com.raulrobinson.ports.in;

import com.raulrobinson.model.MultiModel;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;

public interface IMultiUseCase {
    Mono<Map<String, Object>> execute(String operation, MultiModel dto, Context ctx);
}
