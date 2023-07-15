package com.clinic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DocConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clinic Restful Rest API")
                        .version("0.1.0")
                        .description( "Clinic 后端 API")
                        .termsOfService("http://dev.cuihairu.site:2346/"));
    }
}
