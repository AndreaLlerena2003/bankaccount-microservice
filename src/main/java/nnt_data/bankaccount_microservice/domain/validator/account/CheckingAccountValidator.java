package nnt_data.bankaccount_microservice.domain.validator.account;

import nnt_data.bankaccount_microservice.domain.validator.AccountTypeValidator;
import nnt_data.bankaccount_microservice.model.AccountBase;
import nnt_data.bankaccount_microservice.model.CustomerSubtype;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * CheckingAccountValidator es una implementación de AccountTypeValidator que proporciona
 * validaciones específicas para cuentas de tipo CHECKING. Verifica que la comisión de mantenimiento
 * no sea negativa y que no haya límite de movimientos mensuales para este tipo de cuenta.
 */
@Component
public class CheckingAccountValidator implements AccountTypeValidator {
    @Override
    public Mono<AccountBase> validate(AccountBase account) {
        return this.hasValidLimitsAndFees(account)
                .flatMap(valid -> Mono.just(account))
                .flatMap(acc -> {
                    if (acc.getCustomerSubType() != null && CustomerSubtype.PYME.equals(acc.getCustomerSubType())) {
                        if (acc.getMaintenanceFee().compareTo(BigDecimal.ZERO) > 0) {
                            return Mono.error(new IllegalArgumentException(
                                    "Las cuentas corrientes de clientes PYME no deben tener comision de mantenimiento"));
                        }
                        return hasCreditCard(acc.getCustomerId())
                                .filter(hasCard -> hasCard)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Las Pymes no pueden abrir cuenta sin tarjeta de credito")))
                                .map(hasCard -> acc);
                    }
                    if (acc.getMaintenanceFee() == null || acc.getMaintenanceFee().compareTo(BigDecimal.ZERO) < 0) {
                        return Mono.error(new IllegalArgumentException(
                                "La comisión de mantenimiento no puede ser negativa"));
                    }
                    if(acc.getMonthlyMovementLimit() != null) {
                        return Mono.error(new IllegalArgumentException("Este tipo de cuenta no tiene limite de movimientos para mantenimiento"));
                    }
                    return Mono.just(acc);
                });
    }

}
