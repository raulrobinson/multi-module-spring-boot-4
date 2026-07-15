package com.raulrobinson.model;

import java.util.List;
import java.util.Map;

/**
 * Respuesta genérica del backend externo. Contrato LIMPIO:
 * solo java.util — sin Jackson, sin Spring.
 *
 * @param status  código HTTP
 * @param headers headers de la respuesta (inmutables)
 * @param body    payload ya deserialization al tipo del dominio;
 *                null si el backend no devolvió cuerpo o T = Void
 */
public record ApiResponse<T>(
        int status,
        Map<String, List<String>> headers,
        T body
) {}
