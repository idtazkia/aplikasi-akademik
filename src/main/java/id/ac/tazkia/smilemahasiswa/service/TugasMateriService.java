package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class TugasMateriService {

    @Autowired private MateriMengajarDao materiMengajarDao;

    @Autowired private BobotTugasDao bobotTugasDao;

    @Autowired private KrsDetailDao krsDetailDao;

    @Autowired private NilaiTugasDao nilaiTugasDao;

    @Value("${upload.tugas.dosen}")
    private String uploadTugas;

    @Value("${upload.tugas.mhs}")
    private String uploadJawaban;

    @Value("${upload.materi.dosen}")
    private String uploadMateri;

    public void saveBobotTugas(Jadwal jadwal, JadwalTugas jadwalTugas){
        BobotTugas bt = bobotTugasDao.findByJadwalAndPertemuanAndStatus(jadwal, String.valueOf(jadwalTugas.getPertemuanKe()), StatusRecord.AKTIF);
        if(bt == null){
            BobotTugas bobotTugas = new BobotTugas();
            bobotTugas.setJadwal(jadwal);
            bobotTugas.setBobot(jadwalTugas.getBobot());
            bobotTugas.setNamaTugas(jadwalTugas.getNamaTugas());
            bobotTugas.setPertemuan(String.valueOf(jadwalTugas.getPertemuanKe()));
            bobotTugasDao.save(bobotTugas);

            List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatus(bobotTugas.getJadwal(), StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail) {
                NilaiTugas nilaiTugas = new NilaiTugas();
                nilaiTugas.setNilai(BigDecimal.ZERO);
                nilaiTugas.setNilaiAkhir(BigDecimal.ZERO);
                nilaiTugas.setKrsDetail(kd);
                nilaiTugas.setBobotTugas(bobotTugas);
                nilaiTugas.setStatus(StatusRecord.AKTIF);
                nilaiTugasDao.save(nilaiTugas);
            }
        }else{
            bt.setJadwal(jadwal);
            bt.setBobot(jadwalTugas.getBobot());
            bt.setNamaTugas(jadwalTugas.getNamaTugas());
            bt.setPertemuan(String.valueOf(jadwalTugas.getPertemuanKe()));
            bobotTugasDao.save(bt);
        }
    }

    public void deleteBobotTugas(JadwalTugas jadwalTugas){
        BobotTugas bt = bobotTugasDao.findByJadwalAndPertemuanAndStatus(jadwalTugas.getSesiKuliah().getJadwal(), String.valueOf(jadwalTugas.getPertemuanKe()), StatusRecord.AKTIF);
        if(bt != null){
            bt.setStatus(StatusRecord.HAPUS);
            bobotTugasDao.save(bt);
        }

    }

    public void saveNilaiJawaban(String pilihan, JadwalTugasJawaban j, JadwalTugas jadwalTugas){
        BobotTugas bt = bobotTugasDao.findByJadwalAndPertemuanAndStatus(jadwalTugas.getSesiKuliah().getJadwal(), String.valueOf(jadwalTugas.getPertemuanKe()), StatusRecord.AKTIF);
        if (bt != null) {
            KrsDetail kd = krsDetailDao.findByMahasiswaAndJadwalAndStatus(j.getMahasiswa(), jadwalTugas.getSesiKuliah().getJadwal(), StatusRecord.AKTIF);
            if (kd != null) {
                NilaiTugas validasi = nilaiTugasDao.findByStatusAndBobotTugasAndKrsDetail(StatusRecord.AKTIF, bt, kd);
                BigDecimal nilaiAkhir = new BigDecimal(pilihan).multiply(bt.getBobot()).divide(new BigDecimal(100));
                if (validasi == null) {
                    NilaiTugas nilai = new NilaiTugas();
                    nilai.setKrsDetail(kd);
                    nilai.setBobotTugas(bt);
                    nilai.setNilai(new BigDecimal(pilihan));
                    nilai.setNilaiAkhir(nilaiAkhir);
                    nilaiTugasDao.save(nilai);
                }else{
                    validasi.setNilai(new BigDecimal(pilihan));
                    validasi.setNilaiAkhir(nilaiAkhir);
                    nilaiTugasDao.save(validasi);
                }
            }
        }
    }

    public String uploadFile(MultipartFile file, JadwalTugas jadwalTugas) throws IOException {
        String namaAsli = file.getOriginalFilename();

        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));
        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String folder = jadwalTugas.getSesiKuliah().getJadwal().getDosen().getKaryawan().getNamaKaryawan() + " - " + jadwalTugas.getSesiKuliah().getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + " - " + jadwalTugas.getPertemuanKe();
        String lokasi = uploadTugas + File.separator + folder;
        String idFile = UUID.randomUUID().toString();
        String namaFile = idFile + '.' + extension;
        new File(lokasi).mkdirs();
        File tujuan = new File(lokasi + File.separator + namaFile);
        file.transferTo(tujuan);

        return namaFile;
    }

    public String uploadJawaban(MultipartFile file, JadwalTugasJawaban tugasJawaban) throws IOException {

        String namaAsli = file.getOriginalFilename();

        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));
        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String folder = tugasJawaban.getMahasiswa().getNim() + " - " + tugasJawaban.getJadwalTugas().getSesiKuliah().getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah();
        String lokasi = uploadJawaban + File.separator + folder;
        String idFile = UUID.randomUUID().toString();
        String namaFile = idFile + '.' + extension;
        new File(lokasi).mkdirs();
        File tujuan = new File(lokasi + File.separator + namaFile);
        file.transferTo(tujuan);

        return namaFile;
    }

    public void uploadMateri(MultipartFile[] files, SesiKuliah sesiKuliah){
        for (MultipartFile file : files){
            try{
                String namaAsli = file.getOriginalFilename();

                String extension = "";

                int i = namaAsli.lastIndexOf('.');
                int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));
                if (i > p) {
                    extension = namaAsli.substring(i + 1);
                }

                MateriMengajar materi = new MateriMengajar();
                String folder = sesiKuliah.getJadwal().getDosen().getKaryawan().getNamaKaryawan() + " - " + sesiKuliah.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + " - " + sesiKuliah.getPertemuanKe();
                String lokasi = uploadMateri + File.separator + folder;
                String idFile = UUID.randomUUID().toString();
                materi.setFile(idFile + '.' + extension);
                new File(lokasi).mkdirs();
                File tujuan = new File(lokasi + File.separator + materi.getFile());
                file.transferTo(tujuan);
                materi.setSesiKuliah(sesiKuliah);
                materi.setNamaFile(namaAsli);
                materiMengajarDao.save(materi);

            }catch (Exception err){
                log.error(err.getMessage(), err);
            }
        }
    }
}
