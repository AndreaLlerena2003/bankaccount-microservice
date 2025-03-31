package nnt_data.bankAccount_service.domain.port;

import nnt_data.bankAccount_service.model.AccountBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountOperationsPort {
    Mono<AccountBase> createAccount(AccountBase accountBase);
    Mono<AccountBase> updateAccount(String accountId,AccountBase updatedAccount);
    Mono<AccountBase> findAccount(String accountId);
    Flux<AccountBase> findAllAccounts();
    Mono<Void> deleteAccount(String accountId);
}
