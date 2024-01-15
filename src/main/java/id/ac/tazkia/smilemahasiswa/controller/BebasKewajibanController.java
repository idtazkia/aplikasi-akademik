package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.*;
import id.ac.tazkia.smilemahasiswa.service.BebasKewajibanService;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.MailService;
import lombok.extern.slf4j.Slf4j;
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
    public static final List<String> USER_AKADEMIK = Arrays.asList("akademik1", "akademik2", "kabagakademik", "superuser");

    @Value("${upload.sertifikatTlc}")
    private String uploadTlc;

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
            return "/kewajiban/gagal";
        }
    }

    @GetMapping("/kewajiban/mahasiswa/detail")
    public void detailBebasKewajiban(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findByMahasiswa(mhs);
        if ("S2".equalsIgnoreCase(mhs.getIdProdi().getIdJenjang().getKodeJenjang())) {
            model.addAttribute("mhsS2", "Hide menu TLC untuk s2");
        }

        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("listKewajibanMahasiswa", kewajibanPerpusMahasiswaDao.findByBebasKewajibanOrderByKewajibanPerpusId(bebasKewajiban));
        // sertifikasi tlc
        model.addAttribute("listSertifikat", kewajibanSertifikatTlcDao.findByStatusAndBebasKewajiban(StatusRecord.AKTIF, bebasKewajiban));

        // cek histori komentar
        HistoriBebasKewajiban perpus = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF);
        HistoriBebasKewajiban kabagperpus = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.KABAG_PERPUS, StatusRecord.AKTIF);
        HistoriBebasKewajiban keuangan = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.KEUANGAN, StatusRecord.AKTIF);
        HistoriBebasKewajiban tlc = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.TLC, StatusRecord.AKTIF);
        HistoriBebasKewajiban akademik = historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.AKADEMIK, StatusRecord.AKTIF);
        if (perpus != null) {
            model.addAttribute("hPerpus", perpus);
        }
        if (perpus != null) {
            model.addAttribute("hKabagPerpus", kabagperpus);
        }
        if (perpus != null) {
            model.addAttribute("hKeuangan", keuangan);
        }
        if (perpus != null) {
            model.addAttribute("hTlc", tlc);
        }
        if (perpus != null) {
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
    public String sertiTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam String tanggal, @RequestParam String jenis){
        KewajibanSertifikatTlc cek = kewajibanSertifikatTlcDao.findByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF);
        if (cek == null) {
            KewajibanSertifikatTlc tlc = new KewajibanSertifikatTlc();
            tlc.setBebasKewajiban(bebasKewajiban);
            tlc.setTanggalUjian(LocalDate.parse(tanggal));
            tlc.setKeteranganUjian(ByBagian.DIKAMPUS);
            tlc.setJenisTes(jenis);
            kewajibanSertifikatTlcDao.save(tlc);

            PendaftaranBebasKewajiban kewajiban = tlc.getBebasKewajiban();
            kewajiban.setStatusApproveTlc(StatusApprove.WAITING);
            bebasKewajibanDao.save(kewajiban);

        }

        return "redirect:/kewajiban/mahasiswa/detail";
    }

    @PostMapping("/kewajiban/mahasiswa/tlc/diluar")
    public String sertiTlcDiluar(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam String tanggal, @RequestParam String tempat, @RequestParam String jenis, MultipartFile file) throws IOException {
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
            kewajibanSertifikatTlcDao.save(tlc);

            PendaftaranBebasKewajiban kewajiban = tlc.getBebasKewajiban();
            kewajiban.setStatusApproveTlc(StatusApprove.WAITING);
            bebasKewajibanDao.save(kewajiban);
        }

        return "redirect:/kewajiban/mahasiswa/detail";
    }

    @PostMapping("/kewajiban/mahasiswa/tlc/delete")
    public String deleteFileUpload(@RequestParam KewajibanSertifikatTlc sertifikat){

        sertifikat.setStatus(StatusRecord.HAPUS);
        kewajibanSertifikatTlcDao.save(sertifikat);

        return "redirect:/kewajiban/mahasiswa/detail";
    }

    // CETAK SKL
    @GetMapping("/cetakSkl")
    public String sklMahasiswa(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=SKL "+bebasKewajiban.getMahasiswa().getNim()+"_"+bebasKewajiban.getMahasiswa().getNama()+".pdf";

        response.setHeader(headerKey, headerValue);
        bebasKewajibanService.cetakSKL(bebasKewajiban, response);

        return "redirect:/kewajiban/mahasiswa/detail";
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
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusAndStatusApproveKabagPerpusNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, search, search, page));
            }else{
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusAndStatusApproveKabagPerpusNotOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, page));
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

    @PostMapping("/kewajiban/admin/perpus/approve")
    public String approveKabagPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveKabagPerpus(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKabagPerpus(bebasKewajiban, komentar, user);
        mailService.mailApproveKabagPerpus(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/perpus/list";
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
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveTlcNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndStatusApproveTlcNotOrderByTanggalRequest(StatusRecord.AKTIF, StatusApprove.APPROVED, page));
        }
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TLC, StatusRecord.AKTIF));
        model.addAttribute("sertifikat", kewajibanSertifikatTlcDao.findByStatus(StatusRecord.AKTIF));
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

        bebasKewajibanService.saveTlc(bebasKewajiban, komentar, user);
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
    public String rejectTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        bebasKewajiban.setStatusApproveTlc(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        List<KewajibanSertifikatTlc> listSerti = kewajibanSertifikatTlcDao.findByStatusAndBebasKewajiban(StatusRecord.AKTIF, bebasKewajiban);
        for (KewajibanSertifikatTlc serti : listSerti){
            serti.setStatus(StatusRecord.HAPUS);
            kewajibanSertifikatTlcDao.save(serti);
        }

        bebasKewajibanService.saveTlc(bebasKewajiban, komentar, user);
        mailService.mailRejectTlc(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/tlc/list";
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
