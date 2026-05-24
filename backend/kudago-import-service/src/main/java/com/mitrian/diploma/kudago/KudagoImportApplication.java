package com.mitrian.diploma.kudago;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.mitrian.diploma.kudago")
@EntityScan(basePackages = {
	"com.mitrian.diploma.voting.catalog.entity",
	"com.mitrian.diploma.voting.room.filter.entity"
})
@EnableJpaRepositories(basePackages = {
	"com.mitrian.diploma.voting.catalog.repository",
	"com.mitrian.diploma.voting.room.filter.repository"
})
public class KudagoImportApplication {

	public static void main(String[] args) {
		SpringApplication.run(KudagoImportApplication.class, args);
	}
}
