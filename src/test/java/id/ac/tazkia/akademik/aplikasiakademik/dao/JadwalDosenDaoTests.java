package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapJadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.apache.commons.collections4.IterableUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

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

        assertNotNull(hasil);
        for (JadwalDosen j : hasil) {
            System.out.println("Matkul : "+j.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
            System.out.println("Hari : "+j.getJadwal().getHari().getNamaHari());
            System.out.println("Dosen : "+j.getDosen().getKaryawan().getNamaKaryawan());
            System.out.println("Status : "+j.getStatusJadwalDosen());
        }
    }


    @Test
    public void testRekapJadwalDosen() {
        TahunAkademik ta = new TahunAkademik();
        ta.setId("20191");
        Page<RekapJadwalDosenDto> rekap = jadwalDosenDao
                .rekapJadwalDosen(StatusJadwalDosen.PENGAMPU, ta, StatusRecord.AKTIF, PageRequest.of(0, 100));
//        Assert.assertNotNull(rekap);

        Map<String, RekapSksDosenDto> rekapJumlahSks = new LinkedHashMap<>();
        for (RekapJadwalDosenDto r : rekap.getContent()) {
            System.out.println(r);
            RekapSksDosenDto rsks = rekapJumlahSks.get(r.getIdDosen());
            if (rsks == null) {
                rsks = new RekapSksDosenDto();
                rsks.setNamaDosen(r.getNamaDosen());
                rsks.setIdDosen(r.getIdDosen());
                rsks.setTotalSks(0);
            }

            rsks.tambahSks(r.getSks());
            rekapJumlahSks.put(r.getIdDosen(), rsks);
        }

        System.out.println("Dosen | SKS");
        for (String dosen : rekapJumlahSks.keySet()) {
            RekapSksDosenDto rs = rekapJumlahSks.get(dosen);
            System.out.println(rs.getIdDosen() +"/"+ rs.getNamaDosen() + " | " + rekapJumlahSks.get(dosen));
        }


    }

    @Test
    public void testRekapJadwalPerDosen() {
        TahunAkademik ta = new TahunAkademik();
        ta.setId("20191");

        Dosen dosen = new Dosen();
        dosen.setId("masgrandi");

        Page<RekapJadwalDosenDto> rekap = jadwalDosenDao.rekapJadwalPerDosen(dosen, StatusJadwalDosen.PENGAMPU, ta, StatusRecord.AKTIF, PageRequest.of(0,1000));
        for (RekapJadwalDosenDto r : rekap.getContent()) {
            System.out.println(r);
        }
    }

    @Test
    public void testCariJadwalDosen() {
        Jadwal j = new Jadwal();
        j.setId("6165");

        Dosen dosen = new Dosen();
        dosen.setId("sfatma");

        JadwalDosen jd = jadwalDosenDao.findByJadwalAndDosen(j, dosen);
        assertNotNull(jd);
        System.out.println("Matakuliah : "+jd.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
        System.out.println("Status Jadwal : "+jd.getStatusJadwalDosen());

    }
}
