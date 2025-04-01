package nnt_data.bankAccount_service.domain.validator.account;

import nnt_data.bankAccount_service.domain.validator.AccountTypeValidator;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.AccountType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * SavingsAccountValidator es una implementación de AccountTypeValidator que proporciona
 * validaciones específicas para cuentas de tipo SAVINGS. Verifica que el límite mensual de movimientos
 * sea mayor a 0 y que no haya comisiones de mantenimiento para este tipo de cuenta.
 */
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