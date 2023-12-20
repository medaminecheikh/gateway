package org.gateway.AuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerAggregatorConfig {

    @Bean
    @Primary
    public SwaggerResourcesProvider swaggerResourcesProvider() {
        return () -> {
            List<SwaggerResource> resources = new ArrayList<>();

            // Add Swagger resources for each microservice
            resources.add(swaggerResource("Microservice 1", "/microservice1/v3/api-docs", "2.0"));
            resources.add(swaggerResource("Microservice 2", "/microservice2/v3/api-docs", "2.0"));
            // Add more services as needed
// Add Swagger resource for the "microadmin" service
            resources.add(swaggerResource("Microadmin", "/microadmin/v3/api-docs", "2.0"));
            return resources;
        };
    }

    private SwaggerResource swaggerResource(String name, String location, String version) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion(version);
        return swaggerResource;
    }
}
