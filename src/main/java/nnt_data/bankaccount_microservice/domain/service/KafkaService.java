package nnt_data.bankaccount_microservice.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nnt_data.bankaccount_microservice.infrastructure.kafka.MessageWrapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, MonoSink<Object>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * Sends a message to a Kafka topic and waits for a response on a response topic.
     *
     * @param requestTopic The topic to send the request message to.
     * @param responseTopic The topic to listen for the response on.
     * @param message The message payload to send.
     * @param responseType The class type of the expected response.
     * @param timeout The maximum time to wait for a response.
     * @param <T> The type of the request message payload.
     * @param <R> The type of the expected response payload.
     * @return A Mono containing the response object.
     */
    public <T, R> Mono<R> sendAndReceive(String requestTopic, String responseTopic,
                                         T message, Class<R> responseType, Duration timeout) {
        String correlationId = UUID.randomUUID().toString();

        // Create a Mono that will complete when the response is received
        Mono<Object> responseMono = Mono.create(sink -> {
                    pendingRequests.put(correlationId, sink);
                    sink.onDispose(() -> pendingRequests.remove(correlationId));
                }).timeout(timeout)
                .onErrorResume(TimeoutException.class, ex ->
                        Mono.error(new Exception("Service timeout waiting for response on topic: " + responseTopic)));

        // Prepare the message with correlation ID
        MessageWrapper<T> wrappedMessage = new MessageWrapper<>(message, correlationId);

        log.debug("Sending message to topic {} with correlationId {}", requestTopic, correlationId);

        // Convert the Future to CompletableFuture using toCompletableFuture()
        return Mono.fromFuture(() ->
                        kafkaTemplate.send(requestTopic, correlationId, wrappedMessage).toCompletableFuture()
                )
                .then(responseMono)
                .flatMap(response -> {
                    try {
                        R result;
                        if (response instanceof MessageWrapper) {
                            Object payload = ((MessageWrapper<?>) response).getPayload();
                            result = objectMapper.convertValue(payload, responseType);
                        } else {
                            result = objectMapper.convertValue(response, responseType);
                        }
                        return Mono.just(result);
                    } catch (Exception e) {
                        log.error("Error converting response: {}", e.getMessage());
                        return Mono.error(new RuntimeException("Error processing response", e));
                    }
                })
                .doOnError(error -> log.error("Error in Kafka request-response cycle", error));
    }

    /**
     * Sends a message to the specified Kafka topic with a given key.
     * This is a fire-and-forget operation.
     *
     * @param topic   The Kafka topic to send the message to.
     * @param key     The key to use for partitioning the message.
     * @param message The message payload to send.
     * @param <T>     The type of the message payload.
     * @return A Mono<Void> that completes when the send operation is acknowledged (or fails).
     */
    public <T> Mono<Void> send(String topic, String key, T message) {
        return Mono.fromFuture(() ->
                        kafkaTemplate.send(topic, key, message).toCompletableFuture()
                )
                .doOnSuccess(result -> log.debug("Message sent to topic {} with key {}", topic, key))
                .doOnError(error -> log.error("Error sending message to topic {} with key {}", topic, key, error))
                .then();
    }

    /**
     * Sends a message to the specified Kafka topic without a key (key will be null).
     * This is a fire-and-forget operation.
     *
     * @param topic   The Kafka topic to send the message to.
     * @param message The message payload to send.
     * @param <T>     The type of the message payload.
     * @return A Mono<Void> that completes when the send operation is acknowledged (or fails).
     */
    public <T> Mono<Void> send(String topic, T message) {
        // Delegates to the send method with a null key
        return send(topic, null, message);
    }
}
