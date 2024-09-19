package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.EnableFitureDao;
import id.ac.tazkia.smilemahasiswa.entity.EnableFiture;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service @Slf4j
public class AktivasiService {

    @Autowired private EnableFitureDao enableFitureDao;

    public void buatEnableFiture(Mahasiswa mahasiswa, StatusRecord fitur, Boolean enabled, TahunAkademik tahunAkademik, String keterangan){
        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa, fitur, enabled, tahunAkademik);
        if (enableFiture == null) {
            enableFiture = new EnableFiture();
            enableFiture.setFitur(fitur);
            enableFiture.setMahasiswa(mahasiswa);
            enableFiture.setTahunAkademik(tahunAkademik);
            enableFiture.setKeterangan("-");
            enableFiture.setKeterangan(keterangan);
            enableFiture.setWaktuInput(LocalDateTime.now());
            enableFiture.setEnable(enabled);
            enableFitureDao.save(enableFiture);
        }
    }

    public void aktivasiEnableFiture(Mahasiswa mahasiswa, StatusRecord fitur, Boolean enabled, TahunAkademik tahunAkademik, String keterangan){
        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa, fitur, enabled, tahunAkademik);
        if (enableFiture == null) {
            enableFiture = new EnableFiture();
            enableFiture.setFitur(fitur);
            enableFiture.setMahasiswa(mahasiswa);
            enableFiture.setTahunAkademik(tahunAkademik);
            enableFiture.setKeterangan(keterangan);
            enableFiture.setWaktuInput(LocalDateTime.now());
        }else{
            if (Boolean.FALSE.equals(enableFiture.getEnable())) {
                enableFiture.setEnable(Boolean.TRUE);
            }
            enableFiture.setWaktuEdit(LocalDateTime.now());
            enableFiture.setKeterangan(keterangan);
        }
        enableFitureDao.save(enableFiture);
    }

}
