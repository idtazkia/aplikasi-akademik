package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import id.ac.tazkia.smilemahasiswa.constant.StatusUjian;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.kusioner.KuesionerDto;
import id.ac.tazkia.smilemahasiswa.dto.prediksitest.TestDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
public class KuesionerController {

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KuesionerDao kuesionerDao;

    @Autowired
    private KuesionerPertanyaanDao kuesionerPertanyaanDao;

    @Autowired
    private KuesionerPilihanGandaDao kuesionerPilihanGandaDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private PrediksiTestDao prediksiTestDao;

    @Autowired
    private TagihanService tagihanService;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private  TahunAkademikDao tahunAkademikDao;
    @Autowired
    private KonversiNilaiToeflDao konversiNilaiToeflDao;
    @Autowired
    private  PeriodeTestDao periodeTestDao;

    @Value("${upload.prediksiTest}")
    private String uploadPrediksiTest;

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @GetMapping("/kuesioner")
    public String listKuesioner(Model model, @PageableDefault Pageable pageable){
        model.addAttribute("list", kuesionerDao.findByStatus(StatusRecord.AKTIF, pageable));
        return "kuesioner/list";
    }

    @GetMapping("/kuesioner/form")
    public String formKuesioner(Model model, @RequestParam Kuesioner kuesioner){
        model.addAttribute("kuesioner", kuesioner);
        model.addAttribute("listPertanyaan", kuesionerPertanyaanDao.findByStatusAndKuesionerOrderByUrutanAsc(StatusRecord.AKTIF, kuesioner));
        model.addAttribute("urutan", kuesionerPertanyaanDao.countByStatusAndKuesioner(StatusRecord.AKTIF, kuesioner));
        return "kuesioner/form";
    }

    @GetMapping("/kuesioner/deskripsi")
    public String formDeskripsi(Model model, @RequestParam(required = false) Kuesioner id){
        if (id == null){
            model.addAttribute("kuesioner", new Kuesioner());
        }else {
            model.addAttribute("kuesioner", id);
        }
        return "kuesioner/deskripsi";
    }

    @PostMapping("/kuesioner/deskripsi/save")
    public String saveDeskripsi(@Valid Kuesioner kuesioner, Authentication authentication,
                                RedirectAttributes attributes){
        try{
            User user = currentUserService.currentUser(authentication);
            kuesioner.setCreatedBy(user.getUsername());
            kuesioner.setCreatedDate(LocalDateTime.now());
            kuesionerDao.save(kuesioner);
            attributes.addFlashAttribute("success", "Berhasil Disimpan");
            return "redirect:/kuesioner";
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner";
        }
    }

    @GetMapping("/kuesioner/deskripsi/delete")
    public String deleteDeskripsi(@RequestParam Kuesioner kuesioner, RedirectAttributes attributes){
        try{
            kuesioner.setStatus(StatusRecord.HAPUS);
            kuesionerDao.save(kuesioner);
            attributes.addFlashAttribute("success", "Berhasil Dihapus");
            return "redirect:/kuesioner";
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner";
        }
    }

    @PostMapping("/kuesioner/pertanyaan/save")
    public String savePertanyaan(@Valid KuesionerDto kuesionerDto, RedirectAttributes attributes){
        try{
            KuesionerPertanyaan kuesionerPertanyaan = new KuesionerPertanyaan();
            kuesionerPertanyaan.setKuesioner(kuesionerDto.getKuesioner());
            kuesionerPertanyaan.setPertanyaan(kuesionerDto.getPertanyaan());
            kuesionerPertanyaan.setJenisPertanyaan(kuesionerDto.getJenisPertanyaan());
            kuesionerPertanyaan.setWajibIsi(kuesionerDto.getWajibIsi());
            kuesionerPertanyaan.setCreatedDate(LocalDateTime.now());
            kuesionerPertanyaan.setUrutan(kuesionerDto.getUrutan());
            if (kuesionerDto.getJenisPertanyaan().equals("pilihanGanda")){
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
                ArrayList<String> pilihanGanda = kuesionerDto.getPilihanGanda();
                int row = 1;
                for (String data : pilihanGanda){
                    KuesionerPilihanGanda kuesionerPilihanGanda = new KuesionerPilihanGanda();
                    kuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                    kuesionerPilihanGanda.setUrutan(row++);
                    kuesionerPilihanGanda.setOpsi(data);
                    kuesionerPilihanGandaDao.save(kuesionerPilihanGanda);
                }
            }else {
                kuesionerPertanyaan.setSkalaLinearAwal(kuesionerDto.getSkalaLinearAwal());
                kuesionerPertanyaan.setSkalaLinearAkhir(kuesionerDto.getSkalaLinearAkhir());
                kuesionerPertanyaan.setOpsiSkalaLinearAwal(kuesionerDto.getOpsiSkalaLinearAwal());
                kuesionerPertanyaan.setOpsiSkalaLinearAkhir(kuesionerDto.getOpsiSkalaLinearAkhir());
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
            }
            attributes.addFlashAttribute("success", "Berhasil disimpan");
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }
    }

    @PostMapping("/kuesioner/pertanyaan/edit")
    public String editPertanyaan(@Valid KuesionerDto kuesionerDto, RedirectAttributes attributes){
        try{
            KuesionerPertanyaan kuesionerPertanyaan = new KuesionerPertanyaan();
            kuesionerPertanyaan.setId(kuesionerDto.getId());
            kuesionerPertanyaan.setKuesioner(kuesionerDto.getKuesioner());
            kuesionerPertanyaan.setPertanyaan(kuesionerDto.getPertanyaan());
            kuesionerPertanyaan.setJenisPertanyaan(kuesionerDto.getJenisPertanyaan());
            kuesionerPertanyaan.setWajibIsi(kuesionerDto.getWajibIsi());
            kuesionerPertanyaan.setCreatedDate(LocalDateTime.now());
            kuesionerPertanyaan.setUrutan(kuesionerDto.getUrutan());
            if (kuesionerDto.getJenisPertanyaan().equals("pilihanGanda")){
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
                ArrayList<String> pilihanGanda = kuesionerDto.getPilihanGanda();
                ArrayList<String> idPilihanGanda = kuesionerDto.getIdPilihanGanda();
                int row = 1;

                for (int i = 0; i < pilihanGanda.size(); i++) {
                    String dataPilihanGanda = pilihanGanda.get(i);
                    String dataId = i < idPilihanGanda.size() ? idPilihanGanda.get(i) : null;

                    if (dataId == null || dataId.isEmpty()) {
                        KuesionerPilihanGanda newKuesionerPilihanGanda = new KuesionerPilihanGanda();
                        newKuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                        newKuesionerPilihanGanda.setUrutan(row++);
                        newKuesionerPilihanGanda.setOpsi(dataPilihanGanda);
                        kuesionerPilihanGandaDao.save(newKuesionerPilihanGanda);
                    } else {
                        KuesionerPilihanGanda kuesionerPilihanGanda = kuesionerPilihanGandaDao.findById(dataId).orElse(new KuesionerPilihanGanda());
                        kuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                        kuesionerPilihanGanda.setUrutan(row++);
                        kuesionerPilihanGanda.setOpsi(dataPilihanGanda);
                        kuesionerPilihanGandaDao.save(kuesionerPilihanGanda);
                    }
                }
            } else {
                kuesionerPertanyaan.setSkalaLinearAwal(kuesionerDto.getSkalaLinearAwal());
                kuesionerPertanyaan.setSkalaLinearAkhir(kuesionerDto.getSkalaLinearAkhir());
                kuesionerPertanyaan.setOpsiSkalaLinearAwal(kuesionerDto.getOpsiSkalaLinearAwal());
                kuesionerPertanyaan.setOpsiSkalaLinearAkhir(kuesionerDto.getOpsiSkalaLinearAkhir());
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
            }
            attributes.addFlashAttribute("success", "Berhasil diedit");
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }
    }

    @GetMapping("/kuesioner/pertanyaan/delete")
    public String deletePertanyaan(@RequestParam KuesionerPertanyaan id, RedirectAttributes attributes){
        try{
            id.setStatus(StatusRecord.HAPUS);
            kuesionerPertanyaanDao.save(id);
            attributes.addFlashAttribute("success", "Berhasil Dihapus");
            return "redirect:/kuesioner/form?kuesioner=" + id.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + id.getKuesioner().getId();
        }
    }

    @GetMapping("/kuesioner/pertanyaan/edit")
    public String editPertanyaan(Model model, @RequestParam KuesionerPertanyaan id){
        KuesionerDto kuesionerDto = new KuesionerDto();
        BeanUtils.copyProperties(id, kuesionerDto);
        model.addAttribute("kuesionerPertanyaan", kuesionerDto);
        return "kuesioner/edit";
    }

    @ResponseBody
    @GetMapping("/list-pilihanganda")
    public List<KuesionerPilihanGanda> listPilihanGanda(@RequestParam KuesionerPertanyaan id){
        List<KuesionerPilihanGanda> kuesionerPilihanGandas = kuesionerPilihanGandaDao
                .findByStatusAndKuesionerPertanyaanOrderByUrutanAsc(StatusRecord.AKTIF, id);
        return kuesionerPilihanGandas;
    }

    @ResponseBody
    @GetMapping("/delete/pilihanGanda")
    public KuesionerPilihanGanda deletePilihanGanda(@RequestParam KuesionerPilihanGanda id){
        id.setStatus(StatusRecord.HAPUS);
        kuesionerPilihanGandaDao.save(id);

        return id;
    }

    @GetMapping("/kuesioner/toefl/view")
    public String viewKuesionerToefl(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa, StatusRecord.TOEFL, true);
        Integer totalTest = prediksiTestDao.countTotalUjian(mahasiswa);
        if (totalTest < 1){
            return "kuesioner/toefl/view";
        }else {
            PrediksiTest validate = prediksiTestDao.findByStatusNotInAndStatusUjianInAndMahasiswaAndJenisTest(Arrays.asList(StatusRecord.HAPUS), Arrays.asList(StatusUjian.WAITING,StatusUjian.LULUS,StatusUjian.DINILAI), mahasiswa, JenisTest.TOEFL);
            if (validate != null) {
                return "redirect:list";
            }else {
                if (enableFiture != null){
                    return "kuesioner/toefl/view";
                }else {
                    return "redirect:list";
                }
            }
        }
    }

    @GetMapping("/kuesioner/toefl/list")
    public String listKuesionerToefl(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        List<PrediksiTest> prediksiTest = prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS));

        model.addAttribute("mahasiswa", mahasiswa);
        PrediksiTest validate = prediksiTestDao.findByStatusNotInAndStatusUjianInAndMahasiswaAndJenisTest(Arrays.asList(StatusRecord.HAPUS), Arrays.asList(StatusUjian.WAITING,StatusUjian.LULUS, StatusUjian.DINILAI), mahasiswa, JenisTest.TOEFL);
        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa, StatusRecord.TOEFL, true);

        if (enableFiture != null){
            model.addAttribute("enableFitur", enableFiture);
        }

        if (validate != null) {
            model.addAttribute("validate", validate);
        }
        model.addAttribute("listPrediksi", prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS)));

        if (prediksiTest == null){
            return "redirect:view";
        }else {
            return "kuesioner/toefl/list";
        }
    }

    @GetMapping("/kuesioner/ielts/list")
    public String listKuesionerIelts(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        List<PrediksiTest> prediksiTest = prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS));

        model.addAttribute("mahasiswa", mahasiswa);
        PrediksiTest validate = prediksiTestDao.findByStatusNotInAndStatusUjianInAndMahasiswaAndJenisTest(Arrays.asList(StatusRecord.HAPUS), Arrays.asList(StatusUjian.WAITING,StatusUjian.LULUS, StatusUjian.DINILAI), mahasiswa, JenisTest.IELTS);
        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa, StatusRecord.IELTS, true);

        if (enableFiture != null){
            model.addAttribute("enableFitur", enableFiture);
        }

        if (validate != null) {
            model.addAttribute("validate", validate);
        }
        model.addAttribute("listPrediksi", prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS)));

        if (prediksiTest == null){
            return "redirect:view";
        }else {
            return "kuesioner/toefl/list";
        }
    }

    @GetMapping("/kuesioner/toefl/admin/list")
    public void listAdminToefl(Model model, @RequestParam(required = false) String search, @RequestParam(required = false) String status,
                               Authentication authentication, Pageable pageable){
        model.addAttribute("status", status);
        model.addAttribute("listPeriode", periodeTestDao.findAllByJenisTestOrderByTanggalUjianDesc(JenisTest.TOEFL));
        model.addAttribute("periode", periodeTestDao.findByStatusAndJenisTest(StatusRecord.AKTIF, JenisTest.TOEFL));
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            if (status.equals("WAITING")){
                model.addAttribute("listPrediksi", prediksiTestDao.findByNamaOrNimAndStatusUjian(search,status,JenisTest.TOEFL,pageable));
            }else {
                model.addAttribute("listPrediksi", prediksiTestDao.findByNamaOrNimAndStatusUjianNotIn(search,status, String.valueOf(JenisTest.TOEFL),pageable));
            }
        }else {
            if (status.equals("WAITING")){
                model.addAttribute("listPrediksi", prediksiTestDao.findByStatusNotInAndStatusUjianAndJenisTest(Arrays.asList(StatusRecord.HAPUS),StatusUjian.valueOf(status),JenisTest.TOEFL,pageable));
            }else {
                model.addAttribute("listPrediksi", prediksiTestDao.findByDistincMahasiswa(String.valueOf(JenisTest.TOEFL),pageable));
            }
        }
    }

    @GetMapping("/kuesioner/ielts/admin/list")
    public void listAdminIelts(Model model, @RequestParam(required = false) String search, @RequestParam(required = false) String status,
                               Authentication authentication, Pageable pageable){
        model.addAttribute("status", status);
        model.addAttribute("listPeriode", periodeTestDao.findAllByJenisTestOrderByTanggalUjianDesc(JenisTest.IELTS));
        model.addAttribute("periode", periodeTestDao.findByStatusAndJenisTest(StatusRecord.AKTIF, JenisTest.IELTS));
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            if (status.equals("WAITING")){
                model.addAttribute("listPrediksi", prediksiTestDao.findByNamaOrNimAndStatusUjian(search,status,JenisTest.IELTS,pageable));
            }else {
                model.addAttribute("listPrediksi", prediksiTestDao.findByNamaOrNimAndStatusUjianNotIn(search,status, String.valueOf(JenisTest.IELTS),pageable));
            }
        }else {
            if (status.equals("WAITING")){
                model.addAttribute("listPrediksi", prediksiTestDao.findByStatusNotInAndStatusUjianAndJenisTest(Arrays.asList(StatusRecord.HAPUS),StatusUjian.valueOf(status),JenisTest.IELTS,pageable));
            }else {
                model.addAttribute("listPrediksi", prediksiTestDao.findByDistincMahasiswa(String.valueOf(JenisTest.IELTS),pageable));
            }
        }
    }

    @GetMapping("/kuesioner/toefl/download")
    public void downloadRekap(@RequestParam PeriodeTest periode, HttpServletResponse response) throws IOException {
        String[] columns = {"NO", "Timestamps", "NIM", "Full Name", "Place, Date of Birth", "Department"};

        List<PrediksiTest> listMhs = prediksiTestDao.findByPeriodeTestAndStatusAndStatusUjianOrderByMahasiswaNim(periode, StatusRecord.AKTIF, StatusUjian.LULUS);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Prediksi Test Angkatan " + periode.getTanggalUjian());
        sheet.addMergedRegion(CellRangeAddress.valueOf("A1:F1"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A2:F2"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A3:F3"));

        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setUnderline(HSSFFont.U_SINGLE);
        titleFont.setFontHeightInPoints((short) 12);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);

        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        int rowTitle1 = 0;
        Row titleRow = sheet.createRow(rowTitle1);
        titleRow.createCell(0).setCellValue("TOEFL PREDICTION TEST");
        titleRow.getCell(0).setCellStyle(titleStyle);

        int rowTitle2 = 1;
        Row titleRow2 = sheet.createRow(rowTitle2);
        titleRow2.createCell(0).setCellValue("TAZKIA LANGUAGE CENTER");
        titleRow2.getCell(0).setCellStyle(titleStyle);

        int rowTitle3 = 2;
        Row titleRow3 = sheet.createRow(rowTitle3);
        titleRow3.createCell(0).setCellValue("Sentul, " + periode.getTanggalUjian().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        titleRow3.getCell(0).setCellStyle(titleStyle);

        Row headerRow = sheet.createRow(4);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 5;
        int baris = 1;

        for (PrediksiTest list : listMhs){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list.getTanggalUpload().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:ss")));
            row.createCell(2).setCellValue(list.getMahasiswa().getNim());
            row.createCell(3).setCellValue(list.getMahasiswa().getNama());
            row.createCell(4).setCellValue(list.getMahasiswa().getTempatLahir() + ", " + list.getMahasiswa().getTanggalLahir().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
            row.createCell(5).setCellValue(list.getMahasiswa().getIdProdi().getKodeProdi());

            row.getCell(0).setCellStyle(dataStyle);
            row.getCell(1).setCellStyle(dataStyle);
            row.getCell(2).setCellStyle(dataStyle);
            row.getCell(3).setCellStyle(dataStyle);
            row.getCell(4).setCellStyle(dataStyle);
            row.getCell(5).setCellStyle(dataStyle);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Data Rekap Prediksi Test " + String.valueOf(periode.getJenisTest()) + " periode " + periode.getTanggalUjian() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @PostMapping("/kuesioner/toefl/admin/approve")
    public String approveTest(@RequestParam String id){
        PrediksiTest prediksiTest = prediksiTestDao.findById(id).get();
        prediksiTest.setStatus(StatusRecord.AKTIF);
        prediksiTestDao.save(prediksiTest);
        return "redirect:list?status=WAITING";
    }

    @PostMapping("kuesioner/ielts/admin/approve")
    public String approveTestIelts(@RequestParam String id){
        PrediksiTest prediksiTest = prediksiTestDao.findById(id).get();
        prediksiTest.setStatus(StatusRecord.AKTIF);
        prediksiTestDao.save(prediksiTest);
        return "redirect:list?status=WAITING";
    }

    @PostMapping("/kuesioner/toefl/admin/reject")
    public String RejectTest(@Valid TestDto testDto){
        PrediksiTest prediksiTest = prediksiTestDao.findById(testDto.getId()).get();
        prediksiTest.setStatus(StatusRecord.AKTIF);
        prediksiTest.setStatusUjian(StatusUjian.REJECTED);
        prediksiTest.setUjianKe(2);
        prediksiTestDao.save(prediksiTest);
        tagihanService.createTagihanTestInggris(prediksiTest.getMahasiswa(),tahunAkademikDao.findByStatus(StatusRecord.AKTIF), LocalDate.now().plusYears(1) , "toefl");
        return "redirect:list?status=WAITING";
    }

    @PostMapping("/kuesioner/toefl/submit")
    public String submitToefl(@Valid TestDto testDto, Authentication authentication) throws  Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        mahasiswa.setTeleponSeluler(testDto.getNo());
        mahasiswa.setEmailTazkia(testDto.getEmail());
        mahasiswaDao.save(mahasiswa);

        Integer ujianKe = 0;

        PeriodeTest periodeTest = periodeTestDao.findByStatusAndJenisTest(StatusRecord.AKTIF,JenisTest.TOEFL);
        Integer totalKuota = 0;

        if (prediksiTestDao.countAllTest(String.valueOf(JenisTest.IELTS)) != null){
            totalKuota = prediksiTestDao.countAllTest(String.valueOf(JenisTest.TOEFL));
        }

        if (totalKuota > periodeTest.getKuota()){
            return "redirect:../info";

        }else {

            PrediksiTest prediksiTest = new PrediksiTest();
            if (prediksiTestDao.countTotalUjian(mahasiswa) > 0) {
                ujianKe = prediksiTestDao.findMaxUjian(mahasiswa) + 1;
                prediksiTest.setStatus(StatusRecord.AKTIF);
            } else {
                ujianKe = 1;
            }

            prediksiTest.setPeriodeTest(periodeTest);
            prediksiTest.setMahasiswa(mahasiswa);
            prediksiTest.setJenisTest(JenisTest.TOEFL);
            prediksiTest.setUjianKe(ujianKe);

            if (Integer.valueOf(mahasiswa.getAngkatan()) >= 2020) {
                prediksiTest.setStatus(StatusRecord.AKTIF);
            }

            prediksiTestDao.save(prediksiTest);
            return "redirect:list";
        }
    }

    @PostMapping("/kuesioner/ielts/submit")
    public String submitIelts(@Valid TestDto testDto, Authentication authentication) throws  Exception{
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        mahasiswa.setTeleponSeluler(testDto.getNo());
        mahasiswa.setEmailTazkia(testDto.getEmail());
        mahasiswaDao.save(mahasiswa);

        Integer ujianKe = 0;
        PeriodeTest periodeTest = periodeTestDao.findByStatusAndJenisTest(StatusRecord.AKTIF,JenisTest.IELTS);
        Integer totalKuota = 0;

        if (prediksiTestDao.countAllTest(String.valueOf(JenisTest.IELTS)) != null){
            totalKuota = prediksiTestDao.countAllTest(String.valueOf(JenisTest.IELTS));
        }

        if (totalKuota > periodeTest.getKuota()){
            return "redirect:../info";
        }else {
            PrediksiTest prediksiTest = new PrediksiTest();
            if (prediksiTestDao.countTotalUjian(mahasiswa) > 0){
                ujianKe = prediksiTestDao.findMaxUjian(mahasiswa) + 1;
                prediksiTest.setStatus(StatusRecord.AKTIF);
            }else {
                ujianKe = 1;
            }

            prediksiTest.setPeriodeTest(periodeTest);
            prediksiTest.setMahasiswa(mahasiswa);
            prediksiTest.setJenisTest(JenisTest.IELTS);
            prediksiTest.setUjianKe(ujianKe);

            if (Integer.valueOf(mahasiswa.getAngkatan()) >= 2020){
                prediksiTest.setStatus(StatusRecord.AKTIF);
            }

            prediksiTestDao.save(prediksiTest);
            return "redirect:list";
        }

    }

    @GetMapping("/kuesioner/info")
    public void info(){}

    @PostMapping("/kuesioner/toefl/admin/nilai")
    public String penilaianToefl(@Valid TestDto testDto){
        PrediksiTest prediksiTest = prediksiTestDao.findById(testDto.getId()).get();
        prediksiTest.setNilaiReading(konversiNilaiToeflDao.findById(testDto.getNilaiReading()).get().getReading());
        prediksiTest.setNilaiListening(konversiNilaiToeflDao.findById(testDto.getNilaiListening()).get().getListening());
        prediksiTest.setNilaiStructure(konversiNilaiToeflDao.findById(testDto.getNilaiStructure()).get().getStructure());
        Integer nilaiAkhir = (prediksiTest.getNilaiReading() +  prediksiTest.getNilaiListening() + prediksiTest.getNilaiStructure()) / 3;
        prediksiTest.setNilai(nilaiAkhir * 10);
        if (prediksiTest.getNilai() >= 500) {
            prediksiTest.setStatusUjian(StatusUjian.LULUS);

        }else{
            prediksiTest.setStatusUjian(StatusUjian.TIDAK_LULUS);
        }
        prediksiTestDao.save(prediksiTest);

        return "redirect:list?status=WAITING";
    }

    @PostMapping("/kuesioner/ielts/admin/nilai")
    public String penilaianIelts(@Valid TestDto testDto){
        PrediksiTest prediksiTest = prediksiTestDao.findById(testDto.getId()).get();
        prediksiTest.setNilaiReading(testDto.getNilaiReading());
        prediksiTest.setNilaiListening(testDto.getNilaiListening());
        prediksiTest.setNilaiWriting(testDto.getNilaiWriting());
        prediksiTest.setNilaiSpeaking(testDto.getNilaiSpeaking());

        prediksiTest.setNilai(testDto.getNilai());
        if (prediksiTest.getNilai() >= 500) {
            prediksiTest.setStatusUjian(StatusUjian.LULUS);

        }else{
            prediksiTest.setStatusUjian(StatusUjian.TIDAK_LULUS);
        }
        prediksiTestDao.save(prediksiTest);

        return "redirect:list?status=WAITING";
    }

    @GetMapping("/get-detail-prediksi/{id}")
    @ResponseBody
    public PrediksiTest detailTahfidzAdmin(@PathVariable String id) {
        PrediksiTest prediksiTest = prediksiTestDao.findById(id).get();
        return prediksiTest;
    }

    @GetMapping("/get-list-prediksi/{id}")
    @ResponseBody
    public List<PrediksiTest> listPrediksi(@PathVariable String id) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(id).get();
        return prediksiTestDao.findByMahasiswaAndStatusNotInOrderByUjianKeAsc(mahasiswa, Arrays.asList(StatusRecord.HAPUS));
    }

    @GetMapping("/kuesioner/ielts/view")
    public String viewKuesionerIelts(Model model, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa", mahasiswa);

        EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnable(mahasiswa,
                StatusRecord.IELTS, true);

        Integer totalTest = prediksiTestDao.countTotalUjian(mahasiswa);
        if (totalTest < 1) {
            return "kuesioner/ielts/view";
        } else {
            PrediksiTest validate = prediksiTestDao.findByStatusNotInAndStatusUjianInAndMahasiswaAndJenisTest(
                    Arrays.asList(StatusRecord.HAPUS),
                    Arrays.asList(StatusUjian.WAITING, StatusUjian.LULUS, StatusUjian.DINILAI), mahasiswa, JenisTest.IELTS);
            if (validate != null) {
                return "redirect:list";
            } else {
                if (enableFiture != null) {
                    return "kuesioner/ielts /view";
                } else {
                    return "redirect:list";
                }
            }
        }
    }

    @GetMapping("/kuesioner/periodeUjian/list")
    public void listPeriode(Model model, @PageableDefault(size = 10) Pageable page, String search) {

        model.addAttribute("listPeriode", periodeTestDao.findByStatusInOrderByStatusAsc(Arrays.asList(StatusRecord.AKTIF,StatusRecord.NONAKTIF),page));
    }

    @PostMapping("/kuesioner/periodeUjian/submit")
    public String savePeriode(@Valid PeriodeTest periodeTest){
        PeriodeTest periode = periodeTestDao.findByStatusAndJenisTest(StatusRecord.AKTIF,periodeTest.getJenisTest());
        if (periode != null){
            periode.setStatus(StatusRecord.NONAKTIF);
            periodeTestDao.save(periode);
        }
        periodeTestDao.save(periodeTest);

        return "redirect:list";
    }

}
