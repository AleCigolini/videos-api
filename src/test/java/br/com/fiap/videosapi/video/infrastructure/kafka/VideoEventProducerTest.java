package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.common.domain.dto.event.VideoUploadEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoEventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CompletableFuture<SendResult<String, String>> future;

    @Mock
    private SendResult<String, String> sendResult;

    @InjectMocks
    private VideoEventProducer videoEventProducer;

    private VideoUploadEvent videoUploadEvent;
    private final String videoUploadTopic = "video-upload-topic";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoEventProducer, "videoUploadTopic", videoUploadTopic);

        videoUploadEvent = VideoUploadEvent.builder()
                .videoId(1L)
                .originalFileName("test-video.mp4")
                .fileSize(1024L)
                .contentType("video/mp4")
                .build();
    }

    @Test
    @DisplayName("Deve publicar evento de upload com sucesso")
    void devePublicarEventoDeUploadComSucesso() throws JsonProcessingException {
        String eventJson = "{\"videoId\":1,\"fileName\":\"test-video.mp4\"}";
        String expectedKey = "1";

        when(objectMapper.writeValueAsString(videoUploadEvent)).thenReturn(eventJson);
        when(kafkaTemplate.send(videoUploadTopic, expectedKey, eventJson)).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(videoUploadEvent);

        verify(objectMapper).writeValueAsString(videoUploadEvent);
        verify(kafkaTemplate).send(videoUploadTopic, expectedKey, eventJson);
        verify(future).whenComplete(any());
    }

    @Test
    @DisplayName("Deve usar videoId como chave da mensagem")
    void deveUsarVideoIdComoChaveDaMensagem() throws JsonProcessingException {
        String eventJson = "{\"videoId\":123}";
        String expectedKey = "123";

        VideoUploadEvent eventComIdEspecifico = VideoUploadEvent.builder()
                .videoId(123L)
                .originalFileName("outro-video.mp4")
                .build();

        when(objectMapper.writeValueAsString(eventComIdEspecifico)).thenReturn(eventJson);
        when(kafkaTemplate.send(videoUploadTopic, expectedKey, eventJson)).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(eventComIdEspecifico);

        verify(kafkaTemplate).send(videoUploadTopic, expectedKey, eventJson);
    }

    @Test
    @DisplayName("Deve capturar callback de sucesso corretamente")
    void deveCapturaCallbackDeSucessoCorretamente() throws JsonProcessingException {
        String eventJson = "{\"videoId\":1}";
        ArgumentCaptor<BiConsumer<SendResult<String, String>, Throwable>> callbackCaptor =
                ArgumentCaptor.forClass(BiConsumer.class);

        when(objectMapper.writeValueAsString(any())).thenReturn(eventJson);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(videoUploadEvent);

        verify(future).whenComplete(callbackCaptor.capture());

        BiConsumer<SendResult<String, String>, Throwable> callback = callbackCaptor.getValue();
        assertNotNull(callback);
    }

    @Test
    @DisplayName("Deve executar callback de erro quando exceção for lançada")
    void deveExecutarCallbackDeErroQuandoExcecaoForLancada() throws JsonProcessingException {
        String eventJson = "{\"videoId\":1}";
        ArgumentCaptor<BiConsumer<SendResult<String, String>, Throwable>> callbackCaptor =
                ArgumentCaptor.forClass(BiConsumer.class);

        when(objectMapper.writeValueAsString(any())).thenReturn(eventJson);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(videoUploadEvent);

        verify(future).whenComplete(callbackCaptor.capture());

        BiConsumer<SendResult<String, String>, Throwable> callback = callbackCaptor.getValue();
        RuntimeException exception = new RuntimeException("Kafka error");

        assertDoesNotThrow(() -> callback.accept(null, exception));
    }

    @Test
    @DisplayName("Deve tratar JsonProcessingException durante serialização")
    void deveTratarJsonProcessingExceptionDuranteSerializacao() throws JsonProcessingException {
        JsonProcessingException jsonException = new JsonProcessingException("Serialization error") {};

        when(objectMapper.writeValueAsString(videoUploadEvent)).thenThrow(jsonException);

        assertDoesNotThrow(() -> videoEventProducer.publishVideoUploadEvent(videoUploadEvent));

        verify(objectMapper).writeValueAsString(videoUploadEvent);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve publicar múltiplos eventos independentemente")
    void devePublicarMultiplosEventosIndependentemente() throws JsonProcessingException {
        VideoUploadEvent evento1 = VideoUploadEvent.builder().videoId(1L).originalFileName("video1.mp4").build();
        VideoUploadEvent evento2 = VideoUploadEvent.builder().videoId(2L).originalFileName("video2.mp4").build();

        String json1 = "{\"videoId\":1}";
        String json2 = "{\"videoId\":2}";

        when(objectMapper.writeValueAsString(evento1)).thenReturn(json1);
        when(objectMapper.writeValueAsString(evento2)).thenReturn(json2);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(evento1);
        videoEventProducer.publishVideoUploadEvent(evento2);

        verify(kafkaTemplate).send(videoUploadTopic, "1", json1);
        verify(kafkaTemplate).send(videoUploadTopic, "2", json2);
        verify(future, times(2)).whenComplete(any());
    }

    @Test
    @DisplayName("Deve usar tópico configurado corretamente")
    void deveUsarTopicoConfiguradoCorretamente() throws JsonProcessingException {
        String topicoCustomizado = "topico-customizado";
        ReflectionTestUtils.setField(videoEventProducer, "videoUploadTopic", topicoCustomizado);

        String eventJson = "{\"videoId\":1}";

        when(objectMapper.writeValueAsString(any())).thenReturn(eventJson);
        when(kafkaTemplate.send(topicoCustomizado, "1", eventJson)).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(videoUploadEvent);

        verify(kafkaTemplate).send(topicoCustomizado, "1", eventJson);
    }

    @Test
    @DisplayName("Deve converter videoId para string corretamente")
    void deveConverterVideoIdParaStringCorretamente() throws JsonProcessingException {
        VideoUploadEvent eventoComIdGrande = VideoUploadEvent.builder()
                .videoId(999999999L)
                .originalFileName("video-grande-id.mp4")
                .build();

        String eventJson = "{\"videoId\":999999999}";
        String expectedKey = "999999999";

        when(objectMapper.writeValueAsString(eventoComIdGrande)).thenReturn(eventJson);
        when(kafkaTemplate.send(videoUploadTopic, expectedKey, eventJson)).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(eventoComIdGrande);

        verify(kafkaTemplate).send(videoUploadTopic, expectedKey, eventJson);
    }

    @Test
    @DisplayName("Deve chamar ObjectMapper exatamente uma vez por evento")
    void deveChamarObjectMapperExatamenteUmaVezPorEvento() throws JsonProcessingException {
        String eventJson = "{\"videoId\":1}";

        when(objectMapper.writeValueAsString(videoUploadEvent)).thenReturn(eventJson);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(videoUploadEvent);

        verify(objectMapper, times(1)).writeValueAsString(videoUploadEvent);
    }

    @Test
    @DisplayName("Deve chamar KafkaTemplate send exatamente uma vez por evento")
    void deveChamarKafkaTemplateSendExatamenteUmaVezPorEvento() throws JsonProcessingException {
        String eventJson = "{\"videoId\":1}";

        when(objectMapper.writeValueAsString(videoUploadEvent)).thenReturn(eventJson);
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        videoEventProducer.publishVideoUploadEvent(videoUploadEvent);

        verify(kafkaTemplate, times(1)).send(videoUploadTopic, "1", eventJson);
    }
}
