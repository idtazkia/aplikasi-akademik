package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PresensiDosenDaoTests {
    @Autowired private PresensiDosenDao presensiDosenDao;

    @Test
    public void testRekapPresensiDosenSatuMatkul() {
        Jadwal j = new Jadwal();
        j.setId("f715298b-8edc-4b0b-9bf8-8dd4d8614812");

        Dosen d = new Dosen();
        d.setId("Solahudd");

        TahunAkademik ta = new TahunAkademik();
        ta.setId("20191");

        System.out.println("Jumlah kehadiran : "+presensiDosenDao.countByJadwalAndDosenAndTahunAkademikAndStatus(j, d, ta, StatusRecord.AKTIF));

        Iterable<PresensiDosen> hasil = presensiDosenDao.findByJadwalAndDosenAndTahunAkademikAndStatusOrderByWaktuMasuk(j, d, ta, StatusRecord.AKTIF);
        assertNotNull(hasil);
        for (PresensiDosen p : hasil) {
            System.out.println("Mata Kuliah : "+p.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
            System.out.println("Waktu Masuk : "+p.getWaktuMasuk());
            System.out.println("Status Presensi : "+p.getStatusPresensi());
        }
    }

}
