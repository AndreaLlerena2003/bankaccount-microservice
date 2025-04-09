package nnt_data.bankaccount_microservice.domain.validator.account;

import nnt_data.bankaccount_microservice.domain.validator.AccountTypeValidator;
import nnt_data.bankaccount_microservice.model.AccountBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * FixedTermAccountValidator es una implementación de AccountTypeValidator que proporciona
 * validaciones específicas para cuentas de tipo FIXED_TERM. Verifica que se haya especificado
 * el día de retiro permitido.
 */
@Component
public class
FixedTermAccountValidator implements AccountTypeValidator {
//solo tiene un movimeitno de 1 y comision si se llena
    @Override
    public Mono<AccountBase> validate(AccountBase account) {
        return this.hasValidFees(account)
                .flatMap(valid -> Mono.just(account))
                .flatMap(acc -> {
                    acc.setMovementLimit(1);
                    if (acc.getAllowedDayOfMonth() == null) {
                        return Mono.error(new IllegalArgumentException("Debe especificar el dia de retiro permitido"));
                    }
                    return Mono.just(acc);
                });
    }
}