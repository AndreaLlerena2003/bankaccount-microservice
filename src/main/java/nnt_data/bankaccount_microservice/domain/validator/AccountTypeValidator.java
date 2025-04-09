package nnt_data.bankaccount_microservice.domain.validator;

import nnt_data.bankaccount_microservice.model.AccountBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


/**
 * AccountTypeValidator es una interfaz que extiende Validator y proporciona
 * validaciones específicas para tipos de cuentas bancarias.
 */
public interface AccountTypeValidator extends Validator<AccountBase> {

    Logger log = LoggerFactory.getLogger(AccountTypeValidator.class);
    WebClient webClient = WebClient.create("http://localhost:8081");

    default Mono<Boolean> hasCreditCard(String customerId){
        return webClient.get()
                .uri("credits/customer/{customerId}", customerId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(result -> log.info("Respuesta de la API: {}", result))
                .doOnError(error -> log.error("Error en la API: {}", error.getMessage()));
    }

    default Mono<Boolean> hasValidLimitsAndFees(AccountBase account){
        if(account.getMovementLimit() == null || account.getFeePerTransaction() == null){
            return Mono.error(new IllegalArgumentException("Los limites y comisiones no pueden ser nulos"));
        }
        if(account.getTransactionMovements() == null){ //implica una nueva creacion
            account.setTransactionMovements(0);
        }
        return Mono.just(true);
    }

    default Mono<Boolean> hasValidFees(AccountBase account){
        if(account.getFeePerTransaction() == null){
            return Mono.error(new IllegalArgumentException("Los fees y comisiones no pueden ser nulos"));
        }
        if (account.getTransactionMovements() == null){
            account.setTransactionMovements(0);
        }
        return Mono.just(true);
    }
}
