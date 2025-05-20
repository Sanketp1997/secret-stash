package com.secretstash.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .openapi("3.0.1")
            .info(
                Info()
                    .title("Secret Stash API")
                    .version("1.0")
                    .description("This is the Secret Stash API documentation. The API allows users to securely store and manage sensitive notes and information.")
                    .termsOfService("https://secretstash.com/terms")
                    .contact(
                        Contact()
                            .name("Secret Stash Team")
                            .email("support@secretstash.com")
                    )
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("JWT"))
            .components(
                Components()
                    .addSecuritySchemes(
                        "JWT",
                        SecurityScheme()
                            .name("JWT")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter JWT Bearer token")
                    )
            )
    }
}
