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
import java.util.*;

@Controller
public class TugasAkhirController {

    @Autowired
    PeriodeWisudaDao periodeWisudaDao;

    @Autowired
    KategoriTugasAkhirDao kategoriDao;

    @Autowired
    KrsDetailDao krsDetailDao;

    @Autowired
    private SidangDao sidangDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

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

    @Value("classpath:/sample/wisudawan.xlsx")
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


            String namaFile = UUID.randomUUID().toString();
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


            String namaFile = UUID.randomUUID().toString();
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


            String namaFile = UUID.randomUUID().toString();
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


            String namaFile = UUID.randomUUID().toString();
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
    public void resultWisuda(Model model,@RequestParam(required = false) Prodi prodi,@RequestParam(required = false) PeriodeWisuda periode,@PageableDefault(size = 30) Pageable page){
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
    public String approveWisuda(@RequestParam Wisuda wisuda,@RequestParam String komentar) throws Exception{
        String lokasiUpload = wisudaFolder + File.separator + wisuda.getMahasiswa().getNim();
        Path  lokasiFoto = Path.of(lokasiUpload + File.separator + wisuda.getFoto());

        String extension = "";

        int i = wisuda.getFoto().lastIndexOf('.');
        int p = Math.max(wisuda.getFoto().lastIndexOf('/'), wisuda.getFoto().lastIndexOf('\\'));

        if (i > p) {
            extension = wisuda.getFoto().substring(i + 1);
        }

        Files.move(lokasiFoto, lokasiFoto.resolveSibling( wisuda.getMahasiswa().getNim() + "_" +  wisuda.getMahasiswa().getNama() + "." + extension));
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
    public void wisudaExcel (@RequestParam(required = false, name = "periode", value = "periode") PeriodeWisuda periodeWisuda,
                             @RequestParam(required = false, name = "prodi", value = "prodi") Prodi prodi,
                             @RequestParam(required = false, name = "jenjang", value = "jenjang") Jenjang jenjang, HttpServletResponse response) throws IOException {

        InputStream file = getContohExcelWisuda.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), "Data Wisuda");

        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        font.setFontName("Times New Roman");

        Font fontCenter = workbook.createFont();
        fontCenter.setFontHeightInPoints((short) 12);
        fontCenter.setFontName("Times New Roman");

        Font fontHeader = workbook.createFont();
        fontHeader.setBold(true);
        fontHeader.setFontHeightInPoints((short) 14);
        fontHeader.setFontName("Times New Roman");

        CellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setAlignment(HorizontalAlignment.LEFT);
        styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        styleHeader.setFillBackgroundColor(IndexedColors.LIME.getIndex());
        styleHeader.setFont(fontHeader);

        CellStyle styleData = workbook.createCellStyle();
        styleData.setAlignment(HorizontalAlignment.LEFT);
        styleData.setVerticalAlignment(VerticalAlignment.CENTER);
        styleData.setFont(font);
        styleData.setBorderBottom(BorderStyle.THIN);
        styleData.setBorderRight(BorderStyle.THIN);
        styleData.setBorderLeft(BorderStyle.THIN);
        styleData.setBorderTop(BorderStyle.THIN);

        CellStyle styleDataCenter = workbook.createCellStyle();
        styleDataCenter.setAlignment(HorizontalAlignment.LEFT);
        styleDataCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataCenter.setFont(fontCenter);
        styleDataCenter.setBorderBottom(BorderStyle.THIN);
        styleDataCenter.setBorderRight(BorderStyle.THIN);
        styleDataCenter.setBorderLeft(BorderStyle.THIN);
        styleDataCenter.setBorderTop(BorderStyle.THIN);

        int header1 = 0 ;
        int header2 = 1 ;
        int header3 = 2 ;
        String namaFile ="";



        List<Wisuda> wisuda = null;

        if (prodi == null && jenjang == null){
            namaFile = "Data Wisudawan & " + periodeWisuda.getNama();
            wisuda = wisudaDao.findByStatusAndPeriodeWisudaOrderByMahasiswaIdProdiIdAscMahasiswaNimAsc(StatusApprove.APPROVED, periodeWisuda);
            Row HeaderLulusan = sheet.createRow(header1);
            HeaderLulusan.createCell(0).setCellValue("DAFTAR NAMA WISUDAWAN DAN " + periodeWisuda.getNama());
            HeaderLulusan.getCell(0).setCellStyle(styleHeader);
        }

        if (prodi != null){
            namaFile = "Data Wisudawan Prodi " + prodi.getNamaProdi() + " & " + periodeWisuda.getNama();
            wisuda = wisudaDao.findByStatusAndPeriodeWisudaAndMahasiswaIdProdiOrderByMahasiswaNimAsc(StatusApprove.APPROVED, periodeWisuda,prodi);
            Row HeaderLulusan = sheet.createRow(header1);
            HeaderLulusan.createCell(0).setCellValue("DAFTAR NAMA WISUDAWAN DAN " + periodeWisuda.getNama());
            HeaderLulusan.getCell(0).setCellStyle(styleHeader);
            Row HeaderProdi = sheet.createRow(header2);
            HeaderProdi.createCell(0).setCellValue("PROGRAM STUDI  " + periodeWisuda.getNama());
            HeaderProdi.getCell(0).setCellStyle(styleHeader);

        }

        if (jenjang != null){
            wisuda = wisudaDao.findByStatusAndPeriodeWisudaAndMahasiswaIdProdiIdJenjangOrderByMahasiswaIdProdiIdAscMahasiswaNimAsc(StatusApprove.APPROVED, periodeWisuda,jenjang);
            Row HeaderLulusan = sheet.createRow(header1);
            HeaderLulusan.createCell(0).setCellValue("DAFTAR NAMA WISUDAWAN DAN " + periodeWisuda.getNama());
            HeaderLulusan.getCell(0).setCellStyle(styleHeader);
            Row HeaderProdi = sheet.createRow(header2);
            if (jenjang.getId().equals("01")) {
                namaFile = "Data Wisudawan S1 & " + periodeWisuda.getNama();
                HeaderProdi.createCell(0).setCellValue("SARJANA STRATA SATU" + periodeWisuda.getNama());
                HeaderProdi.getCell(0).setCellStyle(styleHeader);
            }else {
                namaFile = "Data Wisudawan S2 & " + periodeWisuda.getNama();
                HeaderProdi.createCell(0).setCellValue("MAGISTER  ");
                HeaderProdi.getCell(0).setCellStyle(styleHeader);
            }
        }

        int dataWisuda = 5 ;
        int no = 1;
        for (Wisuda data : wisuda) {
            Row row = sheet.createRow(dataWisuda);
            row.createCell(0).setCellValue(no++);
            row.createCell(1).setCellValue(data.getMahasiswa().getNim());
            row.createCell(2).setCellValue(data.getMahasiswa().getNama());
            row.createCell(3).setCellValue(data.getMahasiswa().getIdProdi().getNamaProdi());
            row.createCell(4).setCellValue(data.getMahasiswa().getIdProdi().getFakultas().getNamaFakultas());
            row.createCell(5).setCellValue(data.getMahasiswa().getJenisKelamin().name());
            row.getCell(0).setCellStyle(styleData);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(2).setCellStyle(styleData);
            row.getCell(3).setCellStyle(styleData);
            row.getCell(4).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleData);

            if (data.getMahasiswa().getIdProdi().getIdJenjang().getId().equals("01")) {
                row.createCell(6).setCellValue("Sarjana Strata Satu");
                row.getCell(6).setCellStyle(styleData);
            }else {
                row.createCell(6).setCellValue("Magister");
                row.getCell(6).setCellStyle(styleData);

            }

            if (data.getMahasiswa().getTanggalLulus() != null) {
                row.createCell(7).setCellValue(data.getMahasiswa().getTanggalLulus().toString());
                row.getCell(7).setCellStyle(styleData);
            }else {
                row.createCell(7).setCellValue("-");
                row.getCell(7).setCellStyle(styleData);


            }
            row.createCell(8).setCellValue("-");
            row.getCell(8).setCellStyle(styleData);


            List<DataTranskript> listTranskript = krsDetailDao.listTranskript(data.getMahasiswa());
            listTranskript.removeIf(e -> e.getGrade().equals("E"));

            int sks = listTranskript.stream().map(DataTranskript::getSks).mapToInt(Integer::intValue).sum();
            BigDecimal mutu = listTranskript.stream().map(DataTranskript::getMutu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ipk = mutu.divide(new BigDecimal(sks),2,BigDecimal.ROUND_HALF_UP);
            BigDecimal validate = krsDetailDao.validasiTranskrip(data.getMahasiswa());


            row.createCell(12).setCellValue("-");
            row.getCell(12).setCellStyle(styleDataCenter);
            row.createCell(15).setCellValue(ipk.toString());
            row.getCell(15).setCellStyle(styleDataCenter);


            if (ipk.compareTo(new BigDecimal(2.99)) <= 0){
                row.createCell(16).setCellValue("Memuaskan");
                row.getCell(16).setCellStyle(styleData);

            }

            if (ipk.compareTo(new BigDecimal(3.00)) >= 0 && ipk.compareTo(new BigDecimal(3.49)) <= 0){
                row.createCell(16).setCellValue("Sangat Memuaskan");
                row.getCell(16).setCellStyle(styleData);

            }

            if (ipk.compareTo(new BigDecimal(3.50)) >= 0 && ipk.compareTo(new BigDecimal(3.79)) <= 0){

                if (validate != null){
                    row.createCell(16).setCellValue("Sangat Memuaskan");
                    row.getCell(16).setCellStyle(styleData);
                }else {
                    row.createCell(16).setCellValue("Pujian ");
                    row.getCell(16).setCellStyle(styleData);
                }

            }

            if (ipk.compareTo(new BigDecimal(3.80)) >= 0 && ipk.compareTo(new BigDecimal(4.00)) <= 0){

                if (validate != null){
                    row.createCell(16).setCellValue("Sangat Memuaskan");
                    row.getCell(16).setCellStyle(styleData);
                }else {
                    row.createCell(16).setCellValue("Pujian Tertinggi ");
                    row.getCell(16).setCellStyle(styleData);
                }


            }

            if (data.getMahasiswa().getBeasiswa() == null) {
                row.createCell(17).setCellValue("-");
                row.getCell(17).setCellStyle(styleData);
            }else {
                row.createCell(17).setCellValue(data.getMahasiswa().getBeasiswa().getNamaBeasiswa());
                row.getCell(17).setCellStyle(styleData);

            }
            dataWisuda++;
        }



        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

}
