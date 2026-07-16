package com.raulrobinson.api.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class OpenApiSchemasConfig {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = OpenApiResponseConfig.API_PATH,
                    method = RequestMethod.POST,
                    produces = MediaType.APPLICATION_JSON_VALUE,
                    operation = @Operation(
                            operationId = "getXyz",
                            summary = "Consultar todos los xyz...",
                            description = OpenApiResponseConfig.API_DESCRIPTION,
                            tags = {"xyzService"},
                            parameters = {
                                    @Parameter(
                                            name = "Authorization",
                                            in = ParameterIn.HEADER,
                                            required = true,
                                            description = "Bearer access token (OAuth 2.0 / OIDC)",
                                            example = "Bearer eyJhbGc.bbb.ccc"
                                    ),
                                    @Parameter(
                                            name = "X-Correlation-Id",
                                            in = ParameterIn.HEADER,
                                            required = false,
                                            description = "End-to-end correlation id (UUID). If absent, the API may generate one.",
                                            example = "123e4567-e89b-12d3-a456-426614174000"
                                    ),
                                    @Parameter(
                                            name = "X-Source-Bank",
                                            in = ParameterIn.HEADER,
                                            required = true,
                                            description = "Originating bank code (e.g., CO01, US01).",
                                            example = "CO01"
                                    ),
                                    @Parameter(
                                            name = "X-Destination-Bank",
                                            in = ParameterIn.HEADER,
                                            required = false,
                                            description = "Destination bank code when the flow routes to a different bank (optional).",
                                            example = "CO01"
                                    ),
                                    @Parameter(
                                            name = "X-Application-Id",
                                            in = ParameterIn.HEADER,
                                            required = true,
                                            description = "Consuming application id. Can be derived from the token but must be present.",
                                            example = "srv-itintb"
                                    ),
                                    @Parameter(
                                            name = "X-Caller-Service",
                                            in = ParameterIn.HEADER,
                                            required = false,
                                            description = "Logical name of the composing/experience API when multi-hop composition occurs.",
                                            example = "sample-caller-service"
                                    ),
                                    @Parameter(
                                            name = "Channel",
                                            in = ParameterIn.HEADER,
                                            required = false,
                                            description = "Interaction channel (e.g., web, mobile, partner, atm, branch, ivr).",
                                            example = "web"
                                    ),
                                    @Parameter(
                                            name = "Accept-Language",
                                            in = ParameterIn.HEADER,
                                            required = false,
                                            description = "Preferred language for messages (ISO 639-1, e.g., es, en).",
                                            example = "es"
                                    )
                            },
                            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                    required = false,
                                    content = @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            examples = @ExampleObject(
                                                    name = "Request",
                                                    value = OpenApiRequestConfig.GET_REQUEST
                                            )
                                    )
                            ),
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Respuesta exitosa de la API",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "SUCCESS",
                                                            value = OpenApiResponseConfig.SUCCESS
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "400",
                                            description = "Los parámetros o formato son inválidos",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "BAD_REQUEST",
                                                            value = OpenApiResponseConfig.BAD_REQUEST
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "401",
                                            description = "Cliente no autenticado o credenciales inválidas",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "UNAUTHORIZED",
                                                            value = OpenApiResponseConfig.UNAUTHORIZED
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "403",
                                            description = "Cliente autenticado pero sin permisos",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "FORBIDDEN",
                                                            value = OpenApiResponseConfig.FORBIDDEN
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "404",
                                            description = "Recurso solicitado no existe",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "NOT_FOUND",
                                                            value = OpenApiResponseConfig.NOT_FOUND
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "422",
                                            description = "Datos válidos en formato pero con errores de negocio",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "UNPROCESSABLE_ENTITY",
                                                            value = OpenApiResponseConfig.UNPROCESSABLE_ENTITY
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "500",
                                            description = "Error inesperado en el servicio",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "INTERNAL_SERVER_ERROR",
                                                            value = OpenApiResponseConfig.INTERNAL_SERVER_ERROR
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "502",
                                            description = "Falla de un servicio intermedio/gateway",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "BAD_GATEWAY",
                                                            value = OpenApiResponseConfig.BAD_GATEWAY
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "503",
                                            description = "Servicio temporalmente no disponible",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "SERVICE_UNAVAILABLE",
                                                            value = OpenApiResponseConfig.SERVICE_UNAVAILABLE
                                                    )
                                            )
                                    ),
                                    @ApiResponse(
                                            responseCode = "504",
                                            description = "Tiempo de espera excedido en servicio dependiente",
                                            content = @Content(
                                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                    examples = @ExampleObject(
                                                            name = "GATEWAY_TIMEOUT",
                                                            value = OpenApiResponseConfig.GATEWAY_TIMEOUT
                                                    )
                                            )
                                    )
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> documentedRoutes() {
        return route()
                .GET("/__openapi-doc-only", req -> ServerResponse
                        .notFound().build())
                .build();
    }
}
