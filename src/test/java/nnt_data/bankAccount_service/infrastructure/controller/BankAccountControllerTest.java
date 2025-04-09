package nnt_data.bankAccount_service.infrastructure.controller;

import nnt_data.bankAccount_service.application.port.AccountOperationsPort;
import nnt_data.bankAccount_service.application.port.TransactionOperationsPort;
import nnt_data.bankAccount_service.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountControllerTest {

    @Mock
    private AccountOperationsPort accountOperationsPort;

    @Mock
    private TransactionOperationsPort transactionOperationsPort;

    @InjectMocks
    private BankAccountController bankAccountController;

    private WebTestClient webTestClient;
    private AccountBase accountBase;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(bankAccountController).build();


        accountBase = new AccountBase();
        accountBase.setAccountId("acc-123");
        accountBase.setCustomerId("cust-456");
        accountBase.setBalance(new BigDecimal("1000.00"));
        accountBase.setCustomerType(CustomerType.PERSONAL);
        accountBase.setAccountType(AccountType.SAVINGS);
        accountBase.setCustomerSubType(CustomerSubtype.REGULAR);
        accountBase.setTransactionMovements(10);
        accountBase.setFeePerTransaction(new BigDecimal("5.00"));


        transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setAccountId("acc-123");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setType(Transaction.TypeEnum.DEPOSIT);
    }

    @Test
    void createAccount_Success() {
        // Arrange
        when(accountOperationsPort.createAccount(any(AccountBase.class))).thenReturn(Mono.just(accountBase));

        // Act & Assert
        webTestClient.post().uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountBase), AccountBase.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .value(response -> {
                    assertEquals("Cuenta creada exitosamente", response.get("message"));
                    assertEquals("acc-123", response.get("account_id"));
                });

        verify(accountOperationsPort).createAccount(any(AccountBase.class));
    }

    @Test
    void createTransaction_Success() {
        // Arrange
        when(transactionOperationsPort.createTransaction(any(Transaction.class))).thenReturn(Mono.just(transaction));

        // Act & Assert
        webTestClient.post().uri("/accounts/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(transaction), Transaction.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Map.class)
                .value(response -> {
                    assertEquals("Transacción creada exitosamente", response.get("message"));
                    assertNotNull(response.get("transaction_id"));
                });

        verify(transactionOperationsPort).createTransaction(any(Transaction.class));
    }

    @Test
    void deleteAccount_Success() {
        // Arrange
        when(accountOperationsPort.deleteAccount(anyString())).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete().uri("/accounts/{accountId}", "acc-123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    assertEquals("Cuenta eliminada exitosamente", response.get("message"));
                    assertEquals("acc-123", response.get("accountId"));
                });

        verify(accountOperationsPort).deleteAccount("acc-123");
    }

    @Test
    void getAccountById_Success() {
        // Arrange
        when(accountOperationsPort.findAccount(anyString())).thenReturn(Mono.just(accountBase));

        // Act & Assert
        webTestClient.get().uri("/accounts/{accountId}", "acc-123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    Map<String, Object> accountMap = (Map<String, Object>) response.get("accounts");
                    assertNotNull(accountMap);
                    assertEquals("acc-123", accountMap.get("accountId"));
                });

        verify(accountOperationsPort).findAccount("acc-123");
    }

    @Test
    void getAllAccounts_Success() {
        // Arrange
        AccountBase accountBase2 = new AccountBase();
        accountBase2.setAccountId("acc-789");
        accountBase2.setCustomerId("cust-456");
        accountBase2.setBalance(new BigDecimal("2000.00"));
        accountBase2.setCustomerType(CustomerType.BUSINESS);
        accountBase2.setAccountType(AccountType.SAVINGS);

        List<AccountBase> accounts = Arrays.asList(accountBase, accountBase2);
        when(accountOperationsPort.findAllAccounts()).thenReturn(Flux.fromIterable(accounts));

        // Act & Assert
        webTestClient.get().uri("/accounts/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    List<Map<String, Object>> accountsList = (List<Map<String, Object>>) response.get("accounts");
                    assertNotNull(accountsList);
                    assertEquals(2, accountsList.size());
                    assertEquals("acc-123", accountsList.get(0).get("accountId"));
                    assertEquals("acc-789", accountsList.get(1).get("accountId"));
                });

        verify(accountOperationsPort).findAllAccounts();
    }

    @Test
    void getAllTransactions_Success() {
        // Arrange
        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID().toString());
        transaction2.setAccountId("acc-789");
        transaction2.setAmount(new BigDecimal("50.00"));
        transaction2.setType(Transaction.TypeEnum.WITHDRAWAL);

        List<Transaction> transactions = Arrays.asList(transaction, transaction2);
        when(transactionOperationsPort.getTransactions()).thenReturn(Flux.fromIterable(transactions));

        // Act & Assert
        webTestClient.get().uri("/accounts/transactions")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    List<Map<String, Object>> transactionsList = (List<Map<String, Object>>) response.get("transactions");
                    assertNotNull(transactionsList);
                    assertEquals(2, transactionsList.size());
                    assertEquals("acc-123", transactionsList.get(0).get("accountId"));
                    assertEquals("acc-789", transactionsList.get(1).get("accountId"));
                });

        verify(transactionOperationsPort).getTransactions();
    }

    @Test
    void getTransactionsByAccountId_Success() {
        // Arrange
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionOperationsPort.getTransactionsAccountId(anyString())).thenReturn(Flux.fromIterable(transactions));

        // Act & Assert
        webTestClient.get().uri("/accounts/{accountId}/transactions", "acc-123")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    List<Map<String, Object>> transactionsList = (List<Map<String, Object>>) response.get("transactions");
                    assertNotNull(transactionsList);
                    assertEquals(1, transactionsList.size());
                    assertEquals("acc-123", transactionsList.get(0).get("accountId"));
                });

        verify(transactionOperationsPort).getTransactionsAccountId("acc-123");
    }

    @Test
    void updateAccount_Success() {
        // Arrange
        when(accountOperationsPort.updateAccount(anyString(), any(AccountBase.class))).thenReturn(Mono.just(accountBase));

        // Act & Assert
        webTestClient.put().uri("/accounts/{accountId}", "acc-123")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountBase), AccountBase.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Map.class)
                .value(response -> {
                    assertEquals("Cuenta actualizada exitosamente", response.get("message"));
                    assertEquals("acc-123", response.get("account_id"));
                });

        verify(accountOperationsPort).updateAccount(eq("acc-123"), any(AccountBase.class));
    }

    @Test
    void createAccount_Error() {
        // Arrange
        when(accountOperationsPort.createAccount(any(AccountBase.class)))
                .thenReturn(Mono.error(new RuntimeException("Error al crear la cuenta")));

        // Act & Assert
        webTestClient.post().uri("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(accountBase), AccountBase.class)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(accountOperationsPort).createAccount(any(AccountBase.class));
    }

    @Test
    void createTransaction_Error() {
        // Arrange
        when(transactionOperationsPort.createTransaction(any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("Saldo insuficiente")));

        // Act & Assert
        webTestClient.post().uri("/accounts/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(transaction), Transaction.class)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(transactionOperationsPort).createTransaction(any(Transaction.class));
    }

}