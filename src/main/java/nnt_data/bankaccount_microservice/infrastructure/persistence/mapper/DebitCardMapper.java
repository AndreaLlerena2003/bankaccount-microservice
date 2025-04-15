package nnt_data.bankaccount_microservice.infrastructure.persistence.mapper;

import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.DebitCardEntity;
import nnt_data.bankaccount_microservice.model.DebitCard;
import reactor.core.publisher.Mono;

public interface DebitCardMapper {
    Mono<DebitCardEntity> toEntity(DebitCard debitCard);
    Mono<DebitCard> toDomain(DebitCardEntity debitCardEntity);
}
