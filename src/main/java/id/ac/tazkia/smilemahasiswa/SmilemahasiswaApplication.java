package id.ac.tazkia.smilemahasiswa;

import id.ac.tazkia.smilemahasiswa.utility.EntityAuditorAware;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class SmilemahasiswaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmilemahasiswaApplication.class, args);
	}

	@Bean
	public SpringDataDialect springDataDialect() {
		return new SpringDataDialect();
	}

	@Bean
	public LayoutDialect layoutDialect() {
		return new LayoutDialect();
	}

	@Bean
	public AuditorAware<String> auditorProvider() {
		return new EntityAuditorAware();
	}

	@PostConstruct
	void setUTCTimeZone(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}


	}
