package nnt_data.bankAccount_service.infrastructure.persistence.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import nnt_data.bankAccount_service.model.AccountType;
import nnt_data.bankAccount_service.model.CustomerType;
import nnt_data.bankAccount_service.model.Person;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;


@Data
@Getter
@Setter
@Document(collection = "account")
public class AccountBaseEntity {
    @Id
    private String accountId;
    private AccountType accountType;
    private BigDecimal balance;
    private String customerId;
    private CustomerType customerType;
    private List<Person> owners;
    private List<Person> authorizedSigners;
    private BigDecimal maintenanceFee;
    private Integer monthlyMovementLimit;
    private String allowedDayOfMonth;
}
