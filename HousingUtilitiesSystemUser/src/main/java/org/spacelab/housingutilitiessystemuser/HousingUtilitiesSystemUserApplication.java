package org.spacelab.housingutilitiessystemuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HousingUtilitiesSystemUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(HousingUtilitiesSystemUserApplication.class, args);
	}

}
