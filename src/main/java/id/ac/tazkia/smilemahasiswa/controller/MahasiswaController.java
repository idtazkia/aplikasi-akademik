package id.ac.tazkia.smilemahasiswa.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import id.ac.tazkia.smilemahasiswa.service.KrsService;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.smilemahasiswa.dao.AgamaDao;
import id.ac.tazkia.smilemahasiswa.dao.AyahDao;
import id.ac.tazkia.smilemahasiswa.dao.BeasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.CutiDao;
import id.ac.tazkia.smilemahasiswa.dao.IbuDao;
import id.ac.tazkia.smilemahasiswa.dao.JenisTinggalDao;
import id.ac.tazkia.smilemahasiswa.dao.KodeposDao;
import id.ac.tazkia.smilemahasiswa.dao.KonsentrasiDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaBeasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDetailKeluargaDao;
import id.ac.tazkia.smilemahasiswa.dao.PekerjaanDao;
import id.ac.tazkia.smilemahasiswa.dao.PendidikanDao;
import id.ac.tazkia.smilemahasiswa.dao.PenghasilanDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.ProgramDao;
import id.ac.tazkia.smilemahasiswa.dao.RoleDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.UserDao;
import id.ac.tazkia.smilemahasiswa.dao.WaliDao;
import id.ac.tazkia.smilemahasiswa.dto.mahasiswa.MengundurkanDiriDto;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.Ayah;
import id.ac.tazkia.smilemahasiswa.entity.Beasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Cuti;
import id.ac.tazkia.smilemahasiswa.entity.Ibu;
import id.ac.tazkia.smilemahasiswa.entity.Krs;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaBeasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Pekerjaan;
import id.ac.tazkia.smilemahasiswa.entity.Pendidikan;
import id.ac.tazkia.smilemahasiswa.entity.Penghasilan;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Role;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaService;

@Controller
public class MahasiswaController {

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KodeposDao kodeposDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private AgamaDao agamaDao;

    @Autowired
    private JenisTinggalDao jenisTinggalDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private MahasiswaService mahasiswaService;

    @Autowired
    private KrsService krsService;

    @Autowired
    private AyahDao ayahDao;

    @Autowired
    private MahasiswaDetailKeluargaDao mahasiswaDetailKeluargaDao;

    @Autowired
    private IbuDao ibuDao;

    @Autowired
    private WaliDao waliDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PendidikanDao pendidikanDao;

    @Autowired
    private PekerjaanDao pekerjaanDao;

    @Autowired
    private PenghasilanDao penghasilanDao;

    @Autowired
    private MahasiswaBeasiswaDao mahasiswaBeasiswaDao;

    @Autowired
    private BeasiswaDao beasiswaDao;

    @Autowired
    private CutiDao cutiDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private KrsDao krsDao;

    @Value("classpath:sample/contohEmail.xlsx")
    private Resource exampleEmail;

    @ModelAttribute("penghasilan")
    public Iterable<Penghasilan> penghasilan() {
        return penghasilanDao.findAll();
    }

    @ModelAttribute("pekerjaan")
    public Iterable<Pekerjaan> pekerjaan() {
        return pekerjaanDao.findAll();
    }

    @ModelAttribute("pendidikan")
    public Iterable<Pendidikan> pendidikan() {
        return pendidikanDao.findAll();
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @GetMapping("/mahasiswa/list")
    public void daftarMahasiswa(Model model, @PageableDefault(size = 50) Pageable page, String search,
            Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("search", search);
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", mahasiswaDao.findByStatusNotInAndNamaContainingIgnoreCaseOrNimOrderByNim(
                    Arrays.asList(StatusRecord.HAPUS), search, search, page));
        }
    }

    @GetMapping("/sample/email")
    public void downloadContohFileInputEmail(HttpServletResponse response) throws IOException {

        response.setContentType("application/vns.ms-excel");
        response.setHeader("Content-Disposition", "attahment; filename=Example-Email.xlsx");
        FileCopyUtils.copy(exampleEmail.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();

    }

    @Transactional
    @PostMapping("/mahasiswa/email/upload")
    public String uploadEmailFile(MultipartFile file, RedirectAttributes attributes) throws Exception {

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheetPertama = workbook.getSheetAt(0);

            int row = 5; // baris ke 6
            int terakhir = sheetPertama.getLastRowNum() - row;

            for (int i = 0; i <= terakhir; i++) {
                Row baris = sheetPertama.getRow(row + i);
                Cell nimCell = baris.getCell(0);
                nimCell.setCellType(CellType.STRING);
                Cell emailCell = baris.getCell(1);
                emailCell.setCellType(CellType.STRING);
                Mahasiswa mahasiswa = mahasiswaDao.findByNim(nimCell.getStringCellValue());
                if (mahasiswa != null) {
                    mahasiswa.setEmailTazkia(emailCell.getStringCellValue());
                    mahasiswaDao.save(mahasiswa);
                    User user = mahasiswa.getUser();
                    user.setUsername(emailCell.getStringCellValue());
                    userDao.save(user);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "redirect:../list";
    }

    @GetMapping("/mahasiswa/import/angkatan")
    public void importAngkatan(@RequestParam String angkatan, HttpServletResponse response) throws IOException {

        String[] columns = { "NO", "NIM", "Nama", "Prodi", "Email" };

        List<Mahasiswa> mhs = mahasiswaDao.findByStatusAndStatusAktifAndAngkatan(StatusRecord.AKTIF, "AKTIF", angkatan);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Angkatan " + angkatan);

        sheet.addMergedRegion(CellRangeAddress.valueOf("C2:D2"));

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Font ketFont = workbook.createFont();
        ketFont.setBold(true);
        ketFont.setFontHeightInPoints((short) 14);
        ketFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle ketCellStyle = workbook.createCellStyle();
        ketCellStyle.setFont(ketFont);
        ketCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(3);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowKet = 1;
        Row keterangan = sheet.createRow(rowKet);
        keterangan.createCell(2).setCellValue("Data Mahasiswa Angkatan " + angkatan);
        keterangan.getCell(2).setCellStyle(ketCellStyle);

        int rowNum = 4;
        int baris = 1;

        for (Mahasiswa m : mhs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(m.getNim());
            row.createCell(2).setCellValue(m.getNama());
            row.createCell(3).setCellValue(m.getIdProdi().getNamaProdi());
            if (m.getEmailTazkia() != null) {
                row.createCell(4).setCellValue(m.getEmailTazkia());
            } else {
                row.createCell(4).setCellValue("-");
            }
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vns.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Data Mahasiswa Angkatan " + angkatan + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/mahasiswa/rfid")
    public void rfid(@RequestParam(required = false) String nim, @RequestParam(required = false) String rfid,
            Model model) {
        model.addAttribute("nim", nim);
        model.addAttribute("rfid", rfid);

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa != null) {
            model.addAttribute("mahasiswa", mahasiswa);
        }

    }

    @PostMapping("/mahasiswa/rfid")
    public String prosesRfid(@RequestParam String nim, @RequestParam String rfid) {

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        mahasiswa.setRfid(rfid);
        mahasiswaDao.save(mahasiswa);

        return "redirect:rfid?nim=" + nim + "&rfid=" + rfid;
    }

    @GetMapping("/mahasiswa/form")
    public void formMahasiswa(Model model, Mahasiswa mahasiswa) {
        model.addAttribute("agama", agamaDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tinggal", jenisTinggalDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("program", programDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("konsentrasi", konsentrasiDao.findByStatus(StatusRecord.AKTIF));

        model.addAttribute("mhsw", mahasiswa);
        model.addAttribute("ayah", ayahDao.findById(mahasiswa.getAyah().getId()));
        model.addAttribute("ibu", ibuDao.findById(mahasiswa.getIbu().getId()));
        // model.addAttribute("wali", waliDao.findById(mahasiswa.getId()));

        MahasiswaDto mahasiswaDto = new MahasiswaDto();

        mahasiswaDto.setId(mahasiswa.getId());
        mahasiswaDto.setAngkatan(mahasiswa.getAngkatan());
        mahasiswaDto.setIdProdi(mahasiswa.getIdProdi());
        mahasiswaDto.setIdKonsentrasi(mahasiswa.getIdKonsentrasi());
        mahasiswaDto.setNim(mahasiswa.getNim());
        mahasiswaDto.setNama(mahasiswa.getNama());
        mahasiswaDto.setStatusMatrikulasi(mahasiswa.getStatusMatrikulasi());
        mahasiswaDto.setUkuranBaju(mahasiswa.getUkuranBaju());
        mahasiswaDto.setKps(mahasiswa.getKps());
        mahasiswaDto.setNomorKps(mahasiswa.getNomorKps());
        mahasiswaDto.setIdProgram(mahasiswa.getIdProgram());
        mahasiswaDto.setJenisKelamin(mahasiswa.getJenisKelamin());
        mahasiswaDto.setReligion(mahasiswa.getIdAgama());
        mahasiswaDto.setTempat(mahasiswa.getTempatLahir());
        mahasiswaDto.setTanggalLahir(mahasiswa.getTanggalLahir());
        mahasiswaDto.setIdKelurahan(mahasiswa.getIdKelurahan());
        mahasiswaDto.setIdKecamatan(mahasiswa.getIdKecamatan());
        mahasiswaDto.setIdKotaKabupaten(mahasiswa.getIdKotaKabupaten());
        mahasiswaDto.setIdProvinsi(mahasiswa.getIdProvinsi());
        mahasiswaDto.setIdNegara(mahasiswa.getIdNegara());
        mahasiswaDto.setKewarganegaraan(mahasiswa.getKewarganegaraan());
        mahasiswaDto.setNik(mahasiswa.getNik());
        mahasiswaDto.setNisn(mahasiswa.getNisn());
        mahasiswaDto.setNamaJalan(mahasiswa.getNamaJalan());
        mahasiswaDto.setRt(mahasiswa.getRt());
        mahasiswaDto.setRw(mahasiswa.getRw());
        mahasiswaDto.setNamaDusun(mahasiswa.getNamaDusun());
        mahasiswaDto.setKodepos(mahasiswa.getKodepos());
        mahasiswaDto.setJenisTinggal(mahasiswa.getJenisTinggal());
        mahasiswaDto.setAlatTransportasi(mahasiswa.getAlatTransportasi());
        mahasiswaDto.setTeleponRumah(mahasiswa.getTeleponRumah());
        mahasiswaDto.setTeleponSeluler(mahasiswa.getTeleponSeluler());
        mahasiswaDto.setEmailPribadi(mahasiswa.getEmailPribadi());
        mahasiswaDto.setEmailTazkia(mahasiswa.getEmailTazkia());
        mahasiswaDto.setStatusAktif(mahasiswa.getStatusAktif());
        mahasiswaDto.setIdUser(mahasiswa.getUser());

        mahasiswaDto.setIbu(mahasiswa.getIbu().getId());
        mahasiswaDto.setNamaIbuKandung(mahasiswa.getIbu().getNamaIbuKandung());
        mahasiswaDto.setKebutuhanKhususIbu(mahasiswa.getIbu().getKebutuhanKhusus());
        mahasiswaDto.setTempatLahirIbu(mahasiswa.getIbu().getTempatLahir());
        mahasiswaDto.setTanggalLahirIbu(mahasiswa.getIbu().getTanggalLahir());
        mahasiswaDto.setIdJenjangPendidikanIbu(mahasiswa.getIbu().getIdJenjangPendidikan());
        mahasiswaDto.setIdPekerjaanIbu(mahasiswa.getIbu().getIdPekerjaan());
        mahasiswaDto.setPenghasilanIbu(mahasiswa.getIbu().getPenghasilan());
        mahasiswaDto.setAgamaIbu(mahasiswa.getIbu().getAgama());
        mahasiswaDto.setStatusHidupIbu(mahasiswa.getIbu().getStatusHidup());

        mahasiswaDto.setAyah(mahasiswa.getAyah().getId());
        mahasiswaDto.setNamaAyah(mahasiswa.getAyah().getNamaAyah());
        mahasiswaDto.setKebutuhanKhusus(mahasiswa.getAyah().getKebutuhanKhusus());
        mahasiswaDto.setTempatLahirAyah(mahasiswa.getAyah().getTempatLahir());
        mahasiswaDto.setTanggalLahirAyah(mahasiswa.getAyah().getTanggalLahir());
        mahasiswaDto.setIdJenjangPendidikan(mahasiswa.getAyah().getIdJenjangPendidikan());
        mahasiswaDto.setIdPekerjaan(mahasiswa.getAyah().getIdPekerjaan());
        mahasiswaDto.setPenghasilan(mahasiswa.getAyah().getPenghasilan());
        mahasiswaDto.setAgama(mahasiswa.getAyah().getAgama());
        mahasiswaDto.setHidup(mahasiswa.getAyah().getStatusHidup());
        model.addAttribute("mahasiswa", mahasiswaDto);
        mahasiswaDto.setRfid(mahasiswa.getRfid());

        model.addAttribute("mahasiswa", mahasiswaDto);
        model.addAttribute("listbeasiswa", beasiswaDao.findByStatusOrderByNamaBeasiswa(StatusRecord.AKTIF));
        model.addAttribute("beasiswa", mahasiswaBeasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF));
    }

    @PostMapping("/mahasiswa/form")
    public String prosesForm(@RequestParam Mahasiswa mahasiswa, @ModelAttribute @Valid MahasiswaDto mahasiswaDto,
            BindingResult errors, Authentication authentication) {
        User userUpdate = currentUserService.currentUser(authentication);

        // Exclude ukuranBaju to prevent null override from DTO
        BeanUtils.copyProperties(mahasiswaDto, mahasiswa, "ukuranBaju");
        mahasiswa.setNama(WordUtils.capitalizeFully(mahasiswaDto.getNama()));
        mahasiswa.setTempatLahir(mahasiswaDto.getTempat());
        mahasiswa.setIdAgama(mahasiswaDto.getReligion());
        mahasiswa.setStatusUpdateUser(userUpdate.getUsername());

        // Only set ukuranBaju if it's not null in DTO
        if (mahasiswaDto.getUkuranBaju() != null) {
            mahasiswa.setUkuranBaju(mahasiswaDto.getUkuranBaju());
        }

        mahasiswaDao.save(mahasiswa);

        if (mahasiswa.getUser() != null) {
            User user = userDao.findById(mahasiswa.getUser().getId()).get();
            Role role = roleDao.findById(user.getRole().getId()).get();
            user.setId(mahasiswaDto.getIdUser().getId());
            user.setUsername(mahasiswa.getEmailTazkia());
            user.setRole(role);
            user.setActive(Boolean.TRUE);
            userDao.save(user);
        }

        if (mahasiswaDto.getIbu() != null) {
            Ibu ibu = ibuDao.findById(mahasiswaDto.getIbu()).get();
            ibu.setId(mahasiswaDto.getIbu());
            ibu.setNamaIbuKandung(mahasiswaDto.getNamaIbuKandung());
            ibu.setKebutuhanKhusus(mahasiswaDto.getKebutuhanKhususIbu());
            ibu.setTempatLahir(mahasiswaDto.getTempatLahirIbu());
            ibu.setTanggalLahir(mahasiswaDto.getTanggalLahirIbu());
            ibu.setIdJenjangPendidikan(mahasiswaDto.getIdJenjangPendidikanIbu());
            ibu.setIdPekerjaan(mahasiswaDto.getIdPekerjaanIbu());
            ibu.setPenghasilan(mahasiswaDto.getPenghasilanIbu());
            ibu.setAgama(mahasiswaDto.getAgamaIbu());
            ibu.setStatusHidup(mahasiswaDto.getStatusHidupIbu());
            ibuDao.save(ibu);
        }
        if (mahasiswaDto.getAyah() != null) {
            Ayah ayah = ayahDao.findById(mahasiswaDto.getAyah()).get();
            BeanUtils.copyProperties(mahasiswaDto, ayah);
            ayah.setId(mahasiswaDto.getAyah());
            ayah.setTanggalLahir(mahasiswaDto.getTanggalLahirAyah());
            ayah.setTempatLahir(mahasiswaDto.getTempatLahirAyah());
            ayah.setStatusHidup(mahasiswaDto.getHidup());
            ayahDao.save(ayah);
        }
        mahasiswaService.prosesAyah(mahasiswaDto, mahasiswa);
        mahasiswaService.prosesIbu(mahasiswaDto, mahasiswa);
        return "redirect:list";
    }

    @GetMapping("/mahasiswa/generate")
    public void generateCapitalize() {

    }

    @PostMapping("/mahasiswa/generate")
    public String generateCapitalize(@RequestParam String angkatan) {
        List<Mahasiswa> mahasiswa = mahasiswaDao.findByStatusAndAngkatan(StatusRecord.AKTIF, angkatan);
        for (Mahasiswa m : mahasiswa) {
            m.setNama(WordUtils.capitalizeFully(m.getNama()));
            mahasiswaDao.save(m);
        }
        return "redirect:list";
    }

    @PostMapping("/mahasiswa/beasiswa")
    public String prosesBeasiswa(Model model, @Valid MahasiswaBeasiswa mahasiswaBeasiswa, Authentication authentication,
            @RequestParam Beasiswa beasiswa, @RequestParam Mahasiswa mahasiswa) {
        List<MahasiswaBeasiswa> cek = mahasiswaBeasiswaDao.findByMahasiswaAndBeasiswaAndStatus(mahasiswa, beasiswa,
                StatusRecord.AKTIF);

        if (cek.isEmpty()) {

            mahasiswa.setBeasiswa(beasiswa);
            mahasiswaDao.save(mahasiswa);

            mahasiswaBeasiswa.setStatus(StatusRecord.AKTIF);
            mahasiswaBeasiswaDao.save(mahasiswaBeasiswa);
        } else {
            model.addAttribute("validasi", "Beasiswa " + beasiswa.getNamaBeasiswa() + " tersebut sudah ada !!");
        }

        return "redirect:/mahasiswa/form?mahasiswa=" + mahasiswa.getId();
    }

    @PostMapping("/mahasiswa/beasiswadelete")
    public String deleteBeasiswa(@RequestParam MahasiswaBeasiswa mahasiswaBeasiswa, Authentication authentication) {
        MahasiswaBeasiswa mahasiswaBeasiswa1 = mahasiswaBeasiswaDao.findById(mahasiswaBeasiswa.getId()).get();
        System.out.println("Cek = " + mahasiswaBeasiswa1);
        mahasiswaBeasiswa.setStatus(StatusRecord.HAPUS);
        mahasiswaBeasiswaDao.save(mahasiswaBeasiswa);

        Mahasiswa m = mahasiswaDao.findByNim(mahasiswaBeasiswa.getMahasiswa().getNim());
        m.setBeasiswa(null);
        mahasiswaDao.save(m);

        return "redirect:/mahasiswa/form?mahasiswa=" + mahasiswaBeasiswa1.getMahasiswa().getId();
    }

    @GetMapping("/mahasiswa/aktif")
    public String statusAktif(@RequestParam Mahasiswa mahasiswa, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        mahasiswa.setStatusUpdateUser(user.getUsername());
        mahasiswa.setStatusAktif("AKTIF");
        mahasiswaDao.save(mahasiswa);
        return "redirect:list?search=" + mahasiswa.getNama();
    }

    @GetMapping("/mahasiswa/nonaktif")
    public String statusNonaktif(@RequestParam Mahasiswa mahasiswa, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        mahasiswa.setStatusUpdateUser(user.getUsername());
        mahasiswa.setStatusAktif("NONAKTIF");
        mahasiswaDao.save(mahasiswa);

        krsService.setStatusKrs(mahasiswa, StatusRecord.NONAKTIF);

        return "redirect:list?search=" + mahasiswa.getNama();
    }

    @PostMapping("/mahasiswa/mengundurkandiri")
    @Transactional
    public String statusMengundurkanDiri(@Valid MengundurkanDiriDto mengundurkanDiriDto,
            Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mengundurkanDiriDto.getMahasiswa();
        mahasiswa.setStatusUpdateUser(user.getUsername());
        mahasiswa.setStatusAktif("MENGUNDURKAN_DIRI");
        mahasiswa.setTanggalMengundurkanDiri(mengundurkanDiriDto.getTanggalMengundurkanDiri());
        mahasiswa.setTahunAkademikMengundurkanDiri(mengundurkanDiriDto.getTahunAkademik());
        mahasiswaDao.save(mahasiswa);

        krsDao.updateStatusKrsMengundurkanDiri(mahasiswa,
                mengundurkanDiriDto.getTahunAkademik().getKodeTahunAkademik());
        return "redirect:list?search=" + mahasiswa.getNama();
    }

    @GetMapping("/mahasiswa/tahun-aktif/{id}")
    @ResponseBody
    List<TahunAkademik> tahunAkademikList(@PathVariable String id) {
        Mahasiswa mahasiswa = mahasiswaDao.findById(id).get();
        List<TahunAkademik> tahunAktif = krsDao
                .findByMahasiswaAndStatusOrderByTahunAkademikKodeTahunAkademik(mahasiswa, StatusRecord.AKTIF)
                .stream()
                .map(Krs::getTahunAkademik)
                .distinct()
                .collect(Collectors.toList());
        return tahunAktif;

    }

    @GetMapping("/mahasiswa/lulus")
    public String statusLulus(@RequestParam Mahasiswa mahasiswa, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        mahasiswa.setStatusUpdateUser(user.getUsername());
        mahasiswa.setStatusAktif("LULUS");
        mahasiswaDao.save(mahasiswa);

        krsService.setStatusKrs(mahasiswa, StatusRecord.LULUS);

        return "redirect:list?search=" + mahasiswa.getNama();
    }

    @PostMapping("/mahasiswa/cuti")
    public String statusCuti(@RequestParam Mahasiswa mahasiswa, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        mahasiswa.setStatusUpdateUser(user.getUsername());
        mahasiswa.setStatusAktif("CUTI");
        mahasiswaDao.save(mahasiswa);

        krsService.setStatusKrs(mahasiswa, StatusRecord.CUTI);

        return "redirect:list?search=" + mahasiswa.getNama();
    }

    @PostMapping("/mahasiswa/cuti-approved")
    public String approvedCuti(@Valid Cuti cuti) {
        cuti.setTanggalPengajuaan(LocalDate.now());
        cuti.setTanggalMulaiCuti(cuti.getTahunAkademik().getTanggalMulaiKuliah());
        cuti.setTanggalBerakhirCuti(cuti.getTahunAkademik().getTanggalSelesaiKuliah());
        cuti.setStatusPengajuaan(StatusApprove.APPROVED);
        cuti.setKpsApproved(StatusApprove.APPROVED_AKADEMIK);
        cuti.setDosenWaliApproved(StatusApprove.APPROVED_AKADEMIK);
        cuti.setDosenWali(cuti.getMahasiswa().getDosen());
        cuti.setKps(cuti.getMahasiswa().getIdProdi().getDosen());
        cutiDao.save(cuti);

        Mahasiswa mahasiswa = mahasiswaDao.findById(cuti.getMahasiswa().getId()).get();
        mahasiswa.setStatusAktif("CUTI");
        mahasiswaDao.save(mahasiswa);

        krsService.setStatusKrs(mahasiswa, StatusRecord.CUTI);
        return "redirect:list?search=" + mahasiswa.getNama();
    }

    @GetMapping("/mahasiswa/keluar")
    public String statusKeluar(@RequestParam Mahasiswa mahasiswa, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        mahasiswa.setStatusUpdateUser(user.getUsername());
        mahasiswa.setStatusAktif("KELUAR");
        mahasiswaDao.save(mahasiswa);

        krsService.setStatusKrs(mahasiswa, StatusRecord.KELUAR);

        return "redirect:list";
    }

    // cek status mahassiswa untuk pddikti
    @GetMapping("/mahasiswa/status")
    public void statusMahasiswa(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) String[] angkatan) {

        if (angkatan != null && tahunAkademik != null) {
            List<String> ang = new ArrayList<>();
            for (String a : angkatan) {
                ang.add(a);
            }
            model.addAttribute("selectAngkatan", ang.toString());
            model.addAttribute("downloadAngkatan", angkatan);
            model.addAttribute("selectTahun", tahunAkademik);
            model.addAttribute("listMhs", mahasiswaDao.listStatusMahasiswa(ang, tahunAkademik.getId()));
        }
    }

    @GetMapping("/download/status/mahasiswa")
    public void downloadStatusMahasiswa(@RequestParam String[] angkatan, @RequestParam TahunAkademik tahun,
            HttpServletResponse response) throws IOException {

        String[] columns = { "No", "Prodi", "Angkatan", "NIM", "Nama", "Jenis Kelamin", "Program", "Beasiswa", "Status Semester",
                "Status Mahasiswa", "Tahun Akademik", "Jumlah SKS", "IPK" };

        List<String> ang = new ArrayList<>();
        for (String a : angkatan) {
            ang.add(a);
        }

        List<Object[]> listTagihan = mahasiswaDao.listStatusMahasiswa(ang, tahun.getId());

        System.out.println("List Angkatan : " + ang);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Status Mahasiswa ");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Font ketFont = workbook.createFont();
        ketFont.setBold(true);
        ketFont.setFontHeightInPoints((short) 14);
        ketFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle ketCellStyle = workbook.createCellStyle();
        ketCellStyle.setFont(ketFont);
        ketCellStyle.setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listTagihan) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[0].toString());
            row.createCell(2).setCellValue(list[1].toString());
            row.createCell(3).setCellValue(list[2].toString());
            row.createCell(4).setCellValue(list[3].toString());
            row.createCell(5).setCellValue(list[4].toString());
            row.createCell(6).setCellValue(list[10].toString());
            row.createCell(7).setCellValue(list[11].toString());
            row.createCell(8).setCellValue(list[5].toString());
            row.createCell(9).setCellValue(list[6].toString());
            row.createCell(10).setCellValue(list[7].toString());
            row.createCell(11).setCellValue(list[8].toString());
            row.createCell(12).setCellValue(list[9].toString());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vns.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment; filename=Data Status Mahasiswa " + tahun.getNamaTahunAkademik() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

}
