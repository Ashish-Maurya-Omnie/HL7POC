package com.example.hl7poc.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hl7PocOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HL7POC FHIR API")
                        .description("POC APIs for local HL7 FHIR and Amazon HealthLake integration")
                        .version("v1")
                        .contact(new Contact().name("HL7POC Team"))
                        .license(new License().name("POC Internal Use")));
    }
}
