package by.delmark.portal.labor_cost_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@EnableCaching
public class LaborCostBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(LaborCostBotApplication.class, args);
	}

}
