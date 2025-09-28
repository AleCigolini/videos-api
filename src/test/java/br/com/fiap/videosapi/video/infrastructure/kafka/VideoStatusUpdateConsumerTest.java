package br.com.fiap.videosapi.video.infrastructure.kafka;

import br.com.fiap.videosapi.video.application.usecase.VideoStatusUpdateUseCase;
import br.com.fiap.videosapi.video.common.domain.dto.event.VideoStatusUpdateEvent;
import br.com.fiap.videosapi.video.domain.entity.VideoStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoStatusUpdateConsumerTest {

    @Mock
    private VideoStatusUpdateUseCase videoStatusUpdateUseCase;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private VideoStatusUpdateConsumer videoStatusUpdateConsumer;

    private VideoStatusUpdateEvent videoStatusUpdateEvent;
    private final String topic = "video-status-update-events";
    private final int partition = 0;
    private final long offset = 123L;

    @BeforeEach
    void setUp() {
        videoStatusUpdateEvent = VideoStatusUpdateEvent.builder()
                .videoId(1L)
                .newStatus(VideoStatus.PROCESSED)
                .message("Processamento concluído com sucesso")
                .processedBy("processing-service")
                .userId("user-123")
                .build();
    }

    @Test
    @DisplayName("Deve consumir evento de atualização de status com sucesso")
    void deveConsumirEventoDeAtualizacaoDeStatusComSucesso() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"message\":\"Processamento concluído\",\"userId\":\"user-123\"}";

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment);

        verify(objectMapper).readValue(message, VideoStatusUpdateEvent.class);
        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(videoStatusUpdateEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento com status PROCESSING")
    void deveProcessarEventoComStatusProcessing() throws JsonProcessingException {
        String message = "{\"videoId\":2,\"newStatus\":\"PROCESSING\",\"userId\":\"user-abc\"}";

        VideoStatusUpdateEvent eventProcessing = VideoStatusUpdateEvent.builder()
                .videoId(2L)
                .newStatus(VideoStatus.PROCESSING)
                .message("Iniciando processamento")
                .userId("user-abc")
                .build();

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(eventProcessing);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment);

        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(eventProcessing);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento com status ERROR")
    void deveProcessarEventoComStatusError() throws JsonProcessingException {
        String message = "{\"videoId\":3,\"newStatus\":\"ERROR\",\"userId\":\"user-xyz\"}";

        VideoStatusUpdateEvent eventError = VideoStatusUpdateEvent.builder()
                .videoId(3L)
                .newStatus(VideoStatus.FAILED)
                .message("Falha no processamento")
                .userId("user-xyz")
                .build();

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(eventError);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment);

        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(eventError);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve lançar exceção e não fazer acknowledge quando ocorrer JsonProcessingException")
    void deveLancarExcecaoENaoFazerAcknowledgeQuandoOcorrerJsonProcessingException() throws JsonProcessingException {
        String message = "{invalid json}";
        JsonProcessingException jsonException = new JsonProcessingException("Deserialization error") {};

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenThrow(jsonException);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment));

        assertEquals("Failed to process video status update", exception.getMessage());
        assertEquals(jsonException, exception.getCause());

        verify(objectMapper).readValue(message, VideoStatusUpdateEvent.class);
        verify(videoStatusUpdateUseCase, never()).processStatusUpdateEvent(any());
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Deve lançar exceção e não fazer acknowledge quando ocorrer erro no use case")
    void deveLancarExcecaoENaoFazerAcknowledgeQuandoOcorrerErroNoUseCase() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";
        RuntimeException useCaseException = new RuntimeException("Erro no processamento");

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);
        doThrow(useCaseException).when(videoStatusUpdateUseCase)
                .processStatusUpdateEvent(videoStatusUpdateEvent);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment));

        assertEquals("Failed to process video status update", exception.getMessage());
        assertEquals(useCaseException, exception.getCause());

        verify(objectMapper).readValue(message, VideoStatusUpdateEvent.class);
        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(videoStatusUpdateEvent);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    @DisplayName("Deve processar mensagem de diferentes tópicos")
    void deveProcessarMensagemDeDiferentesTopicos() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";
        String customTopic = "custom-video-status-topic";

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, customTopic, partition, offset, acknowledgment);

        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(videoStatusUpdateEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar mensagem de diferentes partições")
    void deveProcessarMensagemDeDiferentesParticoes() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";
        int customPartition = 5;

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, customPartition, offset, acknowledgment);

        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(videoStatusUpdateEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar mensagem com diferentes offsets")
    void deveProcessarMensagemComDiferentesOffsets() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";
        long customOffset = 999999L;

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, customOffset, acknowledgment);

        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(videoStatusUpdateEvent);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve processar evento com videoId grande")
    void deveProcessarEventoComVideoIdGrande() throws JsonProcessingException {
        String message = "{\"videoId\":999999999,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";

        VideoStatusUpdateEvent eventIdGrande = VideoStatusUpdateEvent.builder()
                .videoId(999999999L)
                .newStatus(VideoStatus.PROCESSED)
                .userId("user-123")
                .build();

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(eventIdGrande);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment);

        verify(videoStatusUpdateUseCase).processStatusUpdateEvent(eventIdGrande);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve fazer acknowledge apenas após processamento bem-sucedido")
    void deveFazerAcknowledgeApenasAposProcessamentoBemSucedido() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\"}";

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment);

        var inOrder = inOrder(videoStatusUpdateUseCase, acknowledgment);
        inOrder.verify(videoStatusUpdateUseCase).processStatusUpdateEvent(videoStatusUpdateEvent);
        inOrder.verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Deve chamar ObjectMapper readValue exatamente uma vez")
    void deveChamarObjectMapperReadValueExatamenteUmaVez() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment);

        verify(objectMapper, times(1)).readValue(message, VideoStatusUpdateEvent.class);
    }

    @Test
    @DisplayName("Deve chamar use case processStatusUpdateEvent exatamente uma vez")
    void deveChamarUseCaseProcessStatusUpdateEventExatamenteUmaVez() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenReturn(videoStatusUpdateEvent);

        videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment);

        verify(videoStatusUpdateUseCase, times(1)).processStatusUpdateEvent(videoStatusUpdateEvent);
    }

    @Test
    @DisplayName("Deve propagar exceção original quando ocorrer erro inesperado")
    void devePropagarExcecaoOriginalQuandoOcorrerErroInesperado() throws JsonProcessingException {
        String message = "{\"videoId\":1,\"newStatus\":\"PROCESSED\",\"userId\":\"user-123\"}";
        IllegalStateException originalException = new IllegalStateException("Estado inválido");

        when(objectMapper.readValue(message, VideoStatusUpdateEvent.class))
                .thenThrow(originalException);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                videoStatusUpdateConsumer.consumeVideoStatusUpdate(message, topic, partition, offset, acknowledgment));

        assertEquals("Failed to process video status update", exception.getMessage());
        assertEquals(originalException, exception.getCause());
    }
}
