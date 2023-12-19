package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaService;
import id.ac.tazkia.smilemahasiswa.service.PresensiService;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.management.ObjectName;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    public void daftarMahasiswa(Model model, @PageableDefault(size = 50) Pageable page, String search,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("search", search);
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", mahasiswaDao.findByStatusNotInAndNamaContainingIgnoreCaseOrNimOrderByNim(Arrays.asList(StatusRecord.HAPUS), search,search, page));
        }
    }

    @GetMapping("/mahasiswa/import/angkatan")
    public void importAngkatan(@RequestParam String angkatan, HttpServletResponse response) throws IOException {

        String[] columns = {"NO", "NIM", "Nama", "Prodi", "Email"};

        List<Mahasiswa> mhs = mahasiswaDao.findByStatusAndStatusAktifAndAngkatan(StatusRecord.AKTIF, "AKTIF", angkatan);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Angkatan "+angkatan);

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
        for (int i = 0; i<columns.length; i++){
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

        for (Mahasiswa m : mhs){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(m.getNim());
            row.createCell(2).setCellValue(m.getNama());
            row.createCell(3).setCellValue(m.getIdProdi().getNamaProdi());
            if (m.getEmailTazkia() != null) {
                row.createCell(4).setCellValue(m.getEmailTazkia());
            }else{
                row.createCell(4).setCellValue("-");
            }
        }

        for (int i = 0; i<columns.length;i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vns.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Data Mahasiswa Angkatan " + angkatan +".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }



    @GetMapping("/mahasiswa/rfid")
    public void rfid(@RequestParam(required = false) String nim, @RequestParam(required = false) String rfid, Model model){
        model.addAttribute("nim", nim);
        model.addAttribute("rfid", rfid);

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa != null){
            model.addAttribute("mahasiswa", mahasiswa);
        }

    }

    @PostMapping("/mahasiswa/rfid")
    public String prosesRfid(@RequestParam String nim,@RequestParam String rfid){

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        mahasiswa.setRfid(rfid);
        mahasiswaDao.save(mahasiswa);

        return "redirect:rfid?nim="+nim+"&rfid="+rfid;
    }

    @GetMapping("/mahasiswa/form")
    public void  formMahasiswa(Model model,Mahasiswa mahasiswa){
        model.addAttribute("agama",agamaDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("tinggal",jenisTinggalDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("program",programDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("prodi",prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("konsentrasi",konsentrasiDao.findByStatus(StatusRecord.AKTIF));

        model.addAttribute("mhsw",mahasiswa);
        model.addAttribute("ayah", ayahDao.findById(mahasiswa.getAyah().getId()));
        model.addAttribute("ibu", ibuDao.findById(mahasiswa.getIbu().getId()));
//        model.addAttribute("wali", waliDao.findById(mahasiswa.getId()));

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
    public String prosesForm(@RequestParam Mahasiswa mahasiswa, @ModelAttribute @Valid MahasiswaDto mahasiswaDto, BindingResult errors){



        BeanUtils.copyProperties(mahasiswaDto,mahasiswa);
        mahasiswa.setNama(WordUtils.capitalizeFully(mahasiswaDto.getNama()));
        mahasiswa.setTempatLahir(mahasiswaDto.getTempat());
        mahasiswa.setIdAgama(mahasiswaDto.getReligion());
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

        if (mahasiswaDto.getIbu() != null){
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
        if (mahasiswaDto.getAyah() != null){
            Ayah ayah = ayahDao.findById(mahasiswaDto.getAyah()).get();
            BeanUtils.copyProperties(mahasiswaDto, ayah);
            ayah.setId(mahasiswaDto.getAyah());
            ayah.setTanggalLahir(mahasiswaDto.getTanggalLahirAyah());
            ayah.setTempatLahir(mahasiswaDto.getTempatLahirAyah());
            ayah.setStatusHidup(mahasiswaDto.getHidup());
            ayahDao.save(ayah);
        }
        mahasiswaService.prosesAyah(mahasiswaDto,mahasiswa);
        mahasiswaService.prosesIbu(mahasiswaDto,mahasiswa);
        return "redirect:list";
    }

    @GetMapping("/mahasiswa/generate")
    public void generateCapitalize(){

    }

    @PostMapping("/mahasiswa/generate")
    public String generateCapitalize(@RequestParam String angkatan){
        List<Mahasiswa> mahasiswa = mahasiswaDao.findByStatusAndAngkatan(StatusRecord.AKTIF,angkatan);
        for (Mahasiswa m : mahasiswa){
            m.setNama(WordUtils.capitalizeFully(m.getNama()));
            mahasiswaDao.save(m);
        }
        return "redirect:list";
    }

    @PostMapping("/mahasiswa/beasiswa")
    public String prosesBeasiswa(Model model,@Valid MahasiswaBeasiswa mahasiswaBeasiswa, Authentication authentication, @RequestParam Beasiswa beasiswa, @RequestParam Mahasiswa mahasiswa){
        List<MahasiswaBeasiswa> cek = mahasiswaBeasiswaDao.findByMahasiswaAndBeasiswaAndStatus(mahasiswa, beasiswa, StatusRecord.AKTIF);

        if (cek.isEmpty()){

            mahasiswa.setBeasiswa(beasiswa);
            mahasiswaDao.save(mahasiswa);

            mahasiswaBeasiswa.setStatus(StatusRecord.AKTIF);
            mahasiswaBeasiswaDao.save(mahasiswaBeasiswa);
        }else {
            model.addAttribute("validasi", "Beasiswa " + beasiswa.getNamaBeasiswa() + " tersebut sudah ada !!");
        }

        return "redirect:/mahasiswa/form?mahasiswa=" + mahasiswa.getId();
    }

    @PostMapping("/mahasiswa/beasiswadelete")
    public String deleteBeasiswa(@RequestParam MahasiswaBeasiswa mahasiswaBeasiswa, Authentication authentication){
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
    public String statusAktif(@RequestParam Mahasiswa mahasiswa){
        mahasiswa.setStatusAktif("AKTIF");
        mahasiswaDao.save(mahasiswa);
        return "redirect:list?search="+ mahasiswa.getNama();
    }

    @GetMapping("/mahasiswa/nonaktif")
    public String statusNonaktif(@RequestParam Mahasiswa mahasiswa){
        mahasiswa.setStatusAktif("NONAKTIF");
        mahasiswaDao.save(mahasiswa);
        return "redirect:list?search="+ mahasiswa.getNama();
    }

    @GetMapping("/mahasiswa/mengundurkandiri")
    public String statusMengundurkanDiri(@RequestParam Mahasiswa mahasiswa){
        mahasiswa.setStatusAktif("MENGUNDURKAN_DIRI");
        mahasiswaDao.save(mahasiswa);
        return "redirect:list?search="+ mahasiswa.getNama();
    }

    @GetMapping("/mahasiswa/lulus")
    public String statusLulus(@RequestParam Mahasiswa mahasiswa){
        mahasiswa.setStatusAktif("LULUS");
        mahasiswaDao.save(mahasiswa);
        return "redirect:list?search="+ mahasiswa.getNama();
    }

    @PostMapping("/mahasiswa/cuti")
    public String statusCuti(@RequestParam Mahasiswa mahasiswa){
        mahasiswa.setStatusAktif("CUTI");
        mahasiswaDao.save(mahasiswa);
        return "redirect:list?search="+ mahasiswa.getNama();
    }


    @PostMapping("/mahasiswa/cuti-approved")
    public String approvedCuti(@Valid Cuti cuti){
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
        return "redirect:list?search="+ mahasiswa.getNama();
    }

    @GetMapping("/mahasiswa/keluar")
    public String statusKeluar(@RequestParam Mahasiswa mahasiswa){
        mahasiswa.setStatusAktif("KELUAR");
        mahasiswaDao.save(mahasiswa);
        return "redirect:list";
    }

    // cek status mahassiswa untuk pddikti
    @GetMapping("/mahasiswa/status")
    public void statusMahasiswa(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) String[] angkatan){

        if (angkatan != null && tahunAkademik != null) {
            List<String> ang = new ArrayList<>();
            for (String a : angkatan){
                ang.add(a);
            }
            model.addAttribute("selectAngkatan", ang.toString());
            model.addAttribute("downloadAngkatan", angkatan);
            model.addAttribute("selectTahun", tahunAkademik);
            model.addAttribute("listMhs", mahasiswaDao.listStatusMahasiswa(ang, tahunAkademik.getId()));
        }
    }

    @GetMapping("/download/status/mahasiswa")
    public void downloadStatusMahasiswa(@RequestParam String[] angkatan, @RequestParam TahunAkademik tahun, HttpServletResponse response) throws IOException {

        String[] columns = {"No", "Prodi", "Angkatan", "NIM", "Nama", "Program", "Beasiswa", "Status Semester", "Status Mahasiswa", "Tahun Akademik", "Jumlah SKS", "IPK"};

        List<String> ang = new ArrayList<>();
        for (String a : angkatan){
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
        for (int i = 0; i<columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listTagihan){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[0].toString());
            row.createCell(2).setCellValue(list[1].toString());
            row.createCell(3).setCellValue(list[2].toString());
            row.createCell(4).setCellValue(list[3].toString());
            row.createCell(5).setCellValue(list[9].toString());
            row.createCell(6).setCellValue(list[10].toString());
            row.createCell(7).setCellValue(list[4].toString());
            row.createCell(8).setCellValue(list[5].toString());
            row.createCell(9).setCellValue(list[6].toString());
            row.createCell(10).setCellValue(list[7].toString());
            row.createCell(11).setCellValue(list[8].toString());
        }

        for (int i = 0; i<columns.length;i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vns.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Data Status Mahasiswa " + tahun.getNamaTahunAkademik() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

}
