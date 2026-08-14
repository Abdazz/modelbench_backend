package com.example.modelbench.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SCHEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI documentationModelBench() {
        return new OpenAPI()
                .info(new Info()
                        .title("API ModelBench")
                        .version("1.0.0")
                        .description("""
                                Catalogue de jeux de donnees, de modeles de Machine Learning et \
                                d'experimentations pour un laboratoire de recherche.

                                Authentification : appeler POST /api/auth/login puis coller le \
                                jeton renvoye dans le bouton Authorize ci-dessus.

                                Comptes de demonstration : admin@example.com / admin123 \
                                (lecture et ecriture), chercheur@example.com / chercheur123 \
                                (lecture seule).""")
                        .contact(new Contact().name("ModelBench"))
                        .license(new License().name("Usage academique")))
                .components(new Components().addSecuritySchemes(SCHEMA_BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Jeton obtenu via POST /api/auth/login")))
                .addSecurityItem(new SecurityRequirement().addList(SCHEMA_BEARER));
    }
}
