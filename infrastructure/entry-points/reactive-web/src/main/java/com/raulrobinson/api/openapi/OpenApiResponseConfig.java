package com.raulrobinson.api.openapi;

public final class OpenApiResponseConfig {

    public static final String ERROR_BASE_URL = "{{ERROR_BASE_URL}}";
    public static final String API_DESCRIPTION = "{{API_DESCRIPTION}}";
    public static final String API_PATH = "{{API_PATH}}";

    public static final String SUCCESS = """
    { }
    """;

    public static final String BAD_REQUEST = """
    {
      "type": "{{ERROR_BASE_URL}}/bad-request",
      "title": "Bad Request",
      "status": 400,
      "detail": "Los parámetros o formato son inválidos",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String UNAUTHORIZED = """
    {
      "type": "{{ERROR_BASE_URL}}/unauthorized",
      "title": "Unauthorized",
      "status": 401,
      "detail": "Cliente no autenticado o credenciales inválidas",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String FORBIDDEN = """
    {
      "type": "{{ERROR_BASE_URL}}/forbidden",
      "title": "Forbidden",
      "status": 403,
      "detail": "Cliente autenticado pero sin permisos",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String NOT_FOUND = """
    {
      "type": "{{ERROR_BASE_URL}}/not-found",
      "title": "Not Found",
      "status": 404,
      "detail": "Recurso solicitado no existe",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String UNPROCESSABLE_ENTITY = """
    {
      "type": "{{ERROR_BASE_URL}}/unprocessable-entity",
      "title": "Unprocessable Entity",
      "status": 422,
      "detail": "Datos válidos en formato pero con errores de negocio",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String INTERNAL_SERVER_ERROR = """
    {
      "type": "{{ERROR_BASE_URL}}/internal-server-error",
      "title": "Internal Server Error",
      "status": 500,
      "detail": "Error inesperado en el servicio",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String BAD_GATEWAY = """
    {
      "type": "{{ERROR_BASE_URL}}/bad-gateway",
      "title": "Bad Gateway",
      "status": 502,
      "detail": "Falla de un servicio intermedio/gateway",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String SERVICE_UNAVAILABLE = """
    {
      "type": "{{ERROR_BASE_URL}}/service-unavailable",
      "title": "Service Unavailable",
      "status": 503,
      "detail": "Servicio temporalmente no disponible",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    public static final String GATEWAY_TIMEOUT = """
    {
      "type": "{{ERROR_BASE_URL}}/gateway-timeout",
      "title": "Gateway Timeout",
      "status": 504,
      "detail": "Tiempo de espera excedido en servicio dependiente",
      "instance": "/business/v1",
      "correlationId": "4f7b-abc123",
      "timestamp": "2025-08-14T20:15:00Z"
    }
    """;

    private OpenApiResponseConfig() {
    }
}