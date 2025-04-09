package nnt_data.bankaccount_microservice.infrastructure.persistence.entity;

import lombok.Data;
import nnt_data.bankaccount_microservice.model.AccountType;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import nnt_data.bankaccount_microservice.model.Person;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
/**
 * AccountBaseEntity es una entidad que representa una cuenta bancaria en la base de datos.
 * Utiliza Lombok para generar automáticamente los métodos getter, setter y otros métodos útiles.
 * La clase está anotada con @Document para indicar que es un documento de MongoDB.
 */

@Data
@Document(collection = "account")
public class AccountBaseEntity {
    @Id
    private String accountId;
    private AccountType accountType;
    private BigDecimal balance;
    private String customerId;
    private CustomerType customerType;
    private CustomerSubtype customerSubType;
    private List<Person> owners;
    private List<Person> authorizedSigners;
    private BigDecimal maintenanceFee;
    private BigDecimal feePerTransaction;
    private Integer movementLimit;
    private Integer monthlyMovementLimit;
    private Integer transactionMovements;
    private String allowedDayOfMonth;
    private Double minimumDailyAverage;
}
