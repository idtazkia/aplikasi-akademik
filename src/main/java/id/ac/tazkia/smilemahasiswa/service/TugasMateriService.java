package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
@Slf4j
public class TugasMateriService {

    @Autowired private MateriMengajarDao materiMengajarDao;

    @Autowired private BobotTugasDao bobotTugasDao;

    @Autowired private KrsDetailDao krsDetailDao;

    @Autowired private NilaiTugasDao nilaiTugasDao;

    @Autowired private JadwalTugasJawabanDao tugasJawabanDao;

    @Autowired private ScoreService scoreService;

    @Value("${upload.tugas.dosen}")
    private String uploadTugas;

    @Value("${upload.tugas.mhs}")
    private String uploadJawaban;

    @Value("${upload.materi.dosen}")
    private String uploadMateri;

    public JadwalTugas.JenisPengiriman setPengiriman(String online, String upload, String presentasi, String ujian){
        if (online != null && upload != null && presentasi != null && ujian != null) {
            return JadwalTugas.JenisPengiriman.ONLINE_TEKS_UPLOAD_FILE_PRESENTASI_UJIAN;
        }
        if (online != null && upload != null && presentasi != null) {
            return JadwalTugas.JenisPengiriman.ONLINE_TEKS_UPLOAD_FILE_PRESENTASI;
        }
        if (online != null && upload != null && ujian != null) {
            return JadwalTugas.JenisPengiriman.ONLINE_TEKS_UPLOAD_FILE_UJIAN;
        }
        if (upload != null && presentasi != null && ujian != null) {
            return JadwalTugas.JenisPengiriman.UPLOAD_FILE_PRESENTASI_UJIAN;
        }
        if (online != null && upload != null) {
            return JadwalTugas.JenisPengiriman.ONLINE_TEKS_UPLOAD_FILE;
        }
        if (online != null && presentasi != null) {
            return JadwalTugas.JenisPengiriman.ONLINE_TEKS_PRESENTASI;
        }
        if (online != null && ujian != null) {
            return JadwalTugas.JenisPengiriman.ONLINE_TEKS_UJIAN;
        }
        if (upload != null && presentasi != null) {
            return JadwalTugas.JenisPengiriman.UPLOAD_FILE_PRESENTASI;
        }
        if (upload != null && ujian != null) {
            return JadwalTugas.JenisPengiriman.UPLOAD_FILE_UJIAN;
        }
        if (presentasi != null && ujian != null) {
            return JadwalTugas.JenisPengiriman.PRESENTASI_UJIAN;
        }
        if (online != null) {
            return JadwalTugas.JenisPengiriman.ONLINE_TEKS;
        }
        if (upload != null) {
            return JadwalTugas.JenisPengiriman.UPLOAD_FILE;
        }
        if (presentasi != null) {
            return JadwalTugas.JenisPengiriman.PRESENTASI;
        }
        if (ujian != null) {
            return JadwalTugas.JenisPengiriman.UJIAN;
        }
        return JadwalTugas.JenisPengiriman.ONLINE_TEKS_UPLOAD_FILE_PRESENTASI_UJIAN;
    }

    public void saveBobotTugas(Jadwal jadwal, JadwalTugas jadwalTugas){
        BobotTugas bt = bobotTugasDao.findByJadwalAndJadwalTugasAndStatus(jadwal, jadwalTugas, StatusRecord.AKTIF);
        if(bt == null){
            BobotTugas bobotTugas = new BobotTugas();
            bobotTugas.setJadwal(jadwal);
            bobotTugas.setJadwalTugas(jadwalTugas);
            bobotTugas.setBobot(jadwalTugas.getBobot());
            bobotTugas.setNamaTugas(jadwalTugas.getNamaTugas());
            bobotTugas.setPertemuan(String.valueOf(jadwalTugas.getPertemuanKe()));
            bobotTugas.setKategoriTugas(jadwalTugas.getKategoriTugas());
            bobotTugasDao.save(bobotTugas);

            List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatus(bobotTugas.getJadwal(), StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail) {
                NilaiTugas nilaiTugas = new NilaiTugas();
                nilaiTugas.setNilai(BigDecimal.ZERO);
                nilaiTugas.setNilaiAkhir(BigDecimal.ZERO);
                nilaiTugas.setKrsDetail(kd);
                nilaiTugas.setBobotTugas(bobotTugas);
                nilaiTugas.setStatus(StatusRecord.AKTIF);
                nilaiTugas.setKategoriTugas(jadwalTugas.getKategoriTugas());
                nilaiTugasDao.save(nilaiTugas);
            }
        }else{
            bt.setJadwal(jadwal);
            bt.setBobot(jadwalTugas.getBobot());
            bt.setNamaTugas(jadwalTugas.getNamaTugas());
            bt.setPertemuan(String.valueOf(jadwalTugas.getPertemuanKe()));
            bt.setKategoriTugas(jadwalTugas.getKategoriTugas());

            List<NilaiTugas> nt = nilaiTugasDao.findByStatusAndBobotTugas(StatusRecord.AKTIF, bt);
            for (NilaiTugas tugas : nt){
                tugas.setKategoriTugas(jadwalTugas.getKategoriTugas());
                nilaiTugasDao.save(tugas);
            }

            bobotTugasDao.save(bt);
        }

        if (JadwalTugas.JenisPengiriman.PRESENTASI.equals(jadwalTugas.getJenisPengiriman()) ||
                JadwalTugas.JenisPengiriman.UJIAN.equals(jadwalTugas.getJenisPengiriman()) ||
                JadwalTugas.JenisPengiriman.PRESENTASI_UJIAN.equals(jadwalTugas.getJenisPengiriman())) {
            generateJawabanOtomatis(jadwalTugas);
        }
    }

    public void deleteBobotTugas(JadwalTugas jadwalTugas){
        BobotTugas bt = bobotTugasDao.findByJadwalAndJadwalTugasAndStatus(jadwalTugas.getSesiKuliah().getJadwal(), jadwalTugas, StatusRecord.AKTIF);
        if(bt != null){
            bt.setStatus(StatusRecord.HAPUS);
            bobotTugasDao.save(bt);

            List<NilaiTugas> nt = nilaiTugasDao.findByStatusAndBobotTugas(StatusRecord.AKTIF, bt);
            for (NilaiTugas tugas : nt){
                tugas.setStatus(StatusRecord.HAPUS);
                nilaiTugasDao.save(tugas);
            }
        }
        if (JadwalTugas.JenisPengiriman.PRESENTASI.equals(jadwalTugas.getJenisPengiriman()) ||
                JadwalTugas.JenisPengiriman.UJIAN.equals(jadwalTugas.getJenisPengiriman()) ||
                JadwalTugas.JenisPengiriman.PRESENTASI_UJIAN.equals(jadwalTugas.getJenisPengiriman())) {
            List<JadwalTugasJawaban> listJawaban = tugasJawabanDao.findByJadwalTugasAndStatusOrderByWaktuPengumpulan(jadwalTugas, StatusRecord.AKTIF);
            for (JadwalTugasJawaban tj : listJawaban){
                tj.setStatus(StatusRecord.HAPUS);
                tugasJawabanDao.save(tj);
            }
        }
    }

    public void generateJawabanOtomatis(JadwalTugas jt){
        List<KrsDetail> cariMahasiswa = krsDetailDao.findByJadwalAndStatus(jt.getSesiKuliah().getJadwal(), StatusRecord.AKTIF);
        for (KrsDetail kd : cariMahasiswa){
            JadwalTugasJawaban cek = tugasJawabanDao.findTopByMahasiswaAndJadwalTugasAndStatus(kd.getMahasiswa(), jt, StatusRecord.AKTIF);
            if (cek == null) {
                JadwalTugasJawaban jawaban = new JadwalTugasJawaban();
                jawaban.setMahasiswa(kd.getMahasiswa());
                jawaban.setJadwalTugas(jt);
                jawaban.setNilai(BigDecimal.ZERO);
                tugasJawabanDao.save(jawaban);
            }
        }
    }

    public void saveNilaiJawaban(String pilihan, JadwalTugasJawaban j, JadwalTugas jadwalTugas){
        BobotTugas bt = bobotTugasDao.findByJadwalAndJadwalTugasAndStatus(jadwalTugas.getSesiKuliah().getJadwal(), jadwalTugas, StatusRecord.AKTIF);
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
                    switch (jadwalTugas.getKategoriTugas()){
                        case UTS:
                            scoreService.hitungNilaiUTS(nilai);
                            break;
                        case UAS:
                            scoreService.hitungNilaiUAS(nilai);
                            break;
                        default:
                            scoreService.hitungNilaiTugas(nilai);
                            break;
                    }
                }else{
                    validasi.setNilai(new BigDecimal(pilihan));
                    validasi.setNilaiAkhir(nilaiAkhir);
                    nilaiTugasDao.save(validasi);
                    switch (jadwalTugas.getKategoriTugas()){
                        case UTS:
                            scoreService.hitungNilaiUTS(validasi);
                            break;
                        case UAS:
                            scoreService.hitungNilaiUAS(validasi);
                            break;
                        default:
                            scoreService.hitungNilaiTugas(validasi);
                            break;
                    }
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

    public void downloadAllSubmission(JadwalTugas tugas, HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<Path> files = new ArrayList<>();

        List<JadwalTugasJawaban> list = tugasJawabanDao.findByJadwalTugasAndStatusOrderByWaktuPengumpulan(tugas, StatusRecord.AKTIF);
        for (JadwalTugasJawaban jadwalTugas : list){
            if (jadwalTugas.getFile() != null && !jadwalTugas.getFile().isEmpty()){
                files.add(Paths.get(uploadJawaban + File.separator + jadwalTugas.getMahasiswa().getNim() + " - " +
                        jadwalTugas.getJadwalTugas().getSesiKuliah().getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + File.separator + jadwalTugas.getFile()));
            }
        }

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                                                                                    .filename( "Tugas matakuliah " + tugas.getSesiKuliah().getJadwal()
                                                                                    .getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + " pertemuan " +
                                                                                    tugas.getPertemuanKe() + " - " + tugas.getNamaTugas() + ".zip", StandardCharsets.UTF_8)
                                                                                    .build()
                                                                                    .toString());

        try(ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())){
            for (Path file : files){
                try(InputStream inputStream = Files.newInputStream(file)){
                    JadwalTugasJawaban namaFile = tugasJawabanDao.findByFile(file.getFileName().toString());
                    if (namaFile != null) {
                        zipOutputStream.putNextEntry(new ZipEntry(namaFile.getMahasiswa().getNim() + " - " + namaFile.getMahasiswa().getNama() + " - " + namaFile.getNamaFile()));
                    }else{
                        zipOutputStream.putNextEntry(new ZipEntry(file.getFileName().toString()));
                    }
                    StreamUtils.copy(inputStream, zipOutputStream);
                    zipOutputStream.flush();
                }
            }
        }
    }

    public Map<String, Object> getNilaiByJadwal(Jadwal jadwal){

        List<BobotTugas> allTugas = bobotTugasDao.findByJadwalAndStatusOrderByPertemuanAsc(jadwal, StatusRecord.AKTIF);

        List<BobotTugas> tugasList = allTugas.stream()
                .filter(t -> t.getKategoriTugas() == KategoriTugas.TUGAS)
                .collect(Collectors.toList());

        List<BobotTugas> utsList = allTugas.stream()
                .filter(t -> t.getKategoriTugas() == KategoriTugas.UTS)
                .collect(Collectors.toList());

        List<BobotTugas> uasList = allTugas.stream()
                .filter(t -> t.getKategoriTugas() == KategoriTugas.UAS)
                .collect(Collectors.toList());

        Map<String, BigDecimal> nilaiMap = new HashMap<>();
        List<NilaiTugas> listNilai = nilaiTugasDao.findByBobotTugasIn(allTugas);

        for (NilaiTugas nilai : listNilai){
            String key = nilai.getKrsDetail().getMahasiswa().getNim() + "_" + nilai.getBobotTugas().getId();
            nilaiMap.put(key, nilai.getNilai());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("tugasList", tugasList);
        result.put("utsList", utsList);
        result.put("uasList", uasList);
        result.put("nilaiMap", nilaiMap);

        return result;

    }

}
