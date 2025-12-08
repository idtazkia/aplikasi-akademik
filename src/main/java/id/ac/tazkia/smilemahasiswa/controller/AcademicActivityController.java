package id.ac.tazkia.smilemahasiswa.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.smilemahasiswa.dao.DosenDao;
import id.ac.tazkia.smilemahasiswa.dao.EdomQuestionOldDao;
import id.ac.tazkia.smilemahasiswa.dao.GedungDao;
import id.ac.tazkia.smilemahasiswa.dao.GradeDao;
import id.ac.tazkia.smilemahasiswa.dao.HariDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.JadwalUjianDao;
import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.dao.KelasDao;
import id.ac.tazkia.smilemahasiswa.dao.KonsentrasiDao;
import id.ac.tazkia.smilemahasiswa.dao.KonversiDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.KurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.MataKuliahSetaraDao;
import id.ac.tazkia.smilemahasiswa.dao.MatakuliahDao;
import id.ac.tazkia.smilemahasiswa.dao.MatakuliahKurikulumDao;
import id.ac.tazkia.smilemahasiswa.dao.PrasyaratDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.ProgramDao;
import id.ac.tazkia.smilemahasiswa.dao.RuanganDao;
import id.ac.tazkia.smilemahasiswa.dao.SeminarDao;
import id.ac.tazkia.smilemahasiswa.dao.SesiDao;
import id.ac.tazkia.smilemahasiswa.dao.SesiKuliahDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunProdiDao;
import id.ac.tazkia.smilemahasiswa.dto.BaseResponse;
import id.ac.tazkia.smilemahasiswa.dto.BaseResponseDto;
import id.ac.tazkia.smilemahasiswa.dto.MatkulKonversiDto;
import id.ac.tazkia.smilemahasiswa.dto.konversi.RequestKonversiNilai;
import id.ac.tazkia.smilemahasiswa.dto.ploting.DataPlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.ploting.ValidasiSesi;
import id.ac.tazkia.smilemahasiswa.dto.schedule.DataPloting;
import id.ac.tazkia.smilemahasiswa.dto.schedule.DetailPlotinng;
import id.ac.tazkia.smilemahasiswa.dto.schedule.JadwalRequest;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ListJadwalUjian;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ListPlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.RecordPloting;
import id.ac.tazkia.smilemahasiswa.dto.schedule.RequestJadwal;
import id.ac.tazkia.smilemahasiswa.dto.schedule.RoomDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.SesiDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.TableJadwalUjian;
import id.ac.tazkia.smilemahasiswa.dto.select2.CourseDto;
import id.ac.tazkia.smilemahasiswa.dto.select2.SelectDosen;
import id.ac.tazkia.smilemahasiswa.entity.Akses;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Gedung;
import id.ac.tazkia.smilemahasiswa.entity.Grade;
import id.ac.tazkia.smilemahasiswa.entity.Hari;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.JadwalDosen;
import id.ac.tazkia.smilemahasiswa.entity.JadwalUjian;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.Kelas;
import id.ac.tazkia.smilemahasiswa.entity.Konsentrasi;
import id.ac.tazkia.smilemahasiswa.entity.Konversi;
import id.ac.tazkia.smilemahasiswa.entity.Krs;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Matakuliah;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahSetara;
import id.ac.tazkia.smilemahasiswa.entity.Prasyarat;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.Ruangan;
import id.ac.tazkia.smilemahasiswa.entity.Sesi;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusJadwalDosen;
import id.ac.tazkia.smilemahasiswa.entity.StatusPresensi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.JadwalService;
import id.ac.tazkia.smilemahasiswa.service.KrsService;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaService;
import id.ac.tazkia.smilemahasiswa.service.PresensiService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class AcademicActivityController {

    @Value("${upload.silabus}")
    private String uploadFolder;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private KurikulumDao kurikulumDao;

    @Autowired
    private MatakuliahDao matakuliahDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private PrasyaratDao prasyaratDao;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private JadwalUjianDao jadwalUjianDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private SesiDao sesiDao;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private PresensiService presensiService;

    @Autowired
    private GedungDao gedungDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KonversiDao konversiDao;

    @Autowired
    private MataKuliahSetaraDao mataKuliahSetaraDao;

    @Autowired
    private EdomQuestionOldDao edomQuestionOldDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private SesiKuliahDao sesiKuliahDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private SeminarDao seminarDao;

    @Autowired
    private MahasiswaService mahasiswaService;

    @Value("classpath:/sample/jadwalUjian.xlsx")
    private Resource getContohExcelJadwalUjian;
    @Autowired
    private JadwalService jadwalService;
    @Autowired
    private KrsService krsService;

    // Attribute
    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("konsentrasi")
    public Iterable<Konsentrasi> konsentrasis() {
        return konsentrasiDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("ruangan")
    public Iterable<Ruangan> ruangan() {
        return ruanganDao.findByStatus(StatusRecord.AKTIF);
    }

    @ModelAttribute("hari")
    public Iterable<Hari> hari() {
        return hariDao.findAll();
    }

    // API
    @GetMapping("/api/prodi")
    @ResponseBody
    public Page<Prodi> cariProdi(@RequestParam(required = false) String search, Pageable page) {
        if (!StringUtils.hasText(search)) {
            return prodiDao.findByStatus(StatusRecord.AKTIF, page);
        }
        return prodiDao.findByStatusAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(StatusRecord.AKTIF, search, page);

    }

    @GetMapping("/getdata/ploting")
    @ResponseBody
    public RecordPloting getDataPloting(@RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) List<String> kelas) {

        RecordPloting response = new RecordPloting();
        if (tahunAkademik.getJenis() == StatusRecord.GANJIL) {
            List<ListPlotingDto> plotingGanjil = matakuliahKurikulumDao.plotingDosenGanjil(tahunAkademik, kelas);
            List<ListPlotingDto> addGenap = matakuliahKurikulumDao.addPlotingDosenGenap(tahunAkademik, kelas);
            response.setTotal(plotingGanjil.size());
            response.setTotalNotFiltered(plotingGanjil.size());
            plotingGanjil.addAll(addGenap);
            response.setRows(plotingGanjil);
        }
        if (tahunAkademik.getJenis() == StatusRecord.GENAP) {
            List<ListPlotingDto> plotinggenap = matakuliahKurikulumDao.plotingDosenGenap(tahunAkademik, kelas);
            List<ListPlotingDto> addGanjil = matakuliahKurikulumDao.addPlotingDosenGanjil(tahunAkademik, kelas);
            response.setTotal(plotinggenap.size());
            response.setTotalNotFiltered(plotinggenap.size());
            plotinggenap.addAll(addGanjil);
            response.setRows(plotinggenap);
        }
        return response;
    }

    @GetMapping("/getdata-ploting")
    @ResponseBody
    public List<ListPlotingDto> getDataPloting1(@RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) List<String> kelas) {

        if (tahunAkademik.getJenis() == StatusRecord.GANJIL) {
            List<ListPlotingDto> plotingGanjil = matakuliahKurikulumDao.plotingDosenGanjil(tahunAkademik, kelas);
            return plotingGanjil;

        } else {
            List<ListPlotingDto> plotinggenap = matakuliahKurikulumDao.plotingDosenGenap(tahunAkademik, kelas);
            return plotinggenap;

        }
    }

    @GetMapping("/select2/lecture")
    @ResponseBody
    public List<SelectDosen> cariDosen(@RequestParam String search, TahunAkademik tahun) {

        return dosenDao.searchPlotingDosen(tahun, search);
    }

    @GetMapping("/lectures")
    @ResponseBody
    public List<SelectDosen> cariDosen(@RequestParam String search) {

        return dosenDao.searchDosen(search);
    }

    @GetMapping("/select2/team/{id}")
    @ResponseBody
    public List<SelectDosen> cariTeamTeaching(@PathVariable String id, @RequestParam String search,
            TahunAkademik tahun) {

        return dosenDao.searchPlotingTeam(tahun, search, id);
    }

    @GetMapping("/detail/lecture")
    @ResponseBody
    public SelectDosen detailDOsen(@RequestParam String search, @RequestParam TahunAkademik tahun) {

        return dosenDao.detailDosen(tahun, dosenDao.findById(search).get());
    }

    @GetMapping("/detail-ploting")
    @ResponseBody
    public DetailPlotinng detailPloting(@RequestParam String id) {
        Optional<Jadwal> jadwal = jadwalDao.findById(id);
        if (jadwal.isEmpty()) {
            DetailPlotinng detailPlotinng = new DetailPlotinng();
            detailPlotinng.setId("-");
            detailPlotinng.setKodeMatakuliah("-");
            detailPlotinng.setMatakuliah("-");
            detailPlotinng.setCourse("-");
            detailPlotinng.setKelas("-");
            detailPlotinng.setSks(0);
            detailPlotinng.setDosen("-");
            detailPlotinng.setIdDosen("-");
            detailPlotinng.setIdKelas("-");
            detailPlotinng.setValidasi(id);
            return detailPlotinng;
        } else {
            DetailPlotinng detailPlotinng = new DetailPlotinng();
            detailPlotinng.setId(jadwal.get().getId());
            detailPlotinng.setKodeMatakuliah(jadwal.get().getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
            detailPlotinng.setMatakuliah(jadwal.get().getMatakuliahKurikulum().getId());
            detailPlotinng.setCourse(jadwal.get().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
            detailPlotinng.setKelas(jadwal.get().getKelas().getNamaKelas());
            detailPlotinng.setSks(jadwal.get().getMatakuliahKurikulum().getJumlahSks());
            detailPlotinng.setIdDosen(jadwal.get().getDosen().getId());
            detailPlotinng.setDosen(jadwal.get().getDosen().getKaryawan().getNamaKaryawan());
            detailPlotinng.setIdKelas(jadwal.get().getKelas().getId());
            detailPlotinng.setValidasi(jadwal.get().getKelas().getNamaKelas() + "-"
                    + jadwal.get().getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());
            return detailPlotinng;
        }

    }

    @GetMapping("/delete/ploting")
    @ResponseBody
    public Jadwal detailDOsen(@RequestParam Jadwal jadwal) {
        jadwal.setStatus(StatusRecord.HAPUS);
        jadwalDao.save(jadwal);
        return jadwal;
    }

    @GetMapping("/detail/matkur")
    @ResponseBody
    public MatakuliahKurikulum detailMatkur(@RequestParam String search) {

        return matakuliahKurikulumDao.findById(search).get();
    }

    @GetMapping("/search/ploting")
    @ResponseBody
    public List<ListPlotingDto> searchPloting(@RequestParam TahunAkademik tahunAkademik,
            @RequestParam(required = false) List<String> kelas, @RequestParam(required = false) String search) {

        if (tahunAkademik.getJenis() == StatusRecord.GANJIL) {
            return matakuliahKurikulumDao.searchPlotingGenap(tahunAkademik, kelas, search);
        } else if (tahunAkademik.getJenis() == StatusRecord.GENAP) {
            return matakuliahKurikulumDao.searchPlotingGanjil(tahunAkademik, kelas, search);
        } else {
            return matakuliahKurikulumDao.searchPlotingPendek(tahunAkademik, kelas, search);
        }

    }

    @GetMapping("/select2/course")
    @ResponseBody
    public List<CourseDto> cariMatkul(@RequestParam String search) {

        return matakuliahDao.cariMatakuliah(search);
    }

    @GetMapping("/course/detail")
    @ResponseBody
    public Matakuliah detailMatkul(@RequestParam String search) {

        return matakuliahDao.findById(search).get();
    }

    @GetMapping("/api/prodikurikulum")
    @ResponseBody
    public List<Kurikulum> prodiList(@RequestParam(required = false) Prodi prodi) {
        List<Kurikulum> kurikulum = kurikulumDao.findByProdiAndStatusNotIn(prodi, Arrays.asList(StatusRecord.HAPUS));

        return kurikulum;

    }

    @GetMapping("/api/matakuliahkurikulum")
    @ResponseBody
    public List<MatakuliahKurikulum> matakuliahKurikulumList(@RequestParam(required = false) Prodi prodi,
            @RequestParam(required = false) Kurikulum kurikulum) {
        List<MatakuliahKurikulum> matakuliahKurikulumList = matakuliahKurikulumDao
                .findByStatusNotInAndKurikulumAndKurikulumProdiAndSemesterIn(Arrays.asList(StatusRecord.HAPUS),
                        kurikulum, prodi, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

        return matakuliahKurikulumList;

    }

    @GetMapping("/api/matakuliah")
    @ResponseBody
    public Page<Matakuliah> cariData(@RequestParam(required = false) String search, Pageable page) {
        if (!StringUtils.hasText(search)) {
            return matakuliahDao.findAll(page);
        }
        return matakuliahDao.findByNamaMatakuliahContainingIgnoreCase(search, page);

    }

    @GetMapping({ "/api/kurikulum" })
    @ResponseBody
    public List<Kurikulum> findByProdiAndName(@RequestParam(required = false) String namaProdi,
            @RequestParam String search, Pageable page) {
        if (!StringUtils.hasText(search)) {
            return null;
        }
        return kurikulumDao.findByStatusNotInAndProdiAndNamaKurikulumContainingIgnoreCaseOrderByNamaKurikulum(
                Arrays.asList(StatusRecord.HAPUS), prodiDao.findById(namaProdi).get(), search);
    }

    @GetMapping("/api/sesi")
    @ResponseBody
    public List<Sesi> cariSesi(@RequestParam(required = false) String idHari,
            @RequestParam(required = false) String kelas,
            @RequestParam(required = false) String idRuangan, @RequestParam(required = false) Integer sks,
            @RequestParam(required = false) String search, @RequestParam(required = false) String dosen) {
        Ruangan ruangan = ruanganDao.findById(idRuangan).get();
        Kelas k = kelasDao.findById(kelas).get();
        Hari hari = hariDao.findById(idHari).get();
        Dosen d = dosenDao.findById(dosen).get();
        List<SesiDto> jadwal = jadwalDao.cariSesi(tahunAkademikDao.findByStatus(StatusRecord.AKTIF), hari, ruangan, k,
                d);
        List<String> stringList = new ArrayList<>();
        for (SesiDto s : jadwal) {
            stringList.add(s.getSesi());
        }

        if (jadwal == null || jadwal.isEmpty()) {
            return sesiDao.findBySks(sks);
        } else {
            return sesiDao.findBySesiInAndSks(stringList, sks);
        }
    }

    @GetMapping("/api/waktu")
    @ResponseBody
    public Sesi sesi(@RequestParam(required = false) String id) {

        return sesiDao.findById(id).get();

    }

    @GetMapping("/api/tahun")
    @ResponseBody
    public List<MatkulKonversiDto> tahun(@RequestParam(required = false) String idTahun,
            @RequestParam(required = false) String idProdi) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahun).get();
        Prodi p = prodiDao.findById(idProdi).get();
        List<MatkulKonversiDto> jadwal = jadwalDao.cariMatkulKonversi(tahunAkademik);

        return jadwal;
    }

    @GetMapping("/api/jadwal")
    @ResponseBody
    public KrsDetail krsDetail(@RequestParam(required = false) String id,
            @RequestParam(required = false) String idTahun,
            @RequestParam(required = false) String idMahasiswa) {

        Jadwal jadwal = jadwalDao.findById(id).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahun).get();
        Mahasiswa mhs = mahasiswaDao.findByNim(idMahasiswa);
        KrsDetail krsDetail = krsDetailDao.findByJadwalAndTahunAkademikAndMahasiswaAndStatus(jadwal, tahunAkademik, mhs,
                StatusRecord.AKTIF);

        return krsDetail;

    }

    // Curriculum

    @GetMapping("/academic/curriculum/list")
    public void curriculumList(Model model, @RequestParam(required = false) Prodi prodi,
            @PageableDefault(direction = Sort.Direction.DESC, sort = "tahunKurikulum") Pageable page) {
        List<StatusRecord> statusRecords = new ArrayList<>();
        statusRecords.add(StatusRecord.AKTIF);
        statusRecords.add(StatusRecord.NONAKTIF);
        model.addAttribute("prodi", prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));

        if (prodi != null) {
            model.addAttribute("selected", prodi);
            model.addAttribute("kurikulum", kurikulumDao.findByStatusInAndProdi(statusRecords, prodi));
        }
    }

    @Transactional
    @PostMapping("/academic/curriculum/aktif")
    public String aktifKurikulum(@RequestParam Kurikulum kurikulum) {
        kurikulumDao.nonaktifKurikulum(kurikulum.getProdi());

        kurikulum.setStatus(StatusRecord.AKTIF);
        kurikulumDao.save(kurikulum);

        return "redirect:list?prodi=" + kurikulum.getProdi().getId();
    }

    @GetMapping("/academic/curriculum/form")
    public void curriculumForm(Model model, @RequestParam(required = false, name = "id") Kurikulum kurikulum) {
        model.addAttribute("prodi", prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("kurikulum", new Kurikulum());

        if (kurikulum != null) {
            model.addAttribute("kurikulum", kurikulum);
        }
    }

    @PostMapping("/academic/curriculum/form")
    public String prosesCurriculum(@Valid Kurikulum kurikulum) {
        kurikulum.setJumlahSesi(2);
        kurikulumDao.save(kurikulum);

        return "redirect:list?prodi=" + kurikulum.getProdi().getId();
    }

    @PostMapping("/academic/curriculum/delete")
    public String deleteCurriculum(@Valid Kurikulum kurikulum) {
        kurikulum.setStatus(StatusRecord.HAPUS);
        kurikulumDao.save(kurikulum);

        return "redirect:list?prodi=" + kurikulum.getProdi().getId();
    }

    // Courses

    @GetMapping("/academic/courses/list")
    public void listCourses(Model model, @RequestParam(required = false) String search, Pageable pageable) {
        if (!StringUtils.isEmpty(search)) {
            model.addAttribute("search", search);
            model.addAttribute("courses",
                    matakuliahDao.findByNamaMatakuliahContainingIgnoreCaseOrNamaMatakuliahEnglishContainingIgnoreCase(
                            search, search, pageable));
        } else {
            model.addAttribute("courses", matakuliahDao
                    .findByStatusNotInOrderByNamaMatakuliahAsc(Arrays.asList(StatusRecord.HAPUS), pageable));
        }
    }

    @GetMapping("/academic/courses/form")
    public void formCourses(Model model, @RequestParam(required = false, name = "id") Matakuliah matakuliah,
            Authentication authentication) {
        User user = currentUserService.currentUser(authentication);
        model.addAttribute("user", user);
        model.addAttribute("courses", new Matakuliah());

        if (matakuliah != null) {
            model.addAttribute("courses", matakuliah);
            Integer usedMatkul = matakuliahKurikulumDao.countByMatakuliahAndStatus(matakuliah, StatusRecord.AKTIF);
            if (usedMatkul != 0) {
                model.addAttribute("used",
                        "Tidak bisa di edit karena matakuliah sudah di gunakan di matakuliah kurikulum!");
            }
        }
    }

    @GetMapping("/academic/courses/detail")
    public void detailCourses(Model model, @RequestParam(name = "id") Matakuliah matakuliah) {
        if (matakuliah != null) {
            model.addAttribute("matakuliah", matakuliah);
            model.addAttribute("listMatakuliah",
                    mataKuliahSetaraDao.findByStatusAndMatakuliahOrStatusAndMatakuliahSetara(StatusRecord.AKTIF,
                            matakuliah, StatusRecord.AKTIF, matakuliah));
        }
    }

    @PostMapping("/academic/courses/setara/delete")
    public String deleteSetaraByMatakuliah(@RequestParam MatakuliahSetara matakuliahSetara,
            @RequestParam Matakuliah matakuliah, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);

        matakuliahSetara.setStatus(StatusRecord.HAPUS);
        matakuliahSetara.setTanggalDelete(LocalDateTime.now());
        matakuliahSetara.setUserDelete(user.getUsername());
        mataKuliahSetaraDao.save(matakuliahSetara);

        return "redirect:../detail?id=" + matakuliah.getId();
    }

    @PostMapping("/academic/courses/form")
    public String prosesCourses(@Valid Matakuliah matakuliah) {

        matakuliahDao.save(matakuliah);

        return "redirect:list?";
    }

    @PostMapping("/academic/courses/delete")
    public String deleteCourses(@Valid Matakuliah matakuliah) {
        matakuliah.setStatus(StatusRecord.HAPUS);
        matakuliahDao.save(matakuliah);

        return "redirect:list?";
    }

    // Curriculum Courses

    @GetMapping("/academic/curriculumCourses/list")
    public void listCurriculumCourses(Model model, @RequestParam(required = false) Prodi prodi,
            @RequestParam(required = false) Kurikulum kurikulum) {
        model.addAttribute("listProdi", prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("listKurikulum", kurikulumDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));

        if (prodi != null && kurikulum != null) {
            List<Kurikulum> k = kurikulumDao.findByProdiAndStatusNotInAndIdNotIn(prodi,
                    Arrays.asList(StatusRecord.HAPUS), Arrays.asList(kurikulum.getId()));
            model.addAttribute("kurikulum", k);
            model.addAttribute("selected", prodi);
            model.addAttribute("kurikulumSelected", kurikulum);
            model.addAttribute("satu", matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                    Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 1));
            model.addAttribute("dua", matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                    Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 2));
            model.addAttribute("tiga", matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                    Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 3));
            model.addAttribute("empat",
                    matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                            Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 4));
            model.addAttribute("lima", matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                    Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 5));
            model.addAttribute("enam", matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                    Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 6));
            model.addAttribute("tujuh",
                    matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                            Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 7));
            model.addAttribute("delapan",
                    matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                            Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 8));
            model.addAttribute("sembilan",
                    matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(
                            Arrays.asList(StatusRecord.HAPUS), kurikulum, prodi, 9));

            // Get available semesters for sync modal
            List<Integer> availableSemesters = matakuliahKurikulumDao.findDistinctSemesterByKurikulumAndStatus(
                    kurikulum, StatusRecord.AKTIF);
            model.addAttribute("availableSemesters", availableSemesters);
        }
    }

    @GetMapping("/academic/curriculumCourses/form")
    public void formCurriculumCourses(@RequestParam(required = false) String prodi,
            @RequestParam(required = false) String kurikulum,
            @RequestParam(required = false) String id, Model model) {

        model.addAttribute("matakuliah", new MatakuliahKurikulum());

        if (id != null) {
            model.addAttribute("matakuliah", matakuliahKurikulumDao.findById(id).get());
            model.addAttribute("prodi", prodiDao.findById(prodi).get());
            model.addAttribute("kurikulum", kurikulumDao.findById(kurikulum).get());
        } else {
            model.addAttribute("prodi", prodiDao.findById(prodi).get());
            model.addAttribute("kurikulum", kurikulumDao.findById(kurikulum).get());
        }
    }

    @PostMapping("/academic/curriculumCourses/form")
    public String prosesCurriculumCourses(@ModelAttribute @Valid MatakuliahKurikulum matakuliahKurikulum,
            MultipartFile file) throws Exception {
        String namaFile = file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();

        if (file != null || file.isEmpty()) {
            String extension = "";

            int i = namaAsli.lastIndexOf('.');
            int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

            if (i > p) {
                extension = namaAsli.substring(i + 1);
            }

            String idFile = UUID.randomUUID().toString();
            String lokasiUpload = uploadFolder;
            new File(lokasiUpload).mkdirs();
            File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
            file.transferTo(tujuan);

            matakuliahKurikulum.setSilabus(idFile);
        }

        matakuliahKurikulumDao.save(matakuliahKurikulum);

        return "redirect:list?prodi=" + matakuliahKurikulum.getKurikulum().getProdi().getId() + "&kurikulum="
                + matakuliahKurikulum.getKurikulum().getId();
    }

    @PostMapping("/academic/curriculumCourses/delete")
    public String deleteCurriculumCourse(
            @RequestParam(value = "id", name = "id") MatakuliahKurikulum matakuliahKurikulum) {
        Long jumlahMatakuliah = krsDetailDao.hitungMatakuliahKurikulum(matakuliahKurikulum);
        if (jumlahMatakuliah == 0) {
            matakuliahKurikulum.setStatus(StatusRecord.HAPUS);
            matakuliahKurikulumDao.save(matakuliahKurikulum);
        }

        return "redirect:list?prodi=" + matakuliahKurikulum.getKurikulum().getProdi().getId() + "&kurikulum="
                + matakuliahKurikulum.getKurikulum().getId();
    }

    @PostMapping("/academic/curriculumCourses/copy")
    public String copyMatakuliah(@RequestParam(value = "id", name = "id") Kurikulum selectedKurikulum,
            @RequestParam(value = "prodi", name = "prodi") Prodi prodi,
            @RequestParam(value = "kurikulum", name = "kurikulum") Kurikulum kurikulum) {
        List<MatakuliahKurikulum> mk = matakuliahKurikulumDao
                .findByStatusAndKurikulumAndSemesterNotNull(StatusRecord.AKTIF, selectedKurikulum);
        System.out.println(mk.size());
        for (MatakuliahKurikulum k : mk) {
            Set<Program> programs = new HashSet<>();
            MatakuliahKurikulum mkKurikulum = new MatakuliahKurikulum();
            mkKurikulum.setSyaratTugasAkhir(k.getSyaratTugasAkhir());
            mkKurikulum.setNomorUrut(k.getNomorUrut());
            mkKurikulum.setJumlahSks(k.getJumlahSks());
            mkKurikulum.setStatus(k.getStatus());
            mkKurikulum.setMatakuliah(k.getMatakuliah());
            mkKurikulum.setResponsi(k.getResponsi());
            mkKurikulum.setSilabus(k.getSilabus());
            mkKurikulum.setIpkMinimal(k.getIpkMinimal());
            mkKurikulum.setKonsentrasi(k.getKonsentrasi());
            mkKurikulum.setMatakuliahKurikulumSemester(k.getMatakuliahKurikulumSemester());
            mkKurikulum.setSemester(k.getSemester());
            mkKurikulum.setSksMinimal(k.getSksMinimal());
            mkKurikulum.setWajib(k.getWajib());
            mkKurikulum.setKurikulum(kurikulum);
            for (Program program : k.getPrograms()) {
                programs.add(program);
            }
            mkKurikulum.setPrograms(programs);
            matakuliahKurikulumDao.save(mkKurikulum);
        }
        return "redirect:list?prodi=" + prodi.getId() + "&kurikulum=" + kurikulum.getId();
    }

    @GetMapping("/academic/curriculumCourses/prasyarat")
    public void formPrasyarat(Model model,
            @RequestParam(name = "id", value = "id") MatakuliahKurikulum matakuliahKurikulum,
            @RequestParam(required = false) String pras) {
        model.addAttribute("pras", new Prasyarat());
        model.addAttribute("matakuliahKurikulum", matakuliahKurikulum);
        List<MatakuliahKurikulum> mk = matakuliahKurikulumDao
                .findByStatusAndKurikulumAndSemesterNotNull(StatusRecord.AKTIF, matakuliahKurikulum.getKurikulum());
        model.addAttribute("listPrasyarat",
                prasyaratDao.findByMatakuliahKurikulumAndStatus(matakuliahKurikulum, StatusRecord.AKTIF));
        model.addAttribute("matakuliahPrasyarat", mk);
        model.addAttribute("grade", gradeDao.findByStatus(StatusRecord.AKTIF));

        if (pras != null && !pras.isEmpty()) {
            Prasyarat prasyarat = prasyaratDao.findById(pras).get();
            if (pras != null) {
                model.addAttribute("pras", prasyarat);
            }
        }
    }

    @PostMapping("/academic/curriculumCourses/prasyarat")
    public String createPrerequisite(@Valid Prasyarat prasyarat) {

        prasyarat.setMatakuliah(prasyarat.getMatakuliahKurikulum().getMatakuliah());
        prasyarat.setMatakuliahPras(prasyarat.getMatakuliahKurikulumPras().getMatakuliah());
        prasyaratDao.save(prasyarat);
        return "redirect:prasyarat?id=" + prasyarat.getMatakuliahKurikulum().getId();
    }

    @PostMapping("/academic/curriculumCourses/prasyarat/delete")
    public String deletePras(@RequestParam(value = "id", name = "id") Prasyarat prasyarat) {

        prasyarat.setStatus(StatusRecord.HAPUS);
        prasyaratDao.save(prasyarat);
        return "redirect:../prasyarat?id=" + prasyarat.getMatakuliahKurikulum().getId();
    }

    @GetMapping("/academic/curriculumCourses/matakuliahSetara/list")
    public void listMatakuliahSetara(Model model,
            @RequestParam(required = false) MatakuliahKurikulum matakuliahKurikulum,
            String prodi, String kurikulum, @PageableDefault(size = Integer.MAX_VALUE) Pageable page) {
        model.addAttribute("kurikulum", kurikulum);
        model.addAttribute("prodi", prodi);
        model.addAttribute("setara", mataKuliahSetaraDao.pilihanSetara(matakuliahKurikulum.getMatakuliah()));
        model.addAttribute("matakuliahKurikulum", matakuliahKurikulumDao.findById(matakuliahKurikulum.getId()).get());
        model.addAttribute("listMatakuliahSetara",
                mataKuliahSetaraDao.listMatakuliahSetara(matakuliahKurikulum.getMatakuliah().getId()));
        model.addAttribute("listMatakuliah",
                matakuliahDao.pilihMatakuliahSetara(matakuliahKurikulum.getMatakuliah().getId(), page));

    }

    @PostMapping("/academic/curriculumCourses/matakuliahSetara/proses")
    public String prosesMatakuliahSetara(@RequestParam String defaultMatkul,
            @RequestParam(required = false) String[] listMatakuliah,
            @RequestParam MatakuliahKurikulum matakuliahKurikulum, Authentication authentication) {

        User user = currentUserService.currentUser(authentication);
        List<String> matakuliah = new ArrayList<>();
        matakuliah.addAll(Arrays.asList(listMatakuliah));
        matakuliah.add(defaultMatkul);

        for (String m : matakuliah) {
            for (String setara : matakuliah) {
                if (!m.equals(setara)) {
                    MatakuliahSetara matakuliahSetara = new MatakuliahSetara();
                    matakuliahSetara.setMatakuliah(matakuliahDao.findById(m).get());
                    matakuliahSetara.setUserInsert(user.getUsername());
                    matakuliahSetara.setMatakuliahSetara(matakuliahDao.findById(setara).get());
                    matakuliahSetara.setTanggalInsert(LocalDateTime.now());
                    mataKuliahSetaraDao.save(matakuliahSetara);
                }
            }
        }

        return "redirect:list?matakuliahKurikulum=" + matakuliahKurikulum.getId() + "&kurikulum="
                + matakuliahKurikulum.getKurikulum().getId() + "&prodi="
                + matakuliahKurikulum.getKurikulum().getProdi().getId();

    }

    @PostMapping("/academic/curriculumCourses/matakuliahSetara/delete")
    public String hapusMatakuliahSetara(@RequestParam MatakuliahSetara matakuliahSetara,
            @RequestParam String matakuliahKurikulum, Authentication authentication) {
        User user = currentUserService.currentUser(authentication);

        matakuliahSetara.setStatus(StatusRecord.HAPUS);
        matakuliahSetara.setTanggalDelete(LocalDateTime.now());
        matakuliahSetara.setUserDelete(user.getUsername());
        mataKuliahSetaraDao.save(matakuliahSetara);

        return "redirect:list?matakuliahKurikulum=" + matakuliahKurikulum;
    }

    // Ploting

    @GetMapping("/academic/ploting/list")
    public void listPloting(Model model, @RequestParam(required = false) String[] kelas,
            @RequestParam(required = false) TahunAkademik tahun) {

        model.addAttribute("listKelas", kelasDao.kelasPloting());
        if (tahun != null) {
            List<String> kelasList = new ArrayList<>();
            List<Kelas> pilihanKelas = new ArrayList<>();
            for (String s : kelas) {
                Kelas k = kelasDao.findById(s).get();
                kelasList.add(k.getId());
                pilihanKelas.add(k);
            }

            if (tahun.getJenis() == StatusRecord.GANJIL) {
                List<ListPlotingDto> plotingGanjil = matakuliahKurikulumDao.plotingDosenGanjil(tahun, kelasList);
                model.addAttribute("listPloting", plotingGanjil);

            }
            if (tahun.getJenis() == StatusRecord.GENAP) {
                List<ListPlotingDto> plotinggenap = matakuliahKurikulumDao.plotingDosenGenap(tahun, kelasList);
                model.addAttribute("listPloting", plotinggenap);

            }

            model.addAttribute("selectedTahun", tahun);
            model.addAttribute("selectedKelas", kelasList);
            model.addAttribute("pilihanKelas", pilihanKelas);

        }
    }

    @PostMapping("/academic/ploting/save")
    @ResponseBody
    public BaseResponse prosesPloting(@RequestBody PlotingDto plotingDto, @RequestParam TahunAkademik tahun) {

        if (plotingDto.getId() == null) {
            Jadwal jadwal = new Jadwal();
            jadwal.setJumlahSesi(1);
            jadwal.setBobotUts(BigDecimal.ZERO);
            jadwal.setBobotUas(BigDecimal.ZERO);
            jadwal.setBobotPresensi(BigDecimal.ZERO);
            jadwal.setBobotTugas(BigDecimal.ZERO);
            jadwal.setKelas(kelasDao.findById(plotingDto.getKelas()).get());
            jadwal.setProdi(kelasDao.findById(plotingDto.getKelas()).get().getIdProdi());
            jadwal.setDosen(dosenDao.findById(plotingDto.getIdDosen()).get());
            jadwal.setTahunAkademik(tahun);
            TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, jadwal.getProdi());
            jadwal.setTahunAkademikProdi(tahunAkademikProdi);
            jadwal.setMatakuliahKurikulum(matakuliahKurikulumDao.findById(plotingDto.getMatakuliah()).get());
            jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
            jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
            jadwal.setProgram(programDao.findById("01").get());
            jadwal.setAkses(Akses.PRODI);
            jadwal.setAksesUas(dosenDao.findById(plotingDto.getIdDosen()).get());
            jadwal.setAksesUts(dosenDao.findById(plotingDto.getIdDosen()).get());
            jadwal.setFormatUas("Dicetak");
            jadwal.setFormatUts("Dicetak");
            jadwalDao.save(jadwal);

            BaseResponse response = new BaseResponse();
            response.setId(jadwal.getId());
            response.setMessage("Berhasil disimpan");

            return response;
        } else {
            Jadwal jadwal = jadwalDao.findById(plotingDto.getId()).get();
            jadwal.setDosen(dosenDao.findById(plotingDto.getIdDosen()).get());
            jadwal.setAksesUas(dosenDao.findById(plotingDto.getIdDosen()).get());
            jadwal.setAksesUts(dosenDao.findById(plotingDto.getIdDosen()).get());
            jadwalDao.save(jadwal);

            BaseResponse response = new BaseResponse();
            response.setId(jadwal.getId());
            response.setMessage("Berhasil disimpan");

            return response;
        }

    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = "/ploting-update", produces = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse simpanJadwal(@RequestBody JadwalRequest request) {
        Jadwal jadwal = jadwalDao.findById(request.getId()).get();
        Sesi sesi = sesiDao.findById(request.getSesi()).get();

        jadwal.setAkses(Akses.PRODI);
        jadwal.setHari(hariDao.findById(request.getHari()).get());
        jadwal.setRuangan(ruanganDao.findById(request.getRuangan()).get());
        jadwal.setSesi(sesi.getSesi());
        jadwal.setJamMulai(sesi.getJamMulai());
        jadwal.setFinalStatus("N");
        jadwal.setFinalPloting("FINAL");
        jadwal.setKapasitas(ruanganDao.findById(request.getRuangan()).get().getKapasitas().intValue());
        jadwal.setJamSelesai(sesi.getJamSelesai());
        jadwalDao.save(jadwal);

        JadwalDosen jadwalDosen = new JadwalDosen();
        jadwalDosen.setJadwal(jadwal);
        jadwalDosen.setDosen(jadwal.getDosen());
        jadwalDosen.setStatusJadwalDosen(StatusJadwalDosen.valueOf("PENGAMPU"));
        jadwalDosenDao.save(jadwalDosen);
        String message = "Matakuliah  " + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()
                + " telah diploting";

        BaseResponse baseResponse = new BaseResponse();
        baseResponse.setId(jadwal.getId());
        baseResponse.setMessage("Jadwal Disimpan");

        return baseResponse;
    }

    @PostMapping("/academic/ploting-team/save/{id}")
    @ResponseBody
    public void prosesPloting(@PathVariable String id, @RequestParam String dosen) {
        Jadwal jadwal = jadwalDao.findById(id).get();
        JadwalDosen jadwalDosen = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal, StatusJadwalDosen.TEAM);
        if (jadwalDosen != null) {
            jadwalDosenDao.delete(jadwalDosen);
        }
        JadwalDosen newJadwal = new JadwalDosen();
        newJadwal.setJadwal(jadwal);
        newJadwal.setStatusJadwalDosen(StatusJadwalDosen.TEAM);
        newJadwal.setDosen(dosenDao.findById(dosen).get());
        jadwalDosenDao.save(newJadwal);

    }

    // Schedule

    @GetMapping("/academic/schedule/list")
    public void listSchedule(Model model, @RequestParam(required = false) Prodi prodi,
            @RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) Hari hari,
            @RequestParam(required = false) String bahasa) {
        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedProdi", prodi);
        model.addAttribute("bahasa", bahasa);

        if (prodi != null && tahunAkademik != null && hari != null) {
            model.addAttribute("jadwal", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hari));
            model.addAttribute("ploting", jadwalDao.ploting(prodi, tahunAkademik));
        }

        if (prodi != null && tahunAkademik != null && hari == null) {
            model.addAttribute("ploting", jadwalDao.ploting(prodi, tahunAkademik));
            model.addAttribute("minggu",
                    jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("0").get()));
            model.addAttribute("senin",
                    jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("1").get()));
            model.addAttribute("selasa",
                    jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("2").get()));
            model.addAttribute("rabu",
                    jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("3").get()));
            model.addAttribute("kamis",
                    jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("4").get()));
            model.addAttribute("jumat",
                    jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("5").get()));
            model.addAttribute("sabtu",
                    jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("6").get()));
        }
    }

    @GetMapping("/academic/schedule/pindah")
    public void listSchedule(@RequestParam(required = false) Jadwal dari,
            @RequestParam(required = false) Jadwal tujuan) {
        List<KrsDetail> krsDetail = krsDetailDao.cariJadwalPindah(dari);
        List<String> nama = new ArrayList<>();
        System.out.println(krsDetail.size());
        for (KrsDetail kd : krsDetail) {
            if (krsDetailDao.findByJadwalAndStatus(dari, StatusRecord.AKTIF).size() > dari.getKapasitas()
                    && krsDetailDao.findByJadwalAndStatus(tujuan, StatusRecord.AKTIF).size() < tujuan.getKapasitas()) {
                if (krsDetailDao.findByTahunAkademikAndStatusAndJadwalHariAndJadwalSesiAndMahasiswa(
                        dari.getTahunAkademik(), StatusRecord.AKTIF, tujuan.getHari(), tujuan.getSesi(),
                        kd.getMahasiswa()) == null) {
                    kd.setJadwal(tujuan);
                    kd.setMatakuliahKurikulum(tujuan.getMatakuliahKurikulum());
                    krsDetailDao.save(kd);
                    nama.add(kd.getMahasiswa().getNama());
                }
            }
        }

        System.out.println(nama);

    }

    @GetMapping("/academic/schedule/form")
    public void scheduleForm(Model model, @RequestParam(name = "id", value = "id") Jadwal jadwal,
            @RequestParam(required = false) String plot) {
        if (plot != null) {
            model.addAttribute("plot", plot);
        }

        model.addAttribute("jadwal", jadwal);
        model.addAttribute("hari", hariDao.findAll());
    }

    @GetMapping("/academic/schedule/ploting")
    public void plotingSchedule(Model model, @RequestParam(name = "id", value = "id") Prodi prodi,
            @RequestParam(name = "tahun", value = "tahun") TahunAkademik tahunAkademik) {
        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedProdi", prodi);
        model.addAttribute("ploting", jadwalDao.ploting(prodi, tahunAkademik));
        model.addAttribute("hari", hariDao.findAll());

    }

    @GetMapping("/akademik/detail-ploting-jadwal")
    @ResponseBody
    public DataPloting detailPlotingJadwal(@RequestParam String tahun, @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) Integer limit) {

        if (search.isEmpty()) {
            TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
            List<DataPlotingDto> jadwal = jadwalDao.detailJadwalKosongLimit(tahunAkademik, limit, offset);
            DataPloting response = new DataPloting();
            response.setTotal(jadwalDao.detailJadwalKosong(tahunAkademik).size());
            response.setTotalNotFiltered(jadwalDao.detailJadwalKosong(tahunAkademik).size());
            response.setRows(jadwal);
            return response;
        } else {
            TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
            List<DataPlotingDto> jadwal = jadwalDao.detailJadwalKosongSearchLimit(tahunAkademik, search, limit, offset);
            DataPloting response = new DataPloting();
            response.setTotal(jadwalDao.detailJadwalKosongSearch(tahunAkademik, search).size());
            response.setTotalNotFiltered(jadwal.size());
            response.setRows(jadwal);
            return response;
        }

    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = "/get-ruangan-list", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Ruangan> getDataRuangan() {

        return ruanganDao.findByStatus(StatusRecord.AKTIF);
    }

    @GetMapping("/akademik/validasi-sesi/{id}")
    @ResponseBody
    public List<ValidasiSesi> validasiSesi(@PathVariable String id, @RequestParam String tahun,
            @RequestParam String hari, @RequestParam Integer sks,
            @RequestParam String ruangan, @RequestParam String kelas, @RequestParam String dosen) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        Hari h = hariDao.findById(hari).get();
        Ruangan r = ruanganDao.findById(ruangan).get();
        Kelas k = kelasDao.findById(kelas).get();
        Dosen d = dosenDao.findById(dosen).get();

        return jadwalDao.validateSesi(tahunAkademik, h, id, r, k, sks, d);

    }

    @PostMapping("/academic/schedule/ploting")
    public String prosesPloting(HttpServletRequest request, @RequestParam(name = "id", value = "id") Prodi prodi,
            @RequestParam(name = "tahun", value = "tahun") TahunAkademik tahunAkademik) {
        List<PlotingDto> jadwal = jadwalDao.ploting(prodi, tahunAkademik);
        for (PlotingDto j : jadwal) {
            String mulai = request.getParameter(j.getId() + "_mulai");
            String selesai = request.getParameter(j.getId() + "_selesai");
            String sesi = request.getParameter(j.getId() + "_isiSesi");
            String ruangan = request.getParameter(j.getId() + "_roomisi");
            String hari = request.getParameter(j.getId() + "_hariisi");
            String idSesi = request.getParameter(j.getId() + "_sesii");
            if (hari != null && !hari.trim().isEmpty()) {
                Jadwal jdwl = jadwalDao.findById(j.getId()).get();
                jdwl.setSesi(sesi);
                jdwl.setJamMulai(LocalTime.parse(mulai));
                jdwl.setJamSelesai(LocalTime.parse(selesai));
                jdwl.setHari(hariDao.findById(hari).get());
                jdwl.setRuangan(ruanganDao.findById(ruangan).get());
                jdwl.setFinalStatus("N");
                jdwl.setKapasitas(ruanganDao.findById(ruangan).get().getKapasitas().intValue());
                jadwalDao.save(jdwl);

                JadwalDosen jadwalDosen = new JadwalDosen();
                jadwalDosen.setStatusJadwalDosen(StatusJadwalDosen.PENGAMPU);
                jadwalDosen.setJadwal(jdwl);
                jadwalDosen.setDosen(jdwl.getDosen());
                jadwalDosen.setJumlahIzin(0);
                jadwalDosen.setJumlahKehadiran(0);
                jadwalDosen.setJumlahMangkir(0);
                jadwalDosen.setJumlahSakit(0);
                jadwalDosen.setJumlahTerlambat(0);
                jadwalDosenDao.save(jadwalDosen);
            }
        }

        return "redirect:list?tahunAkademik=" + tahunAkademik.getId() + "&prodi=" + prodi.getId();

    }

    @GetMapping("/academic/schedule/download")
    public void downloadJadwal(@RequestParam(required = false) TahunAkademik tahunAkademik,
            HttpServletResponse response) throws IOException {

        String[] columns = { "No", "Ruangan", "Hari", "Waktu", "Prodi", "Kode Matkul", "Matakuliah", "Sks Matkul",
                "Kelas", "Dosen", "Total Sks Dosen", "Jumlah Mahasiswa" };

        List<Object[]> listJadwal = jadwalDao.downloadJadwal(tahunAkademik);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rekap Jadwal");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for (Object[] list : listJadwal) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(list[3].toString());
            row.createCell(2).setCellValue(list[12].toString());
            row.createCell(3).setCellValue(list[4].toString() + " - " + list[5].toString());
            row.createCell(4).setCellValue(list[6].toString());
            row.createCell(5).setCellValue(list[7].toString());
            row.createCell(6).setCellValue(list[8].toString());
            row.createCell(7).setCellValue(list[9].toString());
            row.createCell(8).setCellValue(list[10].toString());
            row.createCell(9).setCellValue(list[11].toString());
            row.createCell(10).setCellValue(list[13].toString());
            row.createCell(11).setCellValue(list[14].toString());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                "attachment; filename=Rekap_Jadwal_" + tahunAkademik.getNamaTahunAkademik() + ".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/academic/schedule/student")
    public void studentSchedule(@RequestParam(value = "id", name = "id") Jadwal jadwal, Model model) {
        model.addAttribute("student", krsDetailDao.cariJadwalMahasiswa(jadwal));
    }

    @PostMapping("/academic/schedule/form")
    public String prosesSchedule(Model model,
            @ModelAttribute @Valid RequestJadwal request,
            RedirectAttributes attributes) throws Exception {

        Boolean cekEdit = Boolean.FALSE;

        Jadwal jadwal = jadwalDao.findById(request.getId()).get();

        if (request.getSesii() != null) {
            if (jadwal.getHari() != request.getHari() || jadwal.getSesi() != request.getSesii().getId()) {
                cekEdit = Boolean.TRUE;
            }
        }

        List<Jadwal> jdwl = jadwalDao.cariJadwal(Arrays.asList(jadwal.getId()), jadwal.getTahunAkademik(),
                jadwal.getHari(), jadwal.getRuangan(), jadwal.getSesi(), StatusRecord.AKTIF);

        jadwal.setHari(request.getHari());
        jadwal.setAkses(request.getAkses());
        jadwal.setRuangan(request.getRuangan());
        jadwal.setDosen(request.getDosen());
        jadwal.setKapasitas(request.getKapasitas());
        jadwal.setFinalStatus("N");
        if ("WAITING".equals(jadwal.getFinalPloting()) || jadwal.getFinalPloting() == null) {
            jadwal.setFinalPloting("FINAL");
        }
        if (request.getSesii() != null) {
            jadwal.setSesi(request.getSesii().getSesi());
            jadwal.setJamMulai(request.getSesii().getJamMulai());
            jadwal.setJamSelesai(request.getSesii().getJamSelesai());
        }
        jadwalDao.save(jadwal);

        JadwalDosen jd = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal, StatusJadwalDosen.PENGAMPU);
        if (jd == null) {

            JadwalDosen jadwalDosen = new JadwalDosen();
            jadwalDosen.setDosen(jadwal.getDosen());
            jadwalDosen.setJadwal(jadwal);
            jadwalDosen.setStatusJadwalDosen(StatusJadwalDosen.PENGAMPU);
            jadwalDosenDao.save(jadwalDosen);
        } else {
            jd.setDosen(jadwal.getDosen());
            jadwalDosenDao.save(jd);
        }
        if (jadwal.getFinalPloting().equals("FINISHED")) {
            if (Boolean.TRUE.equals(cekEdit)) {
                presensiService.updateSesi1(jadwal);
            }
        }

        return "redirect:list?tahunAkademik=" + jadwal.getTahunAkademik().getId() + "&prodi="
                + jadwal.getProdi().getId();

    }

    @PostMapping("/academic/schedule/delete")
    public String deleteSchedule(@RequestParam String jadwal) {

        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();

        jadwal1.setStatus(StatusRecord.HAPUS);
        jadwalDao.save(jadwal1);

        List<JadwalDosen> jadwalDosen = jadwalDosenDao.findByJadwal(jadwal1);
        for (JadwalDosen jd : jadwalDosen) {
            jadwalDosenDao.delete(jd);
        }

        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal1,
                StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail) {
            kd.setStatus(StatusRecord.HAPUS);
            krsDetailDao.save(kd);
        }

        return "redirect:list?tahunAkademik=" + jadwal1.getTahunAkademik().getId() + "&prodi="
                + jadwal1.getProdi().getId();
    }

    @GetMapping("/academic/schedule/team")
    public void teamTeaching(Model model, @RequestParam Jadwal jadwal) {

        model.addAttribute("jadwal", jadwalDosenDao.findByStatusJadwalDosenAndJadwal(StatusJadwalDosen.TEAM, jadwal));
        model.addAttribute("data", jadwal);
        model.addAttribute("hari", hariDao.findAll());
    }

    @PostMapping("/academic/schedule/team")
    public String prosesTeamTeaching(@Valid JadwalDosen jadwalDosen) {

        jadwalDosenDao.save(jadwalDosen);
        return "redirect:team?jadwal=" + jadwalDosen.getJadwal().getId();
    }

    @PostMapping("/academic/schedule/deleteTeam")
    public String deleteTeamTeaching(@RequestParam JadwalDosen jadwalDosen) {

        jadwalDosenDao.delete(jadwalDosen);
        return "redirect:team?jadwal=" + jadwalDosen.getJadwal().getId();
    }

    @GetMapping("/academic/schedule/room")
    public void roomSchedule(Model model, @RequestParam(required = false) Gedung gedung,
            @RequestParam(required = false) Hari hari) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        model.addAttribute("hari", hariDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("gedung", gedungDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedGedung", gedung);

        if (gedung != null && hari != null) {

            Iterable<Ruangan> ruangan = ruanganDao.findByStatusAndGedung(StatusRecord.AKTIF, gedung);
            List<RoomDto> sesi = new ArrayList<>();
            for (Ruangan ruang1 : ruangan) {

                RoomDto roomDto = new RoomDto();
                roomDto.setId(ruang1.getId());
                roomDto.setRuangan1(ruang1.getNamaRuangan());

                List<Jadwal> jadwal1 = jadwalDao.findByStatusAndTahunAkademikAndHariAndRuangan(StatusRecord.AKTIF,
                        tahunAkademik, hari, ruang1);

                for (Jadwal j : jadwal1) {

                    if (j.getSesi().equals("1")) {
                        roomDto.setSesi1(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen1(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas1(j.getKelas().getNamaKelas());
                    }

                    if (j.getSesi().equals("2")) {
                        roomDto.setSesi2(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen2(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas2(j.getKelas().getNamaKelas());
                    }

                    if (j.getSesi().equals("3")) {
                        roomDto.setSesi3(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen3(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas3(j.getKelas().getNamaKelas());
                    }

                    if (j.getSesi().equals("4")) {
                        roomDto.setSesi4(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen4(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas4(j.getKelas().getNamaKelas());
                    }

                }

                sesi.add(roomDto);

            }

            model.addAttribute("jadwalRuang", sesi);

        }
    }

    @GetMapping("/academic/schedule/krs")
    public void addKrs(Model model, @RequestParam Jadwal jadwal) {
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("krs", krsDao.krsList(jadwal.getTahunAkademik(), jadwal));
    }

    @PostMapping("/academic/schedule/krs")
    public String postKrs(@RequestParam(required = false) Jadwal jadwal,
            @RequestParam(required = false) String[] data) {
        if (data != null) {
            for (String krs : data) {
                Krs k = krsDao.findById(krs).get();
                KrsDetail kd = new KrsDetail();
                kd.setJadwal(jadwal);
                kd.setKrs(k);
                kd.setMahasiswa(k.getMahasiswa());
                kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                kd.setNilaiPresensi(BigDecimal.ZERO);
                kd.setNilaiUts(BigDecimal.ZERO);
                kd.setNilaiTugas(BigDecimal.ZERO);
                kd.setFinalisasi("N");
                kd.setNilaiUas(BigDecimal.ZERO);
                kd.setJumlahKehadiran(0);
                kd.setJumlahMangkir(0);
                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                kd.setJumlahTerlambat(0);
                kd.setJumlahIzin(0);
                kd.setJumlahSakit(0);
                kd.setStatusEdom(StatusRecord.UNDONE);
                krsDetailDao.save(kd);
            }
        }

        return "redirect:list?tahunAkademik=" + jadwal.getTahunAkademik().getId() + "&prodi="
                + jadwal.getProdi().getId();
    }

    // Uts & Uas
    @GetMapping("/academic/schedule/uts")
    public void absenUts(@RequestParam Jadwal jadwal, Model model) {

        model.addAttribute("tahun", jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 10));
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("absen", krsDetailDao.absenUts(jadwal, jadwal.getTahunAkademik()));

    }

    @GetMapping("/academic/schedule/uas")
    public void absenUas(@RequestParam Jadwal jadwal, Model model) {

        model.addAttribute("tahun", jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 10));
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("absen", krsDetailDao.absenUas(jadwal, jadwal.getTahunAkademik()));

    }

    // Conversion

    @GetMapping("/academic/conversion/list")
    public void list(Model model, @RequestParam(required = false) String nim) {

        if (nim != null) {
            Mahasiswa dataMahasiswa = mahasiswaDao.findByNimAndStatus(nim, StatusRecord.AKTIF);
            model.addAttribute("nim", nim);
            model.addAttribute("list", krsDetailDao.listKrsDetail(dataMahasiswa.getId()));
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            model.addAttribute("mhs", mahasiswa);
        }

    }

    @GetMapping("/academic/equalization/list")
    public void penyetaraanList(Model model, @RequestParam(required = false) String nim) {

        if (nim != null) {
            Mahasiswa dataMahasiswa = mahasiswaDao.findByNimAndStatus(nim, StatusRecord.AKTIF);
            model.addAttribute("nim", nim);
            model.addAttribute("list", krsDetailDao.listKrsDetailPenyetaraan(dataMahasiswa.getId()));
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            model.addAttribute("mhs", mahasiswa);
        }

    }

    @GetMapping("/academic/conversion/form")
    public void form(Model model, @RequestParam(required = false) String nim,
            @RequestParam(required = false) Jadwal jadwal) {

        model.addAttribute("krsDetail", new KrsDetail());
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        model.addAttribute("grade", gradeDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("mahasiswa", mhs);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));

    }

    @GetMapping("/academic/equalization/form")
    public void equalizationForm(Model model, @RequestParam(required = false) String nim,
            @RequestParam(required = false) Jadwal jadwal) {

        model.addAttribute("krsDetail", new KrsDetail());
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        // Optional<Jadwal> jadwal1 = jadwalDao.findById(jadwal);
        model.addAttribute("grade", gradeDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("mahasiswa", mhs);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("matakuliah", matakuliahDao.findByMahasiswaKurikulum(mhs));
        model.addAttribute("tahunAkademik",
                tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));

    }

    @PostMapping("/academic/conversion/form")
    public String inputForm(@RequestParam(required = false) String nim, @RequestParam(required = false) String ta,
            @RequestParam(required = false) Jadwal jadwal, @RequestParam(required = false) String matakuliahLama,
            @RequestParam(required = false) String grade, RedirectAttributes attributes) {
        int jumlahSks;
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(ta).get();
        Grade grade1 = gradeDao.findByNama(grade);
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahunAkademik, StatusRecord.AKTIF);
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik,
                mhs.getIdProdi());
        KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndTahunAkademikAndJadwalAndStatus(mhs, tahunAkademik, jadwal,
                StatusRecord.AKTIF);
        if (krsDetail == null) {
            if (krs != null) {
                KrsDetail kd = new KrsDetail();
                kd.setKrs(krs);
                kd.setMahasiswa(mhs);
                kd.setJadwal(jadwal);
                kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                kd.setNilaiPresensi(BigDecimal.ZERO);
                kd.setNilaiUts(BigDecimal.ZERO);
                kd.setNilaiTugas(BigDecimal.ZERO);
                kd.setFinalisasi("FINAL");
                kd.setNilaiAkhir(grade1.getBawah());
                kd.setBobot(grade1.getBobot());
                kd.setGrade(grade1.getNama());
                kd.setNilaiUas(BigDecimal.ZERO);
                kd.setJumlahKehadiran(0);
                kd.setJumlahMangkir(0);
                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                kd.setJumlahTerlambat(0);
                kd.setJumlahIzin(0);
                kd.setJumlahSakit(0);
                kd.setStatusEdom(StatusRecord.UNDONE);
                kd.setStatus(StatusRecord.AKTIF);
                kd.setTahunAkademik(tahunAkademik);
                kd.setStatusKonversi(StatusRecord.AKTIF);
                krsDetailDao.save(kd);

                Konversi konversi = new Konversi();
                konversi.setKrsDetail(kd);
                konversi.setNamaMatakuliahLama(matakuliahLama);
                konversi.setStatus(StatusRecord.AKTIF);
                konversiDao.save(konversi);

            } else {

                Krs krs1 = new Krs();
                krs1.setTahunAkademik(tahunAkademik);
                krs1.setTahunAkademikProdi(tahunAkademikProdi);
                krs1.setProdi(mhs.getIdProdi());
                krs1.setMahasiswa(mhs);
                krs1.setNim(mhs.getNim());
                krs1.setTanggalTransaksi(LocalDateTime.now());
                krs1.setStatus(StatusRecord.AKTIF);
                krsDao.save(krs1);

                KrsDetail kd = new KrsDetail();
                kd.setKrs(krs1);
                kd.setMahasiswa(mhs);
                kd.setJadwal(jadwal);
                kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                kd.setNilaiPresensi(BigDecimal.ZERO);
                kd.setNilaiUts(BigDecimal.ZERO);
                kd.setNilaiTugas(BigDecimal.ZERO);
                kd.setFinalisasi("FINAL");
                kd.setNilaiAkhir(grade1.getBawah());
                kd.setBobot(grade1.getBobot());
                kd.setGrade(grade1.getNama());
                kd.setNilaiUas(BigDecimal.ZERO);
                kd.setJumlahKehadiran(0);
                kd.setJumlahMangkir(0);
                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                kd.setJumlahTerlambat(0);
                kd.setJumlahIzin(0);
                kd.setJumlahSakit(0);
                kd.setStatusEdom(StatusRecord.UNDONE);
                kd.setStatus(StatusRecord.AKTIF);
                kd.setTahunAkademik(tahunAkademik);
                kd.setStatusKonversi(StatusRecord.AKTIF);
                krsDetailDao.save(kd);

                Konversi konversi = new Konversi();
                konversi.setKrsDetail(kd);
                konversi.setNamaMatakuliahLama(matakuliahLama);
                konversi.setStatus(StatusRecord.AKTIF);
                konversiDao.save(konversi);

            }
        } else {
            attributes.addFlashAttribute("gagal", "Data sudah ada!");
            return "redirect:form?nim=" + mhs.getNim() + "&jadwal=" + jadwal.getId();
        }

        return "redirect:list?nim=" + mhs.getNim();

    }

    @PostMapping("/academic/equalization/form")
    public String inputPenyetaraanForm(@RequestParam(required = false) String nim,
            @RequestParam(required = false) String matakuliah,
            @RequestParam(required = false) String matakuliahLama,
            @RequestParam(required = false) String grade, RedirectAttributes attributes) {
        int jumlahSks;
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Grade grade1 = gradeDao.findByNama(grade);
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahunAkademik, StatusRecord.AKTIF);
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik,
                mhs.getIdProdi());
        Jadwal jadwal = new Jadwal();
        jadwal.setJumlahSesi(1);
        jadwal.setBobotUts(BigDecimal.ZERO);
        jadwal.setBobotUas(BigDecimal.ZERO);
        jadwal.setBobotTugas(BigDecimal.ZERO);
        jadwal.setBobotPresensi(BigDecimal.ZERO);
        jadwal.setKelas(kelasDao.findById("-").get());
        jadwal.setProdi(mhs.getIdProdi());
        jadwal.setTahunAkademik(tahunAkademik);
        jadwal.setTahunAkademikProdi(tahunAkademikProdi);
        jadwal.setMatakuliahKurikulum(matakuliahKurikulumDao.findById(matakuliah).get());
        jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
        jadwal.setProgram(programDao.findById("01").get());
        jadwal.setAkses(Akses.TERTUTUP);
        jadwal.setFinalStatus("FINAL");
        jadwal.setStatus(StatusRecord.KONVERSI);
        jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
        jadwalDao.save(jadwal);

        KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndTahunAkademikAndJadwalAndStatus(mhs, tahunAkademik, jadwal,
                StatusRecord.AKTIF);
        if (krsDetail == null) {
            if (krs != null) {
                KrsDetail kd = new KrsDetail();
                kd.setKrs(krs);
                kd.setMahasiswa(mhs);
                kd.setJadwal(jadwal);
                kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                kd.setNilaiPresensi(BigDecimal.ZERO);
                kd.setNilaiUts(BigDecimal.ZERO);
                kd.setNilaiTugas(BigDecimal.ZERO);
                kd.setFinalisasi("FINAL");
                kd.setNilaiAkhir(grade1.getBawah());
                kd.setBobot(grade1.getBobot());
                kd.setGrade(grade1.getNama());
                kd.setNilaiUas(BigDecimal.ZERO);
                kd.setJumlahKehadiran(0);
                kd.setJumlahMangkir(0);
                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                kd.setJumlahTerlambat(0);
                kd.setJumlahIzin(0);
                kd.setJumlahSakit(0);
                kd.setStatusEdom(StatusRecord.UNDONE);
                kd.setStatus(StatusRecord.AKTIF);
                kd.setTahunAkademik(tahunAkademik);
                kd.setStatusKonversi(StatusRecord.AKTIF);
                krsDetailDao.save(kd);

                Konversi konversi = new Konversi();
                konversi.setKrsDetail(kd);
                konversi.setNamaMatakuliahLama(matakuliahLama);
                konversi.setStatus(StatusRecord.AKTIF);
                konversiDao.save(konversi);

            } else {

                Krs krs1 = new Krs();
                krs1.setTahunAkademik(tahunAkademik);
                krs1.setTahunAkademikProdi(tahunAkademikProdi);
                krs1.setProdi(mhs.getIdProdi());
                krs1.setMahasiswa(mhs);
                krs1.setNim(mhs.getNim());
                krs1.setTanggalTransaksi(LocalDateTime.now());
                krs1.setStatus(StatusRecord.AKTIF);
                krsDao.save(krs1);

                KrsDetail kd = new KrsDetail();
                kd.setKrs(krs1);
                kd.setMahasiswa(mhs);
                kd.setJadwal(jadwal);
                kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                kd.setNilaiPresensi(BigDecimal.ZERO);
                kd.setNilaiUts(BigDecimal.ZERO);
                kd.setNilaiTugas(BigDecimal.ZERO);
                kd.setFinalisasi("FINAL");
                kd.setNilaiAkhir(grade1.getBawah());
                kd.setBobot(grade1.getBobot());
                kd.setGrade(grade1.getNama());
                kd.setNilaiUas(BigDecimal.ZERO);
                kd.setJumlahKehadiran(0);
                kd.setJumlahMangkir(0);
                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                kd.setJumlahTerlambat(0);
                kd.setJumlahIzin(0);
                kd.setJumlahSakit(0);
                kd.setStatusEdom(StatusRecord.UNDONE);
                kd.setStatus(StatusRecord.AKTIF);
                kd.setTahunAkademik(tahunAkademik);
                kd.setStatusKonversi(StatusRecord.AKTIF);
                krsDetailDao.save(kd);

                Konversi konversi = new Konversi();
                konversi.setKrsDetail(kd);
                konversi.setNamaMatakuliahLama(matakuliahLama);
                konversi.setStatus(StatusRecord.KONVERSI);
                konversiDao.save(konversi);

            }
        } else {
            attributes.addFlashAttribute("gagal", "Data sudah ada!");
            return "redirect:form?nim=" + mhs.getNim() + "&jadwal=" + jadwal.getId();
        }

        return "redirect:list?nim=" + mhs.getNim();

    }

    @PostMapping("/academic/conversion/delete")
    public String deleteConversion(@RequestParam(required = false) KrsDetail krsDetail) {

        krsDetail.setStatus(StatusRecord.HAPUS);
        krsDetailDao.save(krsDetail);

        Konversi konversi = konversiDao.findByKrsDetailAndStatus(krsDetail, StatusRecord.AKTIF);
        konversi.setStatus(StatusRecord.HAPUS);
        konversiDao.save(konversi);

        return "redirect:list?nim=" + krsDetail.getMahasiswa().getNim();
    }

    @PostMapping("/academic/equalization/delete")
    public String deleteEqualization(@RequestParam(required = false) KrsDetail krsDetail) {

        krsDetail.setStatus(StatusRecord.HAPUS);
        krsDetailDao.save(krsDetail);

        Konversi konversi = konversiDao.findByKrsDetailAndStatus(krsDetail, StatusRecord.AKTIF);
        konversi.setStatus(StatusRecord.HAPUS);
        konversiDao.save(konversi);

        return "redirect:list?nim=" + krsDetail.getMahasiswa().getNim();
    }

    @Transactional
    @Scheduled(cron = "0 00 21 * * *", zone = "Asia/Jakarta")
    public void setFinal() {

        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (LocalDate.now().compareTo(tahun.getTanggalSelesaiNilai().plusDays(1)) == 0) {
            krsDetailDao.updateFinalisasi(tahun.getId());
            jadwalDao.updateFinalStatus(tahun.getId());
            System.out.println("TerUpdate final.");

        }

    }

    @GetMapping("/jadwalUjian")
    public void transkriptFormatExcel2(HttpServletResponse response) throws IOException {

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        String jenis = null;
        if (LocalDate.now().isAfter(tahunAkademik.getTanggalSelesaiUts())) {
            jenis = "UAS";
        } else {
            jenis = "UTS";
        }
        System.out.println(jenis);
        InputStream file = getContohExcelJadwalUjian.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), "Jadwal Uts");

        List<Object[]> jadwal = jadwalDao.cariJadwalUjian(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        List<Object[]> ruangan = ruanganDao.listNamaRuangan();
        int rowDatajadwal = 5;
        int rowRuangan = 1000;
        int no = 1;

        for (Object[] data : jadwal) {
            Row row = sheet.createRow(rowDatajadwal);

            row.createCell(0).setCellValue(no++);
            row.createCell(1).setCellValue(data[0].toString());
            row.createCell(2).setCellValue(data[1].toString());
            row.createCell(3).setCellValue(data[2].toString());
            row.createCell(4).setCellValue(data[3].toString());
            row.createCell(8).setCellValue(krsDetailDao.totalMahasiswaJadwal(data[6].toString()));
            row.createCell(9).setCellValue(data[4].toString());
            if (jenis.equals("UTS")) {
                row.createCell(10).setCellValue(data[5].toString());
            } else {
                row.createCell(10).setCellValue(data[7].toString());
            }
            row.createCell(11).setCellValue(data[6].toString());
            rowDatajadwal++;

        }

        for (Object[] data : ruangan) {
            Row row = sheet.createRow(rowRuangan);

            row.createCell(7).setCellValue(data[0].toString());
            rowRuangan++;

        }

        String namaFile = "Jadwal Ujian";
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + namaFile + extentionX + "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/getdata/jadwal-ujian")
    @ResponseBody
    public TableJadwalUjian getDataJadwalUjian(@RequestParam(required = false) TahunAkademik tahunAkademik,
            @RequestParam(required = false) String jenis) {

        TableJadwalUjian response = new TableJadwalUjian();
        response.setTotal(jadwalUjianDao.jadwalUjian(tahunAkademik, jenis).size());
        response.setTotalNotFiltered(jadwalUjianDao.jadwalUjian(tahunAkademik, jenis).size());
        List<ListJadwalUjian> jadwalUjian = jadwalUjianDao.jadwalUjian(tahunAkademik, jenis);
        response.setRows(jadwalUjian);

        return response;
    }

    @GetMapping("/academic/schedule/ujian")
    public void jadwalUjian(@RequestParam(required = false) TahunAkademik tahunAkademik, Model model,
            @RequestParam(required = false) String jenis, @RequestParam(required = false) String tanggal) {
        if (tahunAkademik != null) {
            if (tanggal == null || tanggal.equals("-")) {
                List<ListJadwalUjian> jadwalUjian = jadwalUjianDao.jadwalUjian(tahunAkademik, jenis);
                model.addAttribute("listJadwal", jadwalUjian);
                model.addAttribute("selectedTanggal", tanggal);
            } else {
                List<ListJadwalUjian> jadwalUjian = jadwalUjianDao.jadwalUjianTanggal(tahunAkademik, jenis, tanggal);
                model.addAttribute("listJadwal", jadwalUjian);
                model.addAttribute("selectedTanggal", tanggal);

            }
            model.addAttribute("listTanggal", jadwalUjianDao.listTanggal(jenis, tahunAkademik));
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedJenis", jenis);

        }
    }

    @PostMapping("/academic/schedule/uploadUjian")
    public String listUjian(@RequestParam String jenis, MultipartFile file, RedirectAttributes attributes) {
        List<BaseResponse> errors = new ArrayList<>();
        List<BaseResponse> success = new ArrayList<>();
        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheetPertama = workbook.getSheetAt(0);

            int row = 5;
            int jumlahJadwal = sheetPertama.getLastRowNum() - row;
            for (int i = 0; i < jumlahJadwal; i++) {
                Row baris = sheetPertama.getRow(row + i);

                if (baris == null) {

                } else {
                    if (baris.getCell(11) == null) {

                    } else {
                        String stringJadwal = baris.getCell(11).getStringCellValue();
                        Jadwal jadwal = jadwalDao.findById(stringJadwal).get();
                        if (jadwal == null) {
                            BaseResponse response = new BaseResponse();
                            response.setNim(stringJadwal);
                            response.setMessage("Id jadwal tidak ditemukan");
                            errors.add(response);
                        } else {
                            Cell tanggal = baris.getCell(5);

                            if (tanggal != null) {
                                Ruangan ruangan = ruanganDao.findByNamaRuanganAndStatus(
                                        baris.getCell(7).getStringCellValue(), StatusRecord.AKTIF);
                                if (ruangan != null) {
                                    System.out.println(ruangan.getNamaRuangan());
                                    LocalDate localDate = LocalDate.parse(baris.getCell(5).getStringCellValue());
                                    JadwalUjian ujian = jadwalUjianDao.findByJadwalAndStatusAndJenis(jadwal,
                                            StatusRecord.AKTIF, StatusRecord.valueOf(jenis));
                                    if (ujian == null) {
                                        JadwalUjian jadwalUjian = new JadwalUjian();
                                        jadwalUjian.setJadwal(
                                                jadwalDao.findById(baris.getCell(11).getStringCellValue()).get());
                                        jadwalUjian.setTanggal(localDate);
                                        String jamMulai = baris.getCell(6).toString().substring(0, 5);
                                        String jamSelesai = baris.getCell(6).toString().substring(11 - 5);
                                        jadwalUjian.setJamMulai(LocalTime.parse(jamMulai.replace(".", ":")));
                                        jadwalUjian.setJamSelesai(LocalTime.parse(jamSelesai.replace(".", ":")));
                                        jadwalUjian.setRuangan(ruanganDao.findByNamaRuanganAndStatus(
                                                baris.getCell(7).getStringCellValue(), StatusRecord.AKTIF));
                                        jadwalUjian.setJenis(StatusRecord.valueOf(jenis));
                                        jadwalUjianDao.save(jadwalUjian);

                                        BaseResponse response = new BaseResponse();
                                        response.setNim(jadwal.getDosen().getKaryawan().getNamaKaryawan() + " ( "
                                                + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()
                                                + " / " + jadwal.getKelas().getNamaKelas() + " ) ");
                                        response.setMessage("Sukses");
                                        success.add(response);
                                    } else {
                                        ujian.setTanggal(localDate);
                                        String jamMulai = baris.getCell(6).toString().substring(0, 5);
                                        String jamSelesai = baris.getCell(6).toString().substring(11 - 5);
                                        ujian.setJamMulai(LocalTime.parse(jamMulai.replace(".", ":")));
                                        ujian.setJamMulai(LocalTime.parse(jamSelesai.replace(".", ":")));
                                        ujian.setRuangan(ruanganDao.findByNamaRuanganAndStatus(
                                                baris.getCell(7).getStringCellValue(), StatusRecord.AKTIF));
                                        ujian.setJenis(StatusRecord.valueOf(jenis));
                                        jadwalUjianDao.save(ujian);

                                        BaseResponse response = new BaseResponse();
                                        response.setNim(jadwal.getDosen().getKaryawan().getNamaKaryawan() + " ( "
                                                + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()
                                                + " / " + jadwal.getKelas().getNamaKelas() + " ) ");
                                        response.setMessage("Jadwal Ujian Terupdate");
                                        success.add(response);
                                    }

                                } else {
                                    BaseResponse response = new BaseResponse();
                                    response.setNim(jadwal.getDosen().getKaryawan().getNamaKaryawan() + " ( "
                                            + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()
                                            + " / " + jadwal.getKelas().getNamaKelas() + " ) ");
                                    response.setMessage("Ruangan tidak ditemukan");
                                    errors.add(response);
                                }

                            } else {
                                BaseResponse response = new BaseResponse();
                                response.setNim(jadwal.getDosen().getKaryawan().getNamaKaryawan() + " ( "
                                        + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + " / "
                                        + jadwal.getKelas().getNamaKelas() + " ) ");
                                response.setMessage("Tanggal tidak ditemukan");
                                errors.add(response);
                            }
                        }
                    }
                }

            }

        } catch (IOException e) {
            // LOGGER.error(e.getMessage(), e);
        }
        attributes.addFlashAttribute("errors", errors);
        attributes.addFlashAttribute("success", success);

        return "redirect:ujian";
    }

    @GetMapping("/analytics/dosen/list")
    public void analitikDosen(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik,
            @PageableDefault(size = 10) Pageable pageable, String search, Authentication authentication) {

        TahunAkademik tahunAkademikAktif = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("tahunAkademik",
                tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));

        model.addAttribute("jmlDosen", dosenDao.countDosenByStatus(StatusRecord.AKTIF));

        model.addAttribute("jmlSemuaKelas", sesiKuliahDao.jmlSemuaKelas(tahunAkademik));

        model.addAttribute("jmlKelasOntime",
                presensiDosenDao.jmlKelasOntime(tahunAkademikAktif, StatusPresensi.HADIR.toString()));
        model.addAttribute("jmlKelasTerlambat", presensiDosenDao.jmlKelasTerlambat(tahunAkademikAktif));

        model.addAttribute("jmlKelasOnline", sesiKuliahDao.jmlKelasOnline(tahunAkademik));
        model.addAttribute("jmlKelasOffline", sesiKuliahDao.jmlKelasOffline(tahunAkademik));

        if (StringUtils.hasText(search) && tahunAkademik == null) {
            model.addAttribute("search", search);
            model.addAttribute("klasemenDosen",
                    presensiDosenDao.klasemenDosenByTahunDanSearch(tahunAkademikAktif, search, pageable));
            model.addAttribute("jmlKelasOnline", sesiKuliahDao.jmlKelasOnline(tahunAkademik));
            model.addAttribute("jmlKelasOffline", sesiKuliahDao.jmlKelasOffline(tahunAkademik));
        } else if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("klasemenDosen",
                    presensiDosenDao.klasemenDosenByTahunDanSearch(tahunAkademik, search, pageable));
            model.addAttribute("jmlKelasOnline", sesiKuliahDao.jmlKelasOnline(tahunAkademik));
            model.addAttribute("jmlKelasOffline", sesiKuliahDao.jmlKelasOffline(tahunAkademik));
        } else if (tahunAkademik != null) {
            model.addAttribute("klasemenDosen", presensiDosenDao.klasemenDosen(tahunAkademik, pageable));
            model.addAttribute("jmlKelasOnline", sesiKuliahDao.jmlKelasOnline(tahunAkademik));
            model.addAttribute("jmlKelasOffline", sesiKuliahDao.jmlKelasOffline(tahunAkademik));
        } else {
            model.addAttribute("klasemenDosen", presensiDosenDao.klasemenDosen(tahunAkademikAktif, pageable));
            model.addAttribute("jmlKelasOnline", sesiKuliahDao.jmlKelasOnline(tahunAkademik));
            model.addAttribute("jmlKelasOffline", sesiKuliahDao.jmlKelasOffline(tahunAkademik));
        }

        // model.addAttribute("listTotalSks",
        // krsDetailDao.listTotalSks1(tahunAkademik));
        // model.addAttribute("listTotalSks2",
        // krsDetailDao.listTotalSks2(tahunAkademik,dosen));

    }

    @GetMapping("/analytics/mahasiswa/list")
    public void analitikMahasiswa(Model model, @PageableDefault(size = 10) Pageable pageable) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        model.addAttribute("jmlMhsA",
                krsDao.countKrsByTahunAkademikAndMahasiswaStatus(tahunAkademik, StatusRecord.AKTIF));

        // model.addAttribute("jmlMahasiswaOntime",
        // presensiMahasiswaDao.jmlMahasiswa("20192",StatusPresensi.HADIR.toString()));
        // model.addAttribute("jmlMahasiswaTerlambat",
        // presensiMahasiswaDao.jmlMahasiswa("20192",StatusPresensi.TERLAMBAT.toString()));
        //
        // model.addAttribute("jmlSempro",
        // seminarDao.jmlSempro("20192",StatusApprove.APPROVED.toString()));
        //
        // model.addAttribute("klasemenMahasiswa",
        // presensiMahasiswaDao.klasemenMahasiswa("20192",StatusPresensi.HADIR.toString(),pageable));

    }

    @GetMapping("/api/list-tahunakademik")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> getListTahunakademik() {
        try {
            List<TahunAkademik> tahunAkademik = tahunAkademikDao
                    .findByStatusNotInOrderByKodeTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS));
            if (tahunAkademik.isEmpty()) {
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .responseMessage("Data Kosong")
                                .build());
            } else {
                return ResponseEntity.ok().body(
                        BaseResponseDto.builder()
                                .responseCode("200")
                                .data(tahunAkademik)
                                .responseMessage("Tahun Akademik Ditemukan")
                                .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    BaseResponseDto.builder()
                            .responseCode("ERR500")
                            .responseMessage(e.getLocalizedMessage())
                            .build());
        }
    }

    @PostMapping("/api/create-jadwal-konversi")
    @ResponseBody
    public ResponseEntity<BaseResponseDto> createJadwalKonversi(@RequestBody RequestKonversiNilai request) {
        try {
            String mahasiswaId = request.getMahasiswa();
            String matakuliahId = request.getMatakuliah();

            if (mahasiswaId == null || matakuliahId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponseDto.builder()
                        .responseCode("ERR400")
                        .responseMessage("Mahasiswa or Matakuliah ID not found")
                        .build());
            }

            Mahasiswa mahasiswa = mahasiswaDao.findByNim(mahasiswaId);
            MatakuliahKurikulum matakuliahKurikulum = matakuliahKurikulumDao.findById(matakuliahId)
                    .orElseThrow(() -> new IllegalArgumentException("Matakuliah ID not found"));
            Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(
                    mahasiswa,
                    tahunAkademikDao.findByStatus(StatusRecord.AKTIF),
                    StatusRecord.AKTIF);

            if (krs == null) {
                krs = krsService.createKrs(mahasiswa, tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            }

            Jadwal jadwal = jadwalService.createJadwalKonversi(matakuliahKurikulum, mahasiswa.getIdProdi());
            KrsDetail krsDetail = krsService.createKrsDetailKonversi(krs, jadwal, request.getGrade());

            Konversi konversi = new Konversi();
            konversi.setKrsDetail(krsDetail);
            konversi.setNamaMatakuliahLama(request.getMatakuliahLama());
            konversi.setKodeMatakuliahLama(request.getKode());
            konversi.setStatus(StatusRecord.AKTIF);
            konversiDao.save(konversi);

            return ResponseEntity.ok(BaseResponseDto.builder()
                    .responseCode("200")
                    .responseMessage("Konversi created successfully")
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(BaseResponseDto.builder()
                    .responseCode("ERR400")
                    .responseMessage(e.getMessage())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponseDto.builder()
                    .responseCode("ERR500")
                    .responseMessage("Error: " + e.getMessage())
                    .build());
        }
    }

}
