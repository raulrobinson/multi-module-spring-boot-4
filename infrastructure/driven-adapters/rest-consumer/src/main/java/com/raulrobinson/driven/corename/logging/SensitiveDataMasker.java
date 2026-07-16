package com.raulrobinson.driven.corename.logging;

import com.raulrobinson.driven.corename.config.MultiExternalApiPropsConfig;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enmascara valores sensibles en payloads JSON (recursivo), headers y query
 * params según la lista de campos y niveles del application.yml.
 *
 * <p>Jackson 3 ({@code tools.jackson}) — codec nativo de Boot 4 / Spring 7.
 * {@code maskHeaders} recibe {@link HttpHeaders} directamente: en Spring
 * Framework 7 esa clase YA NO implementa {@code Map}/{@code MultiValueMap}.</p>
 */
public final class SensitiveDataMasker {

    private final ObjectMapper mapper;
    private final Map<String, MaskingLevel> fieldLevels;
    private final int maxPayloadLength;

    public SensitiveDataMasker(ObjectMapper mapper, MultiExternalApiPropsConfig.Logging loggingProps) {
        this.mapper = mapper;
        this.maxPayloadLength = loggingProps.maxPayloadLength() > 0 ? loggingProps.maxPayloadLength() : 4096;
        this.fieldLevels = loggingProps.sensitiveFields().stream()
                .collect(Collectors.toUnmodifiableMap(
                        f -> f.name().toLowerCase(Locale.ROOT),
                        MultiExternalApiPropsConfig.Logging.SensitiveField::level,
                        (a, b) -> a));
    }

    /** Enmascara un objeto serializándolo a JSON. Nunca lanza: logging jamás rompe la llamada. */
    public String maskObject(Object payload) {
        if (payload == null) return "null";
        try {
            JsonNode tree = payload instanceof String s
                    ? mapper.readTree(s)
                    : mapper.valueToTree(payload);
            maskNode(tree);
            return truncate(mapper.writeValueAsString(tree));
        } catch (Exception e) {
            return "<unparseable payload: " + payload.getClass().getSimpleName() + ">";
        }
    }

    /** Enmascara headers (Spring 7: HttpHeaders ya no es Map; se usa su forEach propio). */
    public String maskHeaders(HttpHeaders headers) {
        var sb = new StringBuilder("{");
        headers.forEach((name, values) -> {
            var level = fieldLevels.get(name.toLowerCase(Locale.ROOT));
            values.forEach(v -> sb.append(name).append('=')
                    .append(level == null ? v : level.mask(v)).append(", "));
        });
        if (sb.length() > 1) sb.setLength(sb.length() - 2);
        return sb.append('}').toString();
    }

    /** Enmascara un valor individual (query param / path param) si su nombre es sensible. */
    public String maskParam(String paramName, String value) {
        var level = fieldLevels.get(paramName.toLowerCase(Locale.ROOT));
        return level == null ? value : level.mask(value);
    }

    private void maskNode(JsonNode node) {
        if (node instanceof ObjectNode obj) {
            // Jackson 3: fieldNames() fue removido; properties() es la API vigente.
            for (var entry : List.copyOf(obj.properties())) {
                var level = fieldLevels.get(entry.getKey().toLowerCase(Locale.ROOT));
                if (level != null && entry.getValue().isValueNode()) {
                    obj.put(entry.getKey(), level.mask(entry.getValue().asString()));
                } else {
                    maskNode(entry.getValue());
                }
            }
        } else if (node instanceof ArrayNode arr) {
            arr.forEach(this::maskNode);
        }
    }

    private String truncate(String s) {
        return s.length() <= maxPayloadLength
                ? s
                : s.substring(0, maxPayloadLength) + "...<truncated " + (s.length() - maxPayloadLength) + " chars>";
    }
}
