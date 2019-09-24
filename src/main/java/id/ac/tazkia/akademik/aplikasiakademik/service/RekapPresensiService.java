package id.ac.tazkia.akademik.aplikasiakademik.service;

import id.ac.tazkia.akademik.aplikasiakademik.dao.JadwalDosenDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.PresensiDosenDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.PresensiMahasiswaDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.RekapKehadiranMahasiswaDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class RekapPresensiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RekapPresensiService.class);

    @Autowired private PresensiDosenDao presensiDosenDao;
    @Autowired private PresensiMahasiswaDao presensiMahasiswaDao;
    @Autowired private RekapKehadiranMahasiswaDao rekapKehadiranMahasiswaDao;
    @Autowired private JadwalDosenDao jadwalDosenDao;

    // tiap jam 1 malam
    @Scheduled(cron = "* * 1 * * *")
    public void isiRekap(){
        isiRekap(LocalDate.now().minusDays(1));
    }

    public void isiRekap(LocalDate tanggal) {
        LocalDateTime jam00 = tanggal.atTime(0,0,0,0);
        LocalDateTime jam00besoknya = jam00.plusDays(1);

        LOGGER.info("Membuat rekap presensi dari jam {} sampai jam {}", jam00, jam00besoknya);

        Iterable<PresensiMahasiswa> daftarPresensi = presensiMahasiswaDao.findByTanggal(jam00, jam00besoknya);
        for (PresensiMahasiswa p :daftarPresensi) {
            LOGGER.debug("Mahasiswa : {}, Status : {}", p.getMahasiswa().getNim(), p.getStatusPresensi());

            RekapKehadiranMahasiswa r = rekapKehadiranMahasiswaDao.findByMahasiswaAndJadwal(p.getMahasiswa(), p.getSesiKuliah().getJadwal());
            if(r == null) {
                r = new RekapKehadiranMahasiswa();
                r.setMahasiswa(p.getMahasiswa());
                r.setJadwal(p.getSesiKuliah().getJadwal());
            }
            if(StatusPresensi.HADIR.equals(p.getStatusPresensi())){
                r.setHadir(r.getHadir() + 1);
            }
            if(StatusPresensi.MANGKIR.equals(p.getStatusPresensi())){
                r.setMangkir(r.getMangkir() + 1);
            }
            if(StatusPresensi.IZIN.equals(p.getStatusPresensi())){
                r.setIzin(r.getIzin() + 1);
            }
            if(StatusPresensi.SAKIT.equals(p.getStatusPresensi())){
                r.setSakit(r.getSakit() + 1);
            }
            if(StatusPresensi.TERLAMBAT.equals(p.getStatusPresensi())){
                r.setTerlambat(r.getTerlambat() + 1);
            }
            r.setTerakhirUpdate(LocalDateTime.now());
            rekapKehadiranMahasiswaDao.save(r);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void isiRekapPresensiDosen() {
        isiRekapPresensiDosen(LocalDate.now().minusDays(1));
    }

    public void isiRekapPresensiDosen(LocalDate tanggal) {
        LocalDateTime jam00 = tanggal.atTime(0,0,0,0);
        LocalDateTime jam00besoknya = jam00.plusDays(1);
        Iterable<PresensiDosen> dataPresensi = presensiDosenDao.findByWaktuMasukBetween(jam00, jam00besoknya);

        LOGGER.info("Mengisi rekap presensi dosen untuk tanggal {}", jam00.toLocalDate());

        for (PresensiDosen pd : dataPresensi) {
            JadwalDosen jd = jadwalDosenDao.findByJadwalIdAndDosenId(pd.getJadwal().getId(), pd.getDosen().getId());
            if (jd == null) {
                throw new IllegalStateException("Jadwal dosen dengan id_dosen " + pd.getDosen().getId() + " dan id_jadwal " + pd.getJadwal().getId() + " tidak ditemukan di database");
            }
            LOGGER.debug("Rekap Presensi dosen {} tanggal {} mata kuliah {} dengan status {}",
                    pd.getDosen().getKaryawan().getNamaKaryawan(), jam00.toLocalDate(),
                    pd.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah(),
                    pd.getStatusPresensi());
            if (StatusPresensi.HADIR.equals(pd.getStatusPresensi())) {
                jd.setJumlahKehadiran(jd.getJumlahKehadiran()+1);
            }
            if (StatusPresensi.MANGKIR.equals(pd.getStatusPresensi())) {
                jd.setJumlahMangkir(jd.getJumlahMangkir()+1);
            }
            if (StatusPresensi.IZIN.equals(pd.getStatusPresensi())) {
                jd.setJumlahIzin(jd.getJumlahIzin()+1);
            }
            if (StatusPresensi.TERLAMBAT.equals(pd.getStatusPresensi())) {
                jd.setJumlahTerlambat(jd.getJumlahTerlambat()+1);
            }
            if (StatusPresensi.SAKIT.equals(pd.getStatusPresensi())) {
                jd.setJumlahSakit(jd.getJumlahSakit()+1);
            }
        }
    }
}
