package nnt_data.bankAccount_service.domain.validator.account;

import nnt_data.bankAccount_service.domain.validator.AccountTypeValidator;
import nnt_data.bankAccount_service.model.AccountBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * FixedTermAccountValidator es una implementación de AccountTypeValidator que proporciona
 * validaciones específicas para cuentas de tipo FIXED_TERM. Verifica que se haya especificado
 * el día de retiro permitido.
 */
@Component
public class FixedTermAccountValidator implements AccountTypeValidator {

    @Override
    public Mono<AccountBase> validate(AccountBase account) {
        return Mono.just(account)
                .flatMap(acc -> {
                    if (acc.getAllowedDayOfMonth() == null) {
                        return Mono.error(new IllegalArgumentException("Debe especificar el dia de retiro permitido"));
                    }
                    return Mono.just(acc);
                });
    }
}