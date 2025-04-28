package nnt_data.bankaccount_microservice.infrastructure.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageWrapper<T> {
    private T payload;
    private String correlationId;
}
