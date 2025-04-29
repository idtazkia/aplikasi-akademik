package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class JadwalService {

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private PresensiService presensiService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private RuanganDao ruanganDao;

    public Jadwal createJadwalKonversi(MatakuliahKurikulum matakuliahKurikulum, Prodi prodi) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Jadwal jadwal = new Jadwal();
        jadwal.setFinalStatus("N");
        jadwal.setJumlahSesi(1);
        jadwal.setBobotUts(BigDecimal.ZERO);
        jadwal.setBobotUas(BigDecimal.ZERO);
        jadwal.setBobotTugas(BigDecimal.ZERO);
        jadwal.setBobotPresensi(BigDecimal.ZERO);
        jadwal.setDosen(prodi.getDosen());
        jadwal.setProdi(prodi);
        jadwal.setTahunAkademik(tahunAkademik);
        jadwal.setTahunAkademikProdi(tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, prodi));
        jadwal.setMatakuliahKurikulum(matakuliahKurikulum);
        jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
        jadwal.setProgram(programDao.findById("01").get());
        jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
        jadwal.setHari(hariDao.findById("0").get());
        jadwal.setKelas(kelasDao.findById("-").get());
        jadwal.setRuangan(ruanganDao.findById("01").get());
        jadwal.setStatus(StatusRecord.KONVERSI);
        jadwalDao.save(jadwal);

        return jadwal;
    }

    @Scheduled(cron = "0 00 05 * * *", zone = "Asia/Jakarta")
    public void plotingJadwal1() throws Exception {

        List<Jadwal> cekJadwal = jadwalDao.findByStatusAndTahunAkademikAndFinalPloting(StatusRecord.AKTIF,
                tahunAkademikDao.findByStatus(StatusRecord.AKTIF), "FINAL");
        for (Jadwal j : cekJadwal) {
            System.out.println(j.getId() + " : id jadwal");
            presensiService.generateSesi2(j);

        }
    }

    @Scheduled(cron = "0 30 16 * * *", zone = "Asia/Jakarta")
    public void plotingJadwal2() throws Exception {

        List<Jadwal> cekJadwal = jadwalDao.findByStatusAndTahunAkademikAndFinalPloting(StatusRecord.AKTIF,
                tahunAkademikDao.findByStatus(StatusRecord.AKTIF), "FINAL");
        for (Jadwal j : cekJadwal) {
            presensiService.generateSesi2(j);

        }
    }

    @Scheduled(cron = "0 00 22 * * *", zone = "Asia/Jakarta")
    public void regenerateJadwal() throws Exception {

        List<Jadwal> cekJadwal = jadwalDao.findByStatusAndTahunAkademikAndFinalPloting(StatusRecord.AKTIF, tahunAkademikDao.findByStatus(StatusRecord.AKTIF), "REGENERATE");
        for (Jadwal j : cekJadwal) {
            System.out.println(j.getId() + " : id jadwal");
            presensiService.updateSesi1(j);
        }
    }

}
