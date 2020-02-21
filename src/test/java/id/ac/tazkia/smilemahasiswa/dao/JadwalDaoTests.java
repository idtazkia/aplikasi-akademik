package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Hari;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JadwalDaoTests {
    @Autowired private JadwalDao jadwalDao;

    @Test @Ignore
    public void testSchedule() {
        Prodi p = new Prodi();
        p.setId("04");

        TahunAkademik ta = new TahunAkademik();
        ta.setId("20192");

        List<StatusRecord> statusRecords = Arrays.asList(StatusRecord.HAPUS);

        Hari hari = new Hari();
        hari.setId("4");

        jadwalDao.schedule(p, statusRecords, ta, hari);
    }
}
