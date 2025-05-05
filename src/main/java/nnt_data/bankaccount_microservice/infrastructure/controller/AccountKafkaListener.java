package nnt_data.bankaccount_microservice.infrastructure.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nnt_data.bankaccount_microservice.application.port.AccountOperationsPort;
import nnt_data.bankaccount_microservice.domain.service.KafkaService;
import nnt_data.bankaccount_microservice.infrastructure.kafka.MessageWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountKafkaListener {
    private final KafkaService kafkaService;
    private final AccountOperationsPort accountOperationsPort;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.account-validation-response}")
    private String accountResponseTopic;

    @KafkaListener(
            topics = "${kafka.topics.account-validation-request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenForAccountValidationRequests(String message) {
        try {
            MessageWrapper<String> wrapper = objectMapper.readValue(
                    message,
                    new TypeReference<MessageWrapper<String>>() {}
            );

            String accountId = wrapper.getPayload();
            String correlationId = wrapper.getCorrelationId();

            accountOperationsPort.existsById(accountId)
                    .doOnNext(exists -> {
                        try {
                            MessageWrapper<Boolean> responseWrapper =
                                    new MessageWrapper<>(exists, correlationId);
                            kafkaService.send(accountResponseTopic, responseWrapper)
                                    .subscribe();
                            log.info("Respuesta de validación enviada para cuenta {}: {}",
                                    accountId, exists);
                        } catch (Exception e) {
                            log.error("Error al enviar respuesta de validación: {}",
                                    e.getMessage());
                        }
                    })
                    .doOnError(error -> {
                        log.error("Error al validar cuenta {}: {}",
                                accountId, error.getMessage());
                        MessageWrapper<Boolean> errorWrapper =
                                new MessageWrapper<>(false, correlationId);
                        kafkaService.send(accountResponseTopic, errorWrapper)
                                .subscribe();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Error al procesar mensaje de validación: {}", e.getMessage());
        }
    }

}
