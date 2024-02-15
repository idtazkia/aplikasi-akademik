package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.MBKMService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class MBKMController {

    @Autowired private MBKMService mbkmService;

    @Autowired private PendaftaranMbkmDao pendaftaranMbkmDao;

    @Autowired private CurrentUserService currentUserService;

    @Autowired private KaryawanDao karyawanDao;

    @Autowired private DosenDao dosenDao;

    @Autowired private TahunAkademikDao tahunAkademikDao;

    @Autowired private MatakuliahMbkmDao matakuliahMbkmDao;

    @Autowired private JadwalDao jadwalDao;

    @Autowired private HariDao hariDao;

    @Autowired private KelasDao kelasDao;

    @Autowired private RuanganDao ruanganDao;

    @Autowired private TahunProdiDao tahunProdiDao;

    @Autowired private KrsDao krsDao;

    @Autowired private KrsDetailDao krsDetailDao;

    @Autowired private MahasiswaDao mahasiswaDao;

    @Autowired private GradeDao gradeDao;

    @Autowired private FileMbkmDao fileMbkmDao;

    @Autowired private ProdiDao prodiDao;

    @Value("${upload.mbkm}")
    private String mbkmFolder;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik(){
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
    }

    // api response
    @GetMapping("/get-matkul-mbkm/{id}")
    @ResponseBody
    public List<MatakuliahMbkm> getRequestMatkulMBKM(@PathVariable String id) {
        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Mahasiswa mhs = mahasiswaDao.findById(id).get();
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);

        return matakuliahMbkmDao.findByKrsAndStatus(krs, StatusRecord.AKTIF);
    }
    @GetMapping("/get-matkul-mbkm-tahun/{id}/{tahun}")
    @ResponseBody
    public List<MatakuliahMbkm> getRequestMatkulMBKMbyTahun(@PathVariable String id, @PathVariable String tahun) {
        Mahasiswa mhs = mahasiswaDao.findById(id).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        PendaftaranMbkm pendaftar = pendaftaranMbkmDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahunAkademik, StatusRecord.AKTIF);

        return matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(pendaftar, StatusRecord.AKTIF);
    }

    @GetMapping("/get-matkul-mbkm-approved/{id}")
    @ResponseBody
    public List<KrsDetail> getRequestMatkulMBKMApproved(@PathVariable String id) {
        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Mahasiswa mhs = mahasiswaDao.findById(id).get();
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);

        return krsDetailDao.findByStatusAndTahunAkademikAndKrsAndStatusKonversi(StatusRecord.AKTIF, tahun, krs, StatusRecord.MBKM);
    }

    @PostMapping("/mbkm/delete/{id}")
    public String deleteMatkul(@PathVariable String id){
        MatakuliahMbkm matkul = matakuliahMbkmDao.findById(id).get();

        matkul.setStatus(StatusRecord.HAPUS);
        matakuliahMbkmDao.save(matkul);

        return "redirect:/studiesActivity/krs/approval/list";
    }

    @PostMapping("/mbkm/waiting")
    public String waitingMbkm(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.WAITING);
        pendaftaranMbkmDao.save(mbkm);

        List<MatakuliahMbkm> listMahasiswa = matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(mbkm,  StatusRecord.FINISHED);
        if (listMahasiswa != null || !listMahasiswa.isEmpty()) {
            for (MatakuliahMbkm listMatkur : listMahasiswa){
                listMatkur.setStatus(StatusRecord.AKTIF);
                matakuliahMbkmDao.save(listMatkur);
            }
        }

        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mbkm.getMahasiswa(), mbkm.getTahunAkademik(), StatusRecord.AKTIF);
        if (krs != null) {
            List<KrsDetail> matkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
            if (matkul != null || !matkul.isEmpty()) {
                for (KrsDetail mat : matkul){
                    krsDetailDao.delete(mat);
                    Jadwal jadwal = mat.getJadwal();
                    jadwalDao.delete(jadwal);
                }
            }
        }

        return "redirect:/report/mbkm";
    }

    @PostMapping("/mbkm/approved")
    public String approvedMBKM(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.APPROVED);
        pendaftaranMbkmDao.save(mbkm);

        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mbkm.getMahasiswa().getIdProdi());

        List<MatakuliahMbkm> listMatakuliah = matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(mbkm,  StatusRecord.AKTIF);
        if (listMatakuliah != null || !listMatakuliah.isEmpty()) {
            for (MatakuliahMbkm listMatkur : listMatakuliah){
                listMatkur.setStatus(StatusRecord.FINISHED);
                matakuliahMbkmDao.save(listMatkur);

                Dosen dosen = null;
                if ("08".equalsIgnoreCase(mbkm.getMahasiswa().getIdProdi().getId())) {
                    dosen = dosenDao.findById("9ea8dfe9-704d-4ed2-984e-e53611d6ecf5").get();
                } else if ("01".equalsIgnoreCase(mbkm.getMahasiswa().getIdProdi().getId())) {
                    dosen = dosenDao.findById("dinad").get();
                } else if ("02".equalsIgnoreCase(mbkm.getMahasiswa().getIdProdi().getId())) {
                    dosen = dosenDao.findById("Putrisa").get();
                } else if ("06".equalsIgnoreCase(mbkm.getMahasiswa().getIdProdi().getId()) || "09".equalsIgnoreCase(mbkm.getMahasiswa().getIdProdi().getId())) {
                    dosen = dosenDao.findById("unangf").get();
                }else{
                    dosen = dosenDao.findById("YaserT").get();
                }
                Hari hari = hariDao.findById("0").get();
                Kelas kelas = kelasDao.findById("-").get();
                Ruangan ruangan = ruanganDao.findById("01").get();

                mbkmService.buatJadwalKrsMbkm(listMatkur, dosen, tahun, tahunProdi, hari, kelas, ruangan);
            }
        }

        return "redirect:/studiesActivity/krs/approval/list";
    }

    @PostMapping("/mbkm/reject")
    public String rejectMBKM(@RequestParam PendaftaranMbkm mbkm){

        mbkm.setStatusApproval(StatusApprove.REJECTED);
        pendaftaranMbkmDao.save(mbkm);

        List<MatakuliahMbkm> listMahasiswa = matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(mbkm,  StatusRecord.AKTIF);
        if (listMahasiswa != null || !listMahasiswa.isEmpty()) {
            for (MatakuliahMbkm listMatkur : listMahasiswa){
                listMatkur.setStatus(StatusRecord.REJECTED);
                matakuliahMbkmDao.save(listMatkur);
            }
        }

        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mbkm.getMahasiswa(), mbkm.getTahunAkademik(), StatusRecord.AKTIF);
        if (krs != null) {
            List<KrsDetail> matkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
            if (matkul != null || !matkul.isEmpty()) {
                for (KrsDetail mat : matkul){
                    mat.setStatus(StatusRecord.HAPUS);
                    krsDetailDao.save(mat);
                    Jadwal jadwal = mat.getJadwal();
                    jadwal.setStatus(StatusRecord.HAPUS);
                    jadwalDao.save(jadwal);
                }
            }
        }

        return "redirect:/studiesActivity/krs/approval/list";
    }

    @PostMapping("/mbkm/cancel")
    public String cancelMbkm(@RequestParam PendaftaranMbkm mbkm){

        List<MatakuliahMbkm> listMahasiswa = matakuliahMbkmDao.findByPendaftaranMbkmAndStatus(mbkm, StatusRecord.FINISHED);
        if (listMahasiswa != null || !listMahasiswa.isEmpty()) {
            for (MatakuliahMbkm listMatkur : listMahasiswa){
                matakuliahMbkmDao.delete(listMatkur);
            }
        }

        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mbkm.getMahasiswa(), mbkm.getTahunAkademik(), StatusRecord.AKTIF);
        if (krs != null) {
            List<KrsDetail> matkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
            if (matkul != null || !matkul.isEmpty()) {
                for (KrsDetail mat : matkul){
                    krsDetailDao.delete(mat);
                    Jadwal jadwal = mat.getJadwal();
                    jadwalDao.delete(jadwal);
                }
            }
        }

        mbkm.setStatus(StatusRecord.HAPUS);
        mbkm.setStatusApproval(StatusApprove.HAPUS);
        pendaftaranMbkmDao.save(mbkm);

        return "redirect:/report/mbkm";
    }

    @PostMapping("/mbkm/upload/file")
    public String uploadFileMbkm(@Valid FileMbkm fileMbkm, MultipartFile file, Authentication authentication) throws IOException {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);

        if (file != null || !file.isEmpty()) {
            mbkmService.uploadLaporan(file, fileMbkm);
        }

        return "redirect:/study/krs";
    }

    @GetMapping("/laporan/{file}/mbkm")
    public ResponseEntity<byte[]> showDocument(@PathVariable FileMbkm file) throws Exception{
        String lokasiFile = mbkmFolder + File.separator + file.getKrsDetail().getMahasiswa().getNim() + File.separator +
                file.getFileMbkm();

        log.debug("lokasi file : {}" + lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (file.getFileMbkm().toLowerCase().endsWith("jpeg") || file.getFileMbkm().toLowerCase().endsWith("jpg")){
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (file.getFileMbkm().toLowerCase().endsWith("png")){
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (file.getFileMbkm().toLowerCase().endsWith("pdf")){
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else{
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        }catch (Exception err){
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/mbkm/laporan/delete")
    public String deleteLaporan(@RequestParam FileMbkm file){

        file.setStatus(StatusRecord.HAPUS);
        fileMbkmDao.save(file);

        return "redirect:/study/krs";
    }

    // penilaian MBKM

    @GetMapping("/studiesActivity/mbkm/list")
    public void penilaianMBKM(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) TahunAkademik tahun, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Dosen dosen = dosenDao.findByKaryawanIdUser(user);
        TahunAkademik aktif = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahun != null) {
            model.addAttribute("listMahasiswa", pendaftaranMbkmDao.penilaianMbkmMahasiswa(tahun.getId(), dosen.getId(), page));
            model.addAttribute("selectedTahun", tahun);
        }else{
            model.addAttribute("listMahasiswa", pendaftaranMbkmDao.penilaianMbkmMahasiswa(aktif.getId(), dosen.getId(), page));
            model.addAttribute("selectedTahun", aktif);
        }
    }

    @GetMapping("/studiesActivity/mbkm/detail")
    public void detailPenilaian(Model model, @RequestParam(required = false) Krs krs, Authentication authentication){

        List<KrsDetail> listMatkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
        model.addAttribute("krs", krs);
        model.addAttribute("grade", gradeDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("matkul", listMatkul);
        model.addAttribute("mhs", krs.getMahasiswa());
        model.addAttribute("laporan", fileMbkmDao.findByStatusOrderByWaktuInput(StatusRecord.AKTIF));
    }

    @PostMapping("/mbkm/penilaian/input")
    public String inputNilai(@RequestParam Krs krs, HttpServletRequest request){

        List<KrsDetail> listMatkul = krsDetailDao.findByKrsAndStatusAndStatusKonversi(krs, StatusRecord.AKTIF, StatusRecord.MBKM);
        for (KrsDetail matkul : listMatkul){
            String pilihan = request.getParameter("grade-"+matkul.getId());
            if (pilihan != null || !pilihan.trim().isEmpty()) {
                Grade grade = gradeDao.findByNama(pilihan);
                matkul.setGrade(pilihan);
                matkul.setNilaiAkhir(grade.getBawah());
                matkul.setBobot(grade.getBobot());
                matkul.setFinalisasi("FINAL");
                krsDetailDao.save(matkul);

                Jadwal j = matkul.getJadwal();
                j.setFinalStatus("FINAL");
                jadwalDao.save(j);

            }
        }

        return "redirect:/studiesActivity/mbkm/list";
    }
}
