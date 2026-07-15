package com.raulrobinson.model;

/**
 * Verbo HTTP propio del contrato de dominio.
 * El verbo de cada operación se define en el YAML (Endpoint.method),
 * no lo decide el caso de uso invocation.
 */
public enum HttpVerb {
    GET, POST, PUT, PATCH, DELETE
}
