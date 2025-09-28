package br.com.fiap.videosapi.core.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OperationCustomizer addUserIdHeaderParameter() {
        return (Operation operation, org.springframework.web.method.HandlerMethod handlerMethod) -> {
            // Add global x-cliente-id header to all operations except public ones
            Parameter userIdHeader = new Parameter()
                    .in("header")
                    .name("x-cliente-id")
                    .required(false)
                    .description("User identifier for scoping operations. Required for all business endpoints.")
                    .schema(new StringSchema());

            // Avoid duplicating parameter if already present
            boolean exists = operation.getParameters() != null && operation.getParameters().stream()
                    .anyMatch(p -> "x-cliente-id".equalsIgnoreCase(p.getName()) && "header".equalsIgnoreCase(p.getIn()));
            if (!exists) {
                operation.addParametersItem(userIdHeader);
            }
            return operation;
        };
    }
}