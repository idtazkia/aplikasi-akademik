package id.ac.tazkia.akademik.aplikasiakademik;

import id.ac.tazkia.akademik.aplikasiakademik.service.RekapPresensiService;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

import java.time.LocalDate;

@SpringBootApplication
@EnableScheduling
public class AplikasiAkademikApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(AplikasiAkademikApplication.class, args);
	}

	@Bean
	public SpringDataDialect springDataDialect() {
		return new SpringDataDialect();
	}

	@Bean
	public LayoutDialect layoutDialect() {
		return new LayoutDialect();
	}

	@Autowired private RekapPresensiService rekapPresensiService;

	@Override
	public void run(String... args) throws Exception {
		LocalDate i = LocalDate.of(2018,10,1);
		LocalDate tanggalAkhir = LocalDate.now();

		while(i.isBefore(tanggalAkhir)) {
			//rekapPresensiService.isiRekap(i);
			i = i.plusDays(1);
		}

	}
}

