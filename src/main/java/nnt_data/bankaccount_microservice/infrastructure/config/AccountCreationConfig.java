package nnt_data.bankaccount_microservice.infrastructure.config;
import nnt_data.bankaccount_microservice.model.CustomerType;
import nnt_data.bankaccount_microservice.application.usecase.AccountCreationStrategy;
import nnt_data.bankaccount_microservice.application.usecase.business.BusinessAccountCreationStrategy;
import nnt_data.bankaccount_microservice.application.usecase.personal.PersonalAccountCreationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
/**
 * AccountCreationConfig es una clase de configuración que define los beans necesarios
 * para las estrategias de creación de cuentas. Utiliza un mapa para asociar cada tipo
 * de cliente con su estrategia de creación correspondiente.
 */
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