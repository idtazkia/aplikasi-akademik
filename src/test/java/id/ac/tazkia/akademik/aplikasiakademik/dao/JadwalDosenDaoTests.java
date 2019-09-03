package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JadwalDosenDaoTests {
    @Autowired private JadwalDosenDao jadwalDosenDao;

    @Test
    public void testJadwalDosen() {
        Dosen d = new Dosen();
        d.setId("andangh");

        TahunAkademik ta = new TahunAkademik();
        ta.setId("20191");

        Iterable<JadwalDosen> hasil = jadwalDosenDao.findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(StatusRecord.HAPUS, ta,d);

        Assert.assertNotNull(hasil);
        for (JadwalDosen j : hasil) {
            System.out.println("Matkul : "+j.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
            System.out.println("Hari : "+j.getJadwal().getHari().getNamaHari());
            System.out.println("Dosen : "+j.getDosen().getKaryawan().getNamaKaryawan());
            System.out.println("Status : "+j.getStatusJadwalDosen());
        }
    }
}
