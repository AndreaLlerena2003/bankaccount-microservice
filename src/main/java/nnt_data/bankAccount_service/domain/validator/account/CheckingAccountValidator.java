package nnt_data.bankAccount_service.domain.validator.account;

import nnt_data.bankAccount_service.domain.validator.AccountTypeValidator;
import nnt_data.bankAccount_service.model.AccountBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class CheckingAccountValidator implements AccountTypeValidator {
    @Override
    public Mono<AccountBase> validate(AccountBase account) {
            return Mono.just(account)
                    .flatMap(acc -> {
                        if (acc.getMaintenanceFee() == null || acc.getMaintenanceFee().compareTo(BigDecimal.ZERO) < 0) {
                            return Mono.error(new IllegalArgumentException(
                                    "La comisión de mantenimiento no puede ser negativa"));
                        }
                        if(acc.getMonthlyMovementLimit() != null) {
                            return Mono.error(new IllegalArgumentException("Este tipo de cuenta no tiene limite de movimientos"));
                        }
                        return Mono.just(acc);
                    });
    }
}
