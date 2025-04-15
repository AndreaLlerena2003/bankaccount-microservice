package nnt_data.bankaccount_microservice.infrastructure.persistence.repository;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.DebitCardEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCardEntity, String> {
    Mono<DebitCardEntity> findByCardNumber(String cardNumber);
}
