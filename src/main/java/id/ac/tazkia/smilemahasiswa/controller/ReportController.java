package id.ac.tazkia.smilemahasiswa.controller;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Time;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import id.ac.tazkia.smilemahasiswa.dao.BebasKewajibanDao;
import id.ac.tazkia.smilemahasiswa.dao.BobotTugasDao;
import id.ac.tazkia.smilemahasiswa.dao.CutiDao;
import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.EdomMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.EdomQuestionOldDao;
import id.ac.tazkia.smilemahasiswa.dao.HariDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalTugasDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.KelasMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDosenWaliDao;
import id.ac.tazkia.smilemahasiswa.dao.NilaiTugasDao;
import id.ac.tazkia.smilemahasiswa.dao.NoteDao;
import id.ac.tazkia.smilemahasiswa.dao.PembinaanDao;
import id.ac.tazkia.smilemahasiswa.dao.PembinaanDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.PendaftaranMbkmDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.ProgramDao;
import id.ac.tazkia.smilemahasiswa.dao.SeminarDao;
import id.ac.tazkia.smilemahasiswa.dao.SesiKuliahFotoDao;
import id.ac.tazkia.smilemahasiswa.dao.SidangDao;
import id.ac.tazkia.smilemahasiswa.dao.SoalDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.UjianTahfidzKompreDao;
import id.ac.tazkia.smilemahasiswa.dao.edom.EdomGeneralResponseDao;
import id.ac.tazkia.smilemahasiswa.dto.edom.DetailEdomDto;
import id.ac.tazkia.smilemahasiswa.dto.edom.RekapEdomDto;
import id.ac.tazkia.smilemahasiswa.dto.edom.RekapEdomSarprasDto;
import id.ac.tazkia.smilemahasiswa.dto.graduation.RekapTugasAkhir;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapJadwalDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapSksDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.ReportUjianDto;
import id.ac.tazkia.smilemahasiswa.dto.report.SalaryDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto;
import id.ac.tazkia.smilemahasiswa.entity.Cuti;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.JenisPembinaanMatrikulasi;
import id.ac.tazkia.smilemahasiswa.entity.JenisPembinaanMatrikulasiDetail;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.KelasMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Note;
import id.ac.tazkia.smilemahasiswa.entity.PendaftaranMbkm;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.Seminar;
import id.ac.tazkia.smilemahasiswa.entity.Sidang;
import id.ac.tazkia.smilemahasiswa.entity.Soal;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TugasMateriService;

@Controller
public class ReportController {
    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

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
    private ProgramDao programDao;

    @Autowired
    private EdomMahasiswaDao edomMahasiswaDao;

    @Autowired
    private EdomQuestionOldDao edomQuestionOldDao;

    @Autowired
    private EdomGeneralResponseDao edomGeneralResponseDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private SoalDao soalDao;

    @Autowired
    private SeminarDao seminarDao;

    @Autowired
    private PembinaanDao pembinaanDao;

    @Autowired
    private PembinaanDetailDao pembinaanDetailDao;

    @Autowired
    private MahasiswaDosenWaliDao mahasiswaDosenWaliDao;

    @Autowired
    private PendaftaranMbkmDao pendaftaranMbkmDao;

    @Autowired
    private NoteDao noteDao;

    @Autowired
    private SidangDao sidangDao;

    @Autowired
    private UjianTahfidzKompreDao ujianTahfidzKompreDao;

    @Autowired
    private BebasKewajibanDao bebasKewajibanDao;

    @Autowired
    private JadwalTugasDao jadwalTugasDao;

    @Autowired
    private SesiKuliahFotoDao sesiKuliahFotoDao;

    @Autowired
    private BobotTugasDao bobotTugasDao;

    @Autowired
    private NilaiTugasDao nilaiTugasDao;

    @Autowired
    private TugasMateriService tugasMateriService;

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

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @GetMapping("/report/recapitulation/lecturer")
    public void rekapDosen(Model model, @RequestParam(required = false) TahunAkademik ta,
            @PageableDefault(size = Integer.MAX_VALUE) Pageable page) {
        if (ta == null) {
            ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        }

        model.addAttribute("selectedTahun", ta);

        // Page<RekapJadwalDosenDto> rekap =
        // jadwalDosenDao.rekapJadwalDosen2(ta.getId(), page);
        //
        // Map<String, RekapSksDosenDto> rekapJumlahSks = new LinkedHashMap<>();
        // Map<String, List<RekapJadwalDosenDto>> detailJadwalPerDosen = new
        // LinkedHashMap<>();
        //
        // for (RekapJadwalDosenDto r : rekap.getContent()) {
        // // Hitung total SKS
        // RekapSksDosenDto rsks = rekapJumlahSks.computeIfAbsent(r.getIdDosen(), id ->
        // {
        // RekapSksDosenDto newRsks = new RekapSksDosenDto();
        // newRsks.setNamaDosen(r.getNamaDosen());
        // newRsks.setIdDosen(r.getIdDosen());
        // newRsks.setTotalSks(0);
        // return newRsks;
        // });
        //
        // rsks.tambahSks(r.getSks());
        //
        // // Jadwal per dosen
        // detailJadwalPerDosen.computeIfAbsent(r.getIdDosen(), id -> new
        // ArrayList<>()).add(r);
        // }

        List<Object[]> result = jadwalDosenDao.rekapJadwalDosen3(ta.getId());

        // Konversi hasil query ke DTO
        List<RekapJadwalDosenDto> dtos = result.stream()
                .map(row -> {
                    RekapJadwalDosenDto dto = new RekapJadwalDosenDto();
                    dto.setIdJadwal((String) row[0]); // idJadwal
                    dto.setIdDosen((String) row[1]); // idDosen
                    dto.setNamaDosen((String) row[2]); // namaDosen
                    dto.setNamaMatakuliah((String) row[3]);
                    dto.setSks(((Integer) row[4]).intValue()); // sks
                    dto.setNamaProdi((String) row[5]);
                    dto.setNamaKelas((String) row[6]);
                    dto.setNamaHari((String) row[7]);
                    dto.setSesi((String) row[8]);
                    dto.setJamMulai(((Time) row[9]).toLocalTime());
                    dto.setJamSelesai(((Time) row[10]).toLocalTime());
                    dto.setNamaRuangan((String) row[11]);
                    dto.setNamaGedung((String) row[12]);
                    dto.setJumlahKehadiran(((BigInteger) row[13]).intValue());

                    return dto;

                })
                .collect(Collectors.toList());

        // Inisialisasi map untuk rekap SKS dan jadwal per dosen
        Map<String, RekapSksDosenDto> rekapJumlahSks = new LinkedHashMap<>();
        Map<String, List<RekapJadwalDosenDto>> detailJadwalPerDosen = new LinkedHashMap<>();

        // Iterasi untuk penghitungan total SKS dan detail jadwal
        for (RekapJadwalDosenDto r : dtos) {
            // Rekap total SKS per dosen
            RekapSksDosenDto rsks = rekapJumlahSks.computeIfAbsent(r.getIdDosen(), id -> {
                RekapSksDosenDto newRsks = new RekapSksDosenDto();
                newRsks.setIdDosen(r.getIdDosen());
                newRsks.setNamaDosen(r.getNamaDosen());
                newRsks.setTotalSks(0); // Default nilai SKS
                return newRsks;
            });
            rsks.tambahSks(r.getSks());

            // Tambahkan detail jadwal ke list per dosen
            detailJadwalPerDosen
                    .computeIfAbsent(r.getIdDosen(), id -> new ArrayList<>())
                    .add(r);
        }

        model.addAttribute("rekapJumlahSks", rekapJumlahSks);
        model.addAttribute("rekapJadwalDosen", dtos);
        model.addAttribute("rekapJadwalPerDosen", detailJadwalPerDosen);
    }

    @GetMapping("/report/recapitulation/detail")
    public void detailJadwal(Model model, @RequestParam Jadwal jadwal) {

        // Halaman presensi
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("teamTeaching", jadwalDosenDao.findByJadwal(jadwal));
        model.addAttribute("detail", presensiDosenDao.listPresensiDosen(jadwal));
        model.addAttribute("jadwalTugas",
                jadwalTugasDao.findByStatusAndSesiKuliahJadwalOrderBySesiKuliahPertemuanKe(StatusRecord.AKTIF, jadwal));
        model.addAttribute("sesiKuliahFoto",
                sesiKuliahFotoDao.findByStatusAndJadwalOrderBySesiKuliahPertemuan(StatusRecord.AKTIF, jadwal));

        // Halaman penilaian
        model.addAttribute("krsDetail", krsDetailDao.findByJadwalAndStatus(jadwal, StatusRecord.AKTIF));
        model.addAttribute("nilai", tugasMateriService.getNilaiByJadwal(jadwal));

    }

    @GetMapping("/report/recapitulation/schedule")
    public void laporanPresensi(Model model, @RequestParam(required = false) String tanggal) {
        if (StringUtils.hasText(tanggal)) {
            model.addAttribute("tanggal", LocalDate.parse(tanggal));
            model.addAttribute("rekap", presensiDosenDao.rekapPresensiPerHari(LocalDate.parse(tanggal)));
        } else {
            model.addAttribute("tanggal", LocalDate.now());
            model.addAttribute("rekap", presensiDosenDao.rekapPresensiPerHari(LocalDate.now()));
        }
    }

    @GetMapping("/report/recapitulation/salary")
    public void rekapGajiDosen(Model model, @RequestParam(required = false) String masuk,
            @RequestParam(required = false) String selesai) {

        if (masuk != null) {
            model.addAttribute("masuk", LocalDate.parse(masuk));
            model.addAttribute("selesai", LocalDate.parse(selesai));
        }

    }

    @GetMapping("/rekap/salary/download")
    public void downloadSalary(String masuk, String selesai, HttpServletResponse response) throws IOException {
        LocalDate m = LocalDate.parse(masuk);
        LocalDate s = LocalDate.parse(selesai);

        String[] column = { "No", "Dosen", "Status Dosen", "Matakuliah", "SKS", "Kelas", "Tanggal Offline",
                "Tanggal Online", "Tidak Mengajar" };

        List<SalaryDto> list = presensiDosenDao.rekapSalary(m, s);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Rekap Salary");

        Font header = workbook.createFont();
        header.setBold(true);
        header.setFontHeightInPoints((short) 14);
        header.setFontName("Times New Roman");

        CellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setVerticalAlignment(VerticalAlignment.CENTER);
        styleHeader.setAlignment(HorizontalAlignment.CENTER);
        styleHeader.setFont(header);

        Font fontIsiCenter = workbook.createFont();
        fontIsiCenter.setFontHeightInPoints((short) 12);
        fontIsiCenter.setFontName("Times New Roman");
        CellStyle isiStyleCenter = workbook.createCellStyle();
        isiStyleCenter.setVerticalAlignment(VerticalAlignment.CENTER);
        isiStyleCenter.setAlignment(HorizontalAlignment.CENTER);
        isiStyleCenter.setFont(fontIsiCenter);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < column.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column[i]);
            cell.setCellStyle(styleHeader);
        }

        int rowNum = 1;
        int barus = 1;

        for (SalaryDto salary : list) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(barus++);
            row.createCell(1).setCellValue(salary.getNama());
            row.createCell(2).setCellValue(salary.getSdosen());
            row.createCell(3).setCellValue(salary.getMatkul());
            row.createCell(4).setCellValue(salary.getSks());
            row.createCell(5).setCellValue(salary.getKelas());
            row.createCell(6).setCellValue(salary.getOffline());
            row.createCell(7).setCellValue(salary.getOnline());
            row.createCell(8).setCellValue(salary.getAbstain());

            row.getCell(0).setCellStyle(isiStyleCenter);
            row.getCell(1).setCellStyle(isiStyleCenter);
            row.getCell(2).setCellStyle(isiStyleCenter);
            row.getCell(3).setCellStyle(isiStyleCenter);
            row.getCell(4).setCellStyle(isiStyleCenter);
            row.getCell(5).setCellStyle(isiStyleCenter);
            row.getCell(6).setCellStyle(isiStyleCenter);
            row.getCell(7).setCellStyle(isiStyleCenter);
            row.getCell(8).setCellStyle(isiStyleCenter);
        }

        for (int i = 0; i < column.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Rekap_Salary_" + m + "-" + s + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/rekap-salary")
    @ResponseBody
    List<SalaryDto> getSalaryDto(String masuk, String selesai) {

        return presensiDosenDao.rekapSalary(LocalDate.parse(masuk), LocalDate.parse(selesai));
    }

    @GetMapping("/report/recapitulation/ipk")
    public void rekapSks(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) String angkatan, @RequestParam(required = false) Prodi prodi,
            @RequestParam(required = false) Program program) {

        model.addAttribute("selectedAngkatan", angkatan);
        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedProdi", prodi);
        model.addAttribute("selectedProgram", program);

        if (tahunAkademik != null) {
            model.addAttribute("ipk",
                    krsDetailDao.cariIpk(tahunAkademik, angkatan, tahunAkademik.getKodeTahunAkademik(), prodi,
                            program));
        }
    }

    @GetMapping("/report/recapitulation/downloadipk")
    public void listPerMatkul(@RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) String angkatan, @RequestParam(required = false) Prodi prodi,
            @RequestParam(required = false) Program program, HttpServletResponse response) throws IOException {

        List<Object[]> listDownload = krsDetailDao.cariIpk(tahunAkademik, angkatan,
                tahunAkademik.getKodeTahunAkademik(), prodi, program);

        String[] columns = { "No", "Nim", "Nama", "Prodi", "Program", "Tahun Akademik", "SKS Semester", "IP Semester",
                "SKS Total", "IPK" };

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List Ipk Angkatan " + angkatan);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(2);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 3;
        int baris = 1;

        for (Object[] list : listDownload) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[0].toString());
            row.createCell(2).setCellValue(list[1].toString());
            row.createCell(3).setCellValue(list[2].toString());
            row.createCell(4).setCellValue(list[3].toString());
            row.createCell(5).setCellValue(tahunAkademik.getNamaTahunAkademik());
            row.createCell(6).setCellValue(list[6].toString());
            row.createCell(7).setCellValue(list[7].toString());
            row.createCell(8).setCellValue(list[8].toString());
            row.createCell(9).setCellValue(list[9].toString());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=List Ipk Tahun Akademik-"
                + tahunAkademik.getNamaTahunAkademik() + "-" + "Angkatan " + angkatan + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/report/recapitulation/edom")
    public void rekapEdom(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) Prodi prodi) {

        if (tahunAkademik != null) {
            // === DOSEN ===
            List<RekapEdomDto> rekapEdom;
            List<DetailEdomDto> detailEdom;

            if (prodi != null) {
                rekapEdom = edomGeneralResponseDao.findRekapEdomByProdi(tahunAkademik.getId(), prodi.getId());
                detailEdom = edomGeneralResponseDao.findDetailEdomByProdi(tahunAkademik.getId(), prodi.getId());
            } else {
                rekapEdom = edomGeneralResponseDao.findRekapEdomAll(tahunAkademik.getId());
                detailEdom = Collections.emptyList();
            }

            // === SARPRAS ===
            List<RekapEdomSarprasDto> rekapSarpras = edomGeneralResponseDao.findRekapEdomSarpras(tahunAkademik.getId());

            // Group SARPRAS by category
            Map<String, List<RekapEdomSarprasDto>> sarprasPerCategory = new LinkedHashMap<>();
            for (RekapEdomSarprasDto s : rekapSarpras) {
                sarprasPerCategory.computeIfAbsent(s.getCategoryName(), k -> new ArrayList<>()).add(s);
            }

            if (rekapEdom.isEmpty() && rekapSarpras.isEmpty()) {
                model.addAttribute("questionNull", "Data edom untuk tahun akademik ini belum tersedia");
            }

            model.addAttribute("headerEdom", rekapEdom);
            model.addAttribute("detailEdom", detailEdom);
            model.addAttribute("sarprasPerCategory", sarprasPerCategory);
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
        }

    }

    @GetMapping("/report/recapitulation/downloadedom")
    public void downloadEdom(@RequestParam(required = false) TahunAkademik tahunAkademik, HttpServletResponse response)
            throws IOException {

        List<RekapEdomDto> listEdom = edomGeneralResponseDao.findRekapEdomAll(tahunAkademik.getId());

        String[] columns = { "No", "Nama Dosen", "Nama Matakuliah", "Nama Kelas",
                "Jumlah Mahasiswa", "Jumlah Mengisi Edom", "Nilai Edom" };

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List Edom tahun akademik " + tahunAkademik.getKodeTahunAkademik());

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(2);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 3;
        int baris = 1;

        for (RekapEdomDto edom : listEdom) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(edom.getNamaDosen() != null ? edom.getNamaDosen() : "-");
            row.createCell(2).setCellValue(edom.getNamaMatakuliah() != null ? edom.getNamaMatakuliah() : "-");
            row.createCell(3).setCellValue(edom.getNamaKelas() != null ? edom.getNamaKelas() : "-");
            row.createCell(4).setCellValue(edom.getTotalMahasiswa() != null ? edom.getTotalMahasiswa() : 0);
            row.createCell(5).setCellValue(edom.getTotalMengisi() != null ? edom.getTotalMengisi() : 0);
            row.createCell(6).setCellValue(edom.getRataRata() != null ? edom.getRataRata().doubleValue() : 0);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment; filename=List Edom Tahun Akademik-" + tahunAkademik.getNamaTahunAkademik() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/report/recapitulation/bkd")
    public void rekapBkdDosen(Model model, Authentication authentication,
            @RequestParam(required = false) TahunAkademik tahunAkademik) {
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        if (tahunAkademik != null) {
            model.addAttribute("selectedTahun", tahunAkademik);

            String tlc = "staftlc";

            if (tlc.equals(user.getRole().getId())) {
                List<ScheduleDto> jadwal = jadwalDao.lecturerAssesmentAll(StatusRecord.AKTIF, tahunAkademik);
                model.addAttribute("jadwal", jadwal);
            } else {
                List<ScheduleDto> jadwal = jadwalDao.lecturerAssesment(dosen, StatusRecord.AKTIF, tahunAkademik);
                model.addAttribute("jadwal", jadwal);

            }
        }

    }

    @GetMapping("/report/historymahasiswa")
    public void historyMahasiswa(Model model, @RequestParam(required = false) String nim) {
        if (nim != null) {
            model.addAttribute("nim", nim);
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            String URL = "https://api.whatsapp.com/send?phone=" + mahasiswa.getTeleponSeluler();
            String urlProdi = "https://smile.tazkia.ac.id/mahasiswa/form?mahasiswa=" + mahasiswa.getNim();
            KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
            Note note = noteDao.findByMahasiswaAndStatus(mahasiswa, StatusApprove.APPROVED);
            Seminar seminar = seminarDao.findByNoteAndStatus(note, StatusApprove.APPROVED);
            Sidang sidang = sidangDao.findBySeminarAndStatusSidang(seminar, StatusApprove.APPROVED);

            // Data utama
            List<Object[]> transkriptTampil = krsDetailDao.transkriptTampil(mahasiswa.getId());
            List<Object[]> semesterTranskript = krsDao.semesterTranskript(mahasiswa.getId());

            // Hitung total SKS per semester
            Map<Object, Integer> totalSksPerSemester = new HashMap<>();
            for (Object[] sem : semesterTranskript) {
                Object semesterKey = sem[0];
                int total = transkriptTampil.stream()
                        .filter(row -> row[0].equals(semesterKey))
                        .mapToInt(row -> Integer.parseInt(row[3].toString()))
                        .sum();
                totalSksPerSemester.put(semesterKey, total);
            }

            List<KrsDetail> konversi = krsDetailDao.findByMahasiswaAndStatusAndStatusKonversi(mahasiswa,
                    StatusRecord.AKTIF, StatusRecord.AKTIF);
            int totalSks = konversi.stream()
                    .mapToInt(k -> k.getMatakuliahKurikulum().getJumlahSks())
                    .sum();
            model.addAttribute("totalSksPerSemester", totalSksPerSemester);
            model.addAttribute("totalSksKonversi", totalSks);
            model.addAttribute("mhs", mahasiswa);
            model.addAttribute("kelas", kelasMahasiswa);
            model.addAttribute("url", URL);
            model.addAttribute("urlProdi", urlProdi);
            model.addAttribute("history", krsDetailDao.historyMahasiswa(mahasiswa));
            model.addAttribute("ipk", krsDetailDao.ipk(mahasiswa));
            model.addAttribute("sksTotal", krsDetailDao.totalSks(mahasiswa));
            model.addAttribute("semester", krsDetailDao.semesterHistory(mahasiswa));
            model.addAttribute("tahunMasuk", krsDao.masukMahasiswa(mahasiswa));
            model.addAttribute("khsHistory", krsDetailDao.khsHistoty(mahasiswa));
            model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));
            model.addAttribute("semesterTranskript", krsDao.semesterTranskript(mahasiswa.getId()));
            model.addAttribute("transkriptTampil", krsDetailDao.transkriptTampil(mahasiswa.getId()));
            model.addAttribute("pembimbing", note);
            model.addAttribute("seminar", seminar);
            model.addAttribute("sidang", sidang);
            model.addAttribute("tahfidz", ujianTahfidzKompreDao.findByMahasiswaAndJenisUjianAndStatusLulus(mahasiswa,
                    "UjianTahfidz", StatusRecord.LULUS));
            model.addAttribute("kompre", ujianTahfidzKompreDao.findByMahasiswaAndJenisUjianAndStatusLulus(mahasiswa,
                    "UjianKomprehensif", StatusRecord.LULUS));
            model.addAttribute("inggris",
                    bebasKewajibanDao.findByMahasiswaAndStatusApproveTlc(mahasiswa, StatusApprove.APPROVED));
            model.addAttribute("konversi", krsDetailDao.findByMahasiswaAndStatusAndStatusKonversi(mahasiswa,
                    StatusRecord.AKTIF, StatusRecord.AKTIF));

        }
    }

    @GetMapping("/report/cuti")
    public void mahasiswaCuti(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
            @PageableDefault(size = 10) Pageable pageable) {

        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahunAkademik == null) {
            model.addAttribute("listCutiMahasiswa", cutiDao
                    .findByStatusAndTahunAkademikOrderByStatusPengajuaanDesc(StatusRecord.AKTIF, tahun, pageable));
        } else {
            model.addAttribute("listCutiMahasiswa", cutiDao.findByStatusAndTahunAkademikOrderByStatusPengajuaanDesc(
                    StatusRecord.AKTIF, tahunAkademik, pageable));
            model.addAttribute("selectedTahun", tahunAkademik);
        }

        // model.addAttribute("listCutiMahasiswa",
        // cutiDao.findByStatusAndTahunAkademikOrderByStatusPengajuaanDesc(StatusRecord.AKTIF,
        // pageable));
    }

    @PostMapping("/proses/cuti")
    public String prosesCuti(@Valid Cuti cuti, @RequestParam Mahasiswa mahasiswa) {
        cuti.setMahasiswa(mahasiswa);
        cuti.setTanggalPengajuaan(LocalDate.now());
        cuti.setStatusPengajuaan(StatusApprove.DIAJUKAN);
        cutiDao.save(cuti);

        return "redirect:/report/cuti";
    }

    @GetMapping("/report/recapitulation/nilai")
    public void nilai(Model model, @RequestParam Jadwal jadwal) {
        String tahun = jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 9);

        model.addAttribute("tahun", tahun);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));
        model.addAttribute("nilai", presensiMahasiswaDao.bkdNilai(jadwal));
    }

    @GetMapping("/report/recapitulation/attendance")
    public void attendance(Model model, @RequestParam Jadwal jadwal) {
        String tahun = jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 9);

        model.addAttribute("tahun", tahun);

        model.addAttribute("jadwal", jadwal);

        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));

        List<Object[]> hasil = presensiMahasiswaDao.bkdAttendance(jadwal);

        model.addAttribute("attendance", hasil);

    }

    @GetMapping("/report/fileberkas")
    public void fileBerkas(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
            Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);
        if (tahunAkademik != null) {
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("fileBerkas",
                    jadwalDao.findByTahunAkademikAndDosenAndStatus(tahunAkademik, dosen, StatusRecord.AKTIF));
        }
    }

    @RequestMapping("/download/")
    public void downloadBerkas(HttpServletRequest request, HttpServletResponse response, @RequestParam Jadwal jadwal,
            @RequestParam String status) {

        if (status == "UAS") {
            Soal soal = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF,
                    StatusApprove.APPROVED, StatusRecord.UAS);
            String fileName = soal.getFileApprove();
            String lokasi = uploadFolder + File.separator + soal.getJadwal().getId();

            Path file = Paths.get(lokasi, fileName);
            if (Files.exists(file)) {
                response.setContentType(
                        "application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
                try {
                    Files.copy(file, response.getOutputStream());
                    response.getOutputStream().flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (status == "UTS") {
            Soal soal = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF,
                    StatusApprove.APPROVED, StatusRecord.UTS);
            String fileName = soal.getFileApprove();
            String lokasi = uploadFolder + File.separator + soal.getJadwal().getId();

            Path file = Paths.get(lokasi, fileName);
            if (Files.exists(file)) {
                response.setContentType(
                        "application/application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
                try {
                    Files.copy(file, response.getOutputStream());
                    response.getOutputStream().flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    @GetMapping("/report/tugas_akhir")
    public void rekapTugasAKhir(@RequestParam Prodi prodi, @RequestParam String angkatan, HttpServletResponse response)
            throws IOException {

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet semprop = workbook.createSheet("SEMPROP");
        XSSFSheet skripsi = workbook.createSheet("SIDANG SKRIPSI");
        XSSFSheet hasil = workbook.createSheet("HASIL AKHIR");

        /* Setting */
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

        /* Header */
        int rowJudulSempro = 0;
        Row judulSempro = semprop.createRow(rowJudulSempro);
        judulSempro.createCell(0).setCellValue("JADWAL SEMINAR PROPOSAL SKRIPSI");
        judulSempro.getCell(0).setCellStyle(styleJudul);
        int rowJudulAngkatan = 1;
        Row judulAngkatan = semprop.createRow(rowJudulAngkatan);
        judulAngkatan.createCell(0).setCellValue("Angkatan " + angkatan);
        judulAngkatan.getCell(0).setCellStyle(styleJudul);
        int rowJudulProdi = 2;
        Row judulProdi = semprop.createRow(rowJudulProdi);
        judulProdi.createCell(0).setCellValue("Program Studi " + prodi.getNamaProdi());
        judulProdi.getCell(0).setCellStyle(styleJudul);

        int rowJudulSkripsi = 0;
        Row judulSkripsi = skripsi.createRow(rowJudulSkripsi);
        judulSkripsi.createCell(0).setCellValue("JADWAL SIDANG SKRIPSI");
        judulSkripsi.getCell(0).setCellStyle(styleJudul);
        int rowJudulSkripsiAngkatan = 1;
        Row judulSkripsiAngkatan = skripsi.createRow(rowJudulSkripsiAngkatan);
        judulSkripsiAngkatan.createCell(0).setCellValue("Angkatan " + angkatan);
        judulSkripsiAngkatan.getCell(0).setCellStyle(styleJudul);
        int rowJudulSkripsiProdi = 2;
        Row judulSkripsiProdi = skripsi.createRow(rowJudulSkripsiProdi);
        judulSkripsiProdi.createCell(0).setCellValue("Program Studi " + prodi.getNamaProdi());
        judulSkripsiProdi.getCell(0).setCellStyle(styleJudul);

        int rowJudulHasilSemprop = 0;
        Row hasilSempro = hasil.createRow(rowJudulHasilSemprop);
        hasilSempro.createCell(0).setCellValue("NILAI SEMPRO (40%)");
        hasilSempro.createCell(5).setCellValue("NILAI SKRIPSI (SEMPRO 40% SIDANG 60%)");
        hasilSempro.getCell(0).setCellStyle(styleJudul);
        hasilSempro.getCell(5).setCellStyle(styleJudul);

        int rowHasilAngkatan = 1;
        Row hasilAngkatan = hasil.createRow(rowHasilAngkatan);
        hasil.setColumnWidth(1, 30 * 300);
        hasil.setColumnWidth(2, 30 * 256);
        hasil.setColumnWidth(6, 25 * 150);
        hasil.setColumnWidth(7, 30 * 256);
        hasilAngkatan.createCell(0).setCellValue("Angkatan " + angkatan);
        hasilAngkatan.createCell(5).setCellValue("Angkatan " + angkatan);
        hasilAngkatan.getCell(0).setCellStyle(styleJudul);
        hasilAngkatan.getCell(5).setCellStyle(styleJudul);

        int rowHasilProdi = 2;
        Row HasilProdi = hasil.createRow(rowHasilProdi);
        HasilProdi.createCell(0).setCellValue("Program Studi " + prodi.getNamaProdi());
        hasil.setColumnWidth(0, 30 * 300);
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
                "Nilai Mutu   ");

        int rowSubJudulSempro = 4;
        int cellNumSempro = 0;
        Row subjudulSempro = semprop.createRow(rowSubJudulSempro);
        for (String header : headerTableSempro) {
            Cell cell = subjudulSempro.createCell(cellNumSempro);
            cell.setCellValue(header);
            cell.setCellStyle(styleSubJudul);
            semprop.autoSizeColumn(cellNumSempro);
            cellNumSempro++;
        }

        int rowSubJudulSkripsi = 4;
        int cellNumSkripis = 0;
        Row subjudulSkripsi = skripsi.createRow(rowSubJudulSkripsi);
        for (String header : headerTableSempro) {
            Cell cell = subjudulSkripsi.createCell(cellNumSkripis);
            cell.setCellValue(header);
            cell.setCellStyle(styleSubJudul);
            skripsi.autoSizeColumn(cellNumSkripis);
            cellNumSkripis++;
        }

        /* End Header */

        List<RekapTugasAkhir> tugasAkhir = seminarDao.rekapTugasAkhir(prodi, angkatan);
        /* Data Semprop */

        int rowSemprop = 5;
        int noDataSempro = 1;
        for (RekapTugasAkhir detailSeminar : tugasAkhir) {
            Row row = semprop.createRow(rowSemprop);
            row.createCell(0).setCellValue(noDataSempro);
            row.createCell(1).setCellValue(detailSeminar.getNim());
            row.createCell(2).setCellValue(detailSeminar.getNama());
            if (detailSeminar.getTanggalSempro() == null) {

            }
            if (detailSeminar.getTanggalSempro() != null) {
                LocalDate tanggalSempro = LocalDate.parse(detailSeminar.getTanggalSempro());
                if (tanggalSempro.getDayOfWeek().getValue() != 7) {
                    if (tanggalSempro.getMonthValue() == 1) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Januari "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 2) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Februari "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 3) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Maret "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 4) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " April "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 5) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Mei "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 6) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Juni "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 7) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Juli "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 8) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Agustus "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 9) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " September "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 10) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Oktober "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 11) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " November "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 12) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSempro.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSempro.getDayOfMonth() + " Desember "
                                        + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }

                } else {
                    if (tanggalSempro.getMonthValue() == 1) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Januari " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 2) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Februari " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 3) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Maret " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 4) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " April " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 5) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Mei " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 6) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Juni " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 7) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Juli " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 8) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Agustus " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 9) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " September " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 10) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Oktober " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 11) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " November " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSempro.getMonthValue() == 12) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSempro.getDayOfMonth() + " Desember " + tanggalSempro.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                }
            }
            row.createCell(4).setCellValue(detailSeminar.getKetuaSempro());
            row.createCell(5).setCellValue(detailSeminar.getPembimbingSempro());
            row.createCell(6).setCellValue(detailSeminar.getPengujiSempro());
            row.createCell(7).setCellValue(detailSeminar.getJamMulai());
            row.createCell(8).setCellValue(detailSeminar.getNilai().doubleValue());
            if (detailSeminar.getNilai().compareTo(new BigDecimal(80)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(85)) < 0) {
                row.createCell(9).setCellValue("A-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(75)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(80)) < 0) {
                row.createCell(9).setCellValue("B+");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(70)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(75)) < 0) {
                row.createCell(9).setCellValue("B");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(65)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(70)) < 0) {
                row.createCell(9).setCellValue("B-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(60)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(65)) < 0) {
                row.createCell(9).setCellValue("C+");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(55)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(60)) < 0) {
                row.createCell(9).setCellValue("C");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(50)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(55)) < 0) {
                row.createCell(9).setCellValue("D");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(0)) >= 0
                    && detailSeminar.getNilai().compareTo(new BigDecimal(50)) < 0) {
                row.createCell(9).setCellValue("E");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSeminar.getNilai().compareTo(new BigDecimal(85)) >= 0) {
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

        /* Data Sidang */
        int rowSkripsi = 5;
        int noDataSkripsi = 1;
        for (RekapTugasAkhir detailSidang : tugasAkhir) {
            Row row = skripsi.createRow(rowSkripsi);
            row.createCell(0).setCellValue(noDataSkripsi);
            row.createCell(1).setCellValue(detailSidang.getNim());
            row.createCell(2).setCellValue(detailSidang.getNama());
            if (detailSidang.getTanggalSidang() == null) {
                row.createCell(3).setCellValue("-");
                row.getCell(3).setCellStyle(styleData);
            }
            if (detailSidang.getTanggalSidang() != null) {
                LocalDate tanggalSidang = LocalDate.parse(detailSidang.getTanggalSidang());
                if (tanggalSidang.getDayOfWeek().getValue() != 7) {
                    if (tanggalSidang.getMonthValue() == 1) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Januari "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 2) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Februari "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 3) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Maret "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 4) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " April "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 5) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Mei "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 6) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Juni "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 7) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Juli "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 8) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Agustus "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 9) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " September "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 10) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Oktober "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 11) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " November "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 12) {
                        row.createCell(3)
                                .setCellValue(hariDao.findById(String.valueOf(tanggalSidang.getDayOfWeek().getValue()))
                                        .get().getNamaHari() + ", " + tanggalSidang.getDayOfMonth() + " Desember "
                                        + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }

                } else {
                    if (tanggalSidang.getMonthValue() == 1) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Januari " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 2) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Februari " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 3) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Maret " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 4) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " April " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 5) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Mei " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 6) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Juni " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 7) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Juli " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 8) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Agustus " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 9) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " September " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 10) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Oktober " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 11) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " November " + tanggalSidang.getYear());
                        row.getCell(3).setCellStyle(styleDataLeft);
                    }
                    if (tanggalSidang.getMonthValue() == 12) {
                        row.createCell(3).setCellValue(
                                "Sabtu , " + tanggalSidang.getDayOfMonth() + " Desember " + tanggalSidang.getYear());
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
                row.createCell(7).setCellValue(" - ");
                row.getCell(7).setCellStyle(styleData);
            }
            row.createCell(8).setCellValue(detailSidang.getNilaiSidang().doubleValue());
            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(80)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(85)) < 0) {
                row.createCell(9).setCellValue("A-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(75)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(80)) < 0) {
                row.createCell(9).setCellValue("B+");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(70)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(75)) < 0) {
                row.createCell(9).setCellValue("B");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(65)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(70)) < 0) {
                row.createCell(9).setCellValue("B-");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(60)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(65)) < 0) {
                row.createCell(9).setCellValue("C+");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(55)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(60)) < 0) {
                row.createCell(9).setCellValue("C");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(50)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(55)) < 0) {
                row.createCell(9).setCellValue("D");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(0)) >= 0
                    && detailSidang.getNilaiSidang().compareTo(new BigDecimal(50)) < 0) {
                row.createCell(9).setCellValue("E");
                row.getCell(9).setCellStyle(styleData);

            }

            if (detailSidang.getNilaiSidang().compareTo(new BigDecimal(85)) >= 0) {
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
                "Huruf Mutu");

        int rowSubHasil = 4;
        int cellNumHasil = 0;
        Row subjudulHasil = hasil.createRow(rowSubHasil);
        for (String header : headerHasil) {
            Cell cell = subjudulHasil.createCell(cellNumHasil);
            cell.setCellValue(header);
            cell.setCellStyle(styleSubJudul);
            hasil.autoSizeColumn(cellNumHasil);
            cellNumHasil++;
        }

        int rowHasil = 5;
        int noDataHasil = 1;
        for (RekapTugasAkhir hasilTugas : tugasAkhir) {
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
            row.createCell(9).setCellValue((hasilTugas.getNilai().multiply(new BigDecimal(0.4)))
                    .add(hasilTugas.getNilaiSidang().multiply(new BigDecimal(0.6))).doubleValue());

            BigDecimal nilaiHasil = (hasilTugas.getNilai().multiply(new BigDecimal(0.4)))
                    .add(hasilTugas.getNilaiSidang().multiply(new BigDecimal(0.6)));
            if (nilaiHasil.compareTo(new BigDecimal(80)) >= 0 && nilaiHasil.compareTo(new BigDecimal(85)) < 0) {
                row.createCell(10).setCellValue("A-");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(75)) >= 0 && nilaiHasil.compareTo(new BigDecimal(80)) < 0) {
                row.createCell(10).setCellValue("B+");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(70)) >= 0 && nilaiHasil.compareTo(new BigDecimal(75)) < 0) {
                row.createCell(10).setCellValue("B");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(65)) >= 0 && nilaiHasil.compareTo(new BigDecimal(70)) < 0) {
                row.createCell(10).setCellValue("B-");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(60)) >= 0 && nilaiHasil.compareTo(new BigDecimal(65)) < 0) {
                row.createCell(10).setCellValue("C+");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(55)) >= 0 && nilaiHasil.compareTo(new BigDecimal(60)) < 0) {
                row.createCell(10).setCellValue("C");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(50)) >= 0 && nilaiHasil.compareTo(new BigDecimal(55)) < 0) {
                row.createCell(10).setCellValue("D");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(0)) >= 0 && nilaiHasil.compareTo(new BigDecimal(50)) < 0) {
                row.createCell(10).setCellValue("E");
                row.getCell(10).setCellStyle(styleData);

            }

            if (nilaiHasil.compareTo(new BigDecimal(85)) >= 0) {
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

        String namaFile = prodi.getKodeProdi() + ' ' + angkatan + " - REKAP TUGAS AKHIR";
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + namaFile + extentionX + "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/report/pembinaanMatrikulasi")
    public void pembinaanMatrikulasi(Model model, @RequestParam(required = false) String nim,
            @RequestParam(required = false) TahunAkademik tahunAkademik, Authentication authentication) {

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        List<JenisPembinaanMatrikulasi> jenisPembinaanMatrikulasi = pembinaanDao.findByStatus(StatusRecord.AKTIF);
        List<JenisPembinaanMatrikulasiDetail> jenisPembinaanMatrikulasiDetailList = pembinaanDetailDao
                .findByStatusAndTahunAkademikAndMahasiswa(StatusRecord.AKTIF, tahunAkademik, mhs);
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mhs, StatusRecord.AKTIF);

        model.addAttribute("currentUser", user);
        model.addAttribute("kelas", kelasMahasiswa);
        model.addAttribute("tahunAkademikList",
                tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("nim", nim);
        model.addAttribute("mhs", mhs);
        model.addAttribute("thn", tahunAkademik);
        model.addAttribute("pembinaanList", jenisPembinaanMatrikulasi);
        model.addAttribute("pembinaanDetail", jenisPembinaanMatrikulasiDetailList);
        for (JenisPembinaanMatrikulasi idJenis : jenisPembinaanMatrikulasi) {
            for (JenisPembinaanMatrikulasiDetail idJenisDetail : jenisPembinaanMatrikulasiDetailList) {
                if (idJenis.getId().equals(idJenisDetail.getJenisPembinaanMatrikulasi())) {
                    model.addAttribute("pembimbingDetail2", pembinaanDetailDao.cariJenis(idJenis.getId()));
                    System.out.println("KATEGORI = " + pembinaanDetailDao.cariJenis(idJenis.getId()));
                }

            }

        }

    }

    @PostMapping("/pembinaanDetail/detail")
    public String saveDetail(@Valid JenisPembinaanMatrikulasiDetail jenisPembinaanMatrikulasiDetail,
            @RequestParam(required = false) JenisPembinaanMatrikulasi jenisPembinaanMatrikulasi,
            @RequestParam(required = false) Mahasiswa mahasiswa,
            @RequestParam(required = false) TahunAkademik tahunAkademik, Authentication authentication) {

        User user = currentUserService.currentUser(authentication);

        jenisPembinaanMatrikulasiDetail.setJenisPembinaanMatrikulasi(jenisPembinaanMatrikulasi);
        jenisPembinaanMatrikulasiDetail.setMahasiswa(mahasiswa);
        jenisPembinaanMatrikulasiDetail.setTahunAkademik(tahunAkademik);
        jenisPembinaanMatrikulasiDetail.setStatus(StatusRecord.AKTIF);
        jenisPembinaanMatrikulasiDetail.setUser(user);
        pembinaanDetailDao.save(jenisPembinaanMatrikulasiDetail);

        return "redirect:/report/pembinaanMatrikulasi?nim=" + mahasiswa.getNim() + "&tahunAkademik="
                + tahunAkademik.getId();
    }

    @PostMapping("/pembinaanDetail/delete")
    public String deleteSubPertanyaan(@RequestParam(required = false) JenisPembinaanMatrikulasiDetail id,
            @RequestParam(required = false) Mahasiswa mahasiswa) {
        id.setStatus(StatusRecord.HAPUS);
        pembinaanDetailDao.save(id);
        return "redirect:/report/pembinaanMatrikulasi?nim=" + mahasiswa.getNim();
    }

    @GetMapping("/report/mahasiswaPembinaanList")
    public String mahasiswaPembinaanList(Model model,
            @PageableDefault(size = 20) Pageable page, @RequestParam(required = false) Prodi prodi,
            @RequestParam(required = false) String angkatan, Authentication authentication) {

        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("angkatan", mahasiswaDao.cariAngkatan());
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        model.addAttribute("selectedAngkatan", angkatan);
        model.addAttribute("selectedProdi", prodi);
        model.addAttribute("dosen", dosen.getId());
        System.out.println("DOSENNYA : " + dosen.getId());

        if (prodi == null) {

            // model.addAttribute("listMahasiswaDosenWali",
            // mahasiswaDosenWaliDao.listMahasiswaDosenWali(page));

        } else {

            model.addAttribute("listMahasiswaDosenWali",
                    mahasiswaDosenWaliDao.listMahasiswaDosenWaliProdi(prodi.getId(), page));
            model.addAttribute("selectedMahasiswa",
                    mahasiswaDosenWaliDao.listMahasiswaByDosen(angkatan, prodi, dosen.getId()));
        }

        return "report/mahasiswaPembinaanList";

    }

    // MBKM

    @GetMapping("/report/mbkm")
    public void reportMBKM(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik) {

        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (tahunAkademik == null) {
            List<PendaftaranMbkm> mbkm = pendaftaranMbkmDao
                    .findByStatusAndTahunAkademikOrderByStatusApprovalDesc(StatusRecord.AKTIF, tahun);
            model.addAttribute("listMbkm", mbkm);
            model.addAttribute("selectedTahun", tahun);
        } else {
            List<PendaftaranMbkm> mbkm = pendaftaranMbkmDao
                    .findByStatusAndTahunAkademikOrderByStatusApprovalDesc(StatusRecord.AKTIF, tahunAkademik);
            model.addAttribute("listMbkm", mbkm);
            model.addAttribute("selectedTahun", tahunAkademik);
        }

    }

    // Laporan Ujian
    @GetMapping("/report/ujian")
    public String reportUjian(Model model, @RequestParam(required = false) TahunAkademik ta,
            @RequestParam(required = false) Prodi prodi,
            @RequestParam(required = false) String ujian) {

        model.addAttribute("selectTahunAkademik", ta);
        model.addAttribute("selectedProdi", prodi);
        model.addAttribute("ujian", ujian);
        if (ta != null) {
            model.addAttribute("listReportUjian", presensiDosenDao.reportUjian(ta, prodi, ujian));
        }
        return "report/recapitulation/ujian";
    }

    @GetMapping("/report/ujian/excel")
    public void reportUjianExcel(@RequestParam TahunAkademik tahunAkademik,
            @RequestParam Prodi prodi,
            @RequestParam String ujian,
            HttpServletResponse response) throws IOException {

        String[] column = { "No", "Dosen", "Kelas", "Matakuliah", "Detail Pertemuan" };

        List<ReportUjianDto> reportUjianDtoList = presensiDosenDao.reportUjian(tahunAkademik, prodi, ujian);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data-Report-Ujian");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < column.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (ReportUjianDto data : reportUjianDtoList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(data.getDosen());
            row.createCell(2).setCellValue(data.getKelas());
            row.createCell(3).setCellValue(data.getMatakuliah());
            if (data.getDetailPertemuan() == null) {
                row.createCell(4).setCellValue("-");
            } else {
                row.createCell(4).setCellValue(data.getDetailPertemuan());
            }
        }

        for (int i = 0; i < column.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment; filename=Data-Report-Ujian-" + LocalDate.now() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // REKAP PRESENSI DOSEN

    @GetMapping("/report/recapitulation/presensiDosen/list")
    public void listPresensiDosen(Model model, @RequestParam(required = false) String dari,
            @RequestParam(required = false) String sampai) {
        if (dari != null && sampai != null) {
            model.addAttribute("tglDari", dari);
            model.addAttribute("tglSampai", sampai);

            model.addAttribute("listPresensi",
                    presensiDosenDao.rekapPresensiDosen(LocalDate.parse(dari), LocalDate.parse(sampai)));
        } else {
            LocalDate now = LocalDate.now(ZoneId.systemDefault());
            model.addAttribute("tglDari", now.with(TemporalAdjusters.firstDayOfMonth()));
            model.addAttribute("tglSampai", now.with(TemporalAdjusters.lastDayOfMonth()));
            model.addAttribute("listPresensi", presensiDosenDao.rekapPresensiDosen(
                    now.with(TemporalAdjusters.firstDayOfMonth()), now.with(TemporalAdjusters.lastDayOfMonth())));
        }
    }

    @GetMapping("/report/recapitulation/presensiDosen/detail")
    public void detailPresensiDosen(Model model, @RequestParam Dosen dosen, @RequestParam String dari,
            @RequestParam String sampai) {
        model.addAttribute("dosen", dosen);
        model.addAttribute("tglDari", dari);
        model.addAttribute("tglSampai", sampai);
        model.addAttribute("detail",
                presensiDosenDao.detailPresensiDosen(dosen.getId(), LocalDate.parse(dari), LocalDate.parse(sampai)));
    }

}
