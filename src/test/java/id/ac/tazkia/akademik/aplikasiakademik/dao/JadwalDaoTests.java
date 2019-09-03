package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.KelasDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JadwalDaoTests {
    @Autowired private JadwalDao jadwalDao;
    @Autowired private TahunAkademikProdiDao tahunAkademikProdiDao;
    @Autowired private ProgramDao programDao;
    @Autowired private TahunAkademikDao tahunAkademikDao;
    @Autowired private KelasMahasiswaDao kelasMahasiswaDao;

    @Test
    public void testPloting() {
        System.out.println("Test plotting");
        System.out.println("=====================");
        TahunAkademikProdi tap = tahunAkademikProdiDao.findById("2019101")
                .orElseThrow(() -> new IllegalStateException("Tidak ditemukan"));
        System.out.println("Tahun Akademik Prodi : "+tap.getProdi().getNamaProdi());

        System.out.println("=====================");
        Program program = programDao.findById("01")
                .orElseThrow(() -> new IllegalStateException("Tidak ditemukan"));
        System.out.println("Program : "+program.getNamaProgram());

        System.out.println("=====================");

        List<PlotingDto> hasil = jadwalDao.ploting(tap.getProdi(), StatusRecord.HAPUS, tap);
        System.out.println("Jumlah plotting : "+hasil.size());

        System.out.println("=====================");
        List<KelasDto> daftarKelas = kelasMahasiswaDao.cariKelas(StatusRecord.AKTIF);
        System.out.println("Jumlah kelas : " +daftarKelas.size());
    }

    @Test
    public void testSchedule() {
        TahunAkademikProdi ta = tahunAkademikProdiDao.findById("2019101").get();

        Hari hari = new Hari();
        hari.setId("1");
        Program program = new Program();
        program.setId("01");
        List<PlotingDto> hasil = jadwalDao.schedule(ta.getProdi(), StatusRecord.HAPUS, ta, hari, program);
        Assert.assertNotNull(hasil);

    }
}
