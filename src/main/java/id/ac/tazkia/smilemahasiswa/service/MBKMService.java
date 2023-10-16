package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@Service @Transactional
@Slf4j
public class MBKMService {

    @Autowired private JadwalDao jadwalDao;

    @Autowired private KrsDetailDao krsDetailDao;

    public void buatJadwalKrsMbkm(MatakuliahMbkm matkul, Dosen dosen, TahunAkademik tahun, TahunAkademikProdi tahunProdi, Hari hari, Kelas kelas, Ruangan ruangan){

        Jadwal jadwal = new Jadwal();
        jadwal.setFinalStatus("N");
        jadwal.setJumlahSesi(1);
        jadwal.setBobotUts(BigDecimal.ZERO);
        jadwal.setBobotUas(BigDecimal.ZERO);
        jadwal.setBobotTugas(BigDecimal.ZERO);
        jadwal.setBobotPresensi(BigDecimal.ZERO);
        jadwal.setDosen(dosen);
        jadwal.setProdi(matkul.getKrs().getMahasiswa().getIdProdi());
        jadwal.setTahunAkademik(tahun);
        jadwal.setTahunAkademikProdi(tahunProdi);
        jadwal.setMatakuliahKurikulum(matkul.getMatakuliahKurikulum());
        jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
        jadwal.setProgram(matkul.getKrs().getMahasiswa().getIdProgram());
        jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
        jadwal.setHari(hari);
        jadwal.setKelas(kelas);
        jadwal.setRuangan(ruangan);
        jadwal.setStatus(StatusRecord.MBKM);
        jadwalDao.save(jadwal);

        KrsDetail kd = new KrsDetail();
        kd.setKrs(matkul.getKrs());
        kd.setMahasiswa(matkul.getKrs().getMahasiswa());
        kd.setJadwal(jadwal);
        kd.setMatakuliahKurikulum(matkul.getMatakuliahKurikulum());
        kd.setNilaiPresensi(BigDecimal.ZERO);
        kd.setNilaiUts(BigDecimal.ZERO);
        kd.setNilaiTugas(BigDecimal.ZERO);
        kd.setFinalisasi("N");
        kd.setNilaiUas(BigDecimal.ZERO);
        kd.setJumlahKehadiran(0);
        kd.setJumlahMangkir(0);
        kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
        kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
        kd.setJumlahTerlambat(0);
        kd.setJumlahIzin(0);
        kd.setJumlahSakit(0);
        kd.setStatusEdom(StatusRecord.UNDONE);
        kd.setStatus(StatusRecord.AKTIF);
        kd.setStatusKonversi(StatusRecord.MBKM);
        kd.setTahunAkademik(tahun);
        krsDetailDao.save(kd);

    }

}
