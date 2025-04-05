package nnt_data.bankAccount_service.domain.validator.account;

import nnt_data.bankAccount_service.domain.validator.AccountTypeValidator;
import nnt_data.bankAccount_service.model.AccountBase;
import nnt_data.bankAccount_service.model.CustomerSubtype;
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
        return hasValidLimitsAndFees(account).flatMap(valid -> Mono.just(account))
                .flatMap(acc -> {
                    if (acc.getMonthlyMovementLimit() == null || acc.getMonthlyMovementLimit() < 1) {
                        return Mono.error(new IllegalArgumentException(
                                "El límite mensual de movimientos debe ser mayor a 0"));
                    }
                    if (acc.getMaintenanceFee() != null) {
                        return Mono.error(new IllegalArgumentException(
                                "Las cuentas de Ahorro no tienen comisiones"));
                    }
                    if(acc.getCustomerSubType() == CustomerSubtype.VIP){
                        //validacion de que tenga tarjeta de credito
                        if(acc.getMinimumDailyAverage() == null || acc.getMinimumDailyAverage() <= 0) {
                            return Mono.error(new IllegalArgumentException(
                                    "La cuenta de ahorro debe especificar un monto mínimo de promedio diario"));
                        }
                        return hasCreditCard(acc.getCustomerId())
                                .filter(hasCard -> hasCard)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Los Clientes VIP no pueden abrir cuenta de ahorro sin tarjeta de credito")))
                                .map(hasCard -> acc);
                    }
                    return Mono.just(acc);
                });
    }

}