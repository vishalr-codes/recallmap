package com.recallmap.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "RECALLMAP API",
        version = "v1",
        description = "Developer API for data lineage and deletion verification."
))
public class OpenApiConfiguration {
}
