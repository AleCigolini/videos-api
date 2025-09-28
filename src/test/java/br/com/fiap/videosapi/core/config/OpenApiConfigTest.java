package br.com.fiap.videosapi.core.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    @DisplayName("addUserIdHeaderParameter should add x-cliente-id header if not present")
    void addUserIdHeaderParameter_shouldAddHeaderIfNotPresent() {
        OpenApiConfig config = new OpenApiConfig();
        Operation operation = new Operation();
        HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);

        assertNull(operation.getParameters());
        config.addUserIdHeaderParameter().customize(operation, handlerMethod);

        assertNotNull(operation.getParameters());
        assertTrue(operation.getParameters().stream().anyMatch(
                p -> "x-cliente-id".equalsIgnoreCase(p.getName()) && "header".equalsIgnoreCase(p.getIn())
        ));
    }

    @Test
    @DisplayName("addUserIdHeaderParameter should not duplicate x-cliente-id header if already present")
    void addUserIdHeaderParameter_shouldNotDuplicateHeader() {
        OpenApiConfig config = new OpenApiConfig();
        Operation operation = new Operation();
        Parameter header = new Parameter()
                .in("header")
                .name("x-cliente-id")
                .schema(new StringSchema());
        operation.addParametersItem(header);
        HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);

        int initialSize = operation.getParameters().size();
        config.addUserIdHeaderParameter().customize(operation, handlerMethod);

        assertEquals(initialSize, operation.getParameters().size());
    }

    @Test
    @DisplayName("addUserIdHeaderParameter should return the same operation instance")
    void addUserIdHeaderParameter_shouldReturnSameOperation() {
        OpenApiConfig config = new OpenApiConfig();
        Operation operation = new Operation();
        HandlerMethod handlerMethod = Mockito.mock(HandlerMethod.class);

        Operation result = config.addUserIdHeaderParameter().customize(operation, handlerMethod);
        assertSame(operation, result);
    }
}
