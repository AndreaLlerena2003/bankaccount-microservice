package nnt_data.bankAccount_service.infrastructure.config;

import nnt_data.bankAccount_service.application.usecase.AccountUpdateStrategy;
import nnt_data.bankAccount_service.application.usecase.BusinessAccountUpdateStrategy;
import nnt_data.bankAccount_service.application.usecase.PersonalAccountUpdateStrategy;
import nnt_data.bankAccount_service.model.CustomerType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
/**
 * AccountUpdateConfig es una clase de configuración que define los beans necesarios
 * para las estrategias de actualización de cuentas. Utiliza Spring @Configuration y @Bean
 * para registrar las estrategias de actualización de cuentas personales y empresariales
 * en un mapa basado en el tipo de cliente.
 */
@Configuration
public class AccountUpdateConfig {

    @Bean
    public Map<CustomerType, AccountUpdateStrategy> updateStrategies(
            PersonalAccountUpdateStrategy personalStrategy,
                    BusinessAccountUpdateStrategy businessStrategy
         ) {
        return Map.of(
                CustomerType.PERSONAL, personalStrategy,
                CustomerType.BUSINESS, businessStrategy
        );
    }
}
