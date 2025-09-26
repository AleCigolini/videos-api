package br.com.fiap.videosapi.core.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "spring.cache.type=redis"
})
class RedisConfigTest {

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    private final RedisConfig redisConfig = new RedisConfig();

    @Test
    @DisplayName("Deve criar CacheManager corretamente")
    void deveCriarCacheManagerCorretamente() {
        CacheManager cacheManager = redisConfig.cacheManager(redisConnectionFactory);

        assertNotNull(cacheManager);
        assertInstanceOf(RedisCacheManager.class, cacheManager);
    }

    @Test
    @DisplayName("Deve configurar serializadores corretamente")
    void deveConfigurarSerializadoresCorretamente() {
        RedisTemplate<String, Object> redisTemplate = redisConfig.redisTemplate(redisConnectionFactory);

        assertNotNull(redisTemplate.getKeySerializer());
        assertNotNull(redisTemplate.getHashKeySerializer());

        assertNotNull(redisTemplate.getValueSerializer());
        assertNotNull(redisTemplate.getHashValueSerializer());

        assertEquals(StringRedisSerializer.class, redisTemplate.getKeySerializer().getClass());
        assertEquals(StringRedisSerializer.class, redisTemplate.getHashKeySerializer().getClass());
        assertEquals(GenericJackson2JsonRedisSerializer.class, redisTemplate.getValueSerializer().getClass());
        assertEquals(GenericJackson2JsonRedisSerializer.class, redisTemplate.getHashValueSerializer().getClass());
    }

    @Test
    @DisplayName("Deve retornar instâncias diferentes para múltiplas chamadas")
    void deveRetornarInstanciasDiferentesParaMultiplasChamadas() {
        RedisTemplate<String, Object> template1 = redisConfig.redisTemplate(redisConnectionFactory);
        RedisTemplate<String, Object> template2 = redisConfig.redisTemplate(redisConnectionFactory);
        CacheManager cacheManager1 = redisConfig.cacheManager(redisConnectionFactory);
        CacheManager cacheManager2 = redisConfig.cacheManager(redisConnectionFactory);

        assertNotSame(template1, template2, "Instâncias de RedisTemplate devem ser diferentes");
        assertNotSame(cacheManager1, cacheManager2, "Instâncias de CacheManager devem ser diferentes");
    }
}
