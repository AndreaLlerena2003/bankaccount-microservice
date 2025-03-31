package nnt_data.bankAccount_service.domain.validator.account;

import nnt_data.bankAccount_service.domain.validator.AccountTypeValidator;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SavingsAccountValidator implements AccountTypeValidator {

    @Override
    public Mono<AccountBase> validate(AccountBase account) {
        return Mono.just(account)
                .flatMap(acc -> {
                    if (acc.getMonthlyMovementLimit() == null || acc.getMonthlyMovementLimit() < 1) {
                        return Mono.error(new IllegalArgumentException(
                                "El límite mensual de movimientos debe ser mayor a 0"));
                    }

                    if (acc.getMaintenanceFee() != null) {
                        return Mono.error(new IllegalArgumentException(
                                "Las cuentas de Ahorro no tienen comisiones"));
                    }
                    return Mono.just(acc);
                });
    }

}