package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
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
        List<Jadwal> hasil = jadwalDao.ploting(tap.getProdi(), StatusRecord.HAPUS, tap);
        System.out.println("Jumlah plotting : "+hasil.size());

        System.out.println("=====================");
        List<KelasMahasiswa> daftarKelas = kelasMahasiswaDao.cariKelas(StatusRecord.AKTIF);
        System.out.println("Jumlah kelas : " +daftarKelas.size());
    }
}
