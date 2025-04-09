package nnt_data.bankaccount_microservice.infrastructure.persistence.mapper;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.model.AccountBase;
import nnt_data.bankaccount_microservice.model.AccountType;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import nnt_data.bankaccount_microservice.model.CustomerType;
import nnt_data.bankaccount_microservice.model.Person;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class AccountMapperImplTest {

    @InjectMocks
    private AccountMapperImpl accountMapper;

    @Test
    void testToEntity() {
        // Arrange
        AccountBase account = createSampleAccountBase();

        // Act
        Mono<AccountBaseEntity> result = accountMapper.toEntity(account);

        // Assert
        StepVerifier.create(result)
                .assertNext(entity -> {
                    assertNotNull(entity);
                    assertEquals(account.getAccountId(), entity.getAccountId());
                    assertEquals(account.getAccountType(), entity.getAccountType());
                    assertEquals(account.getBalance(), entity.getBalance());
                    assertEquals(account.getCustomerId(), entity.getCustomerId());
                    assertEquals(account.getCustomerType(), entity.getCustomerType());
                    assertEquals(account.getCustomerSubType(), entity.getCustomerSubType());
                    assertEquals(account.getOwners().size(), entity.getOwners().size());
                    assertEquals(account.getAuthorizedSigners().size(), entity.getAuthorizedSigners().size());
                    assertEquals(account.getMaintenanceFee(), entity.getMaintenanceFee());
                    assertEquals(account.getFeePerTransaction(), entity.getFeePerTransaction());
                    assertEquals(account.getMovementLimit(), entity.getMovementLimit());
                    assertEquals(account.getMonthlyMovementLimit(), entity.getMonthlyMovementLimit());
                    assertEquals(account.getTransactionMovements(), entity.getTransactionMovements());
                    assertEquals(account.getAllowedDayOfMonth(), entity.getAllowedDayOfMonth());
                    assertEquals(account.getMinimumDailyAverage(), entity.getMinimumDailyAverage());
                })
                .verifyComplete();
    }

    @Test
    void testToDomain() {
        // Arrange
        AccountBaseEntity entity = createSampleAccountBaseEntity();

        // Act
        Mono<AccountBase> result = accountMapper.toDomain(entity);

        // Assert
        StepVerifier.create(result)
                .assertNext(domain -> {
                    assertNotNull(domain);
                    assertEquals(entity.getAccountId(), domain.getAccountId());
                    assertEquals(entity.getAccountType(), domain.getAccountType());
                    assertEquals(entity.getBalance(), domain.getBalance());
                    assertEquals(entity.getCustomerId(), domain.getCustomerId());
                    assertEquals(entity.getCustomerType(), domain.getCustomerType());
                    assertEquals(entity.getCustomerSubType(), domain.getCustomerSubType());
                    assertEquals(entity.getOwners().size(), domain.getOwners().size());
                    assertEquals(entity.getAuthorizedSigners().size(), domain.getAuthorizedSigners().size());
                    assertEquals(entity.getMaintenanceFee(), domain.getMaintenanceFee());
                    assertEquals(entity.getFeePerTransaction(), domain.getFeePerTransaction());
                    assertEquals(entity.getMovementLimit(), domain.getMovementLimit());
                    assertEquals(entity.getMonthlyMovementLimit(), domain.getMonthlyMovementLimit());
                    assertEquals(entity.getTransactionMovements(), domain.getTransactionMovements());
                    assertEquals(entity.getAllowedDayOfMonth(), domain.getAllowedDayOfMonth());
                    assertEquals(entity.getMinimumDailyAverage(), domain.getMinimumDailyAverage());
                })
                .verifyComplete();
    }

    private AccountBase createSampleAccountBase() {
        AccountBase account = new AccountBase();
        account.setAccountId("ACC123");
        account.setAccountType(AccountType.SAVINGS);
        account.setBalance(new BigDecimal("1000.50"));
        account.setCustomerId("CUST456");
        account.setCustomerType(CustomerType.PERSONAL);
        account.setCustomerSubType(CustomerSubtype.VIP);

        Person owner = new Person();
        owner.setName("Juan Pérez");
        owner.setDni("12345678");

        Person signer = new Person();
        signer.setName("Ana García");
        signer.setDni("87654321");

        account.setOwners(Arrays.asList(owner));
        account.setAuthorizedSigners(Arrays.asList(signer));

        account.setMaintenanceFee(new BigDecimal("5.00"));
        account.setFeePerTransaction(new BigDecimal("1.50"));
        account.setMovementLimit(10);
        account.setMonthlyMovementLimit(30);
        account.setTransactionMovements(5);
        account.setAllowedDayOfMonth("15");
        account.setMinimumDailyAverage(500.0);

        return account;
    }

    private AccountBaseEntity createSampleAccountBaseEntity() {
        AccountBaseEntity entity = new AccountBaseEntity();
        entity.setAccountId("ACC456");
        entity.setAccountType(AccountType.CHECKING);
        entity.setBalance(new BigDecimal("2000.75"));
        entity.setCustomerId("CUST789");
        entity.setCustomerType(CustomerType.BUSINESS);
        entity.setCustomerSubType(CustomerSubtype.PYME);

        Person owner = new Person();
        owner.setName("María López");
        owner.setDni("23456789");

        Person signer = new Person();
        signer.setName("Carlos Ruiz");
        signer.setDni("98765432");

        entity.setOwners(Arrays.asList(owner));
        entity.setAuthorizedSigners(Arrays.asList(signer));

        entity.setMaintenanceFee(new BigDecimal("10.00"));
        entity.setFeePerTransaction(new BigDecimal("2.50"));
        entity.setMovementLimit(20);
        entity.setMonthlyMovementLimit(50);
        entity.setTransactionMovements(8);
        entity.setAllowedDayOfMonth("20");
        entity.setMinimumDailyAverage(1000.0);

        return entity;
    }
}