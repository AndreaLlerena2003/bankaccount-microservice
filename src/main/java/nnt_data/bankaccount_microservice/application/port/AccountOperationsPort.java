package nnt_data.bankaccount_microservice.application.port;

import nnt_data.bankaccount_microservice.model.AccountBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountOperationsPort {
    Mono<AccountBase> createAccount(AccountBase accountBase);
    Mono<AccountBase> updateAccount(String accountId,AccountBase updatedAccount);
    Mono<AccountBase> findAccount(String accountId);
    Flux<AccountBase> findAllAccounts();
    Mono<Void> deleteAccount(String accountId);
    Mono<Boolean> existsById(String accountId);
}

/**
 * Interfaz AccountOperationsPort
 * Define las operaciones relacionadas con cuentas bancarias utilizando un enfoque reactivo.
 * Las operaciones disponibles incluyen:
 * - Crear una nueva cuenta bancaria.
 * - Actualizar una cuenta existente por su ID.
 * - Buscar una cuenta específica por su ID.
 * - Obtener todas las cuentas disponibles.
 * - Eliminar una cuenta por su ID.
 *
 * Se emplean tipos reactivos (Mono y Flux) para manejar datos de manera asíncrona y escalable.
 */