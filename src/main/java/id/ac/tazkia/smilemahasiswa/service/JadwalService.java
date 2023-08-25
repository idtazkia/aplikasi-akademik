package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
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

    @Scheduled(cron = "0 00 21 * * *", zone = "Asia/Jakarta")
    public void plotingJadwal() throws Exception {

        List<Jadwal> cekJadwal =  jadwalDao.findByStatusAndFinalPloting(StatusRecord.AKTIF, "FINAL");
        for (Jadwal j : cekJadwal){

            presensiService.generateSesi(j);

        }
    }
}
