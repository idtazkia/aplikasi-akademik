package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.*;
import id.ac.tazkia.smilemahasiswa.service.BebasKewajibanService;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.ElearningWebClientService;
import id.ac.tazkia.smilemahasiswa.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller @Slf4j
public class BebasKewajibanController {

    public static final List<String> USER_PERPUS = Arrays.asList("perpustakaan", "kabagperpus");
    public static final List<String> USER_FINANCE = Arrays.asList("finance", "kabagfinance");
    public static final List<String> USER_TLC = Arrays.asList("tlc", "staftlc");
    public static final List<String> USER_TQC = Arrays.asList("staftqc", "adminmatrik");
    public static final List<String> USER_AKADEMIK = Arrays.asList("akademik1", "akademik2", "kabagakademik", "superuser");

    @Value("${upload.sertifikatTlc}")
    private String uploadTlc;

    @Value("${upload.rejectedTlc}")
    private String rejectedByTlc;

    @Autowired
    BebasKewajibanDao bebasKewajibanDao;

    @Autowired
    MahasiswaDao mahasiswaDao;

    @Autowired
    KewajibanPerpusDao kewajibanPerpusDao;

    @Autowired
    KewajibanPerpusMahasiswaDao kewajibanPerpusMahasiswaDao;

    @Autowired
    TagihanDao tagihanDao;

    @Autowired
    PembayaranDao pembayaranDao;

    @Autowired
    JenjangDao jenjangDao;

    @Autowired
    HistoriBebasKewajibanDao historiBebasKewajibanDao;

    @Autowired
    KewajibanSertifikatTlcDao kewajibanSertifikatTlcDao;

    @Autowired
    KrsDao krsDao;

    @Autowired
    KrsDetailDao krsDetailDao;

    @Autowired
    CurrentUserService currentUserService;

    @Autowired
    BebasKewajibanService bebasKewajibanService;

    @Autowired
    MailService mailService;
    @Autowired
    private ElearningWebClientService elearningWebClientService;

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    // halaman mahasiswa

    @GetMapping("/kewajiban/daftar")
    public String daftarBebasKewajiban(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        if ("LULUS".equals(mahasiswa.getStatusAktif())) {
            PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
            if (bebasKewajiban != null) {
                return "redirect:/kewajiban/mahasiswa/detail";
            }else{
                bebasKewajibanService.saveDaftarKewajiban(mahasiswa);
                return "redirect:/kewajiban/mahasiswa/detail";
            }
        }else{
            return "redirect:/kewajiban/gagal";
        }
    }

    @GetMapping("/kewajiban/gagal")
    public void kewajibanGagal(){

    }

    @GetMapping("/kewajiban/mahasiswa/detail")
    public void detailBebasKewajiban(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);

        System.out.println("user login : " + user);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findByMahasiswa(mhs);
        if ("S2".equalsIgnoreCase(mhs.getIdProdi().getIdJenjang().getKodeJenjang())) {
            model.addAttribute("mhsS2", "Hide menu TLC untuk s2");
        }

        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("listKewajibanMahasiswa", kewajibanPerpusMahasiswaDao.findByBebasKewajibanOrderByKewajibanPerpusId(bebasKewajiban));
        // sertifikasi tlc
        model.addAttribute("listSertifikat", kewajibanSertifikatTlcDao.findByStatusAndBebasKewajibanAndTipe(StatusRecord.AKTIF, bebasKewajiban, Tipe.TLC));
        model.addAttribute("listMutqin", kewajibanSertifikatTlcDao.findByStatusAndBebasKewajibanAndTipe(StatusRecord.AKTIF, bebasKewajiban, Tipe.TQC));

        // cek histori komentar
        HistoriBebasKewajiban perpus = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF);
        HistoriBebasKewajiban kabagperpus = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.KABAG_PERPUS, StatusRecord.AKTIF);
        HistoriBebasKewajiban keuangan = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.KEUANGAN, StatusRecord.AKTIF);
        HistoriBebasKewajiban tlc = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.TLC, StatusRecord.AKTIF);
        HistoriBebasKewajiban tqc = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.TQC, StatusRecord.AKTIF);
        HistoriBebasKewajiban akademik = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.AKADEMIK, StatusRecord.AKTIF);
        if (perpus != null) {
            model.addAttribute("hPerpus", perpus);
        }
        if (kabagperpus != null) {
            model.addAttribute("hKabagPerpus", kabagperpus);
        }
        if (keuangan != null) {
            model.addAttribute("hKeuangan", keuangan);
        }
        if (tlc != null) {
            model.addAttribute("hTlc", tlc);
        }
        if (tqc != null) {
            model.addAttribute("hTqc", tqc);
        }
        if (akademik != null) {
            model.addAttribute("hAkademik", akademik);
        }

    }

    @GetMapping("/kewajiban/mahasiswa/transcript")
    public void trancstiptDetail(Model model, @RequestParam String nim){
        model.addAttribute("nim", nim);
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        if (mahasiswa != null) {
            model.addAttribute("mhs", mahasiswa);

            // tampilsemua
            List<DataTranskript> listTransc = krsDetailDao.listTranskript(mahasiswa);
            int sks = listTransc.stream().map(DataTranskript::getSks).mapToInt(Integer::intValue).sum();
            BigDecimal mutu = listTransc.stream().map(DataTranskript::getMutu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("semesterTranskript", krsDao.semesterTranskript(mahasiswa.getId()));
            model.addAttribute("transkriptTampil", listTransc);
            model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));
            model.addAttribute("sks", sks);
            model.addAttribute("mutu", mutu);
            model.addAttribute("ipk", mutu.divide(new BigDecimal(sks), 2, BigDecimal.ROUND_HALF_UP));
        } else {
            model.addAttribute("message", "error message");
        }
    }

    @PostMapping("/kewajiban/mahasiswa/tlc/dikampus")
    public String sertiTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam String tanggal, @RequestParam String jenis, RedirectAttributes attributes){
        KewajibanSertifikatTlc cek = kewajibanSertifikatTlcDao.findByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF);
        if (cek == null) {
            KewajibanSertifikatTlc tlc = new KewajibanSertifikatTlc();
            tlc.setBebasKewajiban(bebasKewajiban);
            tlc.setTanggalUjian(LocalDate.parse(tanggal));
            tlc.setKeteranganUjian(ByBagian.DIKAMPUS);
            tlc.setJenisTes(jenis);
            tlc.setTipe(Tipe.TLC);
            kewajibanSertifikatTlcDao.save(tlc);

            PendaftaranBebasKewajiban kewajiban = tlc.getBebasKewajiban();
            kewajiban.setStatusApproveTlc(StatusApprove.WAITING_TLC);
            bebasKewajibanDao.save(kewajiban);

        }
        attributes.addFlashAttribute("berhasil", "Alert berhasil save tugas");
        return "redirect:/kewajiban/mahasiswa/detail";
    }

    @PostMapping("/kewajiban/mahasiswa/tlc/diluar")
    public String sertiTlcDiluar(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam String tanggal, @RequestParam String tempat, @RequestParam String jenis, MultipartFile file, RedirectAttributes attributes) throws IOException {
        KewajibanSertifikatTlc cek = kewajibanSertifikatTlcDao.findByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF);
        if (cek == null) {

            KewajibanSertifikatTlc tlc = new KewajibanSertifikatTlc();

            String idPeserta = bebasKewajiban.getMahasiswa().getNim();

            String namaAsli = file.getOriginalFilename();

            // Memisahkan extension
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadTlc + File.separator + idPeserta;
            log.info("Info lokasi upload : {}", lokasiUpload);
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);
            tlc.setFile(idFile + "." + extension);
            tlc.setBebasKewajiban(bebasKewajiban);
            tlc.setTempatUjian(tempat);
            tlc.setTanggalUjian(LocalDate.parse(tanggal));
            tlc.setKeteranganUjian(ByBagian.DILUAR);
            tlc.setJenisTes(jenis);
            tlc.setTipe(Tipe.TLC);
            kewajibanSertifikatTlcDao.save(tlc);

            PendaftaranBebasKewajiban kewajiban = tlc.getBebasKewajiban();
            kewajiban.setStatusApproveTlc(StatusApprove.WAITING_TLC);
            bebasKewajibanDao.save(kewajiban);
        }

        attributes.addFlashAttribute("berhasil", "Alert berhasil save tugas");
        return "redirect:/kewajiban/mahasiswa/detail";
    }

    @PostMapping("/kewajiban/mahasiswa/tqc/update")
    public String sertiTqcUpload(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, MultipartFile file, RedirectAttributes attributes) throws IOException {
        KewajibanSertifikatTlc cek = kewajibanSertifikatTlcDao.findByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF);
        if (cek == null) {

            KewajibanSertifikatTlc tlc = new KewajibanSertifikatTlc();

            String idPeserta = bebasKewajiban.getMahasiswa().getNim();

            String namaAsli = file.getOriginalFilename();

            // Memisahkan extension
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadTlc + File.separator + idPeserta;
            log.info("Info lokasi upload : {}", lokasiUpload);
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);
            tlc.setFile(idFile + "." + extension);
            tlc.setBebasKewajiban(bebasKewajiban);
            tlc.setKeteranganUjian(ByBagian.HAFIDZPRENEUR);
            tlc.setJenisTes("Ziadah 30 Juz");
            tlc.setTipe(Tipe.TQC);
            kewajibanSertifikatTlcDao.save(tlc);

            PendaftaranBebasKewajiban kewajiban = tlc.getBebasKewajiban();
            kewajiban.setStatusApproveTlc(StatusApprove.WAITING_TQC);
            bebasKewajibanDao.save(kewajiban);
        }

        attributes.addFlashAttribute("berhasil", "Alert berhasil save tugas");
        return "redirect:/kewajiban/mahasiswa/detail";
    }

    @PostMapping("/kewajiban/mahasiswa/tlc/delete")
    public String deleteFileUpload(@RequestParam KewajibanSertifikatTlc sertifikat, RedirectAttributes attributes){

        sertifikat.setStatus(StatusRecord.HAPUS);
        kewajibanSertifikatTlcDao.save(sertifikat);

        attributes.addFlashAttribute("berhasil", "Alert berhasil save tugas");
        return "redirect:/kewajiban/mahasiswa/detail";
    }

    // CETAK SKL
    @GetMapping("/kewajiban/cetakSkl")
    public void cetakSKL(@RequestParam("bebasKewajiban") String bebasKewajibanId,
                        HttpServletResponse response) throws IOException {

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(bebasKewajibanId)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

        // Set header untuk download
        String fileName = "SKL_" + bebasKewajiban.getMahasiswa().getNim() +
                        "_" + bebasKewajiban.getMahasiswa().getNama().replace(" ", "_") + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Panggil service untuk generate PDF
        bebasKewajibanService.cetakSKL(bebasKewajiban, response.getOutputStream());
    }

    // CETAK SKL ENGLISH
    @GetMapping("/kewajiban/cetakSklEnglish")
    public void cetakSklEnglish(@RequestParam("bebasKewajiban") String bebasKewajibanId,
                        HttpServletResponse response) throws IOException {

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(bebasKewajibanId)
                .orElseThrow(() -> new RuntimeException("Data tidak ditemukan"));

        // Set header untuk download
        String fileName = "SKL_English_" + bebasKewajiban.getMahasiswa().getNim() +
                        "_" + bebasKewajiban.getMahasiswa().getNama().replace(" ", "_") + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Panggil service untuk generate PDF
        bebasKewajibanService.cetakSklEnglish(bebasKewajiban, response.getOutputStream());
    }

    // halaman admin

    @GetMapping("/kewajiban/admin")
    public String setHalamanaAdmin(Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        if (USER_PERPUS.contains(user.getRole().getId())){
            return "redirect:/kewajiban/admin/perpus/list";
        }
        if (USER_FINANCE.contains(user.getRole().getId())) {
            return "redirect:/kewajiban/admin/keuangan/list";
        }
        if (USER_TLC.contains(user.getRole().getId())) {
            return "redirect:/kewajiban/admin/tlc/list";
        }
        if (USER_TQC.contains(user.getRole().getId())) {
            return "redirect:/kewajiban/admin/tqc/list";
        }
        if (USER_AKADEMIK.contains(user.getRole().getId())) {
            return "redirect:/kewajiban/admin/akademik/list";
        }else{
            return "redirect:/dashboard";
        }

    }

    // BAGIAN PERPUSTAKAAN

    @GetMapping("/kewajiban/admin/perpus/list")
    public void listKewajibanPerpus(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        User user = currentUserService.currentUser(authentication);
        if ("perpustakaan".equalsIgnoreCase(user.getRole().getId()) || "superuser".equalsIgnoreCase(user.getRole().getId())) {
            model.addAttribute("staff", "Staff Perpus");
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApprovePerpusNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, search, search));
            }else{
                model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApprovePerpusNotOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED));
            }
        }
        if ("kabagperpus".equalsIgnoreCase(user.getRole().getId()) || "superuser".equalsIgnoreCase(user.getRole().getId())) {
            model.addAttribute("kabag", "Kabag Perpus");
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusAndStatusApproveKabagPerpusNotAndStatusApprovePerpusNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByStatusApprovePerpus(StatusRecord.AKTIF, StatusApprove.APPROVED, StatusApprove.APPROVED, search, search, page));
            }else{
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusAndStatusApproveKabagPerpusNotAndStatusApprovePerpusNotOrderByStatusApprovePerpus(StatusRecord.AKTIF, StatusApprove.APPROVED, StatusApprove.APPROVED, page));
            }
            model.addAttribute("listKewajiban", kewajibanPerpusMahasiswaDao.findAll());
            model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KABAG_PERPUS, StatusRecord.AKTIF));
        }
    }

    @GetMapping("/kewajiban/admin/perpus/detail")
    public void detailKewajibanPerpus(Model model, @RequestParam String id, Authentication authentication){

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(id).get();
//        model.addAttribute("listKewajibanPerpus", kewajibanPerpusDao.findByStatusOrderById(StatusRecord.AKTIF));
        model.addAttribute("listKewajibanMahasiswa", kewajibanPerpusMahasiswaDao.findByBebasKewajibanOrderByKewajibanPerpusId(bebasKewajiban));
        model.addAttribute("mahasiswa", bebasKewajiban.getMahasiswa());
        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("histori", historiBebasKewajibanDao.findByBebasKewajibanAndByBagianAndStatusOrderByWaktuInput(bebasKewajiban, ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF));
    }

    @GetMapping("/kewajiban/admin/perpus/approval/staff")
    public void historiApprovalStaff(Model model){

        model.addAttribute("list", bebasKewajibanDao.historiApprovalPerpus(ByBagian.PERPUSTAKAAN.toString()));
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF));

    }

    @GetMapping("/kewajiban/admin/perpus/approval/approved")
    public void approvedMhsKabagPerpus(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApprovePerpusAndStatusApproveKabagPerpusNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByStatusApprovePerpus(StatusRecord.AKTIF, StatusApprove.APPROVED, StatusApprove.APPROVED, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApprovePerpusAndStatusApproveKabagPerpusNotOrderByStatusApprovePerpus(StatusRecord.AKTIF, StatusApprove.APPROVED, StatusApprove.APPROVED, page));
        }

    }

    @GetMapping("/kewajiban/admin/perpus/approval/kabag")
    public void historiApprovalKabag(Model model){

        model.addAttribute("list", bebasKewajibanDao.historiApproval(ByBagian.KABAG_PERPUS.toString()));
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KABAG_PERPUS, StatusRecord.AKTIF));

    }

    @PostMapping("/kewajiban/admin/perpus/input")
    public String saveKewajibanPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, HttpServletRequest request, Authentication authentication){
        User user = currentUserService.currentUser(authentication);

        List<KewajibanPerpusMahasiswa> list = kewajibanPerpusMahasiswaDao.findByBebasKewajibanOrderByKewajibanPerpusId(bebasKewajiban);
        for (KewajibanPerpusMahasiswa data : list){
            String pilihan = request.getParameter("check-"+data.getId());
            if (pilihan != null) {
                data.setStatus(StatusRecord.AKTIF);
                kewajibanPerpusMahasiswaDao.save(data);
            }else{
                data.setStatus(StatusRecord.NONAKTIF);
                kewajibanPerpusMahasiswaDao.save(data);
            }
        }
        bebasKewajibanService.saveStaffPerpus(bebasKewajiban, komentar, user);

        return "redirect:/kewajiban/admin/perpus/list";
    }

    @GetMapping("/kewajiban/admin/perpus/approve")
    public String approveKabagPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveKabagPerpus(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKabagPerpus(bebasKewajiban, komentar, user);
        mailService.mailApproveKabagPerpus(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/perpus/list";
    }

    @PostMapping("/kewajiban/admin/perpus/approve/select")
    public String approveSelectedKabagPerpus(@RequestParam(required = false) String[] checkedBebasKewajiban, Authentication authentication){

        User user = currentUserService.currentUser(authentication);

        if (checkedBebasKewajiban != null) {
            for (String idBebasKewajiban : checkedBebasKewajiban) {
                PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(idBebasKewajiban).get();

                log.info("Data Bebas kewajiban : {}", bebasKewajiban.getMahasiswa().getNim());

                if (bebasKewajiban != null) {
                    String komentar = "DONE";
                    bebasKewajiban.setStatusApproveKabagPerpus(StatusApprove.APPROVED);
                    bebasKewajibanDao.save(bebasKewajiban);

                    bebasKewajibanService.saveKabagPerpus(bebasKewajiban, komentar, user);
                    mailService.mailApproveKabagPerpus(bebasKewajiban, "DONE");
                }
            }
        }

        return "redirect:/kewajiban/admin/perpus/approval/approved";
    }

    @PostMapping("/kewajiban/admin/perpus/reject")
    public String rejectKabagPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveKabagPerpus(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKabagPerpus(bebasKewajiban, komentar, user);
        mailService.mailRejectKabagPerpus(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/perpus/list";
    }

    // BAGIAN KEUANGAN

    @GetMapping("/kewajiban/admin/keuangan/list")
    public void listKewajibanKeuangan(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveKeuanganNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveKeuanganNotOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, page));
        }
    }

    @GetMapping("/kewajiban/admin/keuangan/approval")
    public void historiApproval(Model model){
        model.addAttribute("list", bebasKewajibanDao.historiApproval(ByBagian.KEUANGAN.toString()));
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KEUANGAN, StatusRecord.AKTIF));
    }

    @GetMapping("/kewajiban/admin/keuangan/detail")
    public void detailKewajibanKeuangan(Model model, @RequestParam String id){

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(id).get();

        model.addAttribute("mahasiswa", bebasKewajiban.getMahasiswa());
        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("listTagihan", tagihanDao.findByMahasiswaAndStatusNotOrderByTahunAkademikKodeTahunAkademik(bebasKewajiban.getMahasiswa(), StatusRecord.HAPUS));
        model.addAttribute("listPembayaran", pembayaranDao.findByTagihanMahasiswaAndTagihanStatusNotOrderByTagihanTahunAkademikKodeTahunAkademik(bebasKewajiban.getMahasiswa(), StatusRecord.HAPUS));
        model.addAttribute("histori", historiBebasKewajibanDao.findByBebasKewajibanAndByBagianAndStatusOrderByWaktuInput(bebasKewajiban, ByBagian.KEUANGAN, StatusRecord.AKTIF));

        List<Tagihan> cekBelumLunas = tagihanDao.findByMahasiswaAndStatusOrderByStatusTagihanDesc(bebasKewajiban.getMahasiswa(), StatusRecord.AKTIF);
        for (Tagihan t : cekBelumLunas){
            if (t.getStatusTagihan() == StatusTagihan.AKTIF) {
                model.addAttribute("disabled", "Ada tagihan yang belum lunas");
            }
        }

    }

    @PostMapping("/kewajiban/admin/keuangan/approve")
    public String approveKeuangan(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveKeuangan(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKeuangan(bebasKewajiban, komentar, user);
        mailService.mailApproveKeuangan(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/keuangan/list";
    }

    @PostMapping("/kewajiban/admin/keuangan/reject")
    public String rejectKeuangan(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveKeuangan(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKeuangan(bebasKewajiban, komentar, user);
        mailService.mailRejectKeuangan(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/keuangan/list";
    }

    // BAGIAN TLC

    @GetMapping("/kewajiban/admin/tlc/list")
    public void listTlc(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveTlcNotInAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, Arrays.asList(StatusApprove.APPROVED, StatusApprove.WAITING_TQC), search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveTlcNotInOrderByTanggalRequest(StatusRecord.AKTIF, Arrays.asList(StatusApprove.APPROVED, StatusApprove.WAITING_TQC), page));
        }
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TLC, StatusRecord.AKTIF));
        model.addAttribute("sertifikat", kewajibanSertifikatTlcDao.findByStatusAndTipe(StatusRecord.AKTIF, Tipe.TLC));
    }

    @GetMapping("/kewajiban/admin/tlc/approval")
    public void historiApprovalTlc(Model model){
        model.addAttribute("list", bebasKewajibanDao.historiApproval(ByBagian.TLC.toString()));
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TLC, StatusRecord.AKTIF));
    }

    @GetMapping("/kewajiban/{sertifikat}/tlc")
    public ResponseEntity<byte[]> showDocument(@PathVariable KewajibanSertifikatTlc sertifikat) throws Exception{
        String lokasiFile = uploadTlc + File.separator + sertifikat.getBebasKewajiban().getMahasiswa().getNim() + File.separator +
                sertifikat.getFile();

        log.debug("lokasi file : {}" + lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (sertifikat.getFile().toLowerCase().endsWith("jpeg") || sertifikat.getFile().toLowerCase().endsWith("jpg")){
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (sertifikat.getFile().toLowerCase().endsWith("png")){
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (sertifikat.getFile().toLowerCase().endsWith("pdf")){
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

    @GetMapping("/kewajiban/admin/download")
    public void reportTlcPerAngkatan(@RequestParam String bagian, @RequestParam String angkatan, HttpServletResponse response) throws IOException {
        String[] columns = {"No", "NIM", "Nama", "Prodi", "Kategori Tes", "Tanggal Tes", "Nilai"};

        List<Object[]> listApporval = bebasKewajibanDao.downloadApproval(bagian, angkatan);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("TLC Approve");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i<columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listApporval){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[1].toString());
            row.createCell(2).setCellValue(list[2].toString());
            row.createCell(3).setCellValue(list[3].toString());
            row.createCell(4).setCellValue(list[4].toString());
            row.createCell(5).setCellValue(list[5].toString());
            row.createCell(6).setCellValue(list[6].toString());
        }

        for (int i = 0; i<columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename = Rekap mahasiswa approve SKL by " + bagian + " angkatan " + angkatan + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/kewajiban/admin/download/periode")
    public void reportTlcPerPeriode(@RequestParam String bagian, @RequestParam String tglMulai, @RequestParam String tglSampai, HttpServletResponse response) throws IOException {
        String[] columns = {"No", "NIM", "Nama", "Prodi", "Angkatan", "Kategori Tes", "Tanggal Tes", "Nilai", "Tanggal di Approve"};

        List<Object[]> listApporval = bebasKewajibanDao.downloadApprovalPerPeriode(bagian, tglMulai, tglSampai);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("TLC Approve");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i<columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listApporval){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[1].toString());
            row.createCell(2).setCellValue(list[2].toString());
            row.createCell(3).setCellValue(list[3].toString());
            row.createCell(4).setCellValue(list[4].toString());
            row.createCell(5).setCellValue(list[5].toString());
            row.createCell(6).setCellValue(list[6].toString());
            row.createCell(7).setCellValue(list[7].toString());
            row.createCell(8).setCellValue(list[8].toString());
        }

        for (int i = 0; i<columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename = Rekap mahasiswa approve SKL by " + bagian + " periode " + tglMulai + " Sampai " + tglSampai + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @PostMapping("/kewajiban/admin/tlc/approve")
    public String approveTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, @RequestParam(required = false) Integer toefl,
                             @RequestParam(required = false) BigDecimal ielts, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveTlc(StatusApprove.APPROVED);
        if (toefl != null) {
            bebasKewajiban.setNilaiToefl(new BigDecimal(toefl));
        }
        if (ielts != null) {
            bebasKewajiban.setNilaiIelts(ielts);
        }
        bebasKewajibanDao.save(bebasKewajiban);

        String file = "-";

        bebasKewajibanService.saveTlc(bebasKewajiban, komentar, user, file);
        mailService.mailApproveTlc(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/tlc/list";
    }

    @PostMapping("/kewajiban/admin/tlc/edit")
    public String editNilaiTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) Integer toefl, @RequestParam(required = false) BigDecimal ielts){
        if (toefl != null) {
            bebasKewajiban.setNilaiToefl(new BigDecimal(toefl));
        }
        if (ielts != null) {
            bebasKewajiban.setNilaiIelts(ielts);
        }
        bebasKewajibanDao.save(bebasKewajiban);

        return "redirect:/kewajiban/admin/tlc/approval";
    }

    @PostMapping("/kewajiban/admin/tlc/reject")
    public String rejectTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, MultipartFile file, Authentication authentication) throws IOException {
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveTlc(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        List<KewajibanSertifikatTlc> listSerti = kewajibanSertifikatTlcDao.findByStatusAndBebasKewajibanAndTipe(StatusRecord.AKTIF, bebasKewajiban, Tipe.TLC);
        for (KewajibanSertifikatTlc serti : listSerti){
            serti.setStatus(StatusRecord.HAPUS);
            kewajibanSertifikatTlcDao.save(serti);
        }

        String namaAsli = file.getOriginalFilename();

        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));
        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = rejectedByTlc + File.separator + bebasKewajiban.getMahasiswa().getNim();
        log.info("Lokasi Upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        String namaFile = idFile + "." + extension;
        File tujuan = new File(lokasiUpload + File.separator + namaFile);
        file.transferTo(tujuan);


        bebasKewajibanService.saveTlc(bebasKewajiban, komentar, user, namaFile);
        mailService.mailRejectTlc(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/tlc/list";
    }

    @GetMapping("/kewajiban/rejected/{rejected}/tlc")
    public ResponseEntity<byte[]> showFileRejected(@PathVariable HistoriBebasKewajiban rejected){
        String lokasiFile = rejectedByTlc + File.separator + rejected.getBebasKewajiban().getMahasiswa().getNim() + File.separator + rejected.getFile();
        log.debug("lokasi file : {}", lokasiFile);

        try {
            HttpHeaders headers = new HttpHeaders();
            if (rejected.getFile().toLowerCase().endsWith("jpeg") || rejected.getFile().toLowerCase().endsWith("jpg")){
                headers.setContentType(MediaType.IMAGE_JPEG);
            }else if (rejected.getFile().toLowerCase().endsWith("png")){
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (rejected.getFile().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            }else{
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        }catch (Exception err){
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // BAGIAN TQC
    @GetMapping("/kewajiban/admin/tqc/list")
    public void listKewajibanTQC(Model model, @PageableDefault(size = 10) Pageable page, @RequestParam(required = false) String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveTlcNotInAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, Arrays.asList(StatusApprove.APPROVED, StatusApprove.WAITING_TLC), search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveTlcNotInOrderByTanggalRequest(StatusRecord.AKTIF, Arrays.asList(StatusApprove.APPROVED, StatusApprove.WAITING_TLC), page));
        }
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TQC, StatusRecord.AKTIF));
        model.addAttribute("sertifikat", kewajibanSertifikatTlcDao.findByStatusAndTipe(StatusRecord.AKTIF, Tipe.TQC));
    }

    @GetMapping("/kewajiban/admin/tqc/approval")
    public void historiApprovalTqc(Model model){
        model.addAttribute("list", bebasKewajibanDao.historiApproval(ByBagian.TQC.toString()));
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TQC, StatusRecord.AKTIF));
    }

    @PostMapping("/kewajiban/admin/tqc/approve")
    public String approveTqc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveTlc(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveTqc(bebasKewajiban, komentar, user);
        mailService.mailApproveTQC(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/tqc/list";
    }

    @PostMapping("/kewajiban/admin/tqc/reject")
    public String rejectTqc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveTlc(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        List<KewajibanSertifikatTlc> listSerti = kewajibanSertifikatTlcDao.findByStatusAndBebasKewajibanAndTipe(StatusRecord.AKTIF, bebasKewajiban, Tipe.TQC);
        for (KewajibanSertifikatTlc serti : listSerti){
            serti.setStatus(StatusRecord.HAPUS);
            kewajibanSertifikatTlcDao.save(serti);
        }

        bebasKewajibanService.saveTqc(bebasKewajiban, komentar, user);
        mailService.mailRejectTQC(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/tqc/list";
    }

    // BAGIAN AKADEMIK

    @GetMapping("/kewajiban/admin/akademik/list")
    public void listKewajibanAkademik(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveAkademikNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrMahasiswaAngkatanContainingIgnoreCaseOrMahasiswaIdProdiNamaProdiContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, search, search, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveAkademikNotOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, page));
        }

        model.addAttribute("hPerpus", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF));
        model.addAttribute("hKabagPerpus", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KABAG_PERPUS, StatusRecord.AKTIF));
        model.addAttribute("hKeuangan", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KEUANGAN, StatusRecord.AKTIF));
        model.addAttribute("hTlc", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TLC, StatusRecord.AKTIF));
        model.addAttribute("hAkademik", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.AKADEMIK, StatusRecord.AKTIF));

    }

    @GetMapping("/get-akademik-detail/{id}")
    @ResponseBody
    public List<HistoriBebasKewajiban> getDetailAkademik(@PathVariable String id){
        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(id).get();

        return historiBebasKewajibanDao.getDetailAkademik(bebasKewajiban.getId());
    }

    @GetMapping("/kewajiban/admin/akademik/approval")
    public void historiApprovalAkademik(Model model){
        model.addAttribute("list", bebasKewajibanDao.historiApproval(ByBagian.AKADEMIK.toString()));
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.AKADEMIK, StatusRecord.AKTIF));
    }

    @PostMapping("/kewajiban/admin/akademik/approve")
    public String approveAkademik(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveAkademik(StatusApprove.APPROVED);
        bebasKewajiban.setStatusApprove(StatusApprove.APPROVED);
        bebasKewajiban.setNomorUrut(bebasKewajibanService.nomorUrut());
        bebasKewajiban.setTahun(String.valueOf(LocalDate.now().getYear()));
        bebasKewajiban.setNomorSurat(bebasKewajibanService.createNomorSurat(bebasKewajiban.getNomorUrut(), bebasKewajiban.getTahun()));
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveAkademik(bebasKewajiban, komentar, user);
        mailService.mailApproveAkademik(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/akademik/list";
    }

    @PostMapping("/kewajiban/admin/akademik/reject")
    public String rejectAkademik(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveAkademik(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveAkademik(bebasKewajiban, komentar, user);
        mailService.mailRejectAkademik(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/akademik/list";
    }

}
