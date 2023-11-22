package id.ac.tazkia.smilemahasiswa.controller;

import groovy.lang.GString;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.*;
import id.ac.tazkia.smilemahasiswa.service.BebasKewajibanService;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.MailService;
import jdk.jshell.Snippet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.event.MouseInputAdapter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller @Slf4j
public class BebasKewajibanController {

    public static final List<String> USER_PERPUS = Arrays.asList("perpustakaan", "kabagperpus");
    public static final List<String> USER_FINANCE = Arrays.asList("finance", "kabagfinance");
    public static final List<String> USER_TLC = Arrays.asList("tlc");
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
    SertifikatTlcDao sertifikatTlcDao;

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
                return "/kewajiban/daftar";
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
        log.info("Mahasiswa : {}", bebasKewajiban);

        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("listKewajibanMahasiswa", kewajibanPerpusMahasiswaDao.findByBebasKewajiban(bebasKewajiban));
        // cek histori komentar
        model.addAttribute("hPerpus", historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF));
        model.addAttribute("hKabagPerpus", historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.KABAG_PERPUS, StatusRecord.AKTIF));
        model.addAttribute("hKeuangan", historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.KEUANGAN, StatusRecord.AKTIF));
        model.addAttribute("hTlc", historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.TLC, StatusRecord.AKTIF));
        model.addAttribute("hAkademik", historiBebasKewajibanDao.findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(bebasKewajiban, ByBagian.AKADEMIK, StatusRecord.AKTIF));

    }

    @PostMapping("/kewajiban/mahasiswa/tlc/dikampus")
    public String sertiTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam String tanggal){
        SertifikatTlc cek = sertifikatTlcDao.findByMahasiswaAndStatus(bebasKewajiban.getMahasiswa(), StatusRecord.AKTIF);
        if (cek == null) {
            SertifikatTlc tlc = new SertifikatTlc();
            tlc.setMahasiswa(bebasKewajiban.getMahasiswa());
            tlc.setTanggalUjian(LocalDate.parse(tanggal));
            tlc.setKeteranganUjian(ByBagian.DIKAMPUS);
            sertifikatTlcDao.save(tlc);
        }

        return "redirect:/kewajiban/mahasiswa/detail";
    }

    @PostMapping("/kewajiban/mahasiswa/tlc/diluar")
    public String sertiTlcDiluar(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam String tanggal, @RequestParam String tempat, MultipartFile file) throws IOException {
        SertifikatTlc cek = sertifikatTlcDao.findByMahasiswaAndStatus(bebasKewajiban.getMahasiswa(), StatusRecord.AKTIF);
        if (cek == null) {

            SertifikatTlc tlc = new SertifikatTlc();

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
            tlc.setMahasiswa(bebasKewajiban.getMahasiswa());
            tlc.setTempatUjian(tempat);
            tlc.setTanggalUjian(LocalDate.parse(tanggal));
            tlc.setKeteranganUjian(ByBagian.DILUAR);
            sertifikatTlcDao.save(tlc);
        }

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
            return "redirect:/kewajiban/admin/perpus/list";
        }

    }

    // BAGIAN PERPUSTAKAAN

    @GetMapping("/kewajiban/admin/perpus/list")
    public void listKewajibanPerpus(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        User user = currentUserService.currentUser(authentication);
        if ("perpustakaan".equalsIgnoreCase(user.getRole().getId()) || "superuser".equalsIgnoreCase(user.getRole().getId())) {
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, search, search, page));
            }else{
                model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusOrderByTanggalRequest(StatusRecord.AKTIF, page));
            }
        }
        if ("kabagperpus".equalsIgnoreCase(user.getRole().getId()) || "superuser".equalsIgnoreCase(user.getRole().getId())) {
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, search, search, page));
            }else{
                model.addAttribute("listMahasiswaKabag", bebasKewajibanDao.findByStatusOrderByTanggalRequest(StatusRecord.AKTIF, page));
            }
            model.addAttribute("listKewajiban", kewajibanPerpusMahasiswaDao.findAll());
            model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KABAG_PERPUS, StatusRecord.AKTIF));
        }
    }

    @GetMapping("/kewajiban/admin/perpus/detail")
    public void detailKewajibanPerpus(Model model, @RequestParam String id, Authentication authentication){

        PendaftaranBebasKewajiban bebasKewajiban = bebasKewajibanDao.findById(id).get();
//        model.addAttribute("listKewajibanPerpus", kewajibanPerpusDao.findByStatusOrderById(StatusRecord.AKTIF));
        model.addAttribute("listKewajibanMahasiswa", kewajibanPerpusMahasiswaDao.findByBebasKewajiban(bebasKewajiban));
        model.addAttribute("mahasiswa", bebasKewajiban.getMahasiswa());
        model.addAttribute("bebasKewajiban", bebasKewajiban);
        model.addAttribute("histori", historiBebasKewajibanDao.findByBebasKewajibanAndByBagianAndStatusOrderByWaktuInput(bebasKewajiban, ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF));
    }

    @PostMapping("/kewajiban/admin/perpus/input")
    public String saveKewajibanPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar, HttpServletRequest request){

        List<KewajibanPerpusMahasiswa> list = kewajibanPerpusMahasiswaDao.findByBebasKewajiban(bebasKewajiban);
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
        bebasKewajibanService.saveStaffPerpus(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/perpus/list";
    }

    @PostMapping("/kewajiban/admin/perpus/approve")
    public String approveKabagPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveKabagPerpus(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKabagPerpus(bebasKewajiban, komentar);
        mailService.mailApproveKabagPerpus(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/perpus/list";
    }

    @PostMapping("/kewajiban/admin/perpus/reject")
    public String rejectKabagPerpus(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveKabagPerpus(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKabagPerpus(bebasKewajiban, komentar);
        mailService.mailRejectKabagPerpus(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/perpus/list";
    }

    // BAGIAN KEUANGAN

    @GetMapping("/kewajiban/admin/keuangan/list")
    public void listKewajibanKeuangan(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusOrderByTanggalRequest(StatusRecord.AKTIF, page));
        }

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
    public String approveKeuangan(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveKeuangan(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKeuangan(bebasKewajiban, komentar);
        mailService.mailApproveKeuangan(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/keuangan/list";
    }

    @PostMapping("/kewajiban/admin/keuangan/reject")
    public String rejectKeuangan(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveKeuangan(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveKeuangan(bebasKewajiban, komentar);
        mailService.mailRejectKeuangan(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/keuangan/list";
    }

    // BAGIAN TLC

    @GetMapping("/kewajiban/admin/tlc/list")
    public void listTlc(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusOrderByTanggalRequest(StatusRecord.AKTIF, page));
        }
        model.addAttribute("histori", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TLC, StatusRecord.AKTIF));
        model.addAttribute("sertifikat", sertifikatTlcDao.findByStatus(StatusRecord.AKTIF));
    }

    @GetMapping("/kewajiban/{sertifikat}/tlc")
    public ResponseEntity<byte[]> showDocument(@PathVariable SertifikatTlc sertifikat) throws Exception{
        String lokasiFile = uploadTlc + File.separator + sertifikat.getMahasiswa().getNim() + File.separator +
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
    public String approveTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveTlc(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveTlc(bebasKewajiban, komentar);
        mailService.mailApproveTlc(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/tlc/list";
    }

    @PostMapping("/kewajiban/admin/tlc/reject")
    public String rejectTlc(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveTlc(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        List<SertifikatTlc> listSerti = sertifikatTlcDao.findByStatusAndMahasiswa(StatusRecord.AKTIF, bebasKewajiban.getMahasiswa());
        for (SertifikatTlc serti : listSerti){
            serti.setStatus(StatusRecord.HAPUS);
            sertifikatTlcDao.save(serti);
        }

        bebasKewajibanService.saveTlc(bebasKewajiban, komentar);
        mailService.mailRejectTlc(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/tlc/list";
    }

    // BAGIAN AKADEMIK

    @GetMapping("/kewajiban/admin/akademik/list")
    public void listKewajibanAkademik(Model model, Authentication authentication, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord.AKTIF, search, search, page));
        }else{
            model.addAttribute("listMahasiswa", bebasKewajibanDao.findByStatusOrderByTanggalRequest(StatusRecord.AKTIF, page));
        }

        model.addAttribute("hPerpus", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.PERPUSTAKAAN, StatusRecord.AKTIF));
        model.addAttribute("hKabagPerpus", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KABAG_PERPUS, StatusRecord.AKTIF));
        model.addAttribute("hKeuangan", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.KEUANGAN, StatusRecord.AKTIF));
        model.addAttribute("hTlc", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.TLC, StatusRecord.AKTIF));
        model.addAttribute("hAkademik", historiBebasKewajibanDao.findByByBagianAndStatusOrderByWaktuInput(ByBagian.AKADEMIK, StatusRecord.AKTIF));

    }

    @PostMapping("/kewajiban/admin/akademik/approve")
    public String approveAkademik(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveAkademik(StatusApprove.APPROVED);
        bebasKewajiban.setStatusApprove(StatusApprove.APPROVED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveAkademik(bebasKewajiban, komentar);
        mailService.mailApproveAkademik(bebasKewajiban, "DONE");

        return "redirect:/kewajiban/admin/akademik/list";
    }

    @PostMapping("/kewajiban/admin/akademik/reject")
    public String rejectAkademik(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, @RequestParam(required = false) String komentar){
        bebasKewajiban.setStatusApproveAkademik(StatusApprove.REJECTED);
        bebasKewajibanDao.save(bebasKewajiban);

        bebasKewajibanService.saveAkademik(bebasKewajiban, komentar);
        mailService.mailRejectAkademik(bebasKewajiban, komentar);

        return "redirect:/kewajiban/admin/akademik/list";
    }

}
