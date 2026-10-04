package com.ridelink.farepayment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI farePaymentOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink Fare & Payment Service")
                        .description("Fare estimation, final fare creation, simulated payments, and receipts.")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes("internalApiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Internal-Api-Key")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
