package nnt_data.bankAccount_service.infrastructure.persistence.model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nnt_data.bankAccount_service.model.Transaction;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;


@Data
@Getter
@Setter
@Document(collection = "transaction")
public class TransactionEntity {
    @Id
    private String transactionId;
    private Date date;
    private Transaction.TypeEnum type;
    private BigDecimal amount;
    private String accountId;
}
