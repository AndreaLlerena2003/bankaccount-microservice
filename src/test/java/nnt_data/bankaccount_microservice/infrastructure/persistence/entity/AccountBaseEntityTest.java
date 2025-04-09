package nnt_data.bankaccount_microservice.infrastructure.persistence.entity;


import nnt_data.bankaccount_microservice.model.AccountType;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import nnt_data.bankaccount_microservice.model.Person;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AccountBaseEntityTest {

    @Test
    void testAccountBaseEntityGettersAndSetters() {
        // Arrange
        AccountBaseEntity account = new AccountBaseEntity();
        String accountId = "ACC123";
        AccountType accountType = AccountType.SAVINGS; // Asumiendo que es un enum
        BigDecimal balance = new BigDecimal("1000.50");
        String customerId = "CUST456";
        CustomerType customerType = CustomerType.PERSONAL; // Asumiendo que es un enum
        CustomerSubtype customerSubType = CustomerSubtype.VIP; // Asumiendo que es un enum
        List<Person> owners = new ArrayList<>();
        owners.add(new Person()); // Asumiendo constructor vacío
        List<Person> authorizedSigners = new ArrayList<>();
        authorizedSigners.add(new Person());
        BigDecimal maintenanceFee = new BigDecimal("5.00");
        BigDecimal feePerTransaction = new BigDecimal("1.50");
        Integer movementLimit = 10;
        Integer monthlyMovementLimit = 30;
        Integer transactionMovements = 5;
        String allowedDayOfMonth = "15";
        Double minimumDailyAverage = 500.0;

        // Act
        account.setAccountId(accountId);
        account.setAccountType(accountType);
        account.setBalance(balance);
        account.setCustomerId(customerId);
        account.setCustomerType(customerType);
        account.setCustomerSubType(customerSubType);
        account.setOwners(owners);
        account.setAuthorizedSigners(authorizedSigners);
        account.setMaintenanceFee(maintenanceFee);
        account.setFeePerTransaction(feePerTransaction);
        account.setMovementLimit(movementLimit);
        account.setMonthlyMovementLimit(monthlyMovementLimit);
        account.setTransactionMovements(transactionMovements);
        account.setAllowedDayOfMonth(allowedDayOfMonth);
        account.setMinimumDailyAverage(minimumDailyAverage);

        // Assert
        assertEquals(accountId, account.getAccountId());
        assertEquals(accountType, account.getAccountType());
        assertEquals(balance, account.getBalance());
        assertEquals(customerId, account.getCustomerId());
        assertEquals(customerType, account.getCustomerType());
        assertEquals(customerSubType, account.getCustomerSubType());
        assertEquals(owners, account.getOwners());
        assertEquals(authorizedSigners, account.getAuthorizedSigners());
        assertEquals(maintenanceFee, account.getMaintenanceFee());
        assertEquals(feePerTransaction, account.getFeePerTransaction());
        assertEquals(movementLimit, account.getMovementLimit());
        assertEquals(monthlyMovementLimit, account.getMonthlyMovementLimit());
        assertEquals(transactionMovements, account.getTransactionMovements());
        assertEquals(allowedDayOfMonth, account.getAllowedDayOfMonth());
        assertEquals(minimumDailyAverage, account.getMinimumDailyAverage());
    }

    @Test
    void testAccountBaseEntityEqualsAndHashCode() {
        // Arrange
        AccountBaseEntity account1 = new AccountBaseEntity();
        account1.setAccountId("ACC123");
        account1.setBalance(new BigDecimal("1000.50"));

        AccountBaseEntity account2 = new AccountBaseEntity();
        account2.setAccountId("ACC123");
        account2.setBalance(new BigDecimal("1000.50"));

        AccountBaseEntity account3 = new AccountBaseEntity();
        account3.setAccountId("ACC456");
        account3.setBalance(new BigDecimal("2000.75"));

        // Assert
        assertEquals(account1, account2);
        assertEquals(account1.hashCode(), account2.hashCode());
        assertNotEquals(account1, account3);
        assertNotEquals(account1.hashCode(), account3.hashCode());
    }

    @Test
    void testAccountBaseEntityToString() {
        // Arrange
        AccountBaseEntity account = new AccountBaseEntity();
        account.setAccountId("ACC123");
        account.setBalance(new BigDecimal("1000.50"));

        // Act
        String toString = account.toString();

        // Assert
        assertTrue(toString.contains("ACC123"));
        assertTrue(toString.contains("1000.50"));
    }

    @Test
    void testAccountBaseEntityBuilder() {
        // Arrange & Act
        AccountBaseEntity account = new AccountBaseEntity();
        account.setAccountId("ACC123");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("1000.50"));

        // Assert
        assertEquals("ACC123", account.getAccountId());
        assertEquals(AccountType.SAVINGS, account.getAccountType());
        assertEquals(new BigDecimal("1000.50"), account.getBalance());
    }
}
