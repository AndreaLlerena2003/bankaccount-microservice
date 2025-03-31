package nnt_data.bankAccount_service.infrastructure.persistence.mapper;

import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.model.AccountBase;
import reactor.core.publisher.Mono;

public interface AccountMapper {
    Mono<AccountBaseEntity> toEntity(AccountBase account);
    Mono<AccountBase> toDomain(AccountBaseEntity accountBaseEntity);
}
