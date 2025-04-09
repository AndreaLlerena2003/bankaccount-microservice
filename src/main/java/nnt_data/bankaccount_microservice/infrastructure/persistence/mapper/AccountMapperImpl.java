package nnt_data.bankaccount_microservice.infrastructure.persistence.mapper;

import lombok.RequiredArgsConstructor;
import nnt_data.bankaccount_microservice.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankaccount_microservice.model.AccountBase;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * AccountMapperImpl es una implementación de AccountMapper que proporciona métodos para mapear
 * entre entidades de cuenta y objetos de dominio. Utiliza BeanUtils para copiar propiedades
 * y Reactor Mono para manejar el mapeo de manera asíncrona.
 */
@Component
@RequiredArgsConstructor
public class AccountMapperImpl implements AccountMapper {

    @Override
    public Mono<AccountBaseEntity> toEntity(AccountBase account) {
        AccountBaseEntity accountBaseEntity = new AccountBaseEntity();
        BeanUtils.copyProperties(account, accountBaseEntity);
        return Mono.just(accountBaseEntity);
    }

    @Override
    public Mono<AccountBase> toDomain(AccountBaseEntity accountBaseEntity) {
        AccountBase accountBase = new AccountBase();
        BeanUtils.copyProperties(accountBaseEntity, accountBase);
        return Mono.just(accountBase);
    }

}