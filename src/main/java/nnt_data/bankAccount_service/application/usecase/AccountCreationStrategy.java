package nnt_data.bankAccount_service.application.usecase;

import nnt_data.bankAccount_service.model.AccountBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface AccountCreationStrategy {
    Mono<AccountBase> createAccount(AccountBase account);
}
