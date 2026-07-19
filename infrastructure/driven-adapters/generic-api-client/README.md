# generic-api-client

Cliente HTTP reutilizable de infraestructura para Spring Boot 4, Spring WebFlux y Jackson 3.

## Regla de dependencia

Solo los adaptadores específicos deben depender de este módulo:

```text
use-case -> puerto de negocio <- adaptador específico -> generic-api-client -> WebClient
```

No agregue esta dependencia a `model`, `ports` ni `use-case`.

## Integración Gradle

Copie el directorio a:

```text
infrastructure/driven-adapters/generic-api-client
```

En `settings.gradle`:

```gradle
include 'generic-api-client'
project(':generic-api-client').projectDir =
        file('infrastructure/driven-adapters/generic-api-client')
```

En un adaptador específico:

```gradle
dependencies {
    implementation project(':model')
    implementation project(':ports')
    implementation project(':generic-api-client')
}
```

## Uso

```java
ApiRequest request = ApiRequest.builder()
        .operation("retrieve-account")
        .pathParams(Map.of("accountNumber", accountNumber))
        .headers(Map.of("Authorization", authorization))
        .correlationId(correlationId)
        .build();

return genericApiClient.execute(request, AccountResponseDto.class)
        .map(ApiResponse::body)
        .map(mapper::toDomain);
```

Los errores HTTP y técnicos se mantienen en este módulo. El adaptador específico debe traducirlos a excepciones del dominio cuando su semántica sea conocida, por ejemplo `404 -> AccountNotFoundException`.

## Resiliencia

El orden aplicado es:

```text
TimeLimiter por intento -> Retry selectivo -> Circuit Breaker
```

- Los `4xx` no se reintentan ni alimentan el circuito.
- Los errores de deserialización no se reintentan ni alimentan el circuito.
- Los `5xx`, errores de conexión y timeout sí son recuperables.
- El Circuit Breaker registra el resultado final de la operación, no cada intento.
