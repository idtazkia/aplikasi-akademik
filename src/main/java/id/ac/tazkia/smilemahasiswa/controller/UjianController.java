package id.ac.tazkia.smilemahasiswa.controller;


import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import id.ac.tazkia.smilemahasiswa.service.UjianTahfidzKompreService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller @Slf4j
public class UjianController {
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private KaryawanDao karyawanDao;
    @Autowired
    private DosenDao dosenDao;
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private UserDao userDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Value("classpath:sample/tahfidz.odt")
    private Resource formulirTahfidz;

    @Value("classpath:sample/komprehensif.odt")
    private Resource formulirKompre;

    @Value("${upload.suratUjian}")
    private String uploadSurat;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private UjianTahfidzKompreService ujianTahfidzKompreService;

    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private UjianTahfidzKompreDao ujianTahfidzKompreDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private TagihanService tagihanService;

    @Autowired
    private RuanganDao ruanganDao;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @GetMapping("/tahfidz")
    public String formPendaftaran(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Integer checkRejected = ujianTahfidzKompreDao.jumlahRejected(mahasiswa, "UjianTahfidz");
        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa, StatusRecord.TAHFIDZ, true);

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        List<DataTranskript> listTranskript = krsDetailDao.listTranskript(mahasiswa);
        listTranskript.removeIf(e -> e.getGrade().equals("E"));

        int sks = listTranskript.stream().map(DataTranskript::getSks).mapToInt(Integer::intValue).sum();


        BigDecimal mutu = listTranskript.stream().map(DataTranskript::getMutu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);



        BigDecimal ipk = mutu.divide(new BigDecimal(sks),2,BigDecimal.ROUND_HALF_UP);

        model.addAttribute("enable", true);
        model.addAttribute("data", mahasiswa);
        model.addAttribute("check", checkRejected);
        model.addAttribute("ipk", ipk);
//        model.addAttribute("ipk", 3.51);
        model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndMahasiswaAndJenisUjian(StatusRecord.AKTIF, mahasiswa, "UjianTahfidz"));


        return "ujian/tahfidz";
    }

    @GetMapping("/kompre")
    public String formKompre(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Integer checkRejected = ujianTahfidzKompreDao.jumlahRejected(mahasiswa, "UjianKomprehensif");
        Integer checkNilai = ujianTahfidzKompreDao.jumlahNilaiD(mahasiswa);

        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa, StatusRecord.KOMPRE, true);

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        List<DataTranskript> listTranskript = krsDetailDao.listTranskript(mahasiswa);
        listTranskript.removeIf(e -> e.getGrade().equals("E"));

        int sks = listTranskript.stream().map(DataTranskript::getSks).mapToInt(Integer::intValue).sum();


        BigDecimal mutu = listTranskript.stream().map(DataTranskript::getMutu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal ipk = mutu.divide(new BigDecimal(sks),2,BigDecimal.ROUND_HALF_UP);

        model.addAttribute("totalSks", sks);
//        model.addAttribute("totalSks", 139);
        model.addAttribute("jumlahNilaiD", checkNilai);
        model.addAttribute("data", mahasiswa);
        model.addAttribute("check", checkRejected);
        model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndMahasiswaAndJenisUjian(StatusRecord.AKTIF, mahasiswa, "UjianKomprehensif"));

        return "ujian/kompre";
    }

    @GetMapping("/tahfidz/batal")
    public String batalTahfidz(@RequestParam String id){
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(id).get();

        ujianTahfidzKompre.setStatus(StatusRecord.HAPUS);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);
        return "redirect:/tahfidz";
    }

    @GetMapping("/kompre/batal")
    public String batalKompre(@RequestParam String id){
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(id).get();

        ujianTahfidzKompre.setStatus(StatusRecord.HAPUS);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);
        return "redirect:/kompre";
    }

    @PostMapping("/tahfidz/kirim")
    public String kirimPendaftaran(@Valid UjianTahfidzKompre ujianTahfidzKompre, @RequestParam("image") MultipartFile file,
                                   Authentication authentication)throws Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Dosen dosen = dosenDao.findById(mahasiswa.getIdProdi().getDosen().getId()).get();
        Prodi prodi = prodiDao.findById(mahasiswa.getIdProdi().getId()).get();


        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Integer checkRejected = ujianTahfidzKompreDao.jumlahRejected(mahasiswa, "UjianTahfidz");

        if (!file.isEmpty() || file != null) {
            String namaAsli = file.getOriginalFilename();
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }
            String idFile = UUID.randomUUID().toString();

            String lokasiUpload = uploadSurat + File.separator;
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);

            ujianTahfidzKompre.setFileSurat(idFile + "." + extension);
        }

//        if (checkRejected == 0){
//            ujianTahfidzKompre.setUjianKe(1);
//        }else {
//            Integer ujianKe = checkRejected + 1;
//            tagihanService.cretatTagihanMengulang(mahasiswa, tahunAkademik, "UjianTahfidz");
//            ujianTahfidzKompre.setUjianKe(ujianKe);
//        }
        ujianTahfidzKompre.setDosenKps(dosen);
        ujianTahfidzKompre.setProdi(prodi);
        ujianTahfidzKompre.setTanggalPengajuan(LocalDate.now());
        ujianTahfidzKompre.setMahasiswa(mahasiswa);
        ujianTahfidzKompre.setStatus(StatusRecord.AKTIF);
        ujianTahfidzKompre.setStatusApprovalAdmin("WAITING");
        ujianTahfidzKompre.setUrutan(1);
        ujianTahfidzKompre.setStatusLulus(StatusRecord.BELUM_DINILAI);
        ujianTahfidzKompre.setTahunAkademik(tahunAkademik);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);


        return "redirect:/tahfidz";

    }

    @PostMapping("/kompre/kirim")
    public String kirimPendaftaranKompre(@Valid UjianTahfidzKompre ujianTahfidzKompre, @RequestParam("image") MultipartFile file,
                                   Authentication authentication)throws Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Dosen dosen = dosenDao.findById(mahasiswa.getIdProdi().getDosen().getId()).get();
        Prodi prodi = prodiDao.findById(mahasiswa.getIdProdi().getId()).get();

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Integer checkRejected = ujianTahfidzKompreDao.jumlahRejected(mahasiswa, "UjianKomprehensif");


        if (!file.isEmpty() || file != null) {
            String namaAsli = file.getOriginalFilename();
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }
            String idFile = UUID.randomUUID().toString();

            String lokasiUpload = uploadSurat + File.separator;
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);

            ujianTahfidzKompre.setFileSurat(idFile + "." + extension);
        }

//        if (checkRejected == 0){
//            ujianTahfidzKompre.setUjianKe(1);
//        }else {
//            Integer ujianKe = checkRejected + 1;
//            tagihanService.cretatTagihanMengulang(mahasiswa, tahunAkademik, "UjianKomprehensif");
//            ujianTahfidzKompre.setUjianKe(ujianKe);
//        }

        ujianTahfidzKompre.setDosenKps(dosen);
        ujianTahfidzKompre.setProdi(prodi);
        ujianTahfidzKompre.setTanggalPengajuan(LocalDate.now());
        ujianTahfidzKompre.setMahasiswa(mahasiswa);
        ujianTahfidzKompre.setStatus(StatusRecord.AKTIF);
        ujianTahfidzKompre.setStatusApprovalAdmin("WAITING");
        ujianTahfidzKompre.setStatusApprovalKps("WAITING");
        ujianTahfidzKompre.setUrutan(1);
        ujianTahfidzKompre.setStatusLulus(StatusRecord.BELUM_DINILAI);
        ujianTahfidzKompre.setTahunAkademik(tahunAkademik);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);

        return "redirect:/kompre";

    }

    @GetMapping("/formulirtahfidz")
    public void formulitTahfidz(@RequestParam(name = "id")Mahasiswa mahasiswa, HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Formulir_Tahfidz_" + mahasiswa.getNim() + ".pdf";

        response.setHeader(headerKey, headerValue);
        ujianTahfidzKompreService.formulirTahfidz(mahasiswa ,response);
//        try {
//            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);
//
//            // 1. Load template dari file
//            InputStream in = formulirTahfidz.getInputStream();
//
//            // 2. Inisialisasi template engine, menentukan sintaks penulisan variabel
//            IXDocReport report = XDocReportRegistry.getRegistry().
//                    loadReport(in, TemplateEngineKind.Freemarker);
//
//            // 3. Context object, untuk mengisi variabel
//            BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
//            BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());
//
//            BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);
//
//            IContext ctx = report.createContext();
//            ctx.put("nama", mahasiswa.getNama());
//            ctx.put("nim", mahasiswa.getNim());
//            ctx.put("prodi",mahasiswa.getIdProdi().getNamaProdi());
//            ctx.put("ipk", ipk);
//            ctx.put("sks", totalSKS);
//            ctx.put("tgl", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
//
//            response.setHeader("Content-Disposition", "attachment;filename=Formulir_Ujian_Tahfidz-"+ mahasiswa.getIdProdi().getKodeProdi() + "-" + mahasiswa.getNim() +".pdf");
//            OutputStream out = response.getOutputStream();
//            report.convert(ctx, options, out);
//            out.flush();
//
//
//        }catch (Exception err){
////            logger.error(err.getMessage(), err);
//        }



    }

    @GetMapping("/formulirkompre")
    public void formulirKompre(@RequestParam(name = "id")Mahasiswa mahasiswa, HttpServletResponse response)throws IOException{
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Formulir_Kompre_" + mahasiswa.getNim() + ".pdf";

        response.setHeader(headerKey, headerValue);
        ujianTahfidzKompreService.formulirKompre(mahasiswa ,response);
//        try {
//            Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);
//
//            // 1. Load template dari file
//            InputStream in = formulirKompre.getInputStream();
//
//            // 2. Inisialisasi template engine, menentukan sintaks penulisan variabel
//            IXDocReport report = XDocReportRegistry.getRegistry().
//                    loadReport(in, TemplateEngineKind.Freemarker);
//
//            // 3. Context object, untuk mengisi variabel
//            BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
//            BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());
//
//            BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);
//
//            IContext ctx = report.createContext();
//            ctx.put("nama", mahasiswa.getNama());
//            ctx.put("nim", mahasiswa.getNim());
//            ctx.put("prodi",mahasiswa.getIdProdi().getNamaProdi());
//            ctx.put("ipk", ipk);
//            ctx.put("sks", totalSKS);
//            ctx.put("tgl", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
//
//            response.setHeader("Content-Disposition", "attachment;filename=Formulir_Sidang_kompre-"+ mahasiswa.getIdProdi().getKodeProdi() + "-" + mahasiswa.getNim() +".pdf");
//            OutputStream out = response.getOutputStream();
//            report.convert(ctx, options, out);
//            out.flush();
//
//
//        }catch (Exception err){
////            logger.error(err.getMessage(), err);
//        }
    }

    @GetMapping("/ujianTahfidz")
    public String listUjianTahfidz(Model model, Authentication authentication,
                                   @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) TahunAkademik tahunAkademik,
                                   @RequestParam(required = false) String search, @PageableDefault(size = 20)Pageable pageable){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        model.addAttribute("listprodi", prodi);
        model.addAttribute("listtahunAkademik", tahunAkademik);
        if (prodi != null && tahunAkademik != null){
            if (StringUtils.hasText(search)){
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, prodi, "UjianTahfidz",tahunAkademik,search,search, pageable));
            }else{
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, prodi, "UjianTahfidz",tahunAkademik, pageable));
            }
        } else if (tahunAkademik != null) {
            if (StringUtils.hasText(search)){
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "UjianTahfidz",tahunAkademik, search, search, pageable));
            }else {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "UjianTahfidz",tahunAkademik, pageable));
            }
        }

        return "ujian/admin/tahfidz";
    }

    @GetMapping("/komprehensif")
    public String list(Model model, Authentication authentication,
                       @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) TahunAkademik tahunAkademik,
                       @RequestParam(required = false) String search, @PageableDefault(size = 20)Pageable pageable){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        if (user.getRole().getName().equals("Superuser")){
            if (tahunAkademik != null && prodi != null){
                if (StringUtils.hasText(search)){
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, prodi, "UjianKomprehensif",tahunAkademik,search,search, pageable));
                }else{
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, prodi, "UjianKomprehensif",tahunAkademik, pageable));
                }
            } else if (tahunAkademik != null) {
                if (StringUtils.hasText(search)){
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "UjianKomprehensif",tahunAkademik, search, search, pageable));
                }else {
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "UjianKomprehensif",tahunAkademik, pageable));
                }
            }
        } else if (user.getRole().getName().equals("Kps")) {
            if (tahunAkademik != null && prodi != null){
                if (StringUtils.hasText(search)){
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, 2, prodi, "UjianKomprehensif",tahunAkademik,search,search, pageable));
                }else{
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, 2, prodi, "UjianKomprehensif",tahunAkademik, pageable));
                }
            } else if (tahunAkademik != null) {
                if (StringUtils.hasText(search)){
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord.AKTIF,2, "UjianKomprehensif",tahunAkademik, search, search, pageable));
                }else {
                    model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, 2,"UjianKomprehensif",tahunAkademik, pageable));
                }
            }
        }


        model.addAttribute("listprodi", prodi);
        model.addAttribute("listtahunAkademik", tahunAkademik);


        return "ujian/admin/kompre";
    }

    @GetMapping("/ujianTahfidKompre")
    public String listUTahfidzKompre(Model model, @RequestParam(required = false) String status, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        model.addAttribute("status", status);
        if (user.getRole().getName().equals("Superuser")){
            if (status.equals("APPROVED")){
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "APPROVED_ADMIN"));
            } else if (status.equals("REJECTED")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "REJECTED"));
            } else if (status.equals("All")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, 1));
            }
        }else if (user.getRole().getName().equals("Kps")) {
            if (status.equals("APPROVED")){
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "APPROVED_KPS"));
            } else if (status.equals("REJECTED")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "REJECTED"));
            } else if (status.equals("All")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanAndDosenKpsOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, 2, dosen));
            }
        }

        return "ujian/admin/list";
    }

    @PostMapping("/approvedTahfidz")
    public String approvedTahfidz(@Valid UjianTahfidzKompre ujianTahfidzKompre, Authentication authentication){
        User user = currentUserService.currentUser(authentication);

        UjianTahfidzKompre saveUjianTafihzKompre = ujianTahfidzKompreDao.findById(ujianTahfidzKompre.getId()).get();

        saveUjianTafihzKompre.setUrutan(3);
        saveUjianTafihzKompre.setStatusApprovalKps("APPROVED_ADMIN");
        saveUjianTafihzKompre.setEmailAdmin(user.getUsername());
        saveUjianTafihzKompre.setStatusApprovalAdmin("APPROVED_ADMIN");
        saveUjianTafihzKompre.setKomentarAdmin(ujianTahfidzKompre.getKomentarAdmin());

        ujianTahfidzKompreDao.save(saveUjianTafihzKompre);

        ujianTahfidzKompreService.notifEmail(ujianTahfidzKompre.getMahasiswa().getEmailTazkia(), "UjianTahfidz", "APPROVED");

        return "redirect:/ujianTahfidz";
    }

    @PostMapping("/approvedKompre")
    public String approvedKompre(@Valid UjianTahfidzKompre ujianTahfidzKompre, Authentication authentication){
        User user = currentUserService.currentUser(authentication);

        UjianTahfidzKompre saveUjianTafihzKompre = ujianTahfidzKompreDao.findById(ujianTahfidzKompre.getId()).get();

        saveUjianTafihzKompre.setUrutan(2);
        saveUjianTafihzKompre.setStatusApprovalKps("WAITING");
        saveUjianTafihzKompre.setEmailAdmin(user.getUsername());
        saveUjianTafihzKompre.setStatusApprovalAdmin("APPROVED_ADMIN");
        saveUjianTafihzKompre.setKomentarAdmin(ujianTahfidzKompre.getKomentarAdmin());

        ujianTahfidzKompreDao.save(saveUjianTafihzKompre);

        ujianTahfidzKompreService.notifEmail(ujianTahfidzKompre.getMahasiswa().getEmailTazkia(), "UjianKomprehensif", "APPROVED");

        return "redirect:/komprehensif";
    }

    @PostMapping("/approvedKps")
    public String approvedKps(@Valid UjianTahfidzKompre ujianTahfidzKompre){
        UjianTahfidzKompre saveUjianTafihzKompre = ujianTahfidzKompreDao.findById(ujianTahfidzKompre.getId()).get();

        saveUjianTafihzKompre.setUrutan(3);
        saveUjianTafihzKompre.setStatusApprovalKps("APPROVED_KPS");
        saveUjianTafihzKompre.setKomentarKps(ujianTahfidzKompre.getKomentarKps());

        ujianTahfidzKompreDao.save(saveUjianTafihzKompre);


        return "redirect:/komprehensif";
    }

    @PostMapping("/rejectedtahfidz")
    public String rejectedTahfidz(@Valid UjianTahfidzKompre ujianTahfidzKompre){
        UjianTahfidzKompre saveUjianTafihzKompre = ujianTahfidzKompreDao.findById(ujianTahfidzKompre.getId()).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

//        EnableFiture checkEnableFitur = enableFitureDao.findByMahasiswaAndFiturAndTahunAkademik(ujianTahfidzKompre.getMahasiswa(), StatusRecord.TAHFIDZ, tahunAkademik);
//        if (checkEnableFitur == null){
//            EnableFiture enableFiture = new EnableFiture();
//
//            enableFiture.setEnable(false);
//            enableFiture.setMahasiswa(ujianTahfidzKompre.getMahasiswa());
//            enableFiture.setFitur(StatusRecord.TAHFIDZ);
//            enableFiture.setTahunAkademik(tahunAkademik);
//            enableFiture.setKeterangan("-");
//            enableFitureDao.save(enableFiture);
//        } else {
//            checkEnableFitur.setEnable(false);
//            enableFitureDao.save(checkEnableFitur);
//        }


        saveUjianTafihzKompre.setUrutan(3);
        saveUjianTafihzKompre.setStatusApprovalAdmin("REJECTED");
        saveUjianTafihzKompre.setStatusApprovalKps("REJECTED");
        ujianTahfidzKompre.setKomentarAdmin(ujianTahfidzKompre.getKomentarAdmin());
        ujianTahfidzKompre.setKomentarKps(ujianTahfidzKompre.getKomentarKps());
        ujianTahfidzKompreDao.save(saveUjianTafihzKompre);

        ujianTahfidzKompreService.notifEmail(ujianTahfidzKompre.getMahasiswa().getEmailTazkia(), "UjianTahfidz", "REJECTED");
        return "redirect:/ujianTahfidz";
    }

    @PostMapping("/rejectedkompre")
    public String rejectedkompre(@Valid UjianTahfidzKompre ujianTahfidzKompre){
        UjianTahfidzKompre saveUjianTafihzKompre = ujianTahfidzKompreDao.findById(ujianTahfidzKompre.getId()).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

//        EnableFiture checkEnableFitur = enableFitureDao.findByMahasiswaAndFiturAndTahunAkademik(ujianTahfidzKompre.getMahasiswa(), StatusRecord.KOMPRE, tahunAkademik);
//        if (checkEnableFitur == null){
//            EnableFiture enableFiture = new EnableFiture();
//
//            enableFiture.setEnable(false);
//            enableFiture.setMahasiswa(ujianTahfidzKompre.getMahasiswa());
//            enableFiture.setFitur(StatusRecord.KOMPRE);
//            enableFiture.setTahunAkademik(tahunAkademik);
//            enableFiture.setKeterangan("-");
//            enableFitureDao.save(enableFiture);
//        } else {
//
//            checkEnableFitur.setEnable(false);
//            enableFitureDao.save(checkEnableFitur);
//        }

        saveUjianTafihzKompre.setUrutan(3);
        saveUjianTafihzKompre.setStatusApprovalAdmin("REJECTED");
        saveUjianTafihzKompre.setStatusApprovalKps("REJECTED");
        ujianTahfidzKompre.setKomentarAdmin(ujianTahfidzKompre.getKomentarAdmin());
        ujianTahfidzKompre.setKomentarKps(ujianTahfidzKompre.getKomentarKps());
        ujianTahfidzKompreDao.save(saveUjianTafihzKompre);

        ujianTahfidzKompreService.notifEmail(ujianTahfidzKompre.getMahasiswa().getEmailTazkia(), "UjianKomprehensif", "REJECTED");
        return "redirect:/komprehensif";
    }

    @GetMapping("/formulir/{file}")
    public ResponseEntity<byte[]> fileGambar(@PathVariable UjianTahfidzKompre file) throws Exception{
        String lokasiFile = uploadSurat + File.separator + file.getFileSurat();
        System.out.println("Test : " + lokasiFile);
        try {
            HttpHeaders headers = new HttpHeaders();
            if (file.getFileSurat().toLowerCase().endsWith("jpeg") || file.getFileSurat().toLowerCase().endsWith("jpg")){
                headers.setContentType(MediaType.IMAGE_JPEG);
            }else if(file.getFileSurat().toLowerCase().endsWith("png")){
                headers.setContentType(MediaType.IMAGE_PNG);
            }else if(file.getFileSurat().toLowerCase().endsWith("pdf")){
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


}
