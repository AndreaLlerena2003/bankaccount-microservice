package nnt_data.bankaccount_microservice.application.usecase;

import nnt_data.bankaccount_microservice.model.AccountBase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface AccountCreationStrategy {
    Mono<AccountBase> createAccount(AccountBase account);
}

/**
 * Interfaz AccountCreationStrategy
 * Define una estrategia para la creación de cuentas bancarias utilizando un enfoque reactivo.
 *
 * Método disponible:
 * - createAccount(AccountBase account): Crea una nueva cuenta bancaria y devuelve un Mono que contiene la información de la cuenta creada.
 *
 * Utiliza el decorador @Component para facilitar la inyección de dependencias en el contexto de Spring Framework.
 */