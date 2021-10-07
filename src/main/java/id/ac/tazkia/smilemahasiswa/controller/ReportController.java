package id.ac.tazkia.smilemahasiswa.controller;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.graduation.RekapTugasAkhir;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapDetailDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapJadwalDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapSksDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Controller
public class ReportController {
    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private CutiDao cutiDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private EdomMahasiswaDao edomMahasiswaDao;

    @Autowired
    private EdomQuestionDao edomQuestionDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private SoalDao soalDao;

    @Autowired
    private SeminarDao seminarDao;

    @Value("${upload.soal}")
    private String uploadFolder;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }


    @GetMapping("/report/recapitulation/lecturer")
    public void rekapDosen(Model model, @RequestParam(required = false) TahunAkademik ta, @PageableDefault(size = Integer.MAX_VALUE) Pageable page){
        model.addAttribute("selectedTahun", ta);

        Page<RekapJadwalDosenDto> rekap = jadwalDosenDao
                .rekapJadwalDosen(StatusJadwalDosen.PENGAMPU, ta, StatusRecord.AKTIF, page);

        Map<String, RekapSksDosenDto> rekapJumlahSks = new LinkedHashMap<>();
        Map<String, List<RekapJadwalDosenDto>> detailJadwalPerDosen = new LinkedHashMap<>();

        for (RekapJadwalDosenDto r : rekap.getContent()) {

            // hitung total sks
            RekapSksDosenDto rsks = rekapJumlahSks.get(r.getIdDosen());
            if (rsks == null) {
                rsks = new RekapSksDosenDto();
                rsks.setNamaDosen(r.getNamaDosen());
                rsks.setIdDosen(r.getIdDosen());
                rsks.setTotalSks(0);
            }

            rsks.tambahSks(r.getSks());
            rekapJumlahSks.put(r.getIdDosen(), rsks);

            // jadwal per dosen
            List<RekapJadwalDosenDto> rd = detailJadwalPerDosen.get(r.getIdDosen());
            if (rd == null) {
                rd = new ArrayList<>();
                detailJadwalPerDosen.put(r.getIdDosen(), rd);
            }

            rd.add(r);
        }

        model.addAttribute("rekapJumlahSks", rekapJumlahSks);
        model.addAttribute("rekapJadwalDosen", rekap);
        model.addAttribute("rekapJadwalPerDosen", detailJadwalPerDosen);

    }

    @GetMapping("/report/recapitulation/salary")
    public void rekapGajiDosen(Model model, @RequestParam(required = false) Integer tahun,@RequestParam(required = false) Integer bulan, @PageableDefault(size = Integer.MAX_VALUE) Pageable page){

        List<RekapDosenDto> rekap = jadwalDosenDao
                .rekapDosen(tahun,bulan);
        model.addAttribute("selectedTahun", tahun);
        model.addAttribute("selectedBulan", bulan);

        Map<String, RekapDetailDosenDto> rekapJumlahSks = new LinkedHashMap<>();
        Map<String, List<RekapDosenDto>> detailJadwalPerDosen = new LinkedHashMap<>();

        for (RekapDosenDto r : rekap) {

            // hitung total sks
            RekapDetailDosenDto rsks = rekapJumlahSks.get(r.getId());
            if (rsks == null) {
                rsks = new RekapDetailDosenDto();
                rsks.setNamaDosen(r.getNama());
                rsks.setIdDosen(r.getId());
                rsks.setSks1(r.getSks());
                rsks.setJumlah(r.getHadir());

            }

            rsks.tambahSks(r.getSks());
            rekapJumlahSks.put(r.getId(), rsks);

            // jadwal per dosen
            List<RekapDosenDto> rd = detailJadwalPerDosen.get(r.getId());
            if (rd == null) {
                rd = new ArrayList<>();
                detailJadwalPerDosen.put(r.getId(), rd);
            }

            rd.add(r);
        }

        model.addAttribute("rekapJumlahSks", rekapJumlahSks);
        model.addAttribute("rekapJadwalDosen", rekap);
        model.addAttribute("rekapJadwalPerDosen", detailJadwalPerDosen);

    }

    @GetMapping("/report/recapitulation/ipk")
    public void rekapSks(Model model,@RequestParam(required = false) TahunAkademik tahunAkademik,
                         @RequestParam(required = false) String angkatan){

        if (tahunAkademik != null) {
            model.addAttribute("selectedAngkatan", angkatan);
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("ipk", krsDetailDao.cariIpk(tahunAkademik,angkatan, tahunAkademik.getKodeTahunAkademik()));
        }
    }

    @GetMapping("/report/recapitulation/downloadipk")
    public void listPerMatkul(@RequestParam(required = false) TahunAkademik tahunAkademik,
                              @RequestParam(required = false) String angkatan, HttpServletResponse response) throws IOException {

        List<Object[]> listDownload = krsDetailDao.cariIpk(tahunAkademik,angkatan, tahunAkademik.getKodeTahunAkademik());

        String[] columns = {"No", "Nim", "Nama", "Prodi", "Tahun Akademik", "SKS Semester", "SKS Total", "IP Semester", "IPK"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List Ipk Angkatan " + angkatan);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);


        Row headerRow = sheet.createRow(2);

        for (int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 3;
        int baris = 1;

        for (Object[] list : listDownload){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[0].toString());
            row.createCell(2).setCellValue(list[1].toString());
            row.createCell(3).setCellValue(list[2].toString());
            row.createCell(4).setCellValue(tahunAkademik.getNamaTahunAkademik());
            row.createCell(5).setCellValue(list[5].toString());
            row.createCell(6).setCellValue(list[7].toString());
            row.createCell(7).setCellValue(list[6].toString());
            row.createCell(8).setCellValue(list[8].toString());
        }

        for (int i = 0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=List Ipk Tahun Akademik-"+tahunAkademik.getNamaTahunAkademik()+"-"+"Angkatan " + angkatan+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/report/recapitulation/edom")
    public void rekapEdom(Model model,@RequestParam(required = false) TahunAkademik tahunAkademik,
                          @RequestParam(required = false) Prodi prodi){

        if (tahunAkademik != null){
            List<Object[]> headerEdom = edomMahasiswaDao.headerEdomMahasiswa(tahunAkademik, prodi);
            model.addAttribute("headerEdom", headerEdom);
            if (headerEdom != null){
                List<Object[]> detailEdom = edomQuestionDao.detailEdom(tahunAkademik, prodi);
                model.addAttribute("detailEdom", detailEdom);

//                EdomQuestion edomQuestion1 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,1,tahunAkademik);
//                EdomQuestion edomQuestion2 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,2,tahunAkademik);
//                EdomQuestion edomQuestion3 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,3,tahunAkademik);
//                EdomQuestion edomQuestion4 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,4,tahunAkademik);
//                EdomQuestion edomQuestion5 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,5,tahunAkademik);
//                model.addAttribute("edomQuestion1",edomQuestion1);
//                model.addAttribute("edomQuestion2",edomQuestion2);
//                model.addAttribute("edomQuestion3",edomQuestion3);
//                model.addAttribute("edomQuestion4",edomQuestion4);
//                model.addAttribute("edomQuestion5",edomQuestion5);
            }else {
                model.addAttribute("questionNull", "Pertanyaan edom untuk tahun akademik ini belum dibuat");
            }
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("rekapEdom",krsDetailDao.rekapEdom(tahunAkademik,prodi));
        }

    }

    @GetMapping("/report/recapitulation/downloadedom")
    public void downloadEdom(@RequestParam(required = false) TahunAkademik tahunAkademik, HttpServletResponse response) throws IOException {

        List<Object[]> listEdom = edomMahasiswaDao.downloadEdom(tahunAkademik);

        String[] columns = {"No", "NIDN", "Nama Dosen", "Status Dosen", "Nama Prodi", "Email", "Nama Matakuliah", "Semester", "Nama Kelas", "Jumlah Mengisi Edom", "Jumlah Mahasiswa Kelas", "Nilai Edom"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List Edom tahun akademik " + tahunAkademik.getKodeTahunAkademik());

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(2);

        for (int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 3;
        int baris = 1;

        for (Object[] list : listEdom){
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
            row.createCell(9).setCellValue(list[10].toString());
            row.createCell(10).setCellValue(list[9].toString());
            row.createCell(11).setCellValue(list[11].toString());
        }

        for (int i = 0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=List Edom Tahun Akademik-"+tahunAkademik.getNamaTahunAkademik()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/report/recapitulation/bkd")
    public void rekapBkdDosen(Model model, Authentication authentication, @RequestParam(required = false)TahunAkademik tahunAkademik){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        if (tahunAkademik != null){
            model.addAttribute("selectedTahun", tahunAkademik);

            List<ScheduleDto> jadwal = jadwalDao.lecturerAssesment(dosen,StatusRecord.AKTIF, tahunAkademik);
            model.addAttribute("jadwal", jadwal);
        }


    }

    @GetMapping("/report/historymahasiswa")
    public void historyMahasiswa(Model model, @RequestParam(required = false)String nim){
        if (nim != null) {
            model.addAttribute("nim", nim);
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            String URL =  "https://api.whatsapp.com/send?phone=" + mahasiswa.getTeleponSeluler();
            String urlProdi = "https://smile.tazkia.ac.id/mahasiswa/form?mahasiswa=" + mahasiswa.getNim();
            KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);

            model.addAttribute("mhs",mahasiswa);
            model.addAttribute("kelas", kelasMahasiswa);
            model.addAttribute("url", URL);
            model.addAttribute("urlProdi", urlProdi);
            model.addAttribute("history", krsDetailDao.historyMahasiswa(mahasiswa));
            model.addAttribute("ipk", krsDetailDao.ipk(mahasiswa));
            model.addAttribute("sksTotal", krsDetailDao.totalSks(mahasiswa));
            model.addAttribute("semester", krsDetailDao.semesterHistory(mahasiswa));
            model.addAttribute("khsHistory", krsDetailDao.khsHistoty(mahasiswa));
            model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));
            model.addAttribute("semesterTranskript", krsDao.semesterTranskript(mahasiswa.getId()));
            model.addAttribute("transkriptTampil", krsDetailDao.transkriptTampil(mahasiswa.getId()));
        }
    }

    @GetMapping("/report/cuti")
    public void mahasiswaCuti(Model model,@PageableDefault(size = 10)Pageable pageable){
        model.addAttribute("listCutiMahasiswa",cutiDao.findByStatusOrderByStatusPengajuaanDesc(StatusRecord.AKTIF, pageable));
    }

    @PostMapping("/proses/cuti")
    public String prosesCuti(@Valid Cuti cuti, @RequestParam Mahasiswa mahasiswa){
        cuti.setMahasiswa(mahasiswa);
        cuti.setTanggalPengajuaan(LocalDate.now());
        cuti.setStatusPengajuaan("DIAJUKAN");
        cutiDao.save(cuti);

        return "redirect:/report/cuti";
    }

    @GetMapping("/report/recapitulation/nilai")
    public void nilai(Model model,@RequestParam Jadwal jadwal){
        String tahun = jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 9);

        model.addAttribute("tahun", tahun);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));
        model.addAttribute("nilai", presensiMahasiswaDao.bkdNilai(jadwal));
    }

    @GetMapping("/report/recapitulation/attendance")
    public void attendance(Model model,@RequestParam Jadwal jadwal){
        String tahun = jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 9);

        model.addAttribute("tahun", tahun);

        model.addAttribute("jadwal", jadwal);

        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));

        List<Object[]> hasil = presensiMahasiswaDao.bkdAttendance(jadwal);

        model.addAttribute("attendance", hasil);


    }

    @GetMapping("/report/fileberkas")
    public void fileBerkas(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        if (tahunAkademik != null){
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("fileBerkas", jadwalDao.findByTahunAkademikAndDosenAndStatus(tahunAkademik, dosen, StatusRecord.AKTIF));
        }
    }

    @RequestMapping("/download/")
    public void downloadBerkas(HttpServletRequest request, HttpServletResponse response, @RequestParam Jadwal jadwal, @RequestParam String status){

        if (status == "UAS"){
            Soal soal = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF, StatusApprove.APPROVED, StatusRecord.UAS);
            String fileName = soal.getFileApprove();
            String lokasi = uploadFolder + File.separator + soal.getJadwal().getId();

            Path file = Paths.get(lokasi, fileName);
            if (Files.exists(file))
            {
                response.setContentType("application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.addHeader("Content-Disposition", "attachment; filename="+fileName);
                try
                {
                    Files.copy(file, response.getOutputStream());
                    response.getOutputStream().flush();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }else if (status == "UTS"){
            Soal soal = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF, StatusApprove.APPROVED, StatusRecord.UTS);
            String fileName = soal.getFileApprove();
            String lokasi = uploadFolder + File.separator + soal.getJadwal().getId();

            Path file = Paths.get(lokasi, fileName);
            if (Files.exists(file))
            {
                response.setContentType("application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.addHeader("Content-Disposition", "attachment; filename="+fileName);
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


    }

    @GetMapping("/report/tugas_akhir")
    public void rekapTugasAKhir(@RequestParam Prodi prodi,@RequestParam String angkatan, HttpServletResponse response)throws IOException{

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet semprop = workbook.createSheet("SEMPROP");
        XSSFSheet skripsi = workbook.createSheet("SIDANG SKRIPSI");
        XSSFSheet hasil = workbook.createSheet("HASIL AKHIR");

        /*Setting*/
        Font judul = workbook.createFont();
        judul.setBold(true);
        judul.setFontHeightInPoints((short) 14);
        judul.setFontName("Times New Roman");
        CellStyle styleJudul = workbook.createCellStyle();
        styleJudul.setVerticalAlignment(VerticalAlignment.CENTER);
        styleJudul.setAlignment(HorizontalAlignment.CENTER);
        styleJudul.setFont(judul);

        Font subJudul = workbook.createFont();
        subJudul.setBold(true);
        subJudul.setFontHeightInPoints((short) 12);
        subJudul.setFontName("Times New Roman");
        CellStyle styleSubJudul = workbook.createCellStyle();
        styleSubJudul.setVerticalAlignment(VerticalAlignment.CENTER);
        styleSubJudul.setAlignment(HorizontalAlignment.CENTER);
        styleSubJudul.setFont(subJudul);
        styleSubJudul.setBorderTop(BorderStyle.THIN);
        styleSubJudul.setBorderBottom(BorderStyle.THIN);
        styleSubJudul.setBorderLeft(BorderStyle.THIN);
        styleSubJudul.setBorderRight(BorderStyle.THIN);

        CellStyle styleBlank = workbook.createCellStyle();
        styleBlank.setFillBackgroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        styleBlank.setFillPattern(FillPatternType.ALT_BARS);

        Font dataCenter = workbook.createFont();
        dataCenter.setFontHeightInPoints((short) 12);
        dataCenter.setFontName("Times New Roman");
        CellStyle styleData = workbook.createCellStyle();
        styleData.setVerticalAlignment(VerticalAlignment.CENTER);
        styleData.setAlignment(HorizontalAlignment.CENTER);
        styleData.setFont(dataCenter);
        styleData.setBorderTop(BorderStyle.THIN);
        styleData.setBorderBottom(BorderStyle.THIN);
        styleData.setBorderLeft(BorderStyle.THIN);
        styleData.setBorderRight(BorderStyle.THIN);

        Font dataLeft = workbook.createFont();
        dataLeft.setFontHeightInPoints((short) 12);
        dataLeft.setFontName("Times New Roman");
        CellStyle styleDataLeft = workbook.createCellStyle();
        styleDataLeft.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataLeft.setAlignment(HorizontalAlignment.LEFT);
        styleDataLeft.setFont(dataLeft);
        styleDataLeft.setBorderTop(BorderStyle.THIN);
        styleDataLeft.setBorderBottom(BorderStyle.THIN);
        styleDataLeft.setBorderLeft(BorderStyle.THIN);
        styleDataLeft.setBorderRight(BorderStyle.THIN);

        semprop.addMergedRegion(CellRangeAddress.valueOf("A1:G1"));
        semprop.addMergedRegion(CellRangeAddress.valueOf("A2:G2"));
        semprop.addMergedRegion(CellRangeAddress.valueOf("A3:G3"));
        skripsi.addMergedRegion(CellRangeAddress.valueOf("A1:G1"));
        skripsi.addMergedRegion(CellRangeAddress.valueOf("A2:G2"));
        skripsi.addMergedRegion(CellRangeAddress.valueOf("A3:G3"));
        hasil.addMergedRegion(CellRangeAddress.valueOf("A1:D1"));
        hasil.addMergedRegion(CellRangeAddress.valueOf("A2:D2"));
        hasil.addMergedRegion(CellRangeAddress.valueOf("A3:D3"));
        hasil.addMergedRegion(CellRangeAddress.valueOf("F1:K1"));
        hasil.addMergedRegion(CellRangeAddress.valueOf("F2:K2"));
        hasil.addMergedRegion(CellRangeAddress.valueOf("F3:K3"));


        /*Header*/
        int rowJudulSempro = 0 ;
        Row judulSempro = semprop.createRow(rowJudulSempro);
        judulSempro.createCell(0).setCellValue("JADWAL SEMINAR PROPOSAL SKRIPSI");
        judulSempro.getCell(0).setCellStyle(styleJudul);
        int rowJudulAngkatan = 1 ;
        Row judulAngkatan = semprop.createRow(rowJudulAngkatan);
        judulAngkatan.createCell(0).setCellValue("Angkatan " + angkatan);
        judulAngkatan.getCell(0).setCellStyle(styleJudul);
        int rowJudulProdi = 2 ;
        Row judulProdi = semprop.createRow(rowJudulProdi);
        judulProdi.createCell(0).setCellValue("Program Studi " + prodi.getNamaProdi());
        judulProdi.getCell(0).setCellStyle(styleJudul);

        int rowJudulSkripsi = 0 ;
        Row judulSkripsi = skripsi.createRow(rowJudulSkripsi);
        judulSkripsi.createCell(0).setCellValue("JADWAL SIDANG SKRIPSI");
        judulSkripsi.getCell(0).setCellStyle(styleJudul);
        int rowJudulSkripsiAngkatan = 1 ;
        Row judulSkripsiAngkatan = skripsi.createRow(rowJudulSkripsiAngkatan);
        judulSkripsiAngkatan.createCell(0).setCellValue("Angkatan "+ angkatan);
        judulSkripsiAngkatan.getCell(0).setCellStyle(styleJudul);
        int rowJudulSkripsiProdi = 2 ;
        Row judulSkripsiProdi = skripsi.createRow(rowJudulSkripsiProdi);
        judulSkripsiProdi.createCell(0).setCellValue("Program Studi "+ prodi.getNamaProdi());
        judulSkripsiProdi.getCell(0).setCellStyle(styleJudul);

        int rowJudulHasilSemprop = 0 ;
        Row hasilSempro = hasil.createRow(rowJudulHasilSemprop);
        hasilSempro.createCell(0).setCellValue("NILAI SEMPRO (40%)");
        hasilSempro.createCell(5).setCellValue("NILAI SKRIPSI (SEMPRO 40% SIDANG 60%)");
        hasilSempro.getCell(0).setCellStyle(styleJudul);
        hasilSempro.getCell(5).setCellStyle(styleJudul);

        int rowHasilAngkatan = 1 ;
        Row hasilAngkatan = hasil.createRow(rowHasilAngkatan);
        hasil.setColumnWidth(1,30 * 300);
        hasil.setColumnWidth(2,30 * 256);
        hasil.setColumnWidth(6,25 * 150);
        hasil.setColumnWidth(7,30 * 256);
        hasilAngkatan.createCell(0).setCellValue("Angkatan " + angkatan);
        hasilAngkatan.createCell(5).setCellValue("Angkatan " + angkatan);
        hasilAngkatan.getCell(0).setCellStyle(styleJudul);
        hasilAngkatan.getCell(5).setCellStyle(styleJudul);

        int rowHasilProdi = 2 ;
        Row HasilProdi = hasil.createRow(rowHasilProdi);
        HasilProdi.createCell(0).setCellValue("Program Studi "+ prodi.getNamaProdi());
        hasil.setColumnWidth(0,30 * 300);
        HasilProdi.createCell(5).setCellValue("Program Studi " + prodi.getNamaProdi());
        HasilProdi.getCell(0).setCellStyle(styleJudul);
        HasilProdi.getCell(5).setCellStyle(styleJudul);

        List<String> headerTableSempro = Arrays.asList("NO  ",
                "   NIM   ",
                "       Nama Mahasiswa       ",
                "Hari / Tanggal  ",
                "Penguji I/Ketua              ",
                "Penguji II/Pembimbing              ",
                "Penguji III       ",
                "Pukul  ",
                "Nilai  ",
                "Nilai Mutu   "
        );

        int rowSubJudulSempro = 4 ;
        int cellNumSempro = 0 ;
        Row subjudulSempro = semprop.createRow(rowSubJudulSempro);
        for (String header : headerTableSempro) {
            Cell cell = subjudulSempro.createCell(cellNumSempro);
            cell.setCellValue(header);
            cell.setCellStyle(styleSubJudul);
            semprop.autoSizeColumn(cellNumSempro);
            cellNumSempro++;
        }

        int rowSubJudulSkripsi = 4 ;
        int cellNumSkripis = 0 ;
        Row subjudulSkripsi = skripsi.createRow(rowSubJudulSkripsi);
        for (String header : headerTableSempro) {
            Cell cell = subjudulSkripsi.createCell(cellNumSkripis);
            cell.setCellValue(header);
            cell.setCellStyle(styleSubJudul);
            skripsi.autoSizeColumn(cellNumSkripis);
            cellNumSkripis++;
        }

        /*End Header*/

        List<RekapTugasAkhir> tugasAkhir = seminarDao.rekapTugasAkhir(prodi,angkatan);
        /*Data Semprop*/

        int rowSemprop = 5 ;
        int noDataSempro = 1;
        for (RekapTugasAkhir detailSeminar : tugasAkhir) {
            Row row = semprop.createRow(rowSemprop);
            row.createCell(0).setCellValue(noDataSempro);
            row.createCell(1).setCellValue(detailSeminar.getNim());
            row.createCell(2).setCellValue(detailSeminar.getNama());
            if (detailSeminar.getTanggalSempro() == null){

            }
            if (detailSeminar.getTanggalSempro() != null) {
                LocalDate tanggalSempro = LocalDate.parse(detailSeminar.getTanggalSempro());
                if (tanggalSempro.getDayOfWeek().getValue() != 7){
                    if (tanggalSempro.getMonthValue() == 1){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Januari " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 2){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Februari " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 3){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Maret " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 4){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " April " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 5){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Mei " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 6){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Juni " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 7){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Juli " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 8){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Agustus " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 9){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " September " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 10){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Oktober " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 11){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " November " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 12){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Desember " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }

                }else {
                    if (tanggalSempro.getMonthValue() == 1){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Januari " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 2){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Februari " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 3){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Maret " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 4){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " April " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 5){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Mei " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 6){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Juni " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 7){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Juli " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 8){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Agustus " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 9){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " September " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 10){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Oktober " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 11){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " November " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 12){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSempro.getDayOfMonth() + " Desember " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                }
            }
            row.createCell(4).setCellValue(detailSeminar.getKetuaSempro());
            row.createCell(5).setCellValue(detailSeminar.getPembimbingSempro());
            row.createCell(6).setCellValue(detailSeminar.getPengujiSempro());
            row.createCell(7).setCellValue(detailSeminar.getJamMulai());
            row.createCell(8).setCellValue(detailSeminar.getNilai().doubleValue());
            if (detailSeminar.getNilai().compareTo(new BigDecimal(80)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(85)) < 0){
                row.createCell(9).setCellValue("A-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(75)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(80)) < 0){
                row.createCell(9).setCellValue("B+");
                row.getCell(9).setCellStyle(styleData);


            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(70)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(75)) < 0){
                row.createCell(9).setCellValue("B");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(65)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(70)) < 0){
                row.createCell(9).setCellValue("B-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(60)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(65)) < 0){
                row.createCell(9).setCellValue("C+");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(55)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(60)) < 0){
                row.createCell(9).setCellValue("C");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(50)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(55)) < 0){
                row.createCell(9).setCellValue("D");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(0)) >= 0 && detailSeminar.getNilai().compareTo(new BigDecimal(50)) < 0){
                row.createCell(9).setCellValue("E");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(85)) >= 0){
                row.createCell(9).setCellValue("A");
                row.getCell(9).setCellStyle(styleData);

            }

            row.getCell(0).setCellStyle(styleData);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(2).setCellStyle(styleDataLeft);
            row.getCell(4).setCellStyle(styleDataLeft);
            row.getCell(5).setCellStyle(styleDataLeft);
            row.getCell(6).setCellStyle(styleDataLeft);
            row.getCell(7).setCellStyle(styleDataLeft);
            row.getCell(7).setCellStyle(styleData);
            row.getCell(8).setCellStyle(styleData);

            rowSemprop++;
            noDataSempro++;
        }

        /* Data Sidang*/
        int rowSkripsi = 5 ;
        int noDataSkripsi = 1;
        for (RekapTugasAkhir detailSidang : tugasAkhir) {
            Row row = skripsi.createRow(rowSkripsi);
            row.createCell(0).setCellValue(noDataSkripsi);
            row.createCell(1).setCellValue(detailSidang.getNim());
            row.createCell(2).setCellValue(detailSidang.getNama());
            if (detailSidang.getTanggalSidang() == null){
                row.createCell(3).setCellValue("-");
                row.getCell(3).setCellStyle(styleData);
            }
            if (detailSidang.getTanggalSidang() != null) {
                LocalDate tanggalSidang = LocalDate.parse(detailSidang.getTanggalSidang());
                if (tanggalSidang.getDayOfWeek().getValue() != 7){
                    if (tanggalSidang.getMonthValue() == 1){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Januari " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 2){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Februari " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 3){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Maret " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 4){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " April " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 5){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Mei " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 6){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Juni " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 7){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Juli " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 8){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Agustus " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 9){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " September " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 10){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Oktober " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 11){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " November " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 12){
                        row.createCell(3).setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue())).get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Desember " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }

                }else {
                    if (tanggalSidang.getMonthValue() == 1){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Januari " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 2){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Februari " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 3){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Maret " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 4){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " April " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 5){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Mei " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 6){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Juni " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 7){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Juli " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 8){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Agustus " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 9){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " September " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 10){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Oktober " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 11){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " November " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 12){
                        row.createCell(3).setCellValue( "Sabtu , " + tanggalSidang.getDayOfMonth() + " Desember " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                }
            }
            row.createCell(4).setCellValue(detailSidang.getKetuaSidang());
            row.createCell(5).setCellValue(detailSidang.getPembimbingSidang());
            row.createCell(6).setCellValue(detailSidang.getPengujiSidang());
            if (detailSidang.getMulaiSidang() != null) {
                row.createCell(7).setCellValue(detailSidang.getMulaiSidang() + " - " + detailSidang.getSelesaiSIdang());
                row.getCell(7).setCellStyle(styleData);
            }

            if (detailSidang.getMulaiSidang() == null) {
                row.createCell(7).setCellValue( " - " );
                row.getCell(7).setCellStyle(styleData);
            }
            row.createCell(8).setCellValue(detailSidang.getNilaiSidang().doubleValue());
            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(80)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(85)) < 0){
                row.createCell(9).setCellValue("A-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(75)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(80)) < 0){
                row.createCell(9).setCellValue("B+");
                row.getCell(9).setCellStyle(styleData);


            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(70)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(75)) < 0){
                row.createCell(9).setCellValue("B");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(65)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(70)) < 0){
                row.createCell(9).setCellValue("B-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(60)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(65)) < 0){
                row.createCell(9).setCellValue("C+");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(55)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(60)) < 0){
                row.createCell(9).setCellValue("C");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(50)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(55)) < 0){
                row.createCell(9).setCellValue("D");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(0)) >= 0 && detailSidang.getNilaiSidang().compareTo(new BigDecimal(50)) < 0){
                row.createCell(9).setCellValue("E");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(85)) >= 0){
                row.createCell(9).setCellValue("A");
                row.getCell(9).setCellStyle(styleData);

            }

            row.getCell(0).setCellStyle(styleData);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(2).setCellStyle(styleDataLeft);
            row.getCell(4).setCellStyle(styleDataLeft);
            row.getCell(5).setCellStyle(styleDataLeft);
            row.getCell(6).setCellStyle(styleDataLeft);
            row.getCell(8).setCellStyle(styleData);

            rowSkripsi++;
            noDataSkripsi++;
        }


        List<String> headerHasil = Arrays.asList("NO  ",
                "   NIM   ",
                "       Nama Mahasiswa       ",
                "Nilai  ",
                "   ",
                "NO  ",
                "   NIM    ",
                "      Nama Mahasiswa       ",
                "Nilai  ",
                "Nilai Akhir",
                "Huruf Mutu"
        );

        int rowSubHasil = 4 ;
        int cellNumHasil = 0 ;
        Row subjudulHasil = hasil.createRow(rowSubHasil);
        for (String header : headerHasil) {
            Cell cell = subjudulHasil.createCell(cellNumHasil);
            cell.setCellValue(header);
            cell.setCellStyle(styleSubJudul);
            hasil.autoSizeColumn(cellNumHasil);
            cellNumHasil++;
        }

        int rowHasil = 5 ;
        int noDataHasil = 1;
        for (RekapTugasAkhir hasilTugas : tugasAkhir){
            Row row = hasil.createRow(rowHasil);
            row.createCell(0).setCellValue(noDataHasil);
            row.createCell(1).setCellValue(hasilTugas.getNim());
            row.createCell(2).setCellValue(hasilTugas.getNama());
            row.createCell(3).setCellValue(hasilTugas.getNilai().doubleValue());
            row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue(noDataHasil);
            row.createCell(6).setCellValue(hasilTugas.getNim());
            row.createCell(7).setCellValue(hasilTugas.getNama());
            row.createCell(8).setCellValue(hasilTugas.getNilaiSidang().doubleValue());
            row.createCell(9).setCellValue((hasilTugas.getNilai().multiply(new BigDecimal(0.4))).add(hasilTugas.getNilaiSidang().multiply(new BigDecimal(0.6))).doubleValue());

            BigDecimal nilaiHasil = (hasilTugas.getNilai().multiply(new BigDecimal(0.4))).add(hasilTugas.getNilaiSidang().multiply(new BigDecimal(0.6)));
            if (nilaiHasil.compareTo(new BigDecimal(80)) >= 0 && nilaiHasil.compareTo(new BigDecimal(85)) < 0){
                row.createCell(10).setCellValue("A-");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(75)) >= 0 && nilaiHasil.compareTo(new BigDecimal(80)) < 0){
                row.createCell(10).setCellValue("B+");
                row.getCell(10).setCellStyle(styleData);


            }

            if (nilaiHasil.compareTo(new BigDecimal(70)) >= 0 && nilaiHasil.compareTo(new BigDecimal(75)) < 0){
                row.createCell(10).setCellValue("B");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(65)) >= 0 && nilaiHasil.compareTo(new BigDecimal(70)) < 0){
                row.createCell(10).setCellValue("B-");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(60)) >= 0 && nilaiHasil.compareTo(new BigDecimal(65)) < 0){
                row.createCell(10).setCellValue("C+");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(55)) >= 0 && nilaiHasil.compareTo(new BigDecimal(60)) < 0){
                row.createCell(10).setCellValue("C");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(50)) >= 0 && nilaiHasil.compareTo(new BigDecimal(55)) < 0){
                row.createCell(10).setCellValue("D");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(0)) >= 0 && nilaiHasil.compareTo(new BigDecimal(50)) < 0){
                row.createCell(10).setCellValue("E");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(85)) >= 0){
                row.createCell(10).setCellValue("A");
                row.getCell(10).setCellStyle(styleData);

            }

            row.getCell(0).setCellStyle(styleDataLeft);
            row.getCell(1).setCellStyle(styleDataLeft);
            row.getCell(2).setCellStyle(styleDataLeft);
            row.getCell(3).setCellStyle(styleData);
            row.getCell(4).setCellStyle(styleBlank);
            row.getCell(5).setCellStyle(styleDataLeft);
            row.getCell(6).setCellStyle(styleDataLeft);
            row.getCell(7).setCellStyle(styleDataLeft);
            row.getCell(8).setCellStyle(styleData);
            row.getCell(9).setCellStyle(styleData);

            rowHasil++;
            noDataHasil++;

        }


        String namaFile = prodi.getKodeProdi()+' '+angkatan + " - REKAP TUGAS AKHIR";
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
