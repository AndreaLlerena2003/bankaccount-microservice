package nnt_data.bankaccount_microservice.infrastructure.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nnt_data.bankaccount_microservice.domain.service.DebitCardOperationsService;
import nnt_data.bankaccount_microservice.domain.service.KafkaService;
import nnt_data.bankaccount_microservice.infrastructure.kafka.MessageWrapper;
import nnt_data.bankaccount_microservice.model.DebitCardValidationRequest;
import nnt_data.bankaccount_microservice.model.DebitCardValidationResponse;
import nnt_data.bankaccount_microservice.model.Transaction;
import nnt_data.bankaccount_microservice.model.YankiTransactionRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebitCardKafkaListener {
    private final KafkaService kafkaService;
    private final DebitCardOperationsService debitCardOperationsService;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.transaction-responses}")
    private String transactionResponsesTopic;

    @Value("${kafka.topics.debit-card-validation-response}")
    private String validationResponseTopic;


    @KafkaListener(
            topics = "${kafka.topics.debit-card-validation-request}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleDebitCardValidation(ConsumerRecord<String,String> record) {
        try {
            MessageWrapper<DebitCardValidationRequest> wrapper = objectMapper.readValue(
                    record.value(),
                    new TypeReference<MessageWrapper<DebitCardValidationRequest>>() {}
            );

            DebitCardValidationRequest request = wrapper.getPayload();
            String correlationId = wrapper.getCorrelationId();

            debitCardOperationsService.existDebitCard(request)
                    .doOnNext(response -> {
                        try {
                            MessageWrapper<DebitCardValidationResponse> responseWrapper =
                                    new MessageWrapper<>(response, correlationId);
                            kafkaService.send(validationResponseTopic, responseWrapper)
                                    .subscribe();
                        } catch (Exception e) {
                            log.error("Error enviando respuesta de validación: {}", e.getMessage());
                        }
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Error procesando validación de tarjeta: {}", e.getMessage());
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.transaction-requests}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTransactionRequest(ConsumerRecord<String,String> record) {
        try {
            MessageWrapper<YankiTransactionRequest> wrapper = objectMapper.readValue(
                    record.value(),
                    new TypeReference<MessageWrapper<YankiTransactionRequest>>() {}
            );
            YankiTransactionRequest request = wrapper.getPayload();
            String correlationId = wrapper.getCorrelationId();

            debitCardOperationsService.processDebitCardTransactionFromId(
                            request.getDebitCardIdOrigin(),
                            request.getDebitCardIdDestiny(),
                            request.getTransaction()
                    )
                    .doOnNext(response -> {
                        try {
                            MessageWrapper<Transaction> responseWrapper =
                                    new MessageWrapper<>(response, correlationId);
                            kafkaService.send(transactionResponsesTopic, responseWrapper)
                                    .subscribe();
                        } catch (Exception e) {
                            log.error("Error enviando respuesta de transacción: {}", e.getMessage());
                        }
                    })
                    .doOnError(error -> {
                        MessageWrapper<String> errorWrapper =
                                new MessageWrapper<>(error.getMessage(), correlationId);
                        kafkaService.send(transactionResponsesTopic, errorWrapper)
                                .subscribe();
                    })
                    .subscribe();
        } catch (Exception e) {
            log.error("Error procesando solicitud de transacción: {}", e.getMessage());
        }
    }
}