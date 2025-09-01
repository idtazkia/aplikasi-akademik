package id.ac.tazkia.smilemahasiswa;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;

import id.ac.tazkia.smilemahasiswa.utility.EntityAuditorAware;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
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

	@Bean
	public MustacheFactory mustacheFactory() {
		return new DefaultMustacheFactory();
	}

	@PostConstruct
	void setUTCTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

}
