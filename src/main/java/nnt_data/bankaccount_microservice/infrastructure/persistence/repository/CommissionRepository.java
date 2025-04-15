package nnt_data.bankaccount_microservice.infrastructure.persistence.repository;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.CommissionEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.Date;

public interface CommissionRepository extends ReactiveMongoRepository<CommissionEntity, String> {
    /**
     * Busca comisiones por ID de cuenta y rango de fechas.
     *
     * @param accountId ID de la cuenta
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Flux de entidades de comisión
     */
    Flux<CommissionEntity> findByAccountIdAndDateTimeBetween(
            String accountId, Date  startDate, Date endDate);
}
