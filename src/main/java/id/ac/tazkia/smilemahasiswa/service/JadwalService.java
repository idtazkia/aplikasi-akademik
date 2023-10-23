package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JadwalService {

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private PresensiService presensiService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Scheduled(cron = "0 00 05 * * *", zone = "Asia/Jakarta")
    public void plotingJadwal1() throws Exception {

        List<Jadwal> cekJadwal = jadwalDao.findByStatusAndTahunAkademikAndFinalPloting(StatusRecord.AKTIF,
                tahunAkademikDao.findByStatus(StatusRecord.AKTIF), "FINAL");
        for (Jadwal j : cekJadwal) {
            System.out.println(j.getId() + " : id jadwal");
            presensiService.generateSesi2(j);

        }
    }

    @Scheduled(cron = "0 00 17 * * *", zone = "Asia/Jakarta")
    public void plotingJadwal2() throws Exception {

        List<Jadwal> cekJadwal = jadwalDao.findByStatusAndTahunAkademikAndFinalPloting(StatusRecord.AKTIF,
                tahunAkademikDao.findByStatus(StatusRecord.AKTIF), "FINAL");
        for (Jadwal j : cekJadwal) {
            System.out.println(j.getId() + " : id jadwal");
            presensiService.generateSesi2(j);

        }
    }

    // @Scheduled(cron = "0 25 11 * * *", zone = "Asia/Jakarta")
    public void regenerateJadwal() throws Exception {

        List<Jadwal> cekJadwal = jadwalDao.jadwalMatrik();
        System.out.println("jadwal matrik");
        for (Jadwal j : cekJadwal) {
            System.out.println(j.getId() + " : id jadwal");
            presensiService.reGenerate1(j);

        }
    }

}
