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

    @Autowired private JadwalDao jadwalDao;

    @Autowired private PresensiService presensiService;

    @Autowired private TahunAkademikDao tahunAkademikDao;

    @Scheduled(cron = "0 05 10 * * *", zone = "Asia/Jakarta")
    public void plotingJadwal() throws Exception {

        List<Jadwal> cekJadwal =  jadwalDao.findByStatusAndTahunAkademikAndFinalPloting(StatusRecord.AKTIF, tahunAkademikDao.findByStatus(StatusRecord.AKTIF), "FINAL");
        for (Jadwal j : cekJadwal){

            presensiService.generateSesi2(j);

        }
    }

    @Scheduled(cron = "0 20 06 * * *", zone = "Asia/Jakarta")
    public void updateJadwal() throws Exception {

        List<Jadwal> cekJadwal =  jadwalDao.findByStatusAndTahunAkademikAndFinalPloting(StatusRecord.AKTIF, tahunAkademikDao.findByStatus(StatusRecord.AKTIF), "FINISHED");
        for (Jadwal j : cekJadwal){

            presensiService.updateSesi1(j);

        }
    }
}
