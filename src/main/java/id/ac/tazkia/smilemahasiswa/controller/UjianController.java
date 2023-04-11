package id.ac.tazkia.smilemahasiswa.controller;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.dto.ujian.TableUjian;
import id.ac.tazkia.smilemahasiswa.dto.ujian.Tahfidz;
import id.ac.tazkia.smilemahasiswa.dto.ujian.TahfidzRequest;
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
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
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
    public String formPendaftaran(Model model, Authentication authentication) {
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


        BigDecimal ipk = mutu.divide(new BigDecimal(sks), 2, BigDecimal.ROUND_HALF_UP);

        model.addAttribute("enable", true);
        model.addAttribute("data", mahasiswa);
        model.addAttribute("check", checkRejected);
        model.addAttribute("ipk", ipk);
        model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndMahasiswaAndJenisUjianOrderByTanggalPengajuanDescStatusApprovalAdminAsc(StatusRecord.AKTIF, mahasiswa, "UjianTahfidz"));


        return "ujian/tahfidz";
    }

    @GetMapping("/get-pendaftaran-tahfidz")
    @ResponseBody
    public List<Tahfidz> listTahfidzAdmin(@RequestParam String ujian){
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(ujian).get();
        return ujianTahfidzKompreDao.getListMahasiswaTahfidz(ujianTahfidzKompre.getMahasiswa(),"UjianTahfidz");
    }

    @GetMapping("/get-pendaftaran-kompre")
    @ResponseBody
    public List<Tahfidz> listKompreAdmin(@RequestParam String ujian){
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(ujian).get();
        return ujianTahfidzKompreDao.getListMahasiswaTahfidz(ujianTahfidzKompre.getMahasiswa(),"UjianKomprehensif");
    }

    @GetMapping("/get-detail-tahfidz/{id}")
    @ResponseBody
    public UjianTahfidzKompre detailTahfidzAdmin(@PathVariable String id){
        return ujianTahfidzKompreDao.findById(id).get();
    }

    @GetMapping("/kompre")
    public String formKompre(Model model, Authentication authentication) {
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


        BigDecimal ipk = mutu.divide(new BigDecimal(sks), 2, BigDecimal.ROUND_HALF_UP);

        model.addAttribute("totalSks", sks);
//        model.addAttribute("totalSks", 139);
        model.addAttribute("jumlahNilaiD", checkNilai);
        model.addAttribute("data", mahasiswa);
        model.addAttribute("check", checkRejected);
        model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndMahasiswaAndJenisUjianOrderByTanggalPengajuanDescStatusApprovalAdminAsc(StatusRecord.AKTIF, mahasiswa, "UjianKomprehensif"));

        return "ujian/kompre";
    }

    @GetMapping("/tahfidz/batal")
    public String batalTahfidz(@RequestParam String id) {
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(id).get();

        ujianTahfidzKompre.setStatus(StatusRecord.HAPUS);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);
        return "redirect:/tahfidz";
    }

    @GetMapping("/kompre/batal")
    public String batalKompre(@RequestParam String id) {
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(id).get();

        ujianTahfidzKompre.setStatus(StatusRecord.HAPUS);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);
        return "redirect:/kompre";
    }

    @PostMapping("/tahfidz/kirim")
    public String kirimPendaftaran(@Valid UjianTahfidzKompre ujianTahfidzKompre, @RequestParam("image") MultipartFile file,
                                   Authentication authentication) throws Exception {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Dosen dosen = dosenDao.findById(mahasiswa.getIdProdi().getDosen().getId()).get();
        Prodi prodi = prodiDao.findById(mahasiswa.getIdProdi().getId()).get();


        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

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

        ujianTahfidzKompre.setProdi(prodi);
        ujianTahfidzKompre.setTanggalPengajuan(LocalDate.now());
        ujianTahfidzKompre.setMahasiswa(mahasiswa);
        ujianTahfidzKompre.setStatus(StatusRecord.AKTIF);
        ujianTahfidzKompre.setStatusApprovalAdmin("WAITING");

        if (!ujianTahfidzKompreDao.findByMahasiswaAndJenisUjian(mahasiswa,"UjianTahfidz").isEmpty()) {
            if (ujianTahfidzKompreDao.getUjianTerakhir(mahasiswa,"UjianTahfidz").getStatusLulus().equals(StatusRecord.TIDAK_LULUS)){
                ujianTahfidzKompre.setUjianKe(ujianTahfidzKompreDao.jumlahUjian(mahasiswa,"UjianTahfidz") + 1);
            }else {
                ujianTahfidzKompre.setUjianKe(ujianTahfidzKompreDao.jumlahUjian(mahasiswa,"UjianTahfidz"));
            }
        }else {
            ujianTahfidzKompre.setUjianKe(ujianTahfidzKompreDao.jumlahUjian(mahasiswa,"UjianTahfidz"));
        }
        ujianTahfidzKompre.setStatusLulus(StatusRecord.NOT_YET_SCHEDULED);
        ujianTahfidzKompre.setTahunAkademik(tahunAkademik);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);


        return "redirect:/tahfidz";

    }

    @PostMapping("/kompre/kirim")
    public String kirimPendaftaranKompre(@Valid UjianTahfidzKompre ujianTahfidzKompre, @RequestParam("image") MultipartFile file,
                                         Authentication authentication) throws Exception {
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


        ujianTahfidzKompre.setDosenKps(dosen);
        ujianTahfidzKompre.setProdi(prodi);
        ujianTahfidzKompre.setTanggalPengajuan(LocalDate.now());
        ujianTahfidzKompre.setMahasiswa(mahasiswa);
        ujianTahfidzKompre.setStatus(StatusRecord.AKTIF);
        ujianTahfidzKompre.setStatusApprovalAdmin("WAITING");
        ujianTahfidzKompre.setStatusApprovalKps("WAITING");

        if (!ujianTahfidzKompreDao.findByMahasiswaAndJenisUjian(mahasiswa,"UjianKomprehensif").isEmpty()) {
            if (ujianTahfidzKompreDao.getUjianTerakhir(mahasiswa,"UjianKomprehensif").getStatusLulus().equals(StatusRecord.TIDAK_LULUS)){
                ujianTahfidzKompre.setUjianKe(ujianTahfidzKompreDao.jumlahUjian(mahasiswa,"UjianKomprehensif") + 1);
            }else {
                ujianTahfidzKompre.setUjianKe(ujianTahfidzKompreDao.jumlahUjian(mahasiswa,"UjianKomprehensif"));
            }
        }else {
            ujianTahfidzKompre.setUjianKe(ujianTahfidzKompreDao.jumlahUjian(mahasiswa,"UjianKomprehensif"));
        }

        ujianTahfidzKompre.setStatusLulus(StatusRecord.NOT_YET_SCHEDULED);
        ujianTahfidzKompre.setTahunAkademik(tahunAkademik);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);

        return "redirect:/kompre";

    }

    @GetMapping("/formulirtahfidz")
    public void formulitTahfidz(@RequestParam(name = "id") Mahasiswa mahasiswa, HttpServletResponse response) throws IOException {

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Formulir_Tahfidz_" + mahasiswa.getNim() + ".pdf";

        response.setHeader(headerKey, headerValue);
        ujianTahfidzKompreService.formulirTahfidz(mahasiswa, response);

    }

    @GetMapping("/formulirkompre")
    public void formulirKompre(@RequestParam(name = "id") Mahasiswa mahasiswa, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Formulir_Kompre_" + mahasiswa.getNim() + ".pdf";

        response.setHeader(headerKey, headerValue);
        ujianTahfidzKompreService.formulirKompre(mahasiswa, response);
    }

    @GetMapping("/ujianTahfidz")
    public String listUjianTahfidz(Model model,
                                   @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) TahunAkademik tahunAkademik,
                                   @RequestParam(required = false) String search, @PageableDefault(size = 20) Pageable pageable) {


        if (prodi != null && tahunAkademik != null) {
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("ruangan", ruanganDao.findByStatus(StatusRecord.AKTIF));
            if (StringUtils.hasText(search)) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminNotInAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord.AKTIF,Arrays.asList("REJECTED"), prodi, "UjianTahfidz", tahunAkademik, search, pageable));
            } else {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, prodi, "UjianTahfidz", tahunAkademik, pageable));
            }
        }

        return "ujian/admin/tahfidz";
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/get-list-tahfidz/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableUjian listTahfidz(@PathVariable String id, @RequestParam String prodi, @RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(required = false) String order,
                                    @RequestParam(required = false) Integer offset,
                                    @RequestParam(required = false) Integer limit) {


        TahunAkademik tahunAkademik = tahunAkademikDao.findById(id).get();
        Prodi p = prodiDao.findById(prodi).get();

        if (search.isEmpty()) {
            TableUjian table = new TableUjian();
            table.setTotal(ujianTahfidzKompreDao.getListTahfidzAdmin(tahunAkademik,"UjianTahfidz", p).size());
            table.setTotalNotFiltered(ujianTahfidzKompreDao.getListTahfidzAdmin(tahunAkademik,"UjianTahfidz", p).size());
            table.setRows(ujianTahfidzKompreDao.getListTahfidzAdminLimit(tahunAkademik,"UjianTahfidz", p,limit,offset));
            return table;

        } else {
            TableUjian table = new TableUjian();
            table.setTotal(ujianTahfidzKompreDao.getListTahfidzAdminSearch(tahunAkademik,"UjianTahfidz", p, search).size());
            table.setTotalNotFiltered(ujianTahfidzKompreDao.getListTahfidzAdminSearchLimit(tahunAkademik,"UjianTahfidz", p, search, limit, offset).size());
            table.setRows(ujianTahfidzKompreDao.getListTahfidzAdminSearchLimit(tahunAkademik,"UjianTahfidz", p, search, limit, offset));
            return table;
        }

    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/get-list-kompre/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TableUjian listKompre(@PathVariable String id, @RequestParam String prodi, @RequestParam(required = false) String search,
                                    @RequestParam(required = false) String sort,
                                    @RequestParam(required = false) String order,
                                    @RequestParam(required = false) Integer offset,
                                    @RequestParam(required = false) Integer limit) {


        TahunAkademik tahunAkademik = tahunAkademikDao.findById(id).get();
        Prodi p = prodiDao.findById(prodi).get();

        if (search.isEmpty()) {
            TableUjian table = new TableUjian();
            table.setTotal(ujianTahfidzKompreDao.getListTahfidzAdmin(tahunAkademik,"UjianKomprehensif", p).size());
            table.setTotalNotFiltered(ujianTahfidzKompreDao.getListTahfidzAdmin(tahunAkademik,"UjianKomprehensif", p).size());
            table.setRows(ujianTahfidzKompreDao.getListTahfidzAdminLimit(tahunAkademik,"UjianKomprehensif", p,limit,offset));
            return table;

        } else {
            TableUjian table = new TableUjian();
            table.setTotal(ujianTahfidzKompreDao.getListTahfidzAdminSearch(tahunAkademik,"UjianKomprehensif", p, search).size());
            table.setTotalNotFiltered(ujianTahfidzKompreDao.getListTahfidzAdminSearchLimit(tahunAkademik,"UjianKomprehensif", p, search, limit, offset).size());
            table.setRows(ujianTahfidzKompreDao.getListTahfidzAdminSearchLimit(tahunAkademik,"UjianKomprehensif", p, search, limit, offset));
            return table;
        }

    }

    @GetMapping("/komprehensif")
    public String list(Model model, Authentication authentication,
                       @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) TahunAkademik tahunAkademik,
                       @RequestParam(required = false) String search, @PageableDefault(size = 20) Pageable pageable) {
        if (prodi != null && tahunAkademik != null) {
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("ruangan", ruanganDao.findByStatus(StatusRecord.AKTIF));

        }


        return "ujian/admin/kompre";
    }

    @GetMapping("/ujianTahfidKompre")
    public String listUTahfidzKompre(Model model, @RequestParam(required = false) String status, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        model.addAttribute("status", status);
        if (user.getRole().getName().equals("Superuser")) {
            if (status.equals("APPROVED")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "APPROVED_ADMIN"));
            } else if (status.equals("REJECTED")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "REJECTED"));
            } else if (status.equals("All")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, 1));
            }
        } else if (user.getRole().getName().equals("Kps")) {
            if (status.equals("APPROVED")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "APPROVED_KPS"));
            } else if (status.equals("REJECTED")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, "REJECTED"));
            } else if (status.equals("All")) {
                model.addAttribute("list", ujianTahfidzKompreDao.findByStatusAndUrutanAndDosenKpsOrderByTanggalPengajuanDesc(StatusRecord.AKTIF, 2, dosen));
            }
        }

        return "ujian/admin/list";
    }

    @PostMapping("/penjadwalan-ujian")
    @ResponseBody
    public ResponseEntity<BaseResponse> pernjadwalanTahfidzh(@RequestBody TahfidzRequest request,@RequestParam String jenisUjian) {
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(request.getId()).get();
        ujianTahfidzKompre.setTanggalUjian(LocalDate.parse(request.getTanggal()));
        ujianTahfidzKompre.setRuangan(ruanganDao.findById(request.getRuangan()).get());
        ujianTahfidzKompre.setPenguji(request.getPenguji());
        ujianTahfidzKompre.setStatusLulus(StatusRecord.SCHEDULED);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);

        String jenis = "";
        if (jenisUjian.equals("UjianTahfidz")){
            jenis = "Tahfidz";
        }else {
            jenis = "Komprehensif";
        }

        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.builder()
                        .code("200")
                        .message("Ujian " + jenis + " Atas Nama " + ujianTahfidzKompre.getMahasiswa().getNama() + " Berhasil Dijadwalkan")
                        .build()
        );
    }

    @PostMapping("/rejected/{id}")
    @ResponseBody
    public ResponseEntity<BaseResponse> rejecteUjian(@PathVariable String id, @RequestParam String komentar, @RequestParam String jenisUjian) {
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(id).get();

        ujianTahfidzKompre.setStatusApprovalAdmin("REJECTED");
        ujianTahfidzKompre.setStatusApprovalKps("REJECTED");
        ujianTahfidzKompre.setKomentarAdmin(komentar);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);

        String jenis = "";
        if (jenisUjian.equals("UjianTahfidz")){
            jenis = "Tahfidz";
        }else {
            jenis = "Komprehensif";
        }

        ujianTahfidzKompreService.notifEmail(ujianTahfidzKompre.getMahasiswa().getEmailTazkia(), jenisUjian, "REJECTED");
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.builder()
                        .code("200")
                        .message("Ujian " + jenis + " Atas Nama " + ujianTahfidzKompre.getMahasiswa().getNama() + " Berhasil Ditolak")
                        .build()
        );
    }

    @PostMapping("/approve/{id}")
    @ResponseBody
    public ResponseEntity<BaseResponse> approveUjian(@PathVariable String id, @RequestParam String komentar, @RequestParam String jenisUjian) {
        UjianTahfidzKompre ujianTahfidzKompre = ujianTahfidzKompreDao.findById(id).get();

        ujianTahfidzKompre.setStatusApprovalAdmin("APPROVED");
        ujianTahfidzKompre.setStatusApprovalKps("APPROVED");
        ujianTahfidzKompre.setKomentarAdmin(komentar);
        ujianTahfidzKompreDao.save(ujianTahfidzKompre);

        String jenis = "";
        if (jenisUjian.equals("UjianTahfidz")){
            jenis = "Tahfidz";
        }else {
            jenis = "Komprehensif";
        }

        ujianTahfidzKompreService.notifEmail(ujianTahfidzKompre.getMahasiswa().getEmailTazkia(), jenis, "APPROVED");
        return ResponseEntity.status(HttpStatus.OK).body(
                BaseResponse.builder()
                        .code("200")
                        .message("Ujian " + jenis + " Atas Nama " + ujianTahfidzKompre.getMahasiswa().getNama() + " Disetujui")

                        .build()
        );
    }

    @GetMapping("/formulir/{file}")
    public ResponseEntity<byte[]> fileGambar(@PathVariable UjianTahfidzKompre file) throws Exception {
        String lokasiFile = uploadSurat + File.separator + file.getFileSurat();
        System.out.println("Test : " + lokasiFile);
        try {
            HttpHeaders headers = new HttpHeaders();
            if (file.getFileSurat().toLowerCase().endsWith("jpeg") || file.getFileSurat().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (file.getFileSurat().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (file.getFileSurat().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            log.warn(err.getMessage(), err);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
