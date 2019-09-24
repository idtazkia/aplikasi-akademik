package id.ac.tazkia.akademik.aplikasiakademik;

import id.ac.tazkia.akademik.aplikasiakademik.service.RekapPresensiService;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thymeleaf.dialect.springdata.SpringDataDialect;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class AplikasiAkademikApplication implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AplikasiAkademikApplication.class);

	public static void main(String[] args) {
        LOGGER.debug("Mulai menjalankan aplikasinya");
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
    @Value("${runOnStartup:#{false}}") private Boolean runOnStartup;

	@Override
	public void run(String... args) throws Exception {
        if (runOnStartup) {
            LOGGER.info("Isi Rekap Presensi Dosen");

            LocalDate i = LocalDate.of(2019,9,1);
            LocalDate tanggalAkhir = LocalDate.now();

            while(i.isBefore(tanggalAkhir)) {
                //rekapPresensiService.isiRekap(i);
                rekapPresensiService.isiRekapPresensiDosen(i);
                i = i.plusDays(1);
            }
        }


	}

	@PostConstruct
	void setUTCTimeZone(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}

