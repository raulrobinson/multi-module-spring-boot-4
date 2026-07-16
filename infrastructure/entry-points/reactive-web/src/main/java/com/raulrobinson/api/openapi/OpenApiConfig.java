package com.raulrobinson.api.openapi;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${info.app.url}")
    private String url;

    @Bean
    public OpenAPI openAPI(
            @Value("${info.app.name}") String title,
            @Value("${info.app.description}") String description,
            @Value("${info.app.version}") String appVersion
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(appVersion)
                        .description(description + """
                                <br><br>
                                <b>Corporate Headers:</b><br>
                                - <code>Authorization</code> (required)<br>
                                - <code>X-Source-Bank</code> (required)<br>
                                - <code>X-Application-Id</code> (required)<br>
                                - <code>Accept-Language</code> (optional)<br>
                                - <code>X-Caller-Service</code> (optional)<br>
                                - <code>Channel</code> (optional)<br>
                                - <code>X-Correlation-Id</code> (optional)<br>
                                - <code>X-Application-User</code> (optional)<br>
                                - <code>X-Destination-Bank</code> (optional)
                                """)
                        .contact(new Contact()
                                .name("Integration Team Raul Bolivar Services")
                                .email("devops@raulrobinson.com")))
                .servers(List.of(
                        new Server().url(url).description("Local"),
                        new Server()
                                .url("https://{environment}.raulrobinson.com")
                                .description("Integration")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer Token for Microservices Authentication"))
                        .addParameters("SourceBankHeader",
                                requiredHeader("X-Source-Bank", "CO01", "Originating bank code"))
                        .addParameters("ApplicationIdHeader",
                                requiredHeader("X-Application-Id", "app-id", "Client application identifier"))
                        .addParameters("AcceptLanguageHeader",
                                optionalHeader("Accept-Language", "es", "Response language"))
                        .addParameters("CallerServiceHeader",
                                optionalHeader("X-Caller-Service", "sample-caller-service", "Name of the invoking microservice"))
                        .addParameters("ChannelHeader",
                                optionalHeader("Channel", "web", "Source channel"))
                        .addParameters("CorrelationIdHeader",
                                optionalHeader("X-Correlation-Id", "550e8400-e29b-41d4-a716-446655440000", "Correlation identifier"))
                        .addParameters("DestinationBankHeader",
                                optionalHeader("X-Destination-Bank", "CO01", "Destination bank code"))
                );
    }

    private Parameter requiredHeader(String name, String example, String description) {
        return new Parameter()
                .in(ParameterIn.HEADER.toString())
                .required(true)
                .name(name)
                .example(example)
                .description(description);
    }

    private Parameter optionalHeader(String name, String example, String description) {
        return new Parameter()
                .in(ParameterIn.HEADER.toString())
                .required(false)
                .name(name)
                .example(example)
                .description(description);
    }
}