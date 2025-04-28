package nnt_data.bankaccount_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BankAccountApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankAccountApiApplication.class, args);
	}

}
