package id.ac.tazkia.akademik.aplikasiakademik;

import id.ac.tazkia.akademik.aplikasiakademik.service.RekapPresensiService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TerjadwalTest {

    @Autowired
    private RekapPresensiService rekapPresensiService;

    @Test
    public void testIsiRekapAbsen() {
        rekapPresensiService.isiRekap(LocalDate.of(2018,10,23));
    }

}
