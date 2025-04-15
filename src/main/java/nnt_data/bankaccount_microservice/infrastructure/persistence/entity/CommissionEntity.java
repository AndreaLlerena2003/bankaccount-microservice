package nnt_data.bankaccount_microservice.infrastructure.persistence.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nnt_data.bankaccount_microservice.model.AccountType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Entidad que registra las comisiones cobradas en las transacciones.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commissions")
public class CommissionEntity {
    @Id
    private String id;
    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private Date dateTime;
}
