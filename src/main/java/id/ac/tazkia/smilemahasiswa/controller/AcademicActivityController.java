package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.schedule.*;
import id.ac.tazkia.smilemahasiswa.dto.select2.CourseDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class  AcademicActivityController {

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
    private HariDao hariDao;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private SesiDao sesiDao;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private GedungDao gedungDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KonversiDao konversiDao;

    @Autowired
    private MataKuliahSetaraDao mataKuliahSetaraDao;

    @Autowired
    private EdomQuestionDao edomQuestionDao;

    //    Attribute
    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
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

    //    API
    @GetMapping("/api/prodi")
    @ResponseBody
    public Page<Prodi> cariProdi(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return prodiDao.findByStatus(StatusRecord.AKTIF,page);
        }
        return prodiDao.findByStatusAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(StatusRecord.AKTIF,search, page);

    }

    @GetMapping("/select2/course")
    @ResponseBody
    public List<CourseDto> cariMatkul(@RequestParam String search){

        return matakuliahDao.cariMatakuliah(search);
    }

    @GetMapping("/course/detail")
    @ResponseBody
    public Matakuliah detailMatkul(@RequestParam String search){

        return matakuliahDao.findById(search).get();
    }

    @GetMapping("/api/prodikurikulum")
    @ResponseBody
    public List<Kurikulum> prodiList(@RequestParam(required = false) Prodi prodi){
        List<Kurikulum> kurikulum = kurikulumDao.findByProdiAndStatusNotIn(prodi, StatusRecord.HAPUS);

        return kurikulum;

    }

    @GetMapping("/api/matakuliah")
    @ResponseBody
    public Page<Matakuliah> cariData(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return matakuliahDao.findAll(page);
        }
        return matakuliahDao.findByNamaMatakuliahContainingIgnoreCase(search, page);

    }

    @GetMapping({"/api/kurikulum"})
    @ResponseBody
    public List<Kurikulum> findByProdiAndName(@RequestParam(required = false) String namaProdi, @RequestParam String search,Pageable page){
        if(!StringUtils.hasText(search)) {
            return null;
        }
        return kurikulumDao.findByStatusNotInAndProdiAndNamaKurikulumContainingIgnoreCaseOrderByNamaKurikulum(Arrays.asList(StatusRecord.HAPUS),prodiDao.findById(namaProdi).get(), search);
    }

    @GetMapping("/api/sesi")
    @ResponseBody
    public List<Sesi> cariSesi(@RequestParam(required = false) String idHari,@RequestParam(required = false) String kelas,
                               @RequestParam(required = false) String idRuangan, @RequestParam(required = false) Integer sks,
                               @RequestParam(required = false) String search, @RequestParam(required = false) String dosen){
        Ruangan ruangan = ruanganDao.findById(idRuangan).get();
        Kelas k = kelasDao.findById(kelas).get();
        Hari hari = hariDao.findById(idHari).get();
        Dosen d = dosenDao.findById(dosen).get();
        List<SesiDto> jadwal = jadwalDao.cariSesi(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,ruangan,k,d);
        List<String> stringList = new ArrayList<>();
        for (SesiDto s : jadwal) {
            stringList.add(s.getSesi());
        }



        if (jadwal == null || jadwal.isEmpty()){
            return sesiDao.findBySks(sks);
        }else {
            return sesiDao.findBySesiInAndSks(stringList, sks);
        }
    }

    @GetMapping("/api/waktu")
    @ResponseBody
    public Sesi sesi(@RequestParam(required = false) String id){


        return sesiDao.findById(id).get();

    }

    @GetMapping("/api/tahun")
    @ResponseBody
    public List<Jadwal> tahun(@RequestParam(required = false) String idTahun,
                              @RequestParam(required = false) String idProdi) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahun).get();
        Prodi p = prodiDao.findById(idProdi).get();
        List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndProdiAndHariNotNull(tahunAkademik, p);


        return jadwal;
    }

    @GetMapping("/api/jadwal")
    @ResponseBody
    public KrsDetail krsDetail(@RequestParam(required = false) String id,
                               @RequestParam(required = false) String idTahun,
                               @RequestParam(required = false) String idMahasiswa){

        Jadwal jadwal = jadwalDao.findById(id).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(idTahun).get();
        Mahasiswa mhs = mahasiswaDao.findByNim(idMahasiswa);
        KrsDetail krsDetail = krsDetailDao.findByJadwalAndTahunAkademikAndMahasiswaAndStatus(jadwal, tahunAkademik, mhs, StatusRecord.AKTIF);

        return krsDetail;

    }


//    Academic Year

    @GetMapping("/academic/year/list")
    public void academicList(Model model, @PageableDefault(size = 10) Pageable page, String search) {
        List<StatusRecord> statusRecords = new ArrayList<>();
        statusRecords.add(StatusRecord.AKTIF);
        statusRecords.add(StatusRecord.NONAKTIF);
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", tahunAkademikDao.findByStatusInAndNamaTahunAkademikContainingIgnoreCaseOrderByKodeTahunAkademikDesc(statusRecords, search, page));
        } else {
            model.addAttribute("list", tahunAkademikDao.cariTahunAkademik(StatusRecord.HAPUS, page));

        }
    }

    @GetMapping("/academic/year/form")
    public void academicForm(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("tahunAkademik", new TahunAkademik());

        if (id != null && !id.isEmpty()) {
            model.addAttribute("stringId", id);
            TahunAkademik tahunAkademik = tahunAkademikDao.findById(id).get();
            if (tahunAkademik != null) {
                model.addAttribute("tahunAkademik", tahunAkademik);
            }
        }
    }

    @PostMapping("/academic/year/form")
    public String prosesForm(@RequestParam String kodeTahunAkademik,
                             @RequestParam String tanggalMulai, @RequestParam String tanggalMulaiKrs, @RequestParam String tanggalMulaiKuliah,
                             @RequestParam String tanggalMulaiUts, @RequestParam String tanggalMulaiUas,
                             @RequestParam String tanggalMulaiNilai, @RequestParam StatusRecord jenis, @RequestParam String namaTahunAkademik,
                             @RequestParam String tanggalSelesai, @RequestParam String tanggalSelesaiKrs,
                             @RequestParam String tanggalSelesaiKuliah, @RequestParam String tanggalSelesaiUts,
                             @RequestParam String tanggalSelesaiUas, @RequestParam String tanggalSelesaiNilai,
                             @RequestParam String tahun, @RequestParam StatusRecord status, @RequestParam(required = false) String id){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


        if (id != null){
            TahunAkademik tahunAkademik = tahunAkademikDao.findById(id).get();
            tahunAkademik.setKodeTahunAkademik(kodeTahunAkademik);
            tahunAkademik.setTanggalMulai(LocalDate.parse(tanggalMulai, formatter));
            tahunAkademik.setTanggalMulaiKrs(LocalDate.parse(tanggalMulaiKrs, formatter));
            tahunAkademik.setTanggalMulaiKuliah(LocalDate.parse(tanggalMulaiKuliah, formatter));
            tahunAkademik.setTanggalMulaiUts(LocalDate.parse(tanggalMulaiUts, formatter));
            tahunAkademik.setTanggalMulaiUas(LocalDate.parse(tanggalMulaiUas, formatter));
            tahunAkademik.setTanggalMulaiNilai(LocalDate.parse(tanggalMulaiNilai, formatter));
            tahunAkademik.setJenis(jenis);
            tahunAkademik.setNamaTahunAkademik(namaTahunAkademik);
            tahunAkademik.setTanggalSelesai(LocalDate.parse(tanggalSelesai, formatter));
            tahunAkademik.setTanggalSelesaiKrs(LocalDate.parse(tanggalSelesaiKrs, formatter));
            tahunAkademik.setTanggalSelesaiKuliah(LocalDate.parse(tanggalSelesaiKuliah, formatter));
            tahunAkademik.setTanggalSelesaiKuliah(LocalDate.parse(tanggalSelesaiUas, formatter));
            tahunAkademik.setTanggalSelesaiUts(LocalDate.parse(tanggalSelesaiUts, formatter));
            tahunAkademik.setTanggalSelesaiNilai(LocalDate.parse(tanggalSelesaiNilai, formatter));
            tahunAkademik.setTahun(tahun);
            tahunAkademik.setStatus(status);
            tahunAkademikDao.save(tahunAkademik);

            List<TahunAkademikProdi> akademikProdi = tahunProdiDao.findByTahunAkademik(tahunAkademik);

            if (akademikProdi != null){
                for (TahunAkademikProdi tahunProdi : akademikProdi) {
                    tahunProdi.setMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
                    tahunProdi.setMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
                    tahunProdi.setMulaiUas(tahunAkademik.getTanggalMulaiUas());
                    tahunProdi.setMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
                    tahunProdi.setMulaiUts(tahunAkademik.getTanggalMulaiUts());
                    tahunProdi.setSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
                    tahunProdi.setSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
                    tahunProdi.setSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
                    tahunProdi.setSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
                    tahunProdi.setSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
                    tahunProdiDao.save(tahunProdi);
                }
            }

            if(akademikProdi ==null||akademikProdi.isEmpty())

            {
                List<Prodi> prodis = prodiDao.findByStatus(StatusRecord.AKTIF);
                for (Prodi prodi : prodis) {
                    TahunAkademikProdi tahunProdi = new TahunAkademikProdi();
                    tahunProdi.setMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
                    tahunProdi.setMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
                    tahunProdi.setMulaiUas(tahunAkademik.getTanggalMulaiUas());
                    tahunProdi.setMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
                    tahunProdi.setMulaiUts(tahunAkademik.getTanggalMulaiUts());
                    tahunProdi.setProdi(prodi);
                    tahunProdi.setSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
                    tahunProdi.setSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
                    tahunProdi.setSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
                    tahunProdi.setSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
                    tahunProdi.setSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
                    tahunProdi.setStatus(StatusRecord.NONAKTIF);
                    tahunProdi.setTahunAkademik(tahunAkademik);
                    tahunProdiDao.save(tahunProdi);

                }
            }

        }else {
            TahunAkademik tahunAkademik = new TahunAkademik();
            List<Prodi> prodis = prodiDao.findByStatus(StatusRecord.AKTIF);

            List<EdomQuestion> edomQuestion = edomQuestionDao.findByStatusAndTahunAkademikOrderByNomorAsc(StatusRecord.AKTIF,tahunAkademikDao.findByStatus(StatusRecord.AKTIF));


            tahunAkademik.setKodeTahunAkademik(kodeTahunAkademik);
            tahunAkademik.setTanggalMulai(LocalDate.parse(tanggalMulai, formatter));
            tahunAkademik.setTanggalMulaiKrs(LocalDate.parse(tanggalMulaiKrs, formatter));
            tahunAkademik.setTanggalMulaiKuliah(LocalDate.parse(tanggalMulaiKuliah, formatter));
            tahunAkademik.setTanggalMulaiUts(LocalDate.parse(tanggalMulaiUts, formatter));
            tahunAkademik.setTanggalMulaiUas(LocalDate.parse(tanggalMulaiUas, formatter));
            tahunAkademik.setTanggalMulaiNilai(LocalDate.parse(tanggalMulaiNilai, formatter));
            tahunAkademik.setJenis(jenis);
            tahunAkademik.setNamaTahunAkademik(namaTahunAkademik);
            tahunAkademik.setTanggalSelesai(LocalDate.parse(tanggalSelesai, formatter));
            tahunAkademik.setTanggalSelesaiKrs(LocalDate.parse(tanggalSelesaiKrs, formatter));
            tahunAkademik.setTanggalSelesaiKuliah(LocalDate.parse(tanggalSelesaiKuliah, formatter));
            tahunAkademik.setTanggalSelesaiUas(LocalDate.parse(tanggalSelesaiUas, formatter));
            tahunAkademik.setTanggalSelesaiUts(LocalDate.parse(tanggalSelesaiUts, formatter));
            tahunAkademik.setTanggalSelesaiNilai(LocalDate.parse(tanggalSelesaiNilai, formatter));
            tahunAkademik.setTahun(tahun);
            if (jenis == StatusRecord.PENDEK){
                tahunAkademik.setStatus(StatusRecord.PRAAKTIF);
            }else{
                tahunAkademik.setStatus(status);
            }
            tahunAkademikDao.save(tahunAkademik);
            for (EdomQuestion question : edomQuestion){
                EdomQuestion edomQues = new EdomQuestion();
                edomQues.setStatus(StatusRecord.AKTIF);
                edomQues.setBahasa(question.getBahasa());
                edomQues.setNomor(question.getNomor());
                edomQues.setPertanyaan(question.getPertanyaan());
                edomQues.setTahunAkademik(tahunAkademik);
                edomQuestionDao.save(edomQues);
            }

            for (Prodi prodi : prodis){
                TahunAkademikProdi tahunProdi = new TahunAkademikProdi();
                tahunProdi.setMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
                tahunProdi.setMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
                tahunProdi.setMulaiUas(tahunAkademik.getTanggalMulaiUas());
                tahunProdi.setMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
                tahunProdi.setMulaiUts(tahunAkademik.getTanggalMulaiUts());
                tahunProdi.setProdi(prodi);
                tahunProdi.setSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
                tahunProdi.setSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
                tahunProdi.setSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
                tahunProdi.setSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
                tahunProdi.setSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
                tahunProdi.setStatus(StatusRecord.NONAKTIF);
                tahunProdi.setTahunAkademik(tahunAkademik);
                tahunProdiDao.save(tahunProdi);
            }
        }

        return "redirect:list";
    }

    @PostMapping("/academic/year/delete")
    public String deleteAcademic(@RequestParam TahunAkademik tahunAkademik) {
        tahunAkademik.setStatus(StatusRecord.HAPUS);
        tahunAkademikDao.save(tahunAkademik);

        return "redirect:list";
    }

    @PostMapping("/academic/year/active")
    public String academicActive(@RequestParam TahunAkademik tahunAkademik){
        List<TahunAkademikProdi> tahunAkademikProdi = tahunProdiDao.findByStatus(StatusRecord.AKTIF);

        for(
                TahunAkademikProdi tahunProdi :tahunAkademikProdi)

        {
            tahunProdi.setStatus(StatusRecord.NONAKTIF);
            tahunProdiDao.save(tahunProdi);
        }

        TahunAkademik thnAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if(thnAkademik !=null)

        {
            thnAkademik.setStatus(StatusRecord.NONAKTIF);
            tahunAkademikDao.save(thnAkademik);
        }

        tahunAkademik.setStatus(StatusRecord.AKTIF);

        tahunAkademikDao.save(tahunAkademik);

        TahunAkademik akademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        List<TahunAkademikProdi> akademikProdi = tahunProdiDao.findByTahunAkademik(akademik);
        if(akademikProdi !=null)

        {
            for (TahunAkademikProdi tahun : akademikProdi) {
                tahun.setStatus(StatusRecord.AKTIF);
                tahunProdiDao.save(tahun);
            }
        }
        if(akademikProdi ==null||akademikProdi.isEmpty())

        {
            List<Prodi> prodis = prodiDao.findByStatus(StatusRecord.AKTIF);
            for (Prodi prodi : prodis) {
                TahunAkademikProdi tahunProdi = new TahunAkademikProdi();
                tahunProdi.setMulaiKuliah(tahunAkademik.getTanggalMulaiKuliah());
                tahunProdi.setMulaiNilai(tahunAkademik.getTanggalMulaiNilai());
                tahunProdi.setMulaiUas(tahunAkademik.getTanggalMulaiUas());
                tahunProdi.setMulaiKrs(tahunAkademik.getTanggalMulaiKrs());
                tahunProdi.setMulaiUts(tahunAkademik.getTanggalMulaiUts());
                tahunProdi.setProdi(prodi);
                tahunProdi.setSelesaiKrs(tahunAkademik.getTanggalSelesaiKrs());
                tahunProdi.setSelesaiKuliah(tahunAkademik.getTanggalSelesaiKuliah());
                tahunProdi.setSelesaiNilai(tahunAkademik.getTanggalSelesaiNilai());
                tahunProdi.setSelesaiUas(tahunAkademik.getTanggalSelesaiUas());
                tahunProdi.setSelesaiUts(tahunAkademik.getTanggalSelesaiUts());
                tahunProdi.setStatus(StatusRecord.AKTIF);
                tahunProdi.setTahunAkademik(tahunAkademik);
                tahunProdiDao.save(tahunProdi);

            }
        }


        return"redirect:list";

    }

    @GetMapping("/academic/prodi/list")
    public void yearProdiList(Model model, @RequestParam(required = false) TahunAkademik tahunAkademik){
        model.addAttribute("selectedTahun", tahunAkademik);
        if (tahunAkademik != null) {
            model.addAttribute("search", tahunAkademik);
            model.addAttribute("prodiTahunAkademik", tahunProdiDao.tahunAkademikProdiGet(tahunAkademik));
        }
        model.addAttribute("tahunAkademik1", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("listYearProdi", tahunProdiDao.listTahaunAkademikProdi());
    }


//Curriculum

    @GetMapping("/academic/curriculum/list")
    public void curriculumList(Model model,@RequestParam(required = false) Prodi prodi,
                               @PageableDefault(direction = Sort.Direction.DESC,sort = "tahunKurikulum") Pageable page){
        List<StatusRecord> statusRecords = new ArrayList<>();
        statusRecords.add(StatusRecord.AKTIF);
        statusRecords.add(StatusRecord.NONAKTIF);
        model.addAttribute("prodi",prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));

        if (prodi != null){
            model.addAttribute("selected",prodi);
            model.addAttribute("kurikulum",kurikulumDao.findByStatusInAndProdi(statusRecords,prodi));
        }
    }

    @Transactional
    @PostMapping("/academic/curriculum/aktif")
    public String aktifKurikulum(@RequestParam Kurikulum kurikulum){
        kurikulumDao.nonaktifKurikulum(kurikulum.getProdi());


        kurikulum.setStatus(StatusRecord.AKTIF);
        kurikulumDao.save(kurikulum);

        return "redirect:list?prodi="+kurikulum.getProdi().getId();
    }

    @GetMapping("/academic/curriculum/form")
    public void curriculumForm(Model model,@RequestParam(required = false, name = "id")
            Kurikulum kurikulum){
        model.addAttribute("prodi",prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("kurikulum", new Kurikulum());

        if (kurikulum != null){
            model.addAttribute("kurikulum",kurikulum);
        }
    }

    @PostMapping("/academic/curriculum/form")
    public String prosesCurriculum(@Valid Kurikulum kurikulum) {
        kurikulum.setJumlahSesi(2);
        kurikulumDao.save(kurikulum);

        return "redirect:list?prodi="+kurikulum.getProdi().getId();
    }


    @PostMapping("/academic/curriculum/delete")
    public String deleteCurriculum(@Valid Kurikulum kurikulum) {
        kurikulum.setStatus(StatusRecord.HAPUS);
        kurikulumDao.save(kurikulum);

        return "redirect:list?prodi="+kurikulum.getProdi().getId();
    }

//    Courses


    @GetMapping("/academic/courses/list")
    public void listCourses(Model model,@RequestParam(required = false) String search, Pageable pageable){
        if (!StringUtils.isEmpty(search)){
            model.addAttribute("search", search);
            model.addAttribute("courses", matakuliahDao.findByNamaMatakuliahContainingIgnoreCaseOrNamaMatakuliahEnglishContainingIgnoreCase(search,search,pageable));
        }else {
            model.addAttribute("courses", matakuliahDao.findByStatusNotInOrderByNamaMatakuliahAsc(Arrays.asList(StatusRecord.HAPUS),pageable));
        }
    }

    @GetMapping("/academic/courses/form")
    public void formCourses(Model model,@RequestParam(required = false, name = "id")
            Matakuliah matakuliah){
        model.addAttribute("courses", new Matakuliah());

        if (matakuliah != null){
            model.addAttribute("courses",matakuliah);
        }
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

    //    Curriculum Courses

    @GetMapping("/academic/curriculumCourses/list")
    public void listCurriculumCourses(Model model, @RequestParam(required = false) Prodi prodi,
                                      @RequestParam(required = false) Kurikulum kurikulum){
        model.addAttribute("listProdi",prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("listKurikulum",kurikulumDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));

        if (prodi != null && kurikulum != null){
            List<Kurikulum> k = kurikulumDao.findByProdiAndStatusNotInAndIdNotIn(prodi,Arrays.asList(StatusRecord.HAPUS),Arrays.asList(kurikulum.getId()));
            model.addAttribute("kurikulum", k);
            model.addAttribute("selected",prodi);
            model.addAttribute("kurikulumSelected",kurikulum);
            model.addAttribute("satu",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,1));
            model.addAttribute("dua",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,2));
            model.addAttribute("tiga",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,3));
            model.addAttribute("empat",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,4));
            model.addAttribute("lima",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,5));
            model.addAttribute("enam",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,6));
            model.addAttribute("tujuh",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,7));
            model.addAttribute("delapan",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,8));
            model.addAttribute("sembilan",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(Arrays.asList(StatusRecord.HAPUS),kurikulum,prodi,9));
        }
    }

    @GetMapping("/academic/curriculumCourses/form")
    public void formCurriculumCourses(@RequestParam(required = false) String prodi,@RequestParam(required = false) String kurikulum,
                                      @RequestParam(required = false)String id, Model model){

        model.addAttribute("matakuliah", new MatakuliahKurikulum());

        if (id != null){
            model.addAttribute("matakuliah", matakuliahKurikulumDao.findById(id).get());
            model.addAttribute("prodi",prodiDao.findById(prodi).get());
            model.addAttribute("kurikulum",kurikulumDao.findById(kurikulum).get());
        }else {
            model.addAttribute("prodi",prodiDao.findById(prodi).get());
            model.addAttribute("kurikulum",kurikulumDao.findById(kurikulum).get());
        }
    }

    @PostMapping("/academic/curriculumCourses/form")
    public String prosesCurriculumCourses(@ModelAttribute @Valid MatakuliahKurikulum matakuliahKurikulum, MultipartFile file)throws Exception{
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

        return "redirect:list?prodi="+matakuliahKurikulum.getKurikulum().getProdi().getId()+"&kurikulum="+matakuliahKurikulum.getKurikulum().getId();
    }

    @PostMapping("/academic/curriculumCourses/delete")
    public String deleteCurriculumCourse(@RequestParam(value = "id", name = "id") MatakuliahKurikulum matakuliahKurikulum){
        Long jumlahMatakuliah = krsDetailDao.hitungMatakuliahKurikulum(matakuliahKurikulum);
        if (jumlahMatakuliah == 0){
            matakuliahKurikulum.setStatus(StatusRecord.HAPUS);
            matakuliahKurikulumDao.save(matakuliahKurikulum);
        }


        return "redirect:list?prodi="+matakuliahKurikulum.getKurikulum().getProdi().getId()+"&kurikulum="+matakuliahKurikulum.getKurikulum().getId();
    }

    @PostMapping("/academic/curriculumCourses/copy")
    public String copyMatakuliah(@RequestParam(value = "id", name = "id") Kurikulum selectedKurikulum,
                                 @RequestParam(value = "prodi", name = "prodi") Prodi prodi,
                                 @RequestParam(value = "kurikulum", name = "kurikulum") Kurikulum kurikulum){
        List<MatakuliahKurikulum> mk = matakuliahKurikulumDao.findByStatusAndKurikulumAndSemesterNotNull(StatusRecord.AKTIF,selectedKurikulum);
        System.out.println(mk.size());
        for (MatakuliahKurikulum k : mk){
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
            for (Program program : k.getPrograms()){
                programs.add(program);
            }
            mkKurikulum.setPrograms(programs);
            matakuliahKurikulumDao.save(mkKurikulum);
        }
        return "redirect:list?prodi="+prodi.getId()+"&kurikulum="+kurikulum.getId();
    }

    @GetMapping("/academic/curriculumCourses/prasyarat")
    public void formPrasyarat(Model model, @RequestParam(name = "id", value = "id") MatakuliahKurikulum matakuliahKurikulum,
                              @RequestParam(required = false) String pras){
        model.addAttribute("pras", new Prasyarat());
        model.addAttribute("matakuliahKurikulum", matakuliahKurikulum);
        List<MatakuliahKurikulum> mk = matakuliahKurikulumDao.findByStatusAndKurikulumAndSemesterNotNull(StatusRecord.AKTIF,matakuliahKurikulum.getKurikulum());
        model.addAttribute("listPrasyarat", prasyaratDao.findByMatakuliahKurikulumAndStatus(matakuliahKurikulum,StatusRecord.AKTIF));
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
    public String deletePras(@RequestParam(value = "id", name = "id") Prasyarat prasyarat){

        prasyarat.setStatus(StatusRecord.HAPUS);
        prasyaratDao.save(prasyarat);
        return "redirect:../prasyarat?id="+prasyarat.getMatakuliahKurikulum().getId();
    }

    @GetMapping("/academic/curriculumCourses/matakuliahSetara/list")
    public void listMatakuliahSetara(Model model, @RequestParam(required = false) MatakuliahKurikulum matakuliahKurikulum,
                                     String prodi, String kurikulum,@PageableDefault(size = Integer.MAX_VALUE) Pageable page){
        model.addAttribute("kurikulum", kurikulum);
        model.addAttribute("prodi", prodi);
        model.addAttribute("setara", mataKuliahSetaraDao.pilihanSetara(matakuliahKurikulum.getMatakuliah()));
        model.addAttribute("matakuliahKurikulum", matakuliahKurikulumDao.findById(matakuliahKurikulum.getId()).get());
        model.addAttribute("listMatakuliahSetara", mataKuliahSetaraDao.listMatakuliahSetara(matakuliahKurikulum.getMatakuliah().getId()));
        model.addAttribute("listMatakuliah" , matakuliahDao.pilihMatakuliahSetara(matakuliahKurikulum.getMatakuliah().getId(), page));

    }

    @PostMapping("/academic/curriculumCourses/matakuliahSetara/proses")
    public String prosesMatakuliahSetara(@RequestParam String defaultMatkul, @RequestParam(required = false) String[] listMatakuliah,
                                         @RequestParam MatakuliahKurikulum matakuliahKurikulum, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        List<String> matakuliah = new ArrayList<>();
        matakuliah.addAll(Arrays.asList(listMatakuliah));
        matakuliah.add(defaultMatkul);

        for (String m : matakuliah){
            for (String setara : matakuliah){
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


        return "redirect:list?matakuliahKurikulum=" + matakuliahKurikulum.getId()+"&kurikulum="+matakuliahKurikulum.getKurikulum().getId()+"&prodi="+matakuliahKurikulum.getKurikulum().getProdi().getId();

    }

    @PostMapping("/academic/curriculumCourses/matakuliahSetara/delete")
    public String hapusMatakuliahSetara(@RequestParam MatakuliahSetara matakuliahSetara, @RequestParam String matakuliahKurikulum, Authentication authentication){
        User user = currentUserService.currentUser(authentication);

        matakuliahSetara.setStatus(StatusRecord.HAPUS);
        matakuliahSetara.setTanggalDelete(LocalDateTime.now());
        matakuliahSetara.setUserDelete(user.getUsername());
        mataKuliahSetaraDao.save(matakuliahSetara);

        return "redirect:list?matakuliahKurikulum=" + matakuliahKurikulum;
    }

    //    Ploting


    @GetMapping("/academic/ploting/list")
    public void listPloting(Model model, @RequestParam(required = false)String angkatan,
                            @RequestParam(required = false)Prodi prodi,@RequestParam(required = false)Integer semester){

        if (prodi != null) {
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("selectedAngkatan", angkatan);
            model.addAttribute("selectedSemester", semester);
            model.addAttribute("listDosen", dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));

            model.addAttribute("ploting", matakuliahKurikulumDao.plotingDosen(prodi, angkatan, semester));
        }
    }

    @PostMapping("/academic/ploting/list")
    public String prosesPloting(HttpServletRequest request,@RequestParam String angkatan,
                                @RequestParam Prodi prodi,@RequestParam Integer semester){
        List<Object[]> matakuliahKurikulum = matakuliahKurikulumDao.plotingDosen(prodi, angkatan, semester);

        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByStatusAndProdi(StatusRecord.AKTIF, prodi);
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahunAkademikProdi.getTahunAkademik().getId()).get();

        for (Object[] mk : matakuliahKurikulum){
            String pilihan = request.getParameter(mk[0].toString()+mk[2].toString()+mk[3].toString());
            if (pilihan != null && !pilihan.trim().isEmpty()) {
                Jadwal jadwal = new Jadwal();
                jadwal.setJumlahSesi(1);
                jadwal.setBobotUts(BigDecimal.ZERO);
                jadwal.setBobotUas(BigDecimal.ZERO);
                jadwal.setBobotPresensi(BigDecimal.ZERO);
                jadwal.setBobotTugas(BigDecimal.ZERO);
                jadwal.setKelas(kelasDao.findById(mk[5].toString()).get());
                jadwal.setProdi(prodi);
                jadwal.setDosen(dosenDao.findById(pilihan).get());
                jadwal.setTahunAkademik(tahunAkademik);
                jadwal.setTahunAkademikProdi(tahunAkademikProdi);
                jadwal.setMatakuliahKurikulum(matakuliahKurikulumDao.findById(mk[1].toString()).get());
                jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
                jadwal.setProgram(programDao.findById("01").get());
                jadwal.setAkses(Akses.TERTUTUP);
                jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
                jadwalDao.save(jadwal);

            }
        }

        return "redirect:list";


    }


    //    Schedule


    @GetMapping("/academic/schedule/list")
    public void listSchedule(Model model, @RequestParam(required = false) Prodi prodi,
                             @RequestParam(required = false) TahunAkademik tahunAkademik,
                             @RequestParam(required = false) Hari hari,
                             @RequestParam(required = false) String bahasa){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedProdi",prodi);
        model.addAttribute("bahasa", bahasa);

        if (prodi != null && tahunAkademik != null && hari != null){
            model.addAttribute("jadwal", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hari));
            model.addAttribute("ploting", jadwalDao.ploting(prodi,tahunAkademik));
        }


        if (prodi != null && tahunAkademik != null && hari == null){
            model.addAttribute("ploting", jadwalDao.ploting(prodi,tahunAkademik));
            model.addAttribute("minggu", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hariDao.findById("0").get()));
            model.addAttribute("senin", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hariDao.findById("1").get()));
            model.addAttribute("selasa", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hariDao.findById("2").get()));
            model.addAttribute("rabu", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hariDao.findById("3").get()));
            model.addAttribute("kamis", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hariDao.findById("4").get()));
            model.addAttribute("jumat", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hariDao.findById("5").get()));
            model.addAttribute("sabtu", jadwalDao.schedule(prodi,StatusRecord.AKTIF,tahunAkademik,hariDao.findById("6").get()));
        }
    }

    @GetMapping("/academic/schedule/form")
    public void scheduleForm(Model model,@RequestParam(name = "id", value = "id") Jadwal jadwal,@RequestParam(required = false) String plot){
        if (plot != null){
            model.addAttribute("plot",plot);
        }

        model.addAttribute("jadwal",jadwal);
        model.addAttribute("hari", hariDao.findAll());
    }

    @GetMapping("/academic/schedule/ploting")
    public void plotingSchedule(Model model,@RequestParam(name = "id", value = "id") Prodi prodi,@RequestParam(name = "tahun", value = "tahun") TahunAkademik tahunAkademik){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedProdi",prodi);
        model.addAttribute("ploting", jadwalDao.ploting(prodi,tahunAkademik));
        model.addAttribute("hari", hariDao.findAll());

    }

    @PostMapping("/academic/schedule/ploting")
    public String prosesPloting(HttpServletRequest request,@RequestParam(name = "id", value = "id") Prodi prodi,@RequestParam(name = "tahun", value = "tahun") TahunAkademik tahunAkademik){
        List<PlotingDto> jadwal = jadwalDao.ploting(prodi,tahunAkademik);
        for (PlotingDto j : jadwal){
            String mulai = request.getParameter(j.getId()+"_mulai");
            String selesai = request.getParameter(j.getId()+"_selesai");
            String sesi = request.getParameter(j.getId()+"_isiSesi");
            String ruangan = request.getParameter(j.getId()+"_roomisi");
            String hari = request.getParameter(j.getId()+"_hariisi");
            String idSesi = request.getParameter(j.getId()+"_sesii");
            if (hari!= null && !hari.trim().isEmpty()){
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

        return "redirect:list?tahunAkademik="+tahunAkademik.getId()+"&prodi="+prodi.getId();


    }

    @GetMapping("/academic/schedule/student")
    public void studentSchedule(@RequestParam(value = "id", name = "id")Jadwal jadwal,Model model){
        model.addAttribute("student", krsDetailDao.cariJadwalMahasiswa(jadwal));
    }

    @PostMapping("/academic/schedule/form")
    public String prosesSchedule(Model model,
                                 @ModelAttribute @Valid Jadwal jadwal,
                                 RedirectAttributes attributes,
                                 @RequestParam Sesi sesii){

        List<Jadwal> jdwl = jadwalDao.cariJadwal(Arrays.asList(jadwal.getId()),jadwal.getTahunAkademik(),jadwal.getHari(),jadwal.getRuangan(),jadwal.getSesi(),StatusRecord.AKTIF);
        System.out.println(sesii);

        Integer jumlahBentrok = jadwalDao.jumlahBentrok(jadwal.getDosen().getId(), jadwal.getSesi(), jadwal.getTahunAkademik().getId(), jadwal.getHari().getId(), jadwal.getId());
        List<Object[]> listBentrok = jadwalDao.cariJadwalAKtifDosen(jadwal.getDosen().getId(), jadwal.getSesi(), jadwal.getTahunAkademik().getId(), jadwal.getHari().getId(), jadwal.getId());

        if(jumlahBentrok == null){
            jumlahBentrok = 0;
        }

        if (jumlahBentrok > 0){

            model.addAttribute("bentrokJadwal", listBentrok);
            model.addAttribute("jadwal", jadwal);

            attributes.addFlashAttribute("bentrokJadwal", listBentrok);
            return "redirect:form?id="+ jadwal.getId();

        }

        if (jdwl == null | jdwl.isEmpty()){
//            jadwal.setJamMulai(sesii.getJamMulai());
//            jadwal.setJamSelesai(sesii.getJamSelesai());
            jadwalDao.save(jadwal);


            JadwalDosen jd = jadwalDosenDao.findByJadwalAndStatusJadwalDosen(jadwal,StatusJadwalDosen.PENGAMPU);
            if (jd == null) {

                JadwalDosen jadwalDosen = new JadwalDosen();
                jadwalDosen.setDosen(jadwal.getDosen());
                jadwalDosen.setJadwal(jadwal);
                jadwalDosen.setStatusJadwalDosen(StatusJadwalDosen.PENGAMPU);
                jadwalDosenDao.save(jadwalDosen);
            }else {
                jd.setDosen(jadwal.getDosen());
                jadwalDosenDao.save(jd);
            }

        }else {
            attributes.addFlashAttribute("validJadwal", jadwal);
            return "redirect:form?id=" + jadwal.getId();
        }

        return "redirect:list?tahunAkademik="+ jadwal.getTahunAkademik().getId() +"&prodi="+jadwal.getProdi().getId();

    }

    @PostMapping("/academic/schedule/delete")
    public String deleteSchedule(@RequestParam String jadwal){

        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();

        jadwal1.setStatus(StatusRecord.HAPUS);
        jadwalDao.save(jadwal1);

        List<JadwalDosen> jadwalDosen = jadwalDosenDao.findByJadwal(jadwal1);
        for (JadwalDosen jd : jadwalDosen){
            jadwalDosenDao.delete(jd);
        }

        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal1,StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail){
            kd.setStatus(StatusRecord.HAPUS);
            krsDetailDao.save(kd);
        }

        return "redirect:list?tahunAkademik="+ jadwal1.getTahunAkademik().getId() +"&prodi="+jadwal1.getProdi().getId();
    }

    @GetMapping("/academic/schedule/team")
    public void teamTeaching(Model model,@RequestParam Jadwal jadwal){

        model.addAttribute("jadwal",jadwalDosenDao.findByStatusJadwalDosenAndJadwal(StatusJadwalDosen.TEAM,jadwal));
        model.addAttribute("data",jadwal);
        model.addAttribute("hari", hariDao.findAll());
    }

    @PostMapping("/academic/schedule/team")
    public String prosesTeamTeaching(@Valid JadwalDosen jadwalDosen){

        jadwalDosenDao.save(jadwalDosen);
        return "redirect:team?jadwal="+jadwalDosen.getJadwal().getId();
    }

    @PostMapping("/academic/schedule/deleteTeam")
    public String deleteTeamTeaching(@RequestParam JadwalDosen jadwalDosen){

        jadwalDosenDao.delete(jadwalDosen);
        return "redirect:team?jadwal="+jadwalDosen.getJadwal().getId();
    }

    @GetMapping("/academic/schedule/room")
    public void roomSchedule(Model model, @RequestParam(required = false) Gedung gedung, @RequestParam(required = false) Hari hari){
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        model.addAttribute("hari", hariDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("gedung", gedungDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedGedung",gedung);


        if (gedung !=null && hari != null){

            Iterable<Ruangan> ruangan = ruanganDao.findByStatusAndGedung(StatusRecord.AKTIF,gedung);
            List<RoomDto> sesi = new ArrayList<>();
            for (Ruangan ruang1 : ruangan ){

                RoomDto roomDto = new RoomDto();
                roomDto.setId(ruang1.getId());
                roomDto.setRuangan1(ruang1.getNamaRuangan());

                List<Jadwal> jadwal1= jadwalDao.findByStatusAndTahunAkademikAndHariAndRuangan
                        (StatusRecord.AKTIF,tahunAkademik,hari,ruang1);

                for (Jadwal j : jadwal1){

                    if (j.getSesi().equals("1")){
                        roomDto.setSesi1(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen1(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas1(j.getKelas().getNamaKelas());
                    }

                    if (j.getSesi().equals("2")){
                        roomDto.setSesi2(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen2(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas2(j.getKelas().getNamaKelas());
                    }

                    if (j.getSesi().equals("3")){
                        roomDto.setSesi3(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen3(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas3(j.getKelas().getNamaKelas());
                    }

                    if (j.getSesi().equals("4")){
                        roomDto.setSesi4(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                        roomDto.setDosen4(j.getDosen().getKaryawan().getNamaKaryawan());
                        roomDto.setKelas4(j.getKelas().getNamaKelas());
                    }

                }

                sesi.add(roomDto);

            }

            model.addAttribute("jadwalRuang",sesi);

        }
    }

    @GetMapping("/academic/schedule/krs")
    public void addKrs(Model model,@RequestParam Jadwal jadwal){
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("krs", krsDao.krsList(jadwal.getTahunAkademik(), jadwal));
    }

    @PostMapping("/academic/schedule/krs")
    public String postKrs(@RequestParam(required = false) Jadwal jadwal, @RequestParam(required = false) String[] data){
        if (data != null){
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

        return "redirect:list?tahunAkademik="+jadwal.getTahunAkademik().getId()+"&prodi="+jadwal.getProdi().getId();
    }

    //    Uts & Uas
    @GetMapping("/academic/schedule/uts")
    public void absenUts(@RequestParam Jadwal jadwal,Model model){

        model.addAttribute("jadwal", jadwal);
        model.addAttribute("absen", krsDetailDao.absenUts(jadwal,jadwal.getTahunAkademik()));

    }

    @GetMapping("/academic/schedule/uas")
    public void absenUas(@RequestParam Jadwal jadwal,Model model){

//        Integer tahun = Integer.parseInt(jadwal.getTahunAkademik().getTahun()) + 1;
        model.addAttribute("jadwal", jadwal);
//        model.addAttribute("tahun", tahun);
        model.addAttribute("absen", krsDetailDao.absenUas(jadwal,jadwal.getTahunAkademik()));

    }


// Conversion

    @GetMapping("/academic/conversion/list")
    public void list(Model model, @RequestParam(required = false)String nim){

        if (nim != null){
            model.addAttribute("nim", nim);
            model.addAttribute("list", krsDetailDao.listKrsDetail(nim));
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            model.addAttribute("mhs", mahasiswa);
        }

    }

    @GetMapping("/academic/conversion/form")
    public void form(Model model, @RequestParam(required = false) String nim,
                     @RequestParam(required = false) Jadwal jadwal){

        model.addAttribute("krsDetail", new KrsDetail());
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
//        Optional<Jadwal> jadwal1 = jadwalDao.findById(jadwal);
        model.addAttribute("grade", gradeDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("mahasiswa", mhs);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));

    }

    @PostMapping("/academic/conversion/form")
    public String inputForm(@RequestParam(required = false) String nim, @RequestParam(required = false) String ta,
                            @RequestParam(required = false) Jadwal jadwal, @RequestParam(required = false) String matakuliahLama,
                            @RequestParam(required = false) String grade, RedirectAttributes attributes){
        int jumlahSks;
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(ta).get();
        if (krsDetailDao.jumlahSksMahasiswa(mhs.getId(), tahunAkademik.getId()) == null){
            jumlahSks = 0;
        }else {
            jumlahSks =krsDetailDao.jumlahSksMahasiswa(mhs.getId(), tahunAkademik.getId()).intValue();

        }
        Grade grade1 = gradeDao.findByNama(grade);
        Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahunAkademik, StatusRecord.AKTIF);
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mhs.getIdProdi());
        KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndTahunAkademikAndJadwalAndStatus(mhs, tahunAkademik, jadwal, StatusRecord.AKTIF);
        if (jumlahSks <= 24) {
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
                    kd.setFinalisasi("N");
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
                    kd.setFinalisasi("N");
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
        }else{
            attributes.addFlashAttribute("gagalSks", "Sks sudah lebih");
            return "redirect:form?nim=" + mhs.getNim() + "&jadwal=" + jadwal.getId();
        }


        return "redirect:list?nim="+mhs.getNim();

    }


}
