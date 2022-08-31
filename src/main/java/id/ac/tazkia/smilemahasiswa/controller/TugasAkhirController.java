package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.graduation.WisudaDto;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.MailService;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Controller
public class TugasAkhirController {

    @Autowired
    PeriodeWisudaDao periodeWisudaDao;

    @Autowired
    KategoriTugasAkhirDao kategoriDao;

    @Autowired
    private SidangDao sidangDao;

    @Autowired
    WisudaDao wisudaDao;

    @Autowired
    ProdiDao prodiDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private AyahDao ayahDao;

    @Autowired
    private IbuDao ibuDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private MailService mailService;

    @Autowired
    private BeasiswaDao beasiswaDao;

    @Value("${upload.wisuda}")
    private String wisudaFolder;

    @Value("classpath:/sample/wisuda.xlsx")
    private Resource getContohExcelWisuda;

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    // PERIODE WISUDA

    @GetMapping("/graduation/periodeWisuda/list")
    public void listPeriode(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listPeriode", periodeWisudaDao.findByStatusNotInAndNamaContainingIgnoreCase(Arrays.asList(StatusRecord.HAPUS), search, page));
        }else{
            model.addAttribute("listPeriode", periodeWisudaDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/graduation/periodeWisuda/form")
    public void formPeriode(Model model, @RequestParam(required = false) String id){
        model.addAttribute("newPeriode", new PeriodeWisuda());

        if (id != null && !id.isEmpty()){
            PeriodeWisuda periodeWisuda = periodeWisudaDao.findById(id).get();
            if (periodeWisuda != null){
                model.addAttribute("newPeriode", periodeWisuda);
                if (periodeWisuda.getStatus() == null){
                    periodeWisuda.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }

    }

    @PostMapping("/graduation/periodeWisuda/new")
    public String inputPeriode(@Valid PeriodeWisuda wisuda){

        periodeWisudaDao.save(wisuda);
        return "redirect:list";

    }

    @PostMapping("/graduation/periodeWisuda/delete")
    public String deletePeriode(@RequestParam PeriodeWisuda wisuda){

        wisuda.setStatus(StatusRecord.HAPUS);
        periodeWisudaDao.save(wisuda);
        return "redirect:list";

    }

    // KETEGORI TUGAS AKHIR

    @GetMapping("/graduation/kategori/list")
    public void listKategori(Model model, @RequestParam(required = false) Prodi prodi, @PageableDefault(size = 10) Pageable page){

        model.addAttribute("selectProdi", prodi);
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listNote", kategoriDao.findByProdiAndJenisAndStatusNotIn(prodi, Jenis.NOTE, Arrays.asList(StatusRecord.HAPUS), page));
        model.addAttribute("listSeminar", kategoriDao.findByProdiAndJenisAndStatusNotIn(prodi, Jenis.SEMINAR, Arrays.asList(StatusRecord.HAPUS), page));
        model.addAttribute("listSidang", kategoriDao.findByProdiAndJenisAndStatusNotIn(prodi, Jenis.SIDANG, Arrays.asList(StatusRecord.HAPUS), page));

        // modal add kategori
        model.addAttribute("jenis", Jenis.values());
        model.addAttribute("jenisValidasi", JenisValidasi.values());

    }

    @PostMapping("/graduation/kategori/new")
    public String saveKategori(@RequestParam(required = false) String[] prodi, @RequestParam(required = false) String nama,
                               @RequestParam(required = false) Jenis jenis, @RequestParam(required = false) JenisValidasi jenisValidasi){

        for (String p : prodi){
            Prodi prod = prodiDao.findById(p).get();
            KategoriTugasAkhir kategori = new KategoriTugasAkhir();
            kategori.setNama(nama);
            kategori.setProdi(prod);
            kategori.setJenis(jenis);
            kategori.setJenisValidasi(jenisValidasi);
            kategori.setStatus(StatusRecord.AKTIF);
            kategoriDao.save(kategori);
        }

        return "redirect:list";

    }
    @GetMapping("/graduation/sidang/mahasiswa/wisuda")
    public void daftarWisuda(Model model,@RequestParam Sidang sidang, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("sidang", sidang);
        model.addAttribute("beasiswa", beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF));
        WisudaDto wisudaDto = new WisudaDto();
        wisudaDto.setMahasiswa(mahasiswa.getId());
        wisudaDto.setNama(mahasiswa.getNama());
        wisudaDto.setTanggal(mahasiswa.getTanggalLahir());
        wisudaDto.setKelamin(mahasiswa.getJenisKelamin().toString());
        wisudaDto.setAyah(mahasiswa.getAyah().getNamaAyah());
        wisudaDto.setSidang(sidang.getId());
        wisudaDto.setIbu(mahasiswa.getIbu().getNamaIbuKandung());
        wisudaDto.setToga(mahasiswa.getUkuranBaju());
        wisudaDto.setNomor(mahasiswa.getTeleponSeluler());
        wisudaDto.setEmail(mahasiswa.getEmailPribadi());
        if (mahasiswa.getBeasiswa() != null) {
            wisudaDto.setBeasiswa(mahasiswa.getBeasiswa().getNamaBeasiswa());
            wisudaDto.setIdBeasiswa(mahasiswa.getBeasiswa().getId());
        }
        wisudaDto.setJudulIndo(sidang.getJudulTugasAkhir());
        wisudaDto.setJudulInggris(sidang.getJudulInggris());
        model.addAttribute("wisuda", wisudaDto);

    }

        @GetMapping("/graduation/wisuda/form")
    public void daftarWisudaPasca(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("beasiswa", beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF));
        WisudaDto wisudaDto = new WisudaDto();
        wisudaDto.setMahasiswa(mahasiswa.getId());
        wisudaDto.setNama(mahasiswa.getNama());
        wisudaDto.setTanggal(mahasiswa.getTanggalLahir());
        wisudaDto.setKelamin(mahasiswa.getJenisKelamin().toString());
        wisudaDto.setAyah(mahasiswa.getAyah().getNamaAyah());
        wisudaDto.setIbu(mahasiswa.getIbu().getNamaIbuKandung());
        wisudaDto.setToga(mahasiswa.getUkuranBaju());
        wisudaDto.setNomor(mahasiswa.getTeleponSeluler());
        wisudaDto.setEmail(mahasiswa.getEmailPribadi());
        if (mahasiswa.getBeasiswa() != null) {
            wisudaDto.setBeasiswa(mahasiswa.getBeasiswa().getNamaBeasiswa());
            wisudaDto.setIdBeasiswa(mahasiswa.getBeasiswa().getId());
        }
        wisudaDto.setJudulIndo(mahasiswa.getJudul());
        wisudaDto.setJudulInggris(mahasiswa.getTitle());
        model.addAttribute("wisuda", wisudaDto);

    }

    @GetMapping("/graduation/sidang/mahasiswa/wisudarevisi")
    public void revisiWisuda(Model model,@RequestParam(name = "id", value = "id") Wisuda wisuda, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("sidang", wisuda.getSidang());
        model.addAttribute("beasiswa", beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF));

        WisudaDto wisudaDto = new WisudaDto();
        wisudaDto.setMahasiswa(mahasiswa.getId());
        wisudaDto.setNama(mahasiswa.getNama());
        wisudaDto.setId(wisuda.getId());
        wisudaDto.setTanggal(mahasiswa.getTanggalLahir());
        wisudaDto.setKelamin(mahasiswa.getJenisKelamin().toString());
        wisudaDto.setAyah(mahasiswa.getAyah().getNamaAyah());
        wisudaDto.setSidang(wisuda.getSidang().getId());
        wisudaDto.setIbu(mahasiswa.getIbu().getNamaIbuKandung());
        wisudaDto.setToga(mahasiswa.getUkuranBaju());
        wisudaDto.setNomor(mahasiswa.getTeleponSeluler());
        wisudaDto.setEmail(mahasiswa.getEmailPribadi());
        if (mahasiswa.getBeasiswa() != null) {
            wisudaDto.setBeasiswa(mahasiswa.getBeasiswa().getNamaBeasiswa());
            wisudaDto.setIdBeasiswa(mahasiswa.getBeasiswa().getId());
        }
        wisudaDto.setJudulIndo(wisuda.getSidang().getJudulTugasAkhir());
        wisudaDto.setJudulInggris(wisuda.getSidang().getJudulInggris());
        model.addAttribute("wisuda", wisudaDto);

    }

    @GetMapping("/graduation/wisuda/revisi")
    public void revisiWisudaPasca(Model model,@RequestParam(name = "id", value = "id") Wisuda wisuda, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("beasiswa", beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF));

        WisudaDto wisudaDto = new WisudaDto();
        wisudaDto.setMahasiswa(mahasiswa.getId());
        wisudaDto.setNama(mahasiswa.getNama());
        wisudaDto.setId(wisuda.getId());
        wisudaDto.setTanggal(mahasiswa.getTanggalLahir());
        wisudaDto.setKelamin(mahasiswa.getJenisKelamin().toString());
        wisudaDto.setAyah(mahasiswa.getAyah().getNamaAyah());
        wisudaDto.setIbu(mahasiswa.getIbu().getNamaIbuKandung());
        wisudaDto.setToga(mahasiswa.getUkuranBaju());
        wisudaDto.setNomor(mahasiswa.getTeleponSeluler());
        wisudaDto.setEmail(mahasiswa.getEmailPribadi());
        if (mahasiswa.getBeasiswa() != null) {
            wisudaDto.setBeasiswa(mahasiswa.getBeasiswa().getNamaBeasiswa());
            wisudaDto.setIdBeasiswa(mahasiswa.getBeasiswa().getId());
        }
        wisudaDto.setJudulIndo(mahasiswa.getJudul());
        wisudaDto.setJudulInggris(mahasiswa.getTitle());
        model.addAttribute("wisuda", wisudaDto);

    }

    @PostMapping("/graduation/sidang/mahasiswa/wisuda-post")
    public String daftarWisuda(@Valid WisudaDto wisudaDto, MultipartFile foto, RedirectAttributes attributes, Authentication authentication) throws Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        String namaAsli = foto.getOriginalFilename();
        Long ukuran = foto.getSize();

        Ayah ayah = ayahDao.findById(mahasiswa.getAyah().getId()).get();
        Ibu ibu = ibuDao.findById(mahasiswa.getIbu().getId()).get();
        Sidang sidang = sidangDao.findById(wisudaDto.getSidang()).get();

        sidang.setJudulTugasAkhir(wisudaDto.getJudulIndo());
        sidang.setJudulInggris(wisudaDto.getJudulInggris());
        sidangDao.save(sidang);

        mahasiswa.setNama(WordUtils.capitalize(wisudaDto.getNama()));
        mahasiswa.setTanggalLahir(wisudaDto.getTanggal());
        mahasiswa.setJudul(wisudaDto.getJudulIndo());
        mahasiswa.setTitle(wisudaDto.getJudulInggris());
        mahasiswa.setUkuranBaju(wisudaDto.getToga());
        mahasiswa.setEmailPribadi(wisudaDto.getEmail());
        mahasiswa.setTeleponSeluler(wisudaDto.getNomor());
        mahasiswaDao.save(mahasiswa);


        ayah.setNamaAyah(WordUtils.capitalize(wisudaDto.getAyah()));
        ibu.setNamaIbuKandung(WordUtils.capitalize(wisudaDto.getIbu()));
        ayahDao.save(ayah);
        ibuDao.save(ibu);

        if (ukuran >= 1000000){
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String namaFile = mahasiswa.getNim() + "_" + mahasiswa.getNama();
            String lokasiUpload = wisudaFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + namaFile + "." + extension);
            foto.transferTo(tujuan);

            PeriodeWisuda periodeWisuda = periodeWisudaDao.findByStatus(StatusRecord.AKTIF);

            Wisuda wisuda = new Wisuda();
            wisuda.setMahasiswa(mahasiswa);
            wisuda.setFoto(namaFile + "." + extension);
            wisuda.setSidang(sidang);
            wisuda.setPeriodeWisuda(periodeWisuda);
            wisuda.setUkuran(String.valueOf(ukuran / (1024 * 1024)) + " Mb");
            wisuda.setStatus(StatusApprove.WAITING);
            wisudaDao.save(wisuda);

            mailService.detailWisuda(wisudaDto);
            return "redirect:waiting";


        }else {
            attributes.addFlashAttribute("kurang", "Ukuran File yg diupload kurang dari 1MB");
            return "redirect:wisuda?sidang="+wisudaDto.getSidang();

        }


    }

    @PostMapping("/graduation/wisuda/form-post")
    public String daftarWisudaPasca(@Valid WisudaDto wisudaDto, MultipartFile foto, RedirectAttributes attributes, Authentication authentication) throws Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        String namaAsli = foto.getOriginalFilename();
        Long ukuran = foto.getSize();

        Ayah ayah = ayahDao.findById(mahasiswa.getAyah().getId()).get();
        Ibu ibu = ibuDao.findById(mahasiswa.getIbu().getId()).get();

        mahasiswa.setNama(WordUtils.capitalize(wisudaDto.getNama()));
        mahasiswa.setTanggalLahir(wisudaDto.getTanggal());
        mahasiswa.setJudul(wisudaDto.getJudulIndo());
        mahasiswa.setTitle(wisudaDto.getJudulInggris());
        mahasiswa.setUkuranBaju(wisudaDto.getToga());
        mahasiswa.setEmailPribadi(wisudaDto.getEmail());
        mahasiswa.setTeleponSeluler(wisudaDto.getNomor());
        mahasiswaDao.save(mahasiswa);


        ayah.setNamaAyah(WordUtils.capitalize(wisudaDto.getAyah()));
        ibu.setNamaIbuKandung(WordUtils.capitalize(wisudaDto.getIbu()));
        ayahDao.save(ayah);
        ibuDao.save(ibu);

        if (ukuran >= 1000000){
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String namaFile = mahasiswa.getNim() + "_" + mahasiswa.getNama();
            String lokasiUpload = wisudaFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + namaFile + "." + extension);
            foto.transferTo(tujuan);

            PeriodeWisuda periodeWisuda = periodeWisudaDao.findByStatus(StatusRecord.AKTIF);

            Wisuda wisuda = new Wisuda();
            wisuda.setMahasiswa(mahasiswa);
            wisuda.setFoto(namaFile + "." + extension);
            wisuda.setPeriodeWisuda(periodeWisuda);
            wisuda.setUkuran(String.valueOf(ukuran / (1024 * 1024)) + " Mb");
            wisuda.setStatus(StatusApprove.WAITING);
            wisudaDao.save(wisuda);

            mailService.detailWisuda(wisudaDto);
            return "redirect:../sidang/mahasiswa/waiting?pasca=aktif";


        }else {
            attributes.addFlashAttribute("kurang", "Ukuran File yg diupload kurang dari 1MB");
            return "redirect:form";

        }


    }

    @PostMapping("/graduation/sidang/mahasiswa/wisuda-revisi")
    public String revisiWisuda(@Valid WisudaDto wisudaDto, MultipartFile foto, RedirectAttributes attributes, Authentication authentication) throws Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        String namaAsli = foto.getOriginalFilename();
        Long ukuran = foto.getSize();

        Ayah ayah = ayahDao.findById(mahasiswa.getAyah().getId()).get();
        Ibu ibu = ibuDao.findById(mahasiswa.getIbu().getId()).get();
        Sidang sidang = sidangDao.findById(wisudaDto.getSidang()).get();

        sidang.setJudulTugasAkhir(wisudaDto.getJudulIndo());
        sidang.setJudulInggris(wisudaDto.getJudulInggris());
        sidangDao.save(sidang);

        mahasiswa.setNama(WordUtils.capitalize(wisudaDto.getNama()));
        mahasiswa.setTanggalLahir(wisudaDto.getTanggal());
        mahasiswa.setJenisKelamin(JenisKelamin.valueOf(wisudaDto.getKelamin()));
        mahasiswa.setUkuranBaju(wisudaDto.getToga());
        mahasiswa.setEmailPribadi(wisudaDto.getEmail());
        mahasiswa.setTeleponSeluler(wisudaDto.getNomor());
        mahasiswaDao.save(mahasiswa);


        ayah.setNamaAyah(WordUtils.capitalize(wisudaDto.getAyah()));
        ibu.setNamaIbuKandung(WordUtils.capitalize(wisudaDto.getIbu()));
        ayahDao.save(ayah);
        ibuDao.save(ibu);

        if (ukuran >= 1000000){
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String namaFile = mahasiswa.getNim() + "_" + mahasiswa.getNama();
            String lokasiUpload = wisudaFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + namaFile + "." + extension);
            foto.transferTo(tujuan);

            PeriodeWisuda periodeWisuda = periodeWisudaDao.findByStatus(StatusRecord.AKTIF);

            Wisuda wisuda = wisudaDao.findById(wisudaDto.getId()).get();
            wisuda.setMahasiswa(mahasiswa);
            wisuda.setFoto(namaFile + "." + extension);
            wisuda.setSidang(sidang);
            wisuda.setPeriodeWisuda(periodeWisuda);
            wisuda.setUkuran(String.valueOf(ukuran / (1024 * 1024)) + " Mb");
            wisuda.setStatus(StatusApprove.WAITING);
            wisudaDao.save(wisuda);

            mailService.detailWisuda(wisudaDto);
            return "redirect:waiting";


        }else {
            attributes.addFlashAttribute("kurang", "Ukuran File yg diupload kurang dari 1MB");
            return "redirect:wisudarevisi?id="+wisudaDto.getId();

        }


    }

    @PostMapping("/graduation/wisuda/wisuda-revisi")
    public String revisiWisudaPasca(@Valid WisudaDto wisudaDto, MultipartFile foto, RedirectAttributes attributes, Authentication authentication) throws Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        String namaAsli = foto.getOriginalFilename();
        Long ukuran = foto.getSize();

        Ayah ayah = ayahDao.findById(mahasiswa.getAyah().getId()).get();
        Ibu ibu = ibuDao.findById(mahasiswa.getIbu().getId()).get();

        mahasiswa.setNama(WordUtils.capitalize(wisudaDto.getNama()));
        mahasiswa.setTanggalLahir(wisudaDto.getTanggal());
        mahasiswa.setJenisKelamin(JenisKelamin.valueOf(wisudaDto.getKelamin()));
        mahasiswa.setJudul(wisudaDto.getJudulIndo());
        mahasiswa.setTitle(wisudaDto.getJudulInggris());
        mahasiswa.setUkuranBaju(wisudaDto.getToga());
        mahasiswa.setEmailPribadi(wisudaDto.getEmail());
        mahasiswa.setTeleponSeluler(wisudaDto.getNomor());
        mahasiswaDao.save(mahasiswa);


        ayah.setNamaAyah(WordUtils.capitalize(wisudaDto.getAyah()));
        ibu.setNamaIbuKandung(WordUtils.capitalize(wisudaDto.getIbu()));
        ayahDao.save(ayah);
        ibuDao.save(ibu);

        if (ukuran >= 1000000){
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }


            String namaFile = mahasiswa.getNim() + "_" + mahasiswa.getNama();
            String lokasiUpload = wisudaFolder + File.separator + mahasiswa.getNim();
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + namaFile + "." + extension);
            foto.transferTo(tujuan);

            PeriodeWisuda periodeWisuda = periodeWisudaDao.findByStatus(StatusRecord.AKTIF);

            Wisuda wisuda = wisudaDao.findById(wisudaDto.getId()).get();
            wisuda.setMahasiswa(mahasiswa);
            wisuda.setFoto(namaFile + "." + extension);
            wisuda.setPeriodeWisuda(periodeWisuda);
            wisuda.setUkuran(String.valueOf(ukuran / (1024 * 1024)) + " Mb");
            wisuda.setStatus(StatusApprove.WAITING);
            wisudaDao.save(wisuda);

            mailService.detailWisuda(wisudaDto);
            return "redirect:../sidang/mahasiswa/waiting?pasca=aktif";


        }else {
            attributes.addFlashAttribute("kurang", "Ukuran File yg diupload kurang dari 1MB");
            return "redirect:revisi?id="+wisudaDto.getId();

        }


    }

    @GetMapping("/graduation/sidang/mahasiswa/result")
    public void resultWisuda(Model model,@RequestParam Wisuda wisuda, Authentication authentication){

        model.addAttribute("approved", wisuda);

    }

    @GetMapping("/graduation/sidang/mahasiswa/waiting")
    public void waitingWisuda(Model model,@RequestParam(required = false)String pasca, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);


        Wisuda waiting = wisudaDao.findByMahasiswaAndStatus(mahasiswa,StatusApprove.WAITING);
        if (waiting != null){
            if (pasca != null){
                model.addAttribute("pasca", pasca);
                model.addAttribute("waiting", waiting);
            }else {
                model.addAttribute("waiting", waiting);
            }
        }else {
            if (pasca != null){
                model.addAttribute("pasca", pasca);
                model.addAttribute("data", wisudaDao.findFirstByMahasiswaAndStatus(mahasiswa, StatusApprove.REJECTED));
                System.out.println(wisudaDao.findFirstByMahasiswaAndStatus(mahasiswa, StatusApprove.REJECTED));
                model.addAttribute("reject", "reject");
            }else {
                model.addAttribute("data", wisudaDao.findFirstByMahasiswaAndStatus(mahasiswa, StatusApprove.REJECTED));
                model.addAttribute("reject", "reject");
            }

        }

    }

    @GetMapping("/graduation/sidang/mahasiswa/valid")
    public String resultWisuda(Model model,@RequestParam(name = "id", value = "id") Mahasiswa mahasiswa, Authentication authentication){
        Wisuda wisuda = wisudaDao.findByMahasiswaAndStatus(mahasiswa,StatusApprove.APPROVED);

        if (wisuda != null){
            return "redirect:result?wisuda=" + wisuda.getId();
        }else {
            if (mahasiswa.getIdProdi().getIdJenjang().getId().equals("02")) {
                return "redirect:waiting?pasca=aktif";
            }else {
                return "redirect:waiting";
            }
        }

    }

    @GetMapping("/graduation/sidang/admin/listwisuda")
    public void resultWisuda(Model model,@RequestParam(required = false) Prodi prodi,@RequestParam(required = false) PeriodeWisuda periode, Pageable page){
        model.addAttribute("periode", periodeWisudaDao.findByStatusNotInOrderByTanggalWisudaDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("selectedProdi",prodi);
        model.addAttribute("selectedPeriode",periode);

        if (prodi == null){
            model.addAttribute("list", wisudaDao.findByPeriodeWisudaAndStatusNotInOrderByStatusDescMahasiswaIdProdiAsc(periode,Arrays.asList(StatusApprove.REJECTED,StatusApprove.HAPUS), page));
        }else {
            model.addAttribute("list", wisudaDao.findByPeriodeWisudaAndMahasiswaIdProdiAndStatusNotInOrderByStatusDesc(periode,prodi,Arrays.asList(StatusApprove.REJECTED,StatusApprove.HAPUS),page));
        }


    }

    @GetMapping("/graduation/sidang/admin/wisuda")
    public void detailWisuda(Model model, @RequestParam(name = "id", value = "id") Wisuda wisuda){
        model.addAttribute("wisuda", wisuda);
    }

    @GetMapping("/graduation/sidang/admin/approve")
    public String approveWisuda(@RequestParam Wisuda wisuda,@RequestParam String komentar){
        wisuda.setStatus(StatusApprove.APPROVED);
        wisuda.setKomentar(komentar);
        wisudaDao.save(wisuda);

        mailService.successWisuda(wisuda);
        return "redirect:listwisuda?periode="+ wisuda.getPeriodeWisuda().getId();
    }

    @GetMapping("/graduation/sidang/admin/reject")
    public String rejectWisuda(@RequestParam Wisuda wisuda,@RequestParam String komentar){
        wisuda.setStatus(StatusApprove.REJECTED);
        wisuda.setKomentar(komentar);
        wisudaDao.save(wisuda);

        return "redirect:listwisuda?periode="+ wisuda.getPeriodeWisuda().getId();
    }

    @GetMapping("/upload/{wisuda}/fotowisuda/")
    public ResponseEntity<byte[]> fotoWisuda(@PathVariable Wisuda wisuda, Model model) throws Exception {
        String lokasiFile = wisudaFolder + File.separator + wisuda.getMahasiswa().getNim()
                + File.separator + wisuda.getFoto();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (wisuda.getFoto().toLowerCase().endsWith("jpeg") || wisuda.getFoto().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (wisuda.getFoto().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (wisuda.getFoto().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }

    }

    @GetMapping("/graduation/info")
    public void detailWisuda(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
    }


    @RequestMapping("/downloadwisuda/{wisuda}")
    public void downloadImage( HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("wisuda") Wisuda wisuda)
    {
        //If user is not authorized - he should be thrown out from here itself

        //Authorized user will download the file
        String lokasi = wisudaFolder + File.separator + wisuda.getMahasiswa().getNim()
                + File.separator + wisuda.getFoto();
        String dataDirectory = request.getServletContext().getRealPath(lokasi);
        Path file = Paths.get(lokasi, wisuda.getFoto());
        System.out.println(file);
        if (Files.exists(file))
        {
            response.setContentType("image/jpeg");
            response.addHeader("Content-Disposition", "attachment; filename="+wisuda.getFoto());
            try
            {
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @GetMapping("/report/wisuda")
    public void wisudaExcel (@RequestParam(required = false) String nim, HttpServletResponse response) throws IOException {

        InputStream file = getContohExcelWisuda.getInputStream();

        List<Wisuda> wisuda = wisudaDao.findByStatusAndPeriodeWisuda(StatusApprove.APPROVED, periodeWisudaDao.findByStatus(StatusRecord.AKTIF));
        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), "Data Wisuda");

        Font manajemenFont = workbook.createFont();
        manajemenFont.setItalic(true);
        manajemenFont.setFontHeightInPoints((short) 10);
        manajemenFont.setFontName("Cambria");

        Font dataManajemenFont = workbook.createFont();
        dataManajemenFont.setFontHeightInPoints((short) 10);
        dataManajemenFont.setFontName("Cambria");

        Font subHeaderFont = workbook.createFont();
        subHeaderFont.setFontHeightInPoints((short) 10);
        subHeaderFont.setFontName("Cambria");
        subHeaderFont.setBold(true);

        Font judulFont = workbook.createFont();
        judulFont.setFontHeightInPoints((short) 10);
        judulFont.setFontName("Cambria");
        judulFont.setBold(true);

        Font thesisFont = workbook.createFont();
        thesisFont.setFontHeightInPoints((short) 10);
        thesisFont.setFontName("Cambria");

        Font symbolFont = workbook.createFont();
        symbolFont.setFontHeightInPoints((short) 10);
        symbolFont.setFontName("Cambria");

        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 10);
        dataFont.setFontName("Cambria");

        Font dataTableFont = workbook.createFont();
        dataTableFont.setFontHeightInPoints((short) 10);
        dataTableFont.setFontName("Cambria");

        Font prestasiFont = workbook.createFont();
        prestasiFont.setFontHeightInPoints((short) 10);
        prestasiFont.setFontName("Cambria");

        Font judulSkripsiFont = workbook.createFont();
        judulSkripsiFont.setFontHeightInPoints((short) 10);
        judulSkripsiFont.setFontName("Cambria");

        Font dataKhsFont = workbook.createFont();
        dataKhsFont.setFontHeightInPoints((short) 10);
        dataKhsFont.setFontName("Cambria");

        Font dataIpkFont = workbook.createFont();
        dataIpkFont.setFontHeightInPoints((short) 10);
        dataIpkFont.setFontName("Cambria");

        Font penilaianFont = workbook.createFont();
        penilaianFont.setFontHeightInPoints((short) 10);
        penilaianFont.setFontName("Cambria");

        Font prestasiFont2 = workbook.createFont();
        prestasiFont2.setFontHeightInPoints((short) 10);
        prestasiFont2.setFontName("Cambria");

        Font dataPrestasiFont = workbook.createFont();
        dataPrestasiFont.setFontHeightInPoints((short) 10);
        dataPrestasiFont.setFontName("Cambria");

        Font matkulFont = workbook.createFont();
        matkulFont.setFontHeightInPoints((short) 10);
        matkulFont.setFontName("Cambria");

        Font dataFontNew = workbook.createFont();
        dataFontNew.setFontHeightInPoints((short) 10);
        dataFontNew.setFontName("Times New Roman");

        Font prodiFont = workbook.createFont();
        prodiFont.setUnderline(XSSFFont.U_DOUBLE);
        prodiFont.setFontHeightInPoints((short) 10);
        prodiFont.setFontName("Cambria");

        Font  presFont = workbook.createFont();
        presFont.setFontHeightInPoints((short) 10);
        presFont.setFontName("Cambria");

        Font ipFont = workbook.createFont();
        ipFont.setBold(true);
        ipFont.setItalic(true);
        ipFont.setFontHeightInPoints((short) 10);
        ipFont.setFontName("Cambria");

        Font ipFontBorder = workbook.createFont();
        ipFontBorder.setBold(true);
        ipFontBorder.setItalic(true);
        ipFontBorder.setFontHeightInPoints((short) 10);
        ipFontBorder.setFontName("Cambria");

        Font lectureFont = workbook.createFont();
        lectureFont.setBold(true);
        lectureFont.setFontName("Cambria");
        lectureFont.setUnderline(XSSFFont.U_DOUBLE);
        lectureFont.setFontHeightInPoints((short) 10);

        Font nikFont = workbook.createFont();
        nikFont.setBold(true);
        nikFont.setFontName("Cambria");
        nikFont.setFontHeightInPoints((short) 10);

        CellStyle styleNik = workbook.createCellStyle();
        styleNik.setVerticalAlignment(VerticalAlignment.CENTER);
        styleNik.setFont(nikFont);


        CellStyle styleManajemen = workbook.createCellStyle();
        styleManajemen.setVerticalAlignment(VerticalAlignment.CENTER);
        styleManajemen.setAlignment(HorizontalAlignment.CENTER);
        styleManajemen.setFont(manajemenFont);

        CellStyle styleDosen = workbook.createCellStyle();
        styleDosen.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDosen.setFont(lectureFont);

        CellStyle styleProdi = workbook.createCellStyle();
        styleProdi.setBorderTop(BorderStyle.MEDIUM);
        styleProdi.setBorderBottom(BorderStyle.MEDIUM);
        styleProdi.setBorderLeft(BorderStyle.MEDIUM);
        styleProdi.setBorderRight(BorderStyle.MEDIUM);
        styleProdi.setFont(dataManajemenFont);

        CellStyle styleSubHeader = workbook.createCellStyle();
        styleSubHeader.setFont(subHeaderFont);
        styleSubHeader.setAlignment(HorizontalAlignment.LEFT);
        styleSubHeader.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle styleJudul = workbook.createCellStyle();
        styleJudul.setFont(judulFont);
        styleJudul.setAlignment(HorizontalAlignment.LEFT);
        styleJudul.setVerticalAlignment(VerticalAlignment.TOP);

        CellStyle styleThesis = workbook.createCellStyle();
        styleThesis.setFont(thesisFont);
        styleThesis.setAlignment(HorizontalAlignment.LEFT);
        styleThesis.setVerticalAlignment(VerticalAlignment.TOP);
        styleThesis.setWrapText(true);


        CellStyle styleData = workbook.createCellStyle();
        styleData.setFont(dataFont);

        CellStyle styleDataNew = workbook.createCellStyle();
        styleDataNew.setFont(dataFontNew);
        styleDataNew.setAlignment(HorizontalAlignment.CENTER);

        CellStyle styleDataKhs = workbook.createCellStyle();
        styleDataKhs.setAlignment(HorizontalAlignment.CENTER);
        styleDataKhs.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataKhs.setFont(dataKhsFont);

        CellStyle styleIsiIpk = workbook.createCellStyle();
        styleIsiIpk.setAlignment(HorizontalAlignment.CENTER);
        styleIsiIpk.setVerticalAlignment(VerticalAlignment.CENTER);
        styleIsiIpk.setFont(dataIpkFont);

        CellStyle stylePenilaian = workbook.createCellStyle();
        stylePenilaian.setAlignment(HorizontalAlignment.CENTER);
        stylePenilaian.setVerticalAlignment(VerticalAlignment.CENTER);
        stylePenilaian.setFont(penilaianFont);

        CellStyle styleDataTale = workbook.createCellStyle();
        styleDataTale.setFont(dataTableFont);
        styleDataTale.setAlignment(HorizontalAlignment.CENTER);
        styleDataTale.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataTale.setBorderRight(BorderStyle.THIN);
        styleDataTale.setBorderLeft(BorderStyle.THIN);

        CellStyle styleDataTale2 = workbook.createCellStyle();
        styleDataTale2.setFont(dataTableFont);
        styleDataTale2.setAlignment(HorizontalAlignment.CENTER);
        styleDataTale2.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataTale2.setBorderRight(BorderStyle.THIN);
        styleDataTale2.setBorderBottom(BorderStyle.THIN);
        styleDataTale2.setBorderLeft(BorderStyle.THIN);

        CellStyle styleDataPrestasiAkademik = workbook.createCellStyle();
        styleDataPrestasiAkademik.setFont(prestasiFont);
        styleDataPrestasiAkademik.setAlignment(HorizontalAlignment.LEFT);
        styleDataPrestasiAkademik.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataPrestasiAkademik.setBorderBottom(BorderStyle.MEDIUM);
        styleDataPrestasiAkademik.setBorderTop(BorderStyle.MEDIUM);
        styleDataPrestasiAkademik.setBorderRight(BorderStyle.MEDIUM);
        styleDataPrestasiAkademik.setBorderLeft(BorderStyle.MEDIUM);

        CellStyle styleDataKhsTable = workbook.createCellStyle();
        styleDataKhsTable.setAlignment(HorizontalAlignment.LEFT);
        styleDataKhsTable.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataKhsTable.setFont(matkulFont);
        styleDataKhsTable.setBorderRight(BorderStyle.THIN);
        styleDataKhsTable.setBorderLeft(BorderStyle.THIN);

        CellStyle styleDataKhsTable2 = workbook.createCellStyle();
        styleDataKhsTable2.setAlignment(HorizontalAlignment.LEFT);
        styleDataKhsTable2.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataKhsTable2.setFont(matkulFont);
        styleDataKhsTable2.setBorderBottom(BorderStyle.THIN);
        styleDataKhsTable2.setBorderRight(BorderStyle.THIN);
        styleDataKhsTable2.setBorderLeft(BorderStyle.THIN);


        CellStyle stylePrestasiAkademik = workbook.createCellStyle();
        stylePrestasiAkademik.setAlignment(HorizontalAlignment.LEFT);
        stylePrestasiAkademik.setVerticalAlignment(VerticalAlignment.CENTER);
        stylePrestasiAkademik.setFont(prestasiFont);

        CellStyle styleSubHeader1 = workbook.createCellStyle();
        styleSubHeader1.setAlignment(HorizontalAlignment.LEFT);
        styleSubHeader1.setVerticalAlignment(VerticalAlignment.CENTER);
        styleSubHeader1.setFont(ipFont);

        CellStyle styleJudulSkripsi = workbook.createCellStyle();
        styleJudulSkripsi.setFont(judulSkripsiFont);
        styleJudulSkripsi.setWrapText(true);

        CellStyle styleSymbol = workbook.createCellStyle();
        styleSymbol.setAlignment(HorizontalAlignment.RIGHT);
        styleSymbol.setFont(symbolFont);

        CellStyle styleTotal = workbook.createCellStyle();
        styleTotal.setAlignment(HorizontalAlignment.LEFT);
        styleTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTotal.setFont(ipFont);

        CellStyle styleTotalBorder = workbook.createCellStyle();
        styleTotalBorder.setAlignment(HorizontalAlignment.CENTER);
        styleTotalBorder.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTotalBorder.setFont(ipFontBorder);
        styleTotalBorder.setBorderBottom(BorderStyle.THIN);
        styleTotalBorder.setBorderTop(BorderStyle.THIN);
        styleTotalBorder.setBorderRight(BorderStyle.THIN);
        styleTotalBorder.setBorderLeft(BorderStyle.THIN);

        CellStyle styleIpk = workbook.createCellStyle();
        styleIpk.setFont(prodiFont);
        styleIpk.setAlignment(HorizontalAlignment.CENTER);
        styleIpk.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle stylePres = workbook.createCellStyle();
        stylePres.setFont(presFont);
        stylePres.setAlignment(HorizontalAlignment.CENTER);
        stylePres.setVerticalAlignment(VerticalAlignment.CENTER);
        stylePres.setBorderBottom(BorderStyle.MEDIUM);
        stylePres.setBorderTop(BorderStyle.MEDIUM);
        stylePres.setBorderRight(BorderStyle.MEDIUM);
        stylePres.setBorderLeft(BorderStyle.MEDIUM);

        CellStyle borderRight = workbook.createCellStyle();
        borderRight.setBorderRight(BorderStyle.MEDIUM);

        int dataWisuda = 5 ;
        for (Wisuda data : wisuda) {
            Row row = sheet.createRow(dataWisuda);
//            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,2,5));
//            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,8,9));

            row.createCell(1).setCellValue(data.getMahasiswa().getNim());
            row.createCell(2).setCellValue(data.getMahasiswa().getNama());
            row.createCell(3).setCellValue(data.getMahasiswa().getTanggalLahir().toString());
            row.createCell(4).setCellValue(data.getMahasiswa().getAyah().getNamaAyah());
            if (data.getMahasiswa().getBeasiswa() != null) {
                row.createCell(5).setCellValue(data.getMahasiswa().getBeasiswa().getNamaBeasiswa());
                row.getCell(5).setCellStyle(styleDataKhsTable2);
            }else {
                row.createCell(5).setCellValue("-");
                row.getCell(5).setCellStyle(styleDataKhsTable2);

            }
            row.createCell(6).setCellValue(data.getMahasiswa().getUkuranBaju());
            row.createCell(7).setCellValue(data.getMahasiswa().getJudul());
            row.createCell(8).setCellValue(data.getMahasiswa().getTitle());
            row.createCell(9).setCellValue(data.getMahasiswa().getJenisKelamin().toString());
            row.createCell(10).setCellValue(data.getMahasiswa().getTeleponSeluler());
            row.createCell(11).setCellValue(data.getMahasiswa().getEmailPribadi());
            row.createCell(12).setCellValue("3.22");
            row.createCell(13).setCellValue("asdasdasdad");
            row.getCell(1).setCellStyle(styleDataTale2);
                row.getCell(2).setCellStyle(styleDataKhsTable2);
                row.getCell(3).setCellStyle(styleDataKhsTable2);
                row.getCell(4).setCellStyle(styleDataKhsTable2);
                row.getCell(6).setCellStyle(styleDataTale2);
                row.getCell(7).setCellStyle(styleDataTale2);
                row.getCell(8).setCellStyle(styleDataTale2);
                row.getCell(10).setCellStyle(styleDataTale2);
                row.getCell(11).setCellStyle(styleDataTale2);
                row.getCell(12).setCellStyle(styleDataTale2);
                row.getCell(13).setCellStyle(styleDataTale2);
            dataWisuda++;
        }



        String namaFile = "Data Wisuda";
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
