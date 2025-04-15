package nnt_data.bankaccount_microservice.infrastructure.persistence.mapper;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.DebitCardEntity;
import nnt_data.bankaccount_microservice.model.DebitCard;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DebitCardMapperImpl implements DebitCardMapper{
    @Override
    public Mono<DebitCardEntity> toEntity(DebitCard debitCard) {
        DebitCardEntity debitCardEntity = new DebitCardEntity();
        BeanUtils.copyProperties(debitCard, debitCardEntity);
        return Mono.just(debitCardEntity);
    }

    @Override
    public Mono<DebitCard> toDomain(DebitCardEntity debitCardEntity){
        DebitCard debitCard = new DebitCard();
        BeanUtils.copyProperties(debitCardEntity, debitCard);
        return Mono.just(debitCard);
    }

}
