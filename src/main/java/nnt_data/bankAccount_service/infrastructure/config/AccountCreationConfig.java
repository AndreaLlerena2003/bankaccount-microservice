package nnt_data.bankAccount_service.infrastructure.config;
import nnt_data.bankAccount_service.model.CustomerType;
import nnt_data.bankAccount_service.application.usecase.AccountCreationStrategy;
import nnt_data.bankAccount_service.application.usecase.BusinessAccountCreationStrategy;
import nnt_data.bankAccount_service.application.usecase.PersonalAccountCreationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AccountCreationConfig {

    @Bean
    public Map<CustomerType, AccountCreationStrategy> creationStrategies(
            PersonalAccountCreationStrategy personalStrategy,
            BusinessAccountCreationStrategy businessStrategy) {
        return Map.of(
                CustomerType.PERSONAL, personalStrategy,
                CustomerType.BUSINESS, businessStrategy
        );
    }
}