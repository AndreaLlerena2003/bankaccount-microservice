package nnt_data.bankAccount_service.infrastructure.persistence.entity;


import lombok.Data;
import nnt_data.bankAccount_service.model.Transaction;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
/**
 * TransactionEntity es una entidad que representa una transacción bancaria en la base de datos.
 * Utiliza Lombok para generar automáticamente los métodos getter, setter y otros métodos útiles.
 * La clase está anotada con @Document para indicar que es un documento de MongoDB.
 */

@Data
@Document(collection = "transaction")
public class TransactionEntity {
    @Id
    private String transactionId;
    private Date date;
    private Transaction.TypeEnum type;
    private BigDecimal amount;
    private String accountId;
}
