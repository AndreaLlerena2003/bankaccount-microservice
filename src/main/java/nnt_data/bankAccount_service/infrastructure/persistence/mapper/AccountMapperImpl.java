package nnt_data.bankAccount_service.infrastructure.persistence.mapper;

import lombok.RequiredArgsConstructor;
import nnt_data.bankAccount_service.infrastructure.persistence.entity.AccountBaseEntity;
import nnt_data.bankAccount_service.model.AccountBase;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


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