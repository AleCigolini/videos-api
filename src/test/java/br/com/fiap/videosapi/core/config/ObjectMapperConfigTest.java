package br.com.fiap.videosapi.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ObjectMapperConfigTest {

    @Test
    void deveDesabilitarWriteDatesAsTimestampsNoObjectMapper() {
        ObjectMapperConfig config = new ObjectMapperConfig();
        ObjectMapper mapper = config.objectMapper();

        assertFalse(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
                "O ObjectMapper não deve serializar datas como timestamps");
    }
}
