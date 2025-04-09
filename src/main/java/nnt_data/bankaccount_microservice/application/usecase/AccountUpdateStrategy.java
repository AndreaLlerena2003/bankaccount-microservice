package nnt_data.bankaccount_microservice.application.usecase;

import nnt_data.bankaccount_microservice.model.AccountBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface AccountUpdateStrategy {
    Mono<AccountBase> updateAccount(String accountId, AccountBase account) ;
}

/**
 * Interfaz AccountUpdateStrategy
 * Define una estrategia para la actualización de cuentas bancarias utilizando un enfoque reactivo.
 *
 * Método disponible:
 * - updateAccount(String accountId, AccountBase account): Actualiza una cuenta existente identificada por su ID
 *   y devuelve un Mono que contiene la información actualizada de la cuenta.
 *
 * Utiliza el decorador @Component para integrarse en el contexto de Spring Framework y permitir la inyección
 * de dependencias de manera eficiente.
 */