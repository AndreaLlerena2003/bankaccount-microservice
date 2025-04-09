package nnt_data.bankaccount_microservice.infrastructure.persistence.repository;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

/**
 * Interfaz TransactionRepository que extiende ReactiveMongoRepository para manejar
 * operaciones CRUD de manera reactiva sobre la colección de transacciones en MongoDB.
 *
 * - Utiliza TransactionEntity como la entidad y String como el tipo de ID.
 * - Define un método personalizado findByAccountId para recuperar todas las transacciones
 *   asociadas a un accountId específico, devolviendo un Flux<TransactionEntity>.
 *
 * Beneficios:
 * - Reactividad: Manejo eficiente y no bloqueante de grandes volúmenes de datos.
 * - Simplicidad: Definición sencilla y declarativa de métodos de consulta personalizados.
 */
public interface TransactionRepository extends ReactiveMongoRepository<TransactionEntity, String> {

    Flux<TransactionEntity> findByAccountId(String accountId);
}
