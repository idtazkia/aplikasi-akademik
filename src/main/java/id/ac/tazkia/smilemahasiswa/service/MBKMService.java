package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Service @Transactional
@Slf4j
public class MBKMService {

    @Autowired private JadwalDao jadwalDao;

    @Autowired private KrsDetailDao krsDetailDao;

    @Autowired private FileMbkmDao fileMbkmDao;

    @Autowired private PendaftaranMbkmDao pendaftaranMbkmDao;

    @Autowired private MatakuliahMbkmDao matakuliahMbkmDao;

    @Value("${upload.mbkm}")
    private String mbkmFolder;

    public void buatMatakuliahMbkm(Krs krs, MatakuliahKurikulum matakuliahKurikulum) {

        PendaftaranMbkm daftarMbkm = pendaftaranMbkmDao.findByMahasiswaAndTahunAkademikAndStatus(krs.getMahasiswa(), krs.getTahunAkademik(), StatusRecord.AKTIF);

        if (daftarMbkm != null) {
            MatakuliahMbkm newMbkm = new MatakuliahMbkm();
            newMbkm.setKrs(krs);
            newMbkm.setMatakuliahKurikulum(matakuliahKurikulum);
            newMbkm.setPendaftaranMbkm(daftarMbkm);
            newMbkm.setStatus(StatusRecord.AKTIF);
            matakuliahMbkmDao.save(newMbkm);
        }
    }

    public void hapusMatakulMbkm(MatakuliahMbkm matakuliahMbkm){
        if (matakuliahMbkm != null) {
            matakuliahMbkm.setStatus(StatusRecord.HAPUS);
            matakuliahMbkmDao.save(matakuliahMbkm);
        }
    }

    public void buatJadwalKrsMbkm(MatakuliahKurikulum matkul, Krs krs, Dosen dosen, TahunAkademik tahun, TahunAkademikProdi tahunProdi, Hari hari, Kelas kelas, Ruangan ruangan){

        Jadwal jadwal = new Jadwal();
        jadwal.setFinalStatus("N");
        jadwal.setJumlahSesi(1);
        jadwal.setBobotUts(BigDecimal.ZERO);
        jadwal.setBobotUas(BigDecimal.ZERO);
        jadwal.setBobotTugas(BigDecimal.ZERO);
        jadwal.setBobotPresensi(BigDecimal.ZERO);
        jadwal.setDosen(dosen);
        jadwal.setProdi(krs.getMahasiswa().getIdProdi());
        jadwal.setTahunAkademik(tahun);
        jadwal.setTahunAkademikProdi(tahunProdi);
        jadwal.setMatakuliahKurikulum(matkul);
        jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
        jadwal.setProgram(krs.getMahasiswa().getIdProgram());
        jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
        jadwal.setHari(hari);
        jadwal.setKelas(kelas);
        jadwal.setRuangan(ruangan);
        jadwal.setStatus(StatusRecord.MBKM);
        jadwalDao.save(jadwal);

        KrsDetail kd = new KrsDetail();
        kd.setKrs(krs);
        kd.setMahasiswa(krs.getMahasiswa());
        kd.setJadwal(jadwal);
        kd.setMatakuliahKurikulum(matkul);
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

    public void hapusJadwal(KrsDetail kd){
        kd.setStatus(StatusRecord.HAPUS);
        krsDetailDao.save(kd);

        Jadwal jadwal = kd.getJadwal();
        jadwal.setStatus(StatusRecord.HAPUS);
        jadwalDao.save(jadwal);
    }

    public void uploadLaporan(MultipartFile file, FileMbkm fileMbkm) throws IOException {

        String namaAsli = file.getOriginalFilename();

        // memisahkan extension
        String extension = "";
        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i+1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = mbkmFolder + File.separator + fileMbkm.getKrsDetail().getMahasiswa().getNim();
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        file.transferTo(tujuan);
        fileMbkm.setFileMbkm(idFile + "." + extension);
        fileMbkm.setNamaFile(namaAsli);

        fileMbkmDao.save(fileMbkm);
    }

}
