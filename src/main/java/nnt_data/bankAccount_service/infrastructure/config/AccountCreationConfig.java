package nnt_data.bankAccount_service.infrastructure.config;
import nnt_data.bankAccount_service.model.CustomerType;
import nnt_data.bankAccount_service.application.usecase.AccountCreationStrategy;
import nnt_data.bankAccount_service.application.usecase.business.BusinessAccountCreationStrategy;
import nnt_data.bankAccount_service.application.usecase.personal.PersonalAccountCreationStrategy;
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