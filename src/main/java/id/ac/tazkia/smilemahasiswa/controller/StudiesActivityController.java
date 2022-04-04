package id.ac.tazkia.smilemahasiswa.controller;

import com.github.mustachejava.MustacheFactory;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.KrsNilaiTugasDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreHitungDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreInput;
import id.ac.tazkia.smilemahasiswa.dto.attendance.JadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.krs.KrsSpDto;
import id.ac.tazkia.smilemahasiswa.dto.krs.SpDto;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.dto.room.KelasMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.dto.transkript.TranskriptDto;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
//import id.ac.tazkia.smilemahasiswa.service.MailService;
import id.ac.tazkia.smilemahasiswa.service.PresensiService;
import id.ac.tazkia.smilemahasiswa.service.ScoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller @Slf4j
public class StudiesActivityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudiesActivityController.class);

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private JenjangDao jenjangDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private MustacheFactory mustacheFactory;

    @Autowired
    private SesiKuliahDao sesiKuliahDao;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private PresensiService presensiService;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private NilaiTugasDao nilaiTugasDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private IpkDao ipkDao;

    @Autowired
    private BobotTugasDao bobotTugasDao;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private SoalDao soalDao;

    @Autowired
    private RpsDao rpsDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired private ScoreService scoreService;

    @Autowired
    private PraKrsSpDao praKrsSpDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private EdomMahasiswaDao edomMahasiswaDao;

    @Autowired
    private EdomQuestionDao edomQuestionDao;

//    @Autowired
//    private MailService mailService;

    @Value("classpath:sample/soal.doc")
    private Resource contohSoal;

    @Value("classpath:/sample/khs.xlsx")
    private Resource contohExcelKhs;

    @Value("classpath:/sample/transkript.xlsx")
    private Resource contohExcelTranskript;

    @Value("classpath:/sample/transkriptIndo.xlsx")
    private Resource contohExcelTranskriptIndo;

    @Value("classpath:sample/uas.doc")
    private Resource contohSoalUas;

    @Value("classpath:tazkia.png")
    private Resource logoTazkia;

    @Value("classpath:tazkia1.png")
    private Resource logoTazkia1;

    @Value("classpath:tazkia2.png")
    private Resource logoTazkia2;

    @Value("${upload.soal}")
    private String uploadFolder;

    @Value("${upload.rps}")
    private String uploadRps;

    //    Attribute

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findAll();
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademik> tahunAkademik() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS));
    }

    @ModelAttribute("hari")
    public Iterable<Hari> hari() {
        return hariDao.findAll();
    }

//    API
        @GetMapping("/api/nilai")
        @ResponseBody
        public KrsDetail findByJadwal(@RequestParam(required = false) KrsDetail krsDetail ,Model model){
            model.addAttribute("otomatisNilai", krsDetail);
            return krsDetail;
        }

        @GetMapping("/api/mahasiswa")
        @ResponseBody
        public Mahasiswa findByNim(@RequestParam(required = false) String nim ,Model model){
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            model.addAttribute("mahasiswa", mahasiswa);
            return mahasiswa;
        }

//    Attendance

    @GetMapping("/studiesActivity/attendance/listdosen")
    public void listLectureAttendance(Model model, Authentication authentication, @RequestParam(required = false) TahunAkademik tahunAkademik){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        if (tahunAkademik != null){
            model.addAttribute("selectedTahun", tahunAkademik);
            Iterable<JadwalDosen> jadwal = jadwalDosenDao.findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNullOrderByJadwalHariAscJadwalJamMulaiAsc(Arrays.asList(StatusRecord.HAPUS), tahunAkademik,dosen);
            model.addAttribute("jadwal", jadwal);

        }else {
            TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            Iterable<JadwalDosen> jadwal = jadwalDosenDao.findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNullOrderByJadwalHariAscJadwalJamMulaiAsc(Arrays.asList(StatusRecord.HAPUS), tahun,dosen);
            model.addAttribute("jadwal", jadwal);
        }


    }

    @GetMapping("/studiesActivity/attendance/list")
    public void listAttendance(Model model, @RequestParam(required = false) Prodi prodi,
                               @RequestParam(required = false) TahunAkademik tahunAkademik,
                               @RequestParam(required = false) Hari hari) {
        model.addAttribute("selectedTahun", tahunAkademik);
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedProdi", prodi);

        if (prodi != null && tahunAkademik != null && hari != null) {
            model.addAttribute("ploting", jadwalDao.ploting(prodi, tahunAkademik));
            model.addAttribute("jadwal", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hari));
        }


        if (prodi != null && tahunAkademik != null && hari == null) {
            model.addAttribute("ploting", jadwalDao.ploting(prodi, tahunAkademik));
            model.addAttribute("minggu", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("0").get()));
            model.addAttribute("senin", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("1").get()));
            model.addAttribute("selasa", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("2").get()));
            model.addAttribute("rabu", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("3").get()));
            model.addAttribute("kamis", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("4").get()));
            model.addAttribute("jumat", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("5").get()));
            model.addAttribute("sabtu", jadwalDao.schedule(prodi, StatusRecord.AKTIF, tahunAkademik, hariDao.findById("6").get()));
        }
    }

    @GetMapping("/studiesActivity/attendance/detail")
    public void detailAttendance(Model model, @RequestParam Jadwal jadwal) {
        List<SesiKuliah> sesiKuliah = sesiKuliahDao.findByJadwalAndPresensiDosenStatusOrderByWaktuMulai(jadwal, StatusRecord.AKTIF);

        List<JadwalDto> detail = new ArrayList<>();
        for (SesiKuliah sk : sesiKuliah) {
            JadwalDto jadwalDto = new JadwalDto();
            jadwalDto.setJadwal(sk.getJadwal());
            jadwalDto.setBeritaAcara(sk.getBeritaAcara());
            jadwalDto.setPresensiDosen(sk.getPresensiDosen());
            jadwalDto.setId(sk.getId());
            LocalDateTime jamMasuk = sk.getWaktuMulai();
            LocalDateTime jamSelesai = sk.getWaktuSelesai();
            DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm:ss");
            jadwalDto.setWaktuMulai(jamMasuk.format(format));
            jadwalDto.setWaktuSelesai(jamSelesai.format(format));
            detail.add(jadwalDto);

        }
        model.addAttribute("detail", detail);
        model.addAttribute("dosenUtama", jadwal.getDosen());
        model.addAttribute("teamTeaching", jadwalDosenDao.findByJadwal(jadwal));

//        String jamMulai1 = jadwal.getJamMulai().toString().substring(0,5);
//        String jamSelesai1 = jadwal.getJamSelesai().toString().substring(0,5);
//        LocalTime jamMulai2 = LocalTime.parse(jamMulai1);
//        LocalTime jamSelesai2 = LocalTime.parse(jamSelesai1);
//        DateTimeFormatter format2 = DateTimeFormatter.ofPattern("hh:mm");
//        jadwal.setJamMulai(LocalTime.parse(jamMulai1).);
//        jadwal.setJamSelesai(LocalTime.parse(jamSelesai1));
        model.addAttribute("jadwal", jadwal);

    }

    @PostMapping("/studiesActivity/attendance/detail")
    public String createPresensi(@ModelAttribute @Valid JadwalDto jadwalDto){

        String jamMulai1 = jadwalDto.getJamMulai().toString().substring(0,5);
        LocalTime jamMulai2 = LocalTime.parse(jamMulai1);
        DateTimeFormatter format2 = DateTimeFormatter.ofPattern("hh:mm");

        presensiService.inputPresensi(jadwalDto.getDosen(),
                jadwalDto.getJadwal(), jadwalDto.getBeritaAcara(),
                LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamMulai()));

        return "redirect:detail?jadwal=" + jadwalDto.getJadwal().getId();

    }

    @GetMapping("/studiesActivity/attendance/mahasiswa")
    public void attendance(@RequestParam(name = "id",value = "id") SesiKuliah sesiKuliah, Model model){
        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliahAndStatus(sesiKuliah,StatusRecord.AKTIF);
        List<Mahasiswa> mahasiswas = new ArrayList<>();


        Map<String, String> statusPresensi = new HashMap<>();
        for(PresensiMahasiswa pm : presensiMahasiswa){
            mahasiswas.add(pm.getMahasiswa());
            statusPresensi.put(pm.getId(), pm.getStatusPresensi().toString());
        }
        if (!mahasiswas.isEmpty()) {
            List<KrsDetail> krsDetails = krsDetailDao.findByMahasiswaNotInAndJadwalAndStatus(mahasiswas, sesiKuliah.getJadwal(), StatusRecord.AKTIF);
            model.addAttribute("detail", krsDetails);
        }else {
            List<KrsDetail> krsDetails = krsDetailDao.findByJadwalAndStatus(sesiKuliah.getJadwal(),StatusRecord.AKTIF);
            model.addAttribute("detail", krsDetails);

        }
        model.addAttribute("statusPresensi", StatusPresensi.values());
        model.addAttribute("status", statusPresensi);
        model.addAttribute("presensi", presensiMahasiswa);
        model.addAttribute("jadwal", sesiKuliah.getJadwal().getId());
        model.addAttribute("sesi",sesiKuliah.getId());
        model.addAttribute("statusPresensi", StatusPresensi.values());
    }

    @PostMapping("/studiesActivity/attendance/mahasiswa")
    public String prosesAttendance(@RequestParam String jadwal, @RequestParam String sesi, HttpServletRequest request){
        Jadwal j = jadwalDao.findById(jadwal).get();
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(sesi).get();

        for (PresensiMahasiswa presensiMahasiswa : presensiMahasiswaDao.findBySesiKuliahAndStatus(sesiKuliah,StatusRecord.AKTIF)){
            String pilihan = request.getParameter(presensiMahasiswa.getMahasiswa().getNim() + "nim");
            if (pilihan == null || pilihan.isEmpty()){
                System.out.println("gaada");
            }else {
                presensiMahasiswa.setMahasiswa(presensiMahasiswa.getMahasiswa());
                StatusPresensi statusPresensi = StatusPresensi.valueOf(pilihan);
                presensiMahasiswa.setStatusPresensi(statusPresensi);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setKrsDetail(presensiMahasiswa.getKrsDetail());
                presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),j.getJamSelesai()));
                presensiMahasiswa.setWaktuMasuk(LocalDateTime.now());
                presensiMahasiswa.setSesiKuliah(sesiKuliah);
                presensiMahasiswaDao.save(presensiMahasiswa);
                System.out.println(presensiMahasiswa.getId());
            }

        }

        return "redirect:detail?jadwal="+j.getId();
    }

    @PostMapping("/studiesActivity/attendance/sesi")
    public String saveSesiBaru(@RequestParam Jadwal jadwal, @RequestParam SesiKuliah sesi,HttpServletRequest request){
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatus(sesi.getJadwal(),StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail){
            String pilihan = request.getParameter(kd.getMahasiswa().getNim() + "nim");
            if (pilihan == null || pilihan.isEmpty()){
                System.out.println("gaada");
            }else {
                PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
                presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
                StatusPresensi statusPresensi = StatusPresensi.valueOf(pilihan);
                presensiMahasiswa.setStatusPresensi(statusPresensi);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setKrsDetail(kd);
                presensiMahasiswa.setWaktuKeluar(sesi.getWaktuSelesai());
                presensiMahasiswa.setWaktuMasuk(sesi.getWaktuMulai());
                presensiMahasiswa.setSesiKuliah(sesi);
                presensiMahasiswaDao.save(presensiMahasiswa);
            }
        }
        return "redirect:mahasiswa?id="+sesi.getId();
    }

    @PostMapping("/studiesActivity/attendance/save")
    public String savePresensi(@RequestParam(required = false) String sesi, @RequestParam(required = false) StatusPresensi statusPresensi){
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(sesi).get();
        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliahAndStatus(sesiKuliah,StatusRecord.AKTIF);
        for (PresensiMahasiswa pm : presensiMahasiswa){
            pm.setStatusPresensi(statusPresensi);
            presensiMahasiswaDao.save(pm);
        }
        return "redirect:detail?jadwal="+sesiKuliah.getJadwal().getId();
    }

    @GetMapping("/studiesActivity/attendance/form")
    public void editAttendance(Model model,@RequestParam(name = "id",value = "id")SesiKuliah sesiKuliah){
        JadwalDto jadwalDto = new JadwalDto();
        jadwalDto.setId(sesiKuliah.getId());
        jadwalDto.setJadwal(sesiKuliah.getJadwal());
        jadwalDto.setBeritaAcara(sesiKuliah.getBeritaAcara());
        jadwalDto.setDosen(sesiKuliah.getPresensiDosen().getDosen());
        jadwalDto.setPresensiDosen(sesiKuliah.getPresensiDosen());
        jadwalDto.setTanggal(sesiKuliah.getWaktuMulai().toLocalDate());
        jadwalDto.setId(sesiKuliah.getId());
        jadwalDto.setJamMulai(sesiKuliah.getWaktuMulai().toLocalTime());
        jadwalDto.setJamSelesai(sesiKuliah.getWaktuSelesai().toLocalTime());

        model.addAttribute("sesi", jadwalDto);
        model.addAttribute("teamTeaching", jadwalDosenDao.findByJadwal(sesiKuliah.getJadwal()));

    }

    @PostMapping("/studiesActivity/attendance/form")
    public String prosesEdit(@ModelAttribute JadwalDto jadwalDto){

        SesiKuliah sesiKuliah = sesiKuliahDao.findById(jadwalDto.getId()).get();
        sesiKuliah.setBeritaAcara(jadwalDto.getBeritaAcara());

//        String date = jadwalDto.getTanggal().toString();
//        String time1 = jadwalDto.getWaktuMulai();
//        String time2 = jadwalDto.getWaktuSelesai();

//        String jammulai = time1.substring(0,5) + ":00";
//        String jamselsai = time2.substring(0,5) + ":00";
//
//        LocalTime jammu = LocalTime.parse(jammulai);
//        LocalTime jamse = LocalTime.parse(jamselsai);
//
//        String tanggalan = date + ' ' + time1 + ':' + "00";
//        String tanggalan2 = date + ' ' + time2 + ':' + "00";
//
//        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime localDate1 = LocalDateTime.parse(tanggalan, formatter1);
//        LocalDateTime localDate2 = LocalDateTime.parse(tanggalan2, formatter1);
////        pejabatMatrikulasi.setTanggalSelesai(localDate1.plusDays(0));
//

//        sesiKuliah.setWaktuMulai(LocalDateTime.of(jadwalDto.getTanggal(),jammu));
//        sesiKuliah.setWaktuSelesai(LocalDateTime.of(jadwalDto.getTanggal(),jamse));

        sesiKuliah.setWaktuMulai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamMulai()));
        sesiKuliah.setWaktuSelesai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamSelesai()));

//        sesiKuliah.setWaktuMulai(localDate1);
//        sesiKuliah.setWaktuSelesai(localDate2);

        PresensiDosen presensiDosen = presensiDosenDao.findById(sesiKuliah.getPresensiDosen().getId()).get();
        presensiDosen.setDosen(jadwalDto.getDosen());
        presensiDosen.setWaktuMasuk(sesiKuliah.getWaktuMulai());
        presensiDosen.setWaktuSelesai(sesiKuliah.getWaktuSelesai());

        sesiKuliahDao.save(sesiKuliah);
        presensiDosenDao.save(presensiDosen);

        return "redirect:detail?jadwal="+sesiKuliah.getJadwal().getId();

    }

    @PostMapping("/studiesActivity/attendance/delete")
    public String deletePresensi(@RequestParam(value = "id", name = "id") SesiKuliah sesiKuliah){

        PresensiDosen presensiDosen = presensiDosenDao.findById(sesiKuliah.getPresensiDosen().getId()).get();
        presensiDosen.setStatus(StatusRecord.HAPUS);
        presensiDosenDao.save(presensiDosen);

        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliah(sesiKuliah);
        for (PresensiMahasiswa pm : presensiMahasiswa){
            pm.setStatus(StatusRecord.HAPUS);
            presensiMahasiswaDao.save(pm);
        }

        return "redirect:detail?jadwal="+sesiKuliah.getJadwal().getId();

    }

//    KRS
    @GetMapping("/studiesActivity/krs/paket")
    public void main(Model model,@RequestParam(required = false) Kelas kelas){
        model.addAttribute("kelas",kelasMahasiswaDao.carikelasMahasiswa());
        model.addAttribute("selected",kelas);
        List<KelasMahasiswaDto> kelasMahasiswaDtos = new ArrayList<>();

        if (kelas != null){
            Iterable<KelasMahasiswa> kelasMahasiswa = kelasMahasiswaDao.findByKelasAndStatus(kelas,StatusRecord.AKTIF);
            if (IterableUtils.size(kelasMahasiswa) == 0){
                model.addAttribute("kosong","gada mahasiswa");
            }
            if (IterableUtils.size(kelasMahasiswa) > 0){
                for (KelasMahasiswa km : kelasMahasiswa){
                    KelasMahasiswaDto kelasMahasiswaDto = new KelasMahasiswaDto();
                    kelasMahasiswaDto.setNim(km.getMahasiswa().getNim());
                    kelasMahasiswaDto.setNama(km.getMahasiswa().getNama());
                    kelasMahasiswaDto.setKelas(km.getKelas().getNamaKelas());
                    kelasMahasiswaDtos.add(kelasMahasiswaDto);

                }
            }
            model.addAttribute("mahasiswaList",kelasMahasiswaDtos);
        }
    }

    @PostMapping("/studiesActivity/krs/paket")
    private String paketKrs(@RequestParam(required = false) Kelas kelas) {
        if (kelas != null){
            Iterable<KelasMahasiswa> kelasMahasiswa = kelasMahasiswaDao.findByKelasAndStatus(kelas,StatusRecord.AKTIF);
            if (kelasMahasiswa == null){
//                model.addAttribute("empty","tidak ada mahasiswa");
            }

            if (kelasMahasiswa != null) {
                for (KelasMahasiswa km : kelasMahasiswa) {
                    Krs krs = krsDao.findByTahunAkademikAndMahasiswaAndStatus(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),km.getMahasiswa(),StatusRecord.AKTIF);
                    if (krs != null){
                        List<Jadwal> jadwal = jadwalDao.findByStatusAndTahunAkademikAndKelasAndHariNotNull(StatusRecord.AKTIF,tahunAkademikDao.findByStatus(StatusRecord.AKTIF),kelas);
                        for (Jadwal j : jadwal) {
                            KrsDetail kd = krsDetailDao.findByJadwalAndStatusAndKrs(j, StatusRecord.AKTIF, krs);
                            if (kd == null){
                                KrsDetail krsDetail = new KrsDetail();
                                krsDetail.setJadwal(j);
                                krsDetail.setKrs(krs);
                                krsDetail.setMahasiswa(krs.getMahasiswa());
                                krsDetail.setMatakuliahKurikulum(j.getMatakuliahKurikulum());
                                krsDetail.setNilaiPresensi(BigDecimal.ZERO);
                                krsDetail.setNilaiUas(BigDecimal.ZERO);
                                krsDetail.setNilaiUts(BigDecimal.ZERO);
                                krsDetail.setNilaiTugas(BigDecimal.ZERO);
                                krsDetail.setFinalisasi("N");
                                krsDetail.setJumlahKehadiran(0);
                                krsDetail.setJumlahMangkir(0);
                                krsDetail.setJumlahTerlambat(0);
                                krsDetail.setJumlahSakit(0);
                                krsDetail.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                                krsDetail.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                krsDetail.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                krsDetail.setJumlahIzin(0);
                                krsDetailDao.save(krsDetail);
                            }
                        }
                    }
                }
            }
        }
        return "redirect:paket?kelas="+kelas.getId();
    }

    @GetMapping("/studiesActivity/krs/list")
    public void listKrs(@RequestParam(required = false)String nim, @RequestParam(required = false) TahunAkademik tahunAkademik, Model model){
        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));

        if (nim != null && tahunAkademik != null) {

//            Mahasiswa dataMahasiswa = mahasiswaDao.findByNimAndStatus(nim, StatusRecord.AKTIF);
            model.addAttribute("nim", nim);
            model.addAttribute("tahun", tahunAkademik);
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

            TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

            if (ta.getTanggalMulaiKrs().compareTo(LocalDate.now()) <= 0) {
                model.addAttribute("validasi", ta);
            }

            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik,StatusRecord.AKTIF);
            if (k == null){
                model.addAttribute("validation", "Krs belum aktif, Silahkan aktifasi terlebih dahulu ");
            }

            model.addAttribute("listKrs", krsDetailDao.findByStatusKonversiIsNullAndStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,k,mahasiswa));
            model.addAttribute("mahasiswa", mahasiswa);

        }

    }

    @GetMapping("/studiesActivity/krs/form")
    public void formKrs(@RequestParam(required = false)String nim, @RequestParam(required = false) String tahunAkademik,
                        Model model, @RequestParam(required = false) String lebih,@RequestParam(required = false) String kosong){
        model.addAttribute("nim", nim);
        model.addAttribute("tahun", tahunAkademik);

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);


        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);

        TahunAkademik ta = tahunAkademikDao.findById(tahunAkademik).get();
        String firstFourChars = ta.getKodeTahunAkademik().substring(0,4);
        System.out.println(firstFourChars);

        if (ta.getJenis() == StatusRecord.GENAP){
            String kode = firstFourChars+"1";
            System.out.println("kode : " + kode);
            TahunAkademik tahun = tahunAkademikDao.findByKodeTahunAkademikAndJenis(kode,StatusRecord.GANJIL);
            IpkDto ipk = krsDetailDao.ip(mahasiswa,tahun);
            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);

            Long sks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

            if (ipk == null){
                model.addAttribute("kosong", "24");
            }else {

                if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0) {
                    model.addAttribute("full", "24");
                }
            }
            model.addAttribute("sks", sks);
        }

        if (ta.getJenis() == StatusRecord.GANJIL){
            Integer prosesKode = Integer.valueOf(firstFourChars)-1;
            String kode = prosesKode.toString()+"2";

            TahunAkademik tahun = tahunAkademikDao.findByKodeTahunAkademikAndJenis(kode,StatusRecord.GENAP);
            IpkDto ipk = krsDetailDao.ip(mahasiswa,tahun);
            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);
            Long sks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

            if (ipk == null){
                model.addAttribute("kosong", "24");
            }else {

                if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0) {
                    model.addAttribute("full", "24");
                }
            }
            model.addAttribute("sks", sks);
        }

        List<Object[]> krsDetail = krsDetailDao.newPilihKrs(ta,kelasMahasiswa.getKelas(),mahasiswa.getIdProdi(),mahasiswa, mahasiswa.getIdProdi().getIdJenjang());
        model.addAttribute("pilihanKrs", krsDetail);
        model.addAttribute("tahun", ta);
        model.addAttribute("lebih", lebih);
        model.addAttribute("kosong", kosong);

    }

    @PostMapping("/studiesActivity/krs/form")
    public String prosesKrs(Authentication authentication, @RequestParam String jumlah, @RequestParam(required = false) String[] selected,
                            @RequestParam TahunAkademik tahunAkademik,@RequestParam String nim){

        Mahasiswa mahasiswa = mahasiswaDao.findByNimAndStatus(nim,StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik,StatusRecord.AKTIF);

        if (k != null) {

//            Long krsDetail = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

            Long krsDetail = krsDetailDao.cariJumlahSks(k.getId());

            if (selected == null) {

            } else {
                Long jadwal = jadwalDao.totalSks(selected);
                if (krsDetail == null) {
                    if (jadwal > Integer.valueOf(jumlah)) {
                        return "redirect:form?lebih=true";
                    } else {
                        for (String idJadwal : selected) {
                            Jadwal j = jadwalDao.findById(idJadwal).get();
                            if (krsDetailDao.cariKrs(j, tahunAkademik, mahasiswa) == null) {
                                KrsDetail kd = new KrsDetail();
                                kd.setJadwal(j);
                                kd.setKrs(k);
                                kd.setMahasiswa(mahasiswa);
                                kd.setMatakuliahKurikulum(j.getMatakuliahKurikulum());
                                kd.setNilaiPresensi(BigDecimal.ZERO);
                                kd.setNilaiUas(BigDecimal.ZERO);
                                kd.setNilaiTugas(BigDecimal.ZERO);
                                kd.setNilaiUts(BigDecimal.ZERO);
                                kd.setFinalisasi("N");
                                kd.setJumlahMangkir(0);
                                kd.setJumlahKehadiran(0);
                                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                kd.setJumlahTerlambat(0);
                                kd.setJumlahIzin(0);
                                kd.setJumlahSakit(0);
                                kd.setTahunAkademik(tahunAkademik);
                                kd.setStatusEdom(StatusRecord.UNDONE);
                                krsDetailDao.save(kd);
                            }
                        }
                    }
                }

                if (krsDetail != null) {
                    if (jadwal + krsDetail > Integer.valueOf(jumlah)) {
                        return "redirect:list?nim="+nim+"&tahunAkademik="+tahunAkademik.getId()+"&lebih=true";
                    } else {
                        for (String idJadwal : selected) {
                            Jadwal j = jadwalDao.findById(idJadwal).get();
                            if (krsDetailDao.cariKrs(j, tahunAkademik, mahasiswa) == null) {

                                KrsDetail kd = new KrsDetail();
                                kd.setJadwal(j);
                                kd.setKrs(k);
                                kd.setMahasiswa(mahasiswa);
                                kd.setMatakuliahKurikulum(j.getMatakuliahKurikulum());
                                kd.setNilaiTugas(BigDecimal.ZERO);
                                kd.setNilaiPresensi(BigDecimal.ZERO);
                                kd.setFinalisasi("N");
                                kd.setNilaiUas(BigDecimal.ZERO);
                                kd.setNilaiUts(BigDecimal.ZERO);
                                kd.setJumlahMangkir(0);
                                kd.setTahunAkademik(tahunAkademik);
                                kd.setJumlahKehadiran(0);
                                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                kd.setJumlahTerlambat(0);
                                kd.setJumlahIzin(0);
                                kd.setJumlahSakit(0);
                                kd.setStatusEdom(StatusRecord.UNDONE);
                                krsDetailDao.save(kd);
                            }
                        }
                    }
                }


            }
        }else {
            return "redirect:list?nim="+nim+"&tahunAkademik="+tahunAkademik.getId();
        }

        return "redirect:list?nim="+nim+"&tahunAkademik="+tahunAkademik.getId();


    }

    @PostMapping("/studiesActivity/krs/delete")
    public String deleteKrs(@RequestParam(name = "id", value = "id") KrsDetail krsDetail){
        krsDetail.setStatus(StatusRecord.HAPUS);
        krsDetailDao.save(krsDetail);

        return "redirect:list?nim="+krsDetail.getMahasiswa().getNim()+"&tahunAkademik="+krsDetail.getTahunAkademik().getId();

    }

//    Assesment

    @GetMapping("/studiesActivity/assesment/list")
    public void assesmentList(Model model,@RequestParam(required = false)TahunAkademik tahunAkademik,
                              @RequestParam(required = false)Prodi prodi,@RequestParam(required = false) String search){
        if (tahunAkademik != null){
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            if (!StringUtils.isEmpty(search)){
                model.addAttribute("search", search);
                model.addAttribute("list",jadwalDao.assesmentSearch(prodi,Arrays.asList(StatusRecord.HAPUS),tahunAkademik,search));
            }else {
                model.addAttribute("list", jadwalDao.assesment(prodi,Arrays.asList(StatusRecord.HAPUS),tahunAkademik));
            }
        }
    }

    @GetMapping("/studiesActivity/assesment/listdosen")
    public void listPenilaianDosen(Model model,Authentication authentication, @RequestParam(required = false)TahunAkademik tahunAkademik){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        if (tahunAkademik != null){
            model.addAttribute("selectedTahun", tahunAkademik);

            TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            model.addAttribute("jadwal", jadwalDao.lecturerAssesment(dosen,StatusRecord.AKTIF, tahunAkademik));
            model.addAttribute("dosenAkses", jadwalDosenDao.findByJadwalTahunAkademik(tahunAkademik));
        }else{
            TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            model.addAttribute("jadwal", jadwalDao.lecturerAssesment(dosen,StatusRecord.AKTIF, tahun));
        }


    }

    @PostMapping("/berkas/uts/setting")
    public String saveAksesUts(@RequestParam Jadwal jadwal, @RequestParam(required = false) String uts){

        log.info("jadwal: {}", jadwal.getId());
        log.info("uts: {}", uts);

        Dosen Duts = dosenDao.findById(uts).get();

        jadwal.setAksesUts(Duts);
        jadwalDao.save(jadwal);

        return "redirect:../uploadSoal/list";
    }

    @PostMapping("/berkas/uas/setting")
    public String saveAksesUas(@RequestParam Jadwal jadwal, @RequestParam(required = false) String uas){

        log.info("jadwal: {}", jadwal.getId());
        log.info("uas: {}", uas);

        Dosen Duas = dosenDao.findById(uas).get();

        jadwal.setAksesUas(Duas);
        jadwalDao.save(jadwal);

        return "redirect:../uploadSoal/list";
    }

    @GetMapping("/studiesActivity/assesment/edit")
    public void editTugas(@RequestParam(name = "id", value = "id") BobotTugas bobot, Model model){
        model.addAttribute("bobotTugas", bobot);

    }

    @PostMapping("/studiesActivity/assesment/edit")
    String prosesEdit(@ModelAttribute @Valid BobotTugas bobotTugas){
        bobotTugasDao.save(bobotTugas);
        return "redirect:weight?jadwal="+bobotTugas.getJadwal().getId();
    }

    @PostMapping("/studiesActivity/assesment/delete")
    public String deleteBobot(@RequestParam(name = "id", value = "id") String bobot){
        BobotTugas bobotTugas = bobotTugasDao.findById(bobot).get();
        bobotTugas.setStatus(StatusRecord.HAPUS);
        bobotTugasDao.save(bobotTugas);
        return "redirect:weight?jadwal="+bobotTugas.getJadwal().getId();
    }

    @GetMapping("/studiesActivity/assesment/upload/soal")
    public void listUas(@RequestParam Jadwal jadwal,@RequestParam StatusRecord status, Authentication authentication, Model model){

        if (StatusRecord.UTS == status) {
            Iterable<Soal> soal = soalDao.findByJadwalAndStatusAndStatusSoal(jadwal,StatusRecord.AKTIF,StatusRecord.UTS);
            User user = currentUserService.currentUser(authentication);
            Karyawan karyawan = karyawanDao.findByIdUser(user);
            Soal s = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF, StatusApprove.APPROVED,StatusRecord.UTS);
            Dosen dosen = dosenDao.findByKaryawan(karyawan);
            Soal validasi = soalDao.findByJadwalAndStatusSoalAndStatusAndStatusApproveIn(jadwal,StatusRecord.UTS, StatusRecord.AKTIF,Arrays.asList(StatusApprove.WAITING,StatusApprove.APPROVED));
            model.addAttribute("cek", validasi);
            model.addAttribute("jadwal", jadwal);
            model.addAttribute("soal", soal);
            model.addAttribute("dosen", dosen);
            model.addAttribute("approve", s);
            model.addAttribute("status",status);
        }

        if (StatusRecord.UAS == status) {
            Iterable<Soal> soal = soalDao.findByJadwalAndStatusAndStatusSoal(jadwal,StatusRecord.AKTIF,StatusRecord.UAS);
            User user = currentUserService.currentUser(authentication);
            Karyawan karyawan = karyawanDao.findByIdUser(user);
            Soal s = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal, StatusRecord.AKTIF, StatusApprove.APPROVED,StatusRecord.UAS);
            Dosen dosen = dosenDao.findByKaryawan(karyawan);
            Soal validasi = soalDao.findByJadwalAndStatusSoalAndStatusAndStatusApproveIn(jadwal,StatusRecord.UAS, StatusRecord.AKTIF,Arrays.asList(StatusApprove.WAITING,StatusApprove.APPROVED));
            model.addAttribute("cek", validasi);
            model.addAttribute("jadwal", jadwal);
            model.addAttribute("soal", soal);
            model.addAttribute("dosen", dosen);
            model.addAttribute("status",status);
            model.addAttribute("approve", s);
        }

    }

    @PostMapping("/studiesActivity/assesment/upload/soal")
    public String uploadBukti(@Valid Soal soal,
                              BindingResult error, MultipartFile file,
                              Authentication currentUser) throws Exception {


        String namaFile = file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();

        System.out.println("Nama File : {}" + namaFile);
        System.out.println("Jenis File : {}" + jenisFile);
        System.out.println("Nama Asli File : {}" + namaAsli);
        System.out.println("Ukuran File : {}"+ ukuran);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }


        String idFile = soal.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()+"-"+ soal.getJadwal().getKelas().getNamaKelas();
        String lokasiUpload = uploadFolder + File.separator + soal.getJadwal().getId();
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        file.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());


        soal.setStatus(StatusRecord.AKTIF);
        soal.setTanggalUpload(LocalDate.now());
        soal.setStatusApprove(StatusApprove.WAITING);
        soal.setFileUpload(idFile + "." + extension);

        if (soal.getStatusSoal() == StatusRecord.UTS) {
            Soal s = soalDao.findByJadwalAndStatusAndStatusApproveNotInAndStatusSoal(soal.getJadwal(),StatusRecord.AKTIF,Arrays.asList(StatusApprove.REJECTED), StatusRecord.UTS);
            if (s == null){
                soalDao.save(soal);
            }

            if (s != null){
                s.setStatus(StatusRecord.NONAKTIF);
                soalDao.save(s);
                soalDao.save(soal);
            }
        } else if (soal.getStatusSoal() == StatusRecord.UAS){
            Soal s = soalDao.findByJadwalAndStatusAndStatusApproveNotInAndStatusSoal(soal.getJadwal(),StatusRecord.AKTIF,Arrays.asList(StatusApprove.REJECTED), StatusRecord.UAS);
            if (s == null){
                soalDao.save(soal);
            }

            if (s != null){
                s.setStatus(StatusRecord.NONAKTIF);
                soalDao.save(s);
                soalDao.save(soal);
            }
        }




        Jadwal jadwal = jadwalDao.findById(soal.getJadwal().getId()).get();
        if (soal.getStatusSoal() == StatusRecord.UTS) {
            jadwal.setStatusUts(StatusApprove.WAITING);
        }
        if (soal.getStatusSoal() == StatusRecord.UAS) {
            jadwal.setStatusUas(StatusApprove.WAITING);
        }
        jadwalDao.save(jadwal);

        return "redirect:soal?jadwal=" +soal.getJadwal().getId()+"&status="+soal.getStatusSoal();

    }

    @PostMapping("/studiesActivity/assesment/upload/soal/delete")
    public String deleteSoal(@RequestParam Soal soal){
        soal.setStatus(StatusRecord.HAPUS);
        soalDao.save(soal);
        return "redirect:../soal?jadwal=" +soal.getJadwal().getId()+"&status="+soal.getStatusSoal();
    }


    @GetMapping("/contoh/soal")
    public void contohUts(HttpServletResponse response) throws Exception {
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=Template-Soal.doc");
        FileCopyUtils.copy(contohSoal.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @GetMapping("/contoh/soaluas")
    public void contohUas(HttpServletResponse response) throws Exception {
        response.setContentType("application/msword");
        response.setHeader("Content-Disposition", "attachment; filename=Template-Soal.doc");
        FileCopyUtils.copy(contohSoalUas.getInputStream(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    @GetMapping("/studiesActivity/assesment/weight")
    public void weightAssesment(@RequestParam Jadwal jadwal,Model model){
        model.addAttribute("absensi", presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF,jadwal));
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("jumlahMahasiswa", krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF).size());
        model.addAttribute("bobot",bobotTugasDao.findByJadwalAndStatusOrderByPertemuanAsc(jadwal,StatusRecord.AKTIF));}

    @PostMapping("/studiesActivity/assesment/weight")
    public String prosesWeight(@ModelAttribute @Valid Jadwal jadwal, RedirectAttributes attributes){
        BigDecimal totalBobot = jadwal.getBobotPresensi().add(jadwal.getBobotTugas()).add(jadwal.getBobotUas()).add(jadwal.getBobotUts());

        if (jadwal.getMatakuliahKurikulum().getSds() != null) {
            BigDecimal total = totalBobot.add(new BigDecimal(jadwal.getMatakuliahKurikulum().getSds()));

            if (total.toBigInteger().intValueExact() > 100){
                attributes.addFlashAttribute("lebih", "Melebihi Batas");
            }else {
                jadwalDao.save(jadwal);
            }
        }

        if (jadwal.getMatakuliahKurikulum().getSds() == null) {
            if (totalBobot.toBigInteger().intValueExact() > 100){
                attributes.addFlashAttribute("lebih", "Melebihi Batas");
            }else {
                jadwalDao.save(jadwal);
            }
        }

        return "redirect:weight?jadwal="+jadwal.getId();
    }

    @PostMapping("/studiesActivity/assesment/task")
    public String tambahTugas(@ModelAttribute @Valid BobotTugas bobotTugas,
                              RedirectAttributes redirectAttributes){

        BigDecimal totalBobotUtama = jadwalDao.bobotUtsUas(bobotTugas.getJadwal().getId());
        if (totalBobotUtama == null){
            totalBobotUtama = BigDecimal.ZERO;
        }

        BigDecimal totalBobotTugas = bobotTugasDao.totalBobotTugas(bobotTugas.getJadwal().getId());
        if (totalBobotTugas == null){
            totalBobotTugas = BigDecimal.ZERO;
        }

        BigDecimal total = totalBobotTugas.add(totalBobotUtama.add(bobotTugas.getBobot()));

        if (total.compareTo(new BigDecimal(100)) > 0){
            redirectAttributes.addFlashAttribute("gagal", "Save Data Gagal");
            return "redirect:weight?jadwal=" + bobotTugas.getJadwal().getId();
        }else {
            bobotTugasDao.save(bobotTugas);
            List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatus(bobotTugas.getJadwal(), StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail) {
                NilaiTugas nilaiTugas = new NilaiTugas();
                nilaiTugas.setNilai(BigDecimal.ZERO);
                nilaiTugas.setNilaiAkhir(BigDecimal.ZERO);
                nilaiTugas.setKrsDetail(kd);
                nilaiTugas.setBobotTugas(bobotTugas);
                nilaiTugas.setStatus(StatusRecord.AKTIF);
                nilaiTugasDao.save(nilaiTugas);
            }
            return "redirect:weight?jadwal=" + bobotTugas.getJadwal().getId();
        }

    }

    @GetMapping("/studiesActivity/assesment/score")
    public String assesmentScore(@RequestParam Jadwal jadwal, Model model, RedirectAttributes attributes){


//            krsDetailDao.insertNilaiTugas(jadwal.getId());

        List<KrsNilaiTugasDto> krsNilaiTugasDtos = krsDetailDao.listKrsNilaiTugas(jadwal.getId());
        if (krsNilaiTugasDtos != null){
            for (KrsNilaiTugasDto kd : krsNilaiTugasDtos) {

                NilaiTugas nilaiTugas = new NilaiTugas();
                nilaiTugas.setKrsDetail(krsDetailDao.findById(kd.getKrsDetail()).get());
                nilaiTugas.setBobotTugas(bobotTugasDao.findById(kd.getJadwalBobotTugas()).get());
                nilaiTugas.setNilai(kd.getNilai());
                nilaiTugas.setNilaiAkhir(kd.getNilaiAkhir());
                nilaiTugas.setStatus(StatusRecord.AKTIF);
                nilaiTugasDao.save(nilaiTugas);

            }
        }

        if (jadwal.getFinalStatus().equals("FINAL")){
            return "redirect:weight?jadwal="+jadwal.getId();

        }else {
            if (jadwal.getMatakuliahKurikulum().getSds() != null){
                BigDecimal totalBobot = jadwal.getBobotPresensi().add(jadwal.getBobotTugas()).add(jadwal.getBobotUas()).add(jadwal.getBobotUts()).add(new BigDecimal(jadwal.getMatakuliahKurikulum().getSds()));
                if (totalBobot.toBigInteger().intValueExact() < 100) {
                    attributes.addFlashAttribute("tidakvalid", "Melebihi Batas");
                    return "redirect:weight ?jadwal="+jadwal.getId();
                }
            }else {
                BigDecimal totalBobot = jadwal.getBobotPresensi().add(jadwal.getBobotTugas()).add(jadwal.getBobotUas()).add(jadwal.getBobotUts());
                if (totalBobot.toBigInteger().intValueExact() < 100) {
                    attributes.addFlashAttribute("tidakvalid", "Melebihi Batas");
                    return "redirect:weight?jadwal="+jadwal.getId();
                }
                if (totalBobot.toBigInteger().intValueExact() > 100) {
                    attributes.addFlashAttribute("tidakvalid", "Melebihi Batas");
                    return "redirect:weight?jadwal="+jadwal.getId();
                }
            }
        }

        List<ScoreDto> score = jadwalDao.scoreInput(jadwal,jadwal.getTahunAkademik());
        for (ScoreDto sc : score){
            System.out.println(sc.getSds());
        }


            List<BobotDto> nilaiTugas = nilaiTugasDao.nilaiTugasList(jadwal);
        model.addAttribute("detailJadwal", jadwal);
        model.addAttribute("jadwal", score);
        model.addAttribute("jumlahMahasiswa", krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF).size());
        model.addAttribute("nilaiTugas", nilaiTugas);
        model.addAttribute("bobot", bobotTugasDao.bobotTugas(jadwal.getId(),StatusRecord.AKTIF));
        model.addAttribute("bobotTugas", bobotTugasDao.Tugas(jadwal.getId(),StatusRecord.AKTIF));

        return "studiesActivity/assesment/score";

    }

    @GetMapping("/studiesActivity/assesment/rekap")
    public void rekapScore(@RequestParam Jadwal jadwal, Model model, RedirectAttributes attributes){
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));
        model.addAttribute("nilai", presensiMahasiswaDao.bkdNilai(jadwal));
    }

    @PostMapping(value = "/studiesActivity/assesment/score")
    @ResponseStatus(HttpStatus.OK)
    public void simpanNilai(@RequestBody @Valid ScoreInput in) throws Exception   {

        if (in != null){

            if (in.getNilai() != null && !in.getNilai().trim().isEmpty()){
                BobotTugas bobotTugas = bobotTugasDao.findById(in.getTugas()).get();
                KrsDetail krsDetail = krsDetailDao.findById(in.getKrs()).get();
                NilaiTugas validasi = nilaiTugasDao.findByStatusAndBobotTugasAndKrsDetail(StatusRecord.AKTIF, bobotTugas, krsDetail);

                if (validasi != null){
                    BigDecimal nilai = bobotTugas.getBobot().multiply(new BigDecimal(in.getNilai()).divide(new BigDecimal(100)));
                    validasi.setNilaiAkhir(nilai);
                    validasi.setNilai(new BigDecimal(in.getNilai()));
                    nilaiTugasDao.save(validasi);
                    List<NilaiTugas> nilaiAkhir = nilaiTugasDao.findByStatusAndKrsDetailAndBobotTugasStatus(StatusRecord.AKTIF, krsDetail,StatusRecord.AKTIF);
                    BigDecimal sum = nilaiAkhir.stream()
                            .map(NilaiTugas::getNilaiAkhir)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    System.out.println(sum);
                    KrsDetail kd = krsDetailDao.findById(validasi.getKrsDetail().getId()).get();

                    BigDecimal nilaiUas = krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas()).divide(new BigDecimal(100));
                    BigDecimal nilaiUts = krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts()).divide(new BigDecimal(100));
                    kd.setNilaiPresensi(new BigDecimal(in.getAbsen()));
                    kd.setNilaiTugas(sum);
                    kd.setNilaiAkhir(kd.getNilaiTugas().add(nilaiUts).add(kd.getNilaiPresensi()).add(nilaiUas).add(new BigDecimal(in.getSds())));

                    scoreService.hitungNilaiAkhir(kd);
                }
            }

            if (in.getUas() != null && !in.getUas().trim().isEmpty()){
                KrsDetail krsDetail = krsDetailDao.findById(in.getKrs()).get();
                krsDetail.setNilaiPresensi(new BigDecimal(in.getAbsen()));
                BigDecimal nilaiUas = new BigDecimal(in.getUas()).multiply(krsDetail.getJadwal().getBobotUas()).divide(new BigDecimal(100));
                BigDecimal nilaiUts = krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts()).divide(new BigDecimal(100));
                krsDetail.setNilaiUas(new BigDecimal(in.getUas()));

                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(nilaiUts).add(krsDetail.getNilaiPresensi()).add(nilaiUas).add(new BigDecimal(in.getSds())));

                scoreService.hitungNilaiAkhir(krsDetail);

            }

            if (in.getUts() != null && !in.getUts().trim().isEmpty()){
                KrsDetail krsDetail = krsDetailDao.findById(in.getKrs()).get();
                BigDecimal nilaiUts = new BigDecimal(in.getUts()).multiply(krsDetail.getJadwal().getBobotUts()).divide(new BigDecimal(100));
                BigDecimal nilaiUas = krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas()).divide(new BigDecimal(100));
                krsDetail.setNilaiPresensi(new BigDecimal(in.getAbsen()));
                krsDetail.setNilaiUts(new BigDecimal(in.getUts()));
                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(nilaiUts).add(krsDetail.getNilaiPresensi()).add(nilaiUas).add(new BigDecimal(in.getSds())));

                scoreService.hitungNilaiAkhir(krsDetail);

            }
        }

    }



    @GetMapping("/studiesActivity/assesment/uploadnilai")
    public String upload(Model model,@RequestParam Jadwal jadwal){

        List<BobotTugas> listTugas = bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("listTugas",listTugas);

        if (jadwal.getFinalStatus().equals("FINAL")){
            return "redirect:weight?jadwal="+jadwal.getId();

        }else {
            return "studiesActivity/assesment/uploadnilai";
        }
    }

    @GetMapping("/studiesActivity/assesment/sds")
    public void sds(Model model,@RequestParam Jadwal jadwal){
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("mahasiswa", jadwalDao.cariMahasiswaBelumSds(jadwal.getProdi(),jadwal.getTahunAkademik()));
    }


    @PostMapping("/studiesActivity/assesment/sds")
    public String sds(@RequestParam Jadwal jadwal, @RequestParam(required = false) String[] data){
        if (data != null){
            for (String krs : data) {
                Krs k = krsDao.findById(krs).get();
                KrsDetail kd = new KrsDetail();
                kd.setJadwal(jadwal);
                kd.setKrs(k);
                kd.setMahasiswa(k.getMahasiswa());
                kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                kd.setNilaiPresensi(BigDecimal.ZERO);
                kd.setNilaiTugas(BigDecimal.ZERO);
                kd.setFinalisasi("N");
                kd.setNilaiUas(BigDecimal.ZERO);
                kd.setNilaiUts(BigDecimal.ZERO);
                kd.setJumlahMangkir(0);
                kd.setJumlahKehadiran(0);
                kd.setTahunAkademik(jadwal.getTahunAkademik());
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


    //TUGAS
    @GetMapping("/studiesActivity/assesment/excelTugas")
    public void downloadExcelTugas (@RequestParam(required = false) String id, HttpServletResponse response) throws IOException {

        List<String> staticColumn1 = new ArrayList<>();

        staticColumn1.add("No.   ");
        staticColumn1.add("NIM    ");
        staticColumn1.add("NAMA                                 ");
        staticColumn1.add("NILAI ");

        BobotTugas tugas = bobotTugasDao.findById(id).get();
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNimAsc(tugas.getJadwal(),StatusRecord.AKTIF);


        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("penilaian");
        sheet.autoSizeColumn(9);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);


        sheet.createRow(0).createCell(3).setCellValue("   SCORE RECAPITULATION FIRST SEMESTER");
        sheet.createRow(1).createCell(3).setCellValue("                        INSTITUT TAZKIA");
        sheet.createRow(2).createCell(3).setCellValue("                 ACADEMIC YEAR 2019/2020");

        int rowInfo = 5 ;
        Row rowi1 = sheet.createRow(rowInfo);
        rowi1.createCell(2).setCellValue("Subject :");
        rowi1.createCell(3).setCellValue(tugas.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
        rowi1.createCell(4).setCellValue("Course :");
        rowi1.createCell(5).setCellValue(tugas.getJadwal().getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());


        int rowInfo2 = 6 ;
        Row rowi2 = sheet.createRow(rowInfo2);
        rowi2.createCell(2).setCellValue("Day/date :");
        rowi2.createCell(3).setCellValue(tugas.getJadwal().getHari().getNamaHari());
        rowi2.createCell(4).setCellValue("Semester :");
        rowi2.createCell(5).setCellValue(tugas.getJadwal().getMatakuliahKurikulum().getSemester().toString());


        int rowInfo3 = 7 ;
        Row rowi3 = sheet.createRow(rowInfo3);
        rowi3.createCell(2).setCellValue("Room No/Time :");
        rowi3.createCell(3).setCellValue(tugas.getJadwal().getRuangan().getNamaRuangan());
        rowi3.createCell(4).setCellValue("Lecturer :");
        rowi3.createCell(5).setCellValue(tugas.getJadwal().getDosen().getKaryawan().getNamaKaryawan());

        int rowInfo4 = 8 ;
        Row rowi4 = sheet.createRow(rowInfo4);
        rowi4.createCell(2).setCellValue("Komponen :");
        rowi4.createCell(3).setCellValue("Tugas " + tugas.getNamaTugas());
        rowi4.createCell(4).setCellValue("Bobot :");
        rowi4.createCell(5).setCellValue(tugas.getBobot().toString());

        /**/
        Row headerRow = sheet.createRow(11);
        Integer cellNum = 2;

        for (String header : staticColumn1) {
            Cell cell = headerRow.createCell(cellNum);
            cell.setCellValue(header);
            cell.setCellStyle(headerCellStyle);
            sheet.autoSizeColumn(cellNum);
            cellNum++;
        }


        int rowNum = 12 ;
        int no = 1;
        for (KrsDetail kd : krsDetail) {
            int kolom = 2;
            Row row = sheet.createRow(rowNum);
            row.createCell(kolom++).setCellValue(no);
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNim());
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNama());
            no++;
            rowNum++;
        }


        String namaFile = "PenilaianTugas ";
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile + tugas.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + "_" + tugas.getJadwal().getKelas().getNamaKelas() + "_" + tugas.getNamaTugas() + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    //UTS
    @GetMapping("/studiesActivity/assesment/excelUTS")
    public void downloadExcelUts (@RequestParam(required = false) String id, HttpServletResponse response) throws IOException {

        List<String> staticColumn1 = new ArrayList<>();

        staticColumn1.add("No.   ");
        staticColumn1.add("NIM    ");
        staticColumn1.add("NAMA                                 ");
        staticColumn1.add("NILAI ");

        Jadwal jadwal = jadwalDao.findById(id).get();
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNimAsc(jadwal,StatusRecord.AKTIF);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("penilaian");
        sheet.autoSizeColumn(9);


        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);


        sheet.createRow(0).createCell(3).setCellValue("   SCORE RECAPITULATION FIRST SEMESTER");
        sheet.createRow(1).createCell(3).setCellValue("                        INSTITUT TAZKIA");
        sheet.createRow(2).createCell(3).setCellValue("                 ACADEMIC YEAR 2019/2020");

        int rowInfo = 5 ;
        Row rowi1 = sheet.createRow(rowInfo);
        rowi1.createCell(2).setCellValue("Subject :");
        rowi1.createCell(3).setCellValue(jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
        rowi1.createCell(4).setCellValue("Course :");
        rowi1.createCell(5).setCellValue(jadwal.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());


        int rowInfo2 = 6 ;
        Row rowi2 = sheet.createRow(rowInfo2);
        rowi2.createCell(2).setCellValue("Day/date :");
        rowi2.createCell(3).setCellValue(jadwal.getHari().getNamaHari());
        rowi2.createCell(4).setCellValue("Semester :");
        rowi2.createCell(5).setCellValue(jadwal.getMatakuliahKurikulum().getSemester().toString());


        int rowInfo3 = 7 ;
        Row rowi3 = sheet.createRow(rowInfo3);
        rowi3.createCell(2).setCellValue("Room No/Time :");
        rowi3.createCell(3).setCellValue(jadwal.getRuangan().getNamaRuangan());
        rowi3.createCell(4).setCellValue("Lecturer :");
        rowi3.createCell(5).setCellValue(jadwal.getDosen().getKaryawan().getNamaKaryawan());

        int rowInfo4 = 8 ;
        Row rowi4 = sheet.createRow(rowInfo4);
        rowi4.createCell(2).setCellValue("Komponen :");
        rowi4.createCell(3).setCellValue("UTS");
        rowi4.createCell(4).setCellValue("Bobot :");
        rowi4.createCell(5).setCellValue(jadwal.getBobotUts().toString());

        /**/
        Row headerRow = sheet.createRow(11);
        Integer cellNum = 2;

        for (String header : staticColumn1) {
            Cell cell = headerRow.createCell(cellNum);
            cell.setCellValue(header);
            sheet.autoSizeColumn(cellNum);
            cell.setCellStyle(headerCellStyle);
            cellNum++;
        }


        int rowNum = 12 ;
        int no = 1;
        for (KrsDetail kd : krsDetail) {
            int kolom = 2;
            Row row = sheet.createRow(rowNum);
            row.createCell(kolom++).setCellValue(no);
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNim());
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNama());
            no++;
            rowNum++;
        }


        String namaFile = "PenilaianUTS ";
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + "_" + jadwal.getKelas().getNamaKelas() + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }


    //UAS
    @GetMapping("/studiesActivity/assesment/excelUAS")
    public void downloadExcelUas (@RequestParam(required = false) String id, HttpServletResponse response) throws IOException {

        List<String> staticColumn1 = new ArrayList<>();

        staticColumn1.add("No.   ");
        staticColumn1.add("NIM    ");
        staticColumn1.add("NAMA                                 ");
        staticColumn1.add("NILAI ");

        Jadwal jadwal = jadwalDao.findById(id).get();
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNimAsc(jadwal,StatusRecord.AKTIF);
        List<BobotTugas> bobotTugas = bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("penilaian");
        sheet.autoSizeColumn(9);


        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);


        sheet.createRow(0).createCell(3).setCellValue("   SCORE RECAPITULATION FIRST SEMESTER");
        sheet.createRow(1).createCell(3).setCellValue("                        INSTITUT TAZKIA");
        sheet.createRow(2).createCell(3).setCellValue("                 ACADEMIC YEAR 2019/2020");

        int rowInfo = 5 ;
        Row rowi1 = sheet.createRow(rowInfo);
        rowi1.createCell(2).setCellValue("Subject :");
        rowi1.createCell(3).setCellValue(jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
        rowi1.createCell(4).setCellValue("Course :");
        rowi1.createCell(5).setCellValue(jadwal.getMatakuliahKurikulum().getMatakuliah().getKodeMatakuliah());


        int rowInfo2 = 6 ;
        Row rowi2 = sheet.createRow(rowInfo2);
        rowi2.createCell(2).setCellValue("Day/date :");
        rowi2.createCell(3).setCellValue(jadwal.getHari().getNamaHari());
        rowi2.createCell(4).setCellValue("Semester :");
        rowi2.createCell(5).setCellValue(jadwal.getMatakuliahKurikulum().getSemester().toString());


        int rowInfo3 = 7 ;
        Row rowi3 = sheet.createRow(rowInfo3);
        rowi3.createCell(2).setCellValue("Room No/Time :");
        rowi3.createCell(3).setCellValue(jadwal.getRuangan().getNamaRuangan());
        rowi3.createCell(4).setCellValue("Lecturer :");
        rowi3.createCell(5).setCellValue(jadwal.getDosen().getKaryawan().getNamaKaryawan());

        int rowInfo4 = 8 ;
        Row rowi4 = sheet.createRow(rowInfo4);
        rowi4.createCell(2).setCellValue("Komponen :");
        rowi4.createCell(3).setCellValue("UAS");
        rowi4.createCell(4).setCellValue("Bobot :");
        rowi4.createCell(5).setCellValue(jadwal.getBobotUas().toString());

        /**/
        Row headerRow = sheet.createRow(11);
        Integer cellNum = 2;

        for (String header : staticColumn1) {
            Cell cell = headerRow.createCell(cellNum);
            cell.setCellStyle(headerCellStyle);
            cell.setCellValue(header);
            sheet.autoSizeColumn(cellNum);
            cellNum++;
        }



        int rowNum = 12 ;
        int no = 1;
        for (KrsDetail kd : krsDetail) {
            int kolom = 2;
            Row row = sheet.createRow(rowNum);
            row.createCell(kolom++).setCellValue(no);
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNim());
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNama());
            no++;
            rowNum++;
        }


        String namaFile = "PenilaianUAS ";
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah() + "_" + jadwal.getKelas().getNamaKelas() + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }



    //post.excel.Tugas
    @PostMapping("/studiesActivity/assesment/formTugas")
    public String prosesFormUploadTugas(MultipartFile file, @RequestParam(name = "jadwal",value = "jadwal") BobotTugas bobotTugas) {

        LOGGER.debug("Nama file : {}", file.getOriginalFilename());
        LOGGER.debug("Ukuran file : {} bytes", file.getSize());

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheetPertama = workbook.getSheetAt(0);


            int row = 12;
            Long jmlMhs = krsDetailDao.countByJadwalAndStatus(bobotTugas.getJadwal(),StatusRecord.AKTIF);
            for (int i = 0; i < jmlMhs;i++){
                Row baris = sheetPertama.getRow(row + i);

                String nim = baris.getCell(3).getStringCellValue();
                Cell nilai = baris.getCell(5 );

                if (nilai != null) {
                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);

                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            bobotTugas.getJadwal(), StatusRecord.AKTIF);
                    NilaiTugas nilaiTugas = nilaiTugasDao.findByStatusAndBobotTugasAndKrsDetail(StatusRecord.AKTIF, bobotTugas,
                            krsDetail);
                    BigDecimal nilaiAkhir = new BigDecimal(nilai.getNumericCellValue()).multiply(bobotTugas.getBobot())
                            .divide(new BigDecimal(100));



                    nilaiTugas.setNilai(new BigDecimal(nilai.getNumericCellValue()));
                    nilaiTugas.setNilaiAkhir(nilaiAkhir);

                    nilaiTugasDao.save(nilaiTugas);
                    List<NilaiTugas> nilaiAkhirnya = nilaiTugasDao.findByStatusAndKrsDetailAndBobotTugasStatus(StatusRecord.AKTIF, krsDetail,StatusRecord.AKTIF);

                    /*sum semua tugas*/ BigDecimal nilTug = nilaiAkhirnya.stream().map(NilaiTugas::getNilaiAkhir).reduce(BigDecimal.ZERO, BigDecimal::add);
                    krsDetail.setNilaiTugas(nilTug);
                    krsDetailDao.save(krsDetail);
                }else {
                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);

                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            bobotTugas.getJadwal(), StatusRecord.AKTIF);
                    NilaiTugas nilaiTugas = nilaiTugasDao.findByStatusAndBobotTugasAndKrsDetail(StatusRecord.AKTIF, bobotTugas,
                            krsDetail);

                    nilaiTugas.setNilai(BigDecimal.ZERO);
                    nilaiTugas.setNilaiAkhir(BigDecimal.ZERO);

                    nilaiTugasDao.save(nilaiTugas);

                }

            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        List<ScoreHitungDto> scoreDtos = jadwalDao.hitungUploadScore(bobotTugas.getJadwal(),bobotTugas.getJadwal().getTahunAkademik());

        for(ScoreHitungDto s : scoreDtos){

            KrsDetail krsDetail2 = krsDetailDao.findById(s.getKrs()).get();
            krsDetail2.setNilaiAkhir(s.getNilaiAkhir());
            krsDetail2.setGrade(s.getGrade());
            krsDetail2.setBobot(s.getBobot());

            krsDetailDao.save(krsDetail2);


        }

        return "redirect:/studiesActivity/assesment/uploadnilai?jadwal=" + bobotTugas.getJadwal().getId();

    }

    //post.excel.UTS
    @PostMapping("/studiesActivity/assesment/formUTS")
    public String prosesFormUploadUTS(MultipartFile file, @RequestParam Jadwal jadwal) {

        LOGGER.debug("Nama file : {}", file.getOriginalFilename());
        LOGGER.debug("Ukuran file : {} bytes", file.getSize());

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheetPertama = workbook.getSheetAt(0);

            int row = 12;
            Long jmlMhs = krsDetailDao.countByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
            for (int i = 0; i < jmlMhs;i++) {
                Row baris = sheetPertama.getRow(row + i);

                String nim = baris.getCell(3).getStringCellValue();
                Cell nilai = baris.getCell(5);


                if(nilai != null) {
                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            jadwal, StatusRecord.AKTIF);

                    if (krsDetail == null) {
                        LOGGER.warn("KRS Detail untuk nim {} dan UTS {} tidak ditemukan", nim, jadwal.getStatusUts());
                        return "redirect:/penilaian/list";
                    }

                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);

                    krsDetail.setNilaiUts(new BigDecimal(nilai.getNumericCellValue()));
                    krsDetailDao.save(krsDetail);


                }else {
                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            jadwal, StatusRecord.AKTIF);
                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);
                    krsDetail.setNilaiUts(BigDecimal.ZERO);
                    krsDetailDao.save(krsDetail);

                    if (krsDetail == null) {
                        LOGGER.warn("KRS Detail untuk nim {} dan UTS {} tidak ditemukan", nim, jadwal.getStatusUts());
                        return "redirect:/penilaian/list";
                    }

                }

            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        List<ScoreHitungDto> scoreDtos = jadwalDao.hitungUploadScore(jadwal,jadwal.getTahunAkademik());

        for(ScoreHitungDto s : scoreDtos){

            KrsDetail krsDetail2 = krsDetailDao.findById(s.getKrs()).get();
            krsDetail2.setNilaiAkhir(s.getNilaiAkhir());
            krsDetail2.setGrade(s.getGrade());
            krsDetail2.setBobot(s.getBobot());

            krsDetailDao.save(krsDetail2);


        }



        return "redirect:/studiesActivity/assesment/uploadnilai?jadwal=" + jadwal.getId();

    }

    //post.excel.UAS
    @PostMapping("/studiesActivity/assesment/formUAS")
    public String prosesFormUploadUAS(MultipartFile file, @RequestParam Jadwal jadwal) {

        LOGGER.debug("Nama file : {}", file.getOriginalFilename());
        LOGGER.debug("Ukuran file : {} bytes", file.getSize());

        try {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheetPertama = workbook.getSheetAt(0);

            int row = 12;
            Long jmlMhs = krsDetailDao.countByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
            for (int i = 0; i < jmlMhs;i++) {
                Row baris = sheetPertama.getRow(row + i);

                Cell nilai = baris.getCell(5);
                String nim = baris.getCell(3).getStringCellValue();


                if (nilai != null) {
                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);

                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            jadwal, StatusRecord.AKTIF);

                    if (krsDetail == null) {
                        LOGGER.warn("KRS Detail untuk nim {} dan UAS {} tidak ditemukan", nim, jadwal.getStatusUas());
                        return "redirect:/penilaian/list";
                    }

                    krsDetail.setNilaiUas(new BigDecimal(nilai.getNumericCellValue()));
                    krsDetailDao.save(krsDetail);


                }else {
                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);

                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            jadwal, StatusRecord.AKTIF);
                    krsDetail.setNilaiUas(BigDecimal.ZERO);
                    krsDetailDao.save(krsDetail);

                    if (krsDetail == null) {
                        LOGGER.warn("KRS Detail untuk nim {} dan UAS {} tidak ditemukan", nim, jadwal.getStatusUas());
                        return "redirect:/penilaian/list";
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        List<ScoreHitungDto> scoreDtos = jadwalDao.hitungUploadScore(jadwal,jadwal.getTahunAkademik());

        for(ScoreHitungDto s : scoreDtos){

            KrsDetail krsDetail2 = krsDetailDao.findById(s.getKrs()).get();
            krsDetail2.setNilaiAkhir(s.getNilaiAkhir());
            krsDetail2.setGrade(s.getGrade());
            krsDetail2.setBobot(s.getBobot());

            krsDetailDao.save(krsDetail2);


        }

        return "redirect:/studiesActivity/assesment/uploadnilai?jadwal=" + jadwal.getId();

    }

    //KHS

    @GetMapping("/studiesActivity/khs/list")
    public void listKhs(Model model,@RequestParam(required = false) TahunAkademik tahunAkademik,
                        @RequestParam(required = false) String nim){

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        model.addAttribute("tahun" , tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        if (mahasiswa != null){
            if (tahunAkademik != null) {
                model.addAttribute("selectedTahun" , tahunAkademik);
                model.addAttribute("selectedNim" , nim);
                List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademik,mahasiswa);
                if (!krsDetail.isEmpty()){
                    model.addAttribute("khs",krsDetail);
                    model.addAttribute("ipk", krsDetailDao.ipkTahunAkademik(mahasiswa,tahunAkademik.getKodeTahunAkademik()));
                    model.addAttribute("ip", krsDetailDao.ip(mahasiswa,tahunAkademik));
                }
            } else {
                model.addAttribute("selectedNim" , nim);
                List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),mahasiswa);
                model.addAttribute("khs",krsDetail);
                model.addAttribute("ipk", krsDetailDao.ipkTahunAkademik(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF).getKodeTahunAkademik()));
                model.addAttribute("ip", krsDetailDao.ip(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));
            }
        }
    }

    @GetMapping("/studiesActivity/khs/download")
    public void downloadKhs(Model model,@RequestParam(required = false) TahunAkademik tahunAkademik,
                            @RequestParam(required = false) String nim){

        if (tahunAkademik != null) {
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            model.addAttribute("selectedTahun" , tahunAkademik);
            model.addAttribute("selectedNim" , nim);
            List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademik,mahasiswa);
            int sumSks = krsDetail.stream().mapToInt(DataKhsDto::getSks).sum();
            Double sumBobot = krsDetail.stream().mapToDouble(DataKhsDto::getTotal).sum();
            model.addAttribute("khs",krsDetail);
            model.addAttribute("sks",sumSks);
            model.addAttribute("bobot",sumBobot);
            model.addAttribute("mahasiswa",mahasiswa);
            model.addAttribute("tahun",tahunAkademik);
            model.addAttribute("ipk", krsDetailDao.ipkTahunAkademik(mahasiswa,tahunAkademik.getKodeTahunAkademik()));
            model.addAttribute("ip", krsDetailDao.ip(mahasiswa,tahunAkademik));
        }

    }

    @GetMapping("/studiesActivity/khs/downloadexcel")
    public void khsExcel (Model model,@RequestParam(required = false) TahunAkademik tahunAkademik,
                                    @RequestParam(required = false) String nim, HttpServletResponse response) throws IOException, URISyntaxException {


        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademik,mahasiswa);

        int sumSks = krsDetail.stream().mapToInt(DataKhsDto::getSks).sum();
        IpkDto ipk = krsDetailDao.ipkTahunAkademik(mahasiswa,tahunAkademik.getKodeTahunAkademik());
        IpkDto ip =  krsDetailDao.ip(mahasiswa,tahunAkademik);

        InputStream file = contohExcelKhs.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), mahasiswa.getNama());
        sheet.addMergedRegion(CellRangeAddress.valueOf("A8:B8"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A9:B9"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A10:B10"));

        Font manajemenFont = workbook.createFont();
        manajemenFont.setBold(true);
        manajemenFont.setFontHeightInPoints((short) 12);

        Font dataManajemenFont = workbook.createFont();
        dataManajemenFont.setFontHeightInPoints((short) 12);

        Font subHeaderFont = workbook.createFont();
        subHeaderFont.setFontHeightInPoints((short) 14);
        subHeaderFont.setFontName("Cambria");
        subHeaderFont.setBold(true);

        Font symbolFont = workbook.createFont();
        symbolFont.setFontHeightInPoints((short) 12);
        symbolFont.setFontName("Cambria");

        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 12);
        dataFont.setFontName("Cambria");

        Font prodiFont = workbook.createFont();
        prodiFont.setBold(true);
        prodiFont.setFontHeightInPoints((short) 12);
        prodiFont.setFontName("Cambria");

        Font ipFont = workbook.createFont();
        ipFont.setBold(true);
        ipFont.setFontHeightInPoints((short) 12);
        ipFont.setFontName("Cambria");

        CellStyle styleManajemen = workbook.createCellStyle();
        styleManajemen.setFont(manajemenFont);

        CellStyle styleProdi = workbook.createCellStyle();
        styleProdi.setFont(dataManajemenFont);

        CellStyle styleSubHeader = workbook.createCellStyle();
        styleSubHeader.setFont(subHeaderFont);
        styleSubHeader.setAlignment(HorizontalAlignment.CENTER);
        styleSubHeader.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle styleData = workbook.createCellStyle();
        styleData.setFont(dataFont);

        CellStyle styleDataKhs = workbook.createCellStyle();
        styleDataKhs.setAlignment(HorizontalAlignment.CENTER);
        styleDataKhs.setBorderTop(BorderStyle.THIN);
        styleDataKhs.setBorderBottom(BorderStyle.THIN);
        styleDataKhs.setBorderLeft(BorderStyle.THIN);
        styleDataKhs.setBorderRight(BorderStyle.THIN);
        styleDataKhs.setFont(dataFont);

        CellStyle styleSymbol = workbook.createCellStyle();
        styleSymbol.setAlignment(HorizontalAlignment.CENTER);
        styleSymbol.setFont(symbolFont);

        CellStyle styleIp = workbook.createCellStyle();
        styleIp.setAlignment(HorizontalAlignment.RIGHT);
        styleIp.setVerticalAlignment(VerticalAlignment.CENTER);
        styleIp.setFont(ipFont);

        CellStyle styleIpk = workbook.createCellStyle();
        styleIpk.setFont(prodiFont);
        styleIpk.setAlignment(HorizontalAlignment.CENTER);



        int rowInfoTahun = 2 ;
        Row rowTahun = sheet.createRow(rowInfoTahun);
        rowTahun.createCell(0).setCellValue(tahunAkademik.getNamaTahunAkademik());
        rowTahun.getCell(0).setCellStyle(styleSubHeader);

        int rowInfoNama = 7 ;
        Row rowNama = sheet.createRow(rowInfoNama);
        rowNama.createCell(0).setCellValue("Nama");
        rowNama.createCell(2).setCellValue(":");
        rowNama.createCell(3).setCellValue(mahasiswa.getNama());
        rowNama.getCell(0).setCellStyle(styleData);
        rowNama.getCell(2).setCellStyle(styleSymbol);
        rowNama.getCell(3).setCellStyle(styleData);

        int rowInfoNim = 8 ;
        Row rowNim = sheet.createRow(rowInfoNim);
        rowNim.createCell(0).setCellValue("NIM");
        rowNim.createCell(2).setCellValue(":");
        rowNim.createCell(3).setCellValue(mahasiswa.getNim());
        rowNim.getCell(0).setCellStyle(styleData);
        rowNim.getCell(2).setCellStyle(styleSymbol);
        rowNim.getCell(3).setCellStyle(styleData);

        int rowInfo3 = 9 ;
        Row rowi3 = sheet.createRow(rowInfo3);
        rowi3.createCell(0).setCellValue("Program Studi");
        rowi3.createCell(2).setCellValue(":");
        rowi3.createCell(3).setCellValue(mahasiswa.getIdProdi().getNamaProdi());
        rowi3.getCell(0).setCellStyle(styleData);
        rowi3.getCell(2).setCellStyle(styleSymbol);
        rowi3.getCell(3).setCellStyle(styleData);

        int rowNum = 13 ;
        int no = 1;
        for (DataKhsDto kd : krsDetail) {
            Row row = sheet.createRow(rowNum);
            sheet.addMergedRegion(new CellRangeAddress(rowNum,rowNum,2,3));

            row.createCell(0).setCellValue(no);
            row.getCell(0).setCellStyle(styleDataKhs);
            row.createCell(1).setCellValue(kd.getKode());
            row.getCell(1).setCellStyle(styleDataKhs);
            row.createCell(2).setCellValue(kd.getMatakuliah());
            row.getCell(2).setCellStyle(styleDataKhs);
            row.createCell(4).setCellValue(kd.getSks());
            row.getCell(4).setCellStyle(styleDataKhs);
            if (kd.getMatakuliah().equals("Student Dynamic Session")){
                row.createCell(5).setCellValue("-");
                row.getCell(5).setCellStyle(styleDataKhs);
                row.createCell(6).setCellValue("-");
                row.getCell(6).setCellStyle(styleDataKhs);
                row.createCell(7).setCellValue("-");
                row.getCell(7).setCellStyle(styleDataKhs);
            }else {
                row.createCell(5).setCellValue(kd.getBobot().toString());
                row.getCell(5).setCellStyle(styleDataKhs);
                row.createCell(6).setCellValue(kd.getGrade());
                row.getCell(6).setCellStyle(styleDataKhs);
                row.createCell(7).setCellValue(kd.getBobot().multiply(BigDecimal.valueOf(kd.getSks())).toString());
                row.getCell(7).setCellStyle(styleDataKhs);
            }
            no++;
            rowNum++;
        }

        int rowTotalSks = 13 + krsDetail.size() ;
        Row totalSks = sheet.createRow(rowTotalSks);
        totalSks.createCell(3).setCellValue("Jumlah SKS  :");
        totalSks.createCell(4).setCellValue(sumSks);
        totalSks.createCell(5).setCellValue("IP Semester  :");
        totalSks.createCell(7).setCellValue(ip.getIpk().toString());
        totalSks.getCell(3).setCellStyle(styleIp);
        totalSks.getCell(5).setCellStyle(styleIp);
        totalSks.getCell(4).setCellStyle(styleIpk);
        totalSks.getCell(7).setCellStyle(styleIpk);
        sheet.addMergedRegion(new CellRangeAddress(rowTotalSks,rowTotalSks,5,6));

        int rowTotalIpk = 13 + krsDetail.size() + 1;
        Row totalIpk = sheet.createRow(rowTotalIpk);
        totalIpk.createCell(5).setCellValue("IPK   :");
        totalIpk.createCell(7).setCellValue(ipk.getIpk().toString());
        totalIpk.getCell(5).setCellStyle(styleIp);
        totalIpk.getCell(7).setCellStyle(styleIpk);
        sheet.addMergedRegion(new CellRangeAddress(rowTotalIpk,rowTotalIpk,5,6));

        int rowKoor = 13 + krsDetail.size() + 5;
        Row koor = sheet.createRow(rowKoor);
        koor.createCell(4).setCellValue("Koordinator Program Studi ");
        koor.getCell(4).setCellStyle(styleManajemen);

        int rowProdi = 13 + krsDetail.size() + 6;
        Row prodi = sheet.createRow(rowProdi);
        prodi.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi());
        prodi.getCell(4).setCellStyle(styleProdi);

        int rowDosen = 13 + krsDetail.size() + 11;
        Row namaDosen = sheet.createRow(rowDosen);
        namaDosen.createCell(4).setCellValue(mahasiswa.getIdProdi().getDosen().getKaryawan().getNamaKaryawan());
        namaDosen.getCell(4).setCellStyle(styleManajemen);


        String namaFile = "KHS-" +mahasiswa.getNim()+"-"+mahasiswa.getNama();
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    //Edom


    @GetMapping("/studiesActivity/assesment/hasiledom")
    public void hasilEdom(Model model, @RequestParam Jadwal jadwal, Authentication authentication) {

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        List<Object[]> headerEdom = edomMahasiswaDao.headerEdomMahasiswaPerDosen(jadwal, dosen);
        model.addAttribute("headerEdom", headerEdom);
        if (headerEdom != null){
            List<Object[]> detailEdom = edomQuestionDao.detailEdomPerDosen(jadwal, dosen);
            model.addAttribute("detailEdom", detailEdom);
        }else{
            model.addAttribute("questionNull", "Pertanyaan Edom Belum Dibuat");
        }
        model.addAttribute("jadwal", jadwal);

//        model.addAttribute("edom", krsDetailDao.edomJadwal(jadwal));
//
//        EdomQuestion edomQuestion1 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,1,jadwal.getTahunAkademik());
//        EdomQuestion edomQuestion2 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,2,jadwal.getTahunAkademik());
//        EdomQuestion edomQuestion3 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,3,jadwal.getTahunAkademik());
//        EdomQuestion edomQuestion4 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,4,jadwal.getTahunAkademik());
//        EdomQuestion edomQuestion5 = edomQuestionDao.findByStatusAndNomorAndTahunAkademik(StatusRecord.AKTIF,5,jadwal.getTahunAkademik());
//        model.addAttribute("edomQuestion1",edomQuestion1);
//        model.addAttribute("edomQuestion2",edomQuestion2);
//        model.addAttribute("edomQuestion3",edomQuestion3);
//        model.addAttribute("edomQuestion4",edomQuestion4);
//        model.addAttribute("edomQuestion5",edomQuestion5);




    }

//    BKD

    @GetMapping("/studiesActivity/assesment/topic")
    public void topic(Model model,@RequestParam Jadwal jadwal){
        String tahun = jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 9);



        model.addAttribute("tahun", tahun);
        model.addAttribute("topic", presensiDosenDao.bkdBeritaAcara(jadwal));
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));

    }

    @GetMapping("/studiesActivity/assesment/nilai")
    public void nilai(Model model,@RequestParam Jadwal jadwal){
        String tahun = jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 9);

        model.addAttribute("tahun", tahun);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));
        model.addAttribute("nilai", presensiMahasiswaDao.bkdNilai(jadwal));
    }

    @GetMapping("/studiesActivity/assesment/attendance")
    public void attendance(Model model,@RequestParam Jadwal jadwal){
        String tahun = jadwal.getTahunAkademik().getNamaTahunAkademik().substring(0, 9);

        model.addAttribute("tahun", tahun);

//        JadwalDosenDto jadwalDosenDto = (JadwalDosenDto) jadwalDosenDao.headerJadwal(jadwal.getId());

        model.addAttribute("jadwal", jadwal);

        model.addAttribute("dosen", jadwalDosenDao.headerJadwal(jadwal.getId()));

        List<Object[]> hasil = presensiMahasiswaDao.bkdAttendance(jadwal);
        log.debug("BKD Attendance : {}", hasil.size());
//
//        hasil = presensiMahasiswaDao.bkdAttendance(jadwal);
//        log.debug("BKD Attendance : {}", hasil.size());
//
//        hasil = presensiMahasiswaDao.bkdAttendance(jadwal);
//        log.debug("BKD Attendance : {}", hasil.size());

        model.addAttribute("attendance", hasil);


    }

    @GetMapping("/studiesActivity/transcript/list")
    public void checkTranscript(Model model, @RequestParam(required = false)String nim){


        if (nim != null) {
            model.addAttribute("nim", nim);
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            if (mahasiswa != null){
                model.addAttribute("mhsw",mahasiswa);

                //tampilsemua
                model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));
                model.addAttribute("sks", krsDetailDao.totalSks(mahasiswa));
                model.addAttribute("mutu", krsDetailDao.totalMutu(mahasiswa));
                model.addAttribute("semesterTranskript", krsDao.semesterTranskript(mahasiswa.getId()));
                model.addAttribute("transkriptTampil", krsDetailDao.transkriptTampil(mahasiswa.getId()));
            }else{
                model.addAttribute("message","error message");
            }

        }




    }

    public static String getPattern(int month) {
        String first = "MMMM d";
        String last = " yyyy";
        String pos = (month == 1 || month == 21 || month == 31) ? "'st'" : (month == 2 || month == 22) ? "'nd'" : (month == 3 || month == 23) ? "'rd'" : "'th'";
        return first + pos + last;
    }

    @GetMapping("/studiesActivity/transcript/cetaktranscript")
    public void cetakTranscript(Model model,@RequestParam(required = false) String nim){
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (StringUtils.hasText(nim)) {
            model.addAttribute("search",nim);

        }
        if (mahasiswa != null){
            model.addAttribute("mahasiswa",mahasiswa);

        }
    }

    @PostMapping("/studiesActivity/transcript/cetaktranscript")
    public String updateTranscript(@Valid @ModelAttribute Mahasiswa mahasiswa){

        mahasiswaDao.save(mahasiswa);

        return "redirect:cetaktranscript?nim=" + mahasiswa.getNim();

    }

    @GetMapping("/studiesActivity/transcript/transkriptexcel")
    public void transkriptExcel (@RequestParam(required = false) String nim, HttpServletResponse response) throws IOException {


        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        List<TranskriptDto> semester1 = krsDetailDao.excelTranskript(mahasiswa.getId(),"1");
        List<TranskriptDto> semester2 = krsDetailDao.excelTranskript(mahasiswa.getId(),"2");
        List<TranskriptDto> semester3 = krsDetailDao.excelTranskript(mahasiswa.getId(),"3");
        List<TranskriptDto> semester4 = krsDetailDao.excelTranskript(mahasiswa.getId(),"4");
        List<TranskriptDto> semester5 = krsDetailDao.excelTranskript(mahasiswa.getId(),"5");
        List<TranskriptDto> semester6 = krsDetailDao.excelTranskript(mahasiswa.getId(),"6");
        List<TranskriptDto> semester7 = krsDetailDao.excelTranskript(mahasiswa.getId(),"7");
        List<TranskriptDto> semester8 = krsDetailDao.excelTranskript(mahasiswa.getId(),"8");

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());

        IpkDto ipk = krsDetailDao.ipk(mahasiswa);

//        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);


        InputStream file = contohExcelTranskript.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), mahasiswa.getNama());

        sheet.addMergedRegion(CellRangeAddress.valueOf("A7:C7"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A8:C8"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A9:C9"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A10:C10"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A11:C11"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A12:C12"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A13:C13"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A14:C14"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A15:C15"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A16:C16"));

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

        Font symbolFont = workbook.createFont();
        symbolFont.setFontHeightInPoints((short) 10);
        symbolFont.setFontName("Cambria");

        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 10);
        dataFont.setFontName("Cambria");

        Font prodiFont = workbook.createFont();
        prodiFont.setUnderline(XSSFFont.U_DOUBLE);
        prodiFont.setFontHeightInPoints((short) 10);
        prodiFont.setFontName("Cambria");

        Font ipFont = workbook.createFont();
        ipFont.setBold(true);
        ipFont.setItalic(true);
        ipFont.setFontHeightInPoints((short) 10);
        ipFont.setFontName("Cambria");

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

        CellStyle styleData = workbook.createCellStyle();
        styleData.setFont(dataFont);

        CellStyle styleDataKhs = workbook.createCellStyle();
        styleDataKhs.setAlignment(HorizontalAlignment.CENTER);
        styleDataKhs.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataKhs.setFont(dataFont);

        CellStyle styleSymbol = workbook.createCellStyle();
        styleSymbol.setAlignment(HorizontalAlignment.CENTER);
        styleSymbol.setFont(symbolFont);

        CellStyle styleTotal = workbook.createCellStyle();
        styleTotal.setAlignment(HorizontalAlignment.CENTER);
        styleTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTotal.setFont(ipFont);

        CellStyle styleIpk = workbook.createCellStyle();
        styleIpk.setFont(prodiFont);
        styleIpk.setAlignment(HorizontalAlignment.CENTER);
        styleIpk.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle stylePrestasiAkademik = workbook.createCellStyle();
        stylePrestasiAkademik.setAlignment(HorizontalAlignment.LEFT);
        stylePrestasiAkademik.setVerticalAlignment(VerticalAlignment.CENTER);
        stylePrestasiAkademik.setFont(dataFont);

        CellStyle styleSubHeader1 = workbook.createCellStyle();
        styleSubHeader1.setAlignment(HorizontalAlignment.CENTER);
        styleSubHeader1.setVerticalAlignment(VerticalAlignment.CENTER);
        styleSubHeader1.setFont(ipFont);


        int rowInfoNama = 5 ;
        Row nama = sheet.createRow(rowInfoNama);
        nama.createCell(0).setCellValue("Name");
        nama.createCell(3).setCellValue(":");
        nama.createCell(4).setCellValue(mahasiswa.getNama());
        nama.getCell(0).setCellStyle(styleData);
        nama.getCell(3).setCellStyle(styleSymbol);
        nama.getCell(4).setCellStyle(styleData);

        int rowInfoNim = 6 ;
        Row matricNo = sheet.createRow(rowInfoNim);
        matricNo.createCell(0).setCellValue("Student Matric No");
        matricNo.createCell(3).setCellValue(":");
        matricNo.createCell(4).setCellValue(mahasiswa.getNim());
        matricNo.getCell(0).setCellStyle(styleData);
        matricNo.getCell(3).setCellStyle(styleSymbol);
        matricNo.getCell(4).setCellStyle(styleData);

        int rowInfoEntry = 7 ;
        Row entry = sheet.createRow(rowInfoEntry);
        entry.createCell(0).setCellValue("Entry Matric No / Graduated Matric No");
        entry.createCell(3).setCellValue(":");
        entry.createCell(4).setCellValue(mahasiswa.getNirm() + " / " + mahasiswa.getNirl());
        entry.getCell(0).setCellStyle(styleData);
        entry.getCell(3).setCellStyle(styleSymbol);
        entry.getCell(4).setCellStyle(styleData);

        int rowInfoBirth = 8 ;
        Row birthDay = sheet.createRow(rowInfoBirth);
        birthDay.createCell(0).setCellValue("Place and Date of Birth");
        birthDay.createCell(3).setCellValue(":");


        int month = mahasiswa.getTanggalLahir().getDayOfMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getPattern(month));
        String birthdate = mahasiswa.getTanggalLahir().format(formatter);
        birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + birthdate);
        birthDay.getCell(4).setCellStyle(styleData);


        birthDay.getCell(0).setCellStyle(styleData);
        birthDay.getCell(3).setCellStyle(styleSymbol);

        int rowInfoLevel = 9 ;
        Row level = sheet.createRow(rowInfoLevel);
        level.createCell(0).setCellValue("Level");
        level.createCell(3).setCellValue(":");

        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("01").get()){
            level.createCell(4).setCellValue("Undergraduate");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("02").get()){
            level.createCell(4).setCellValue("Post Graduate");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("03").get()){
            level.createCell(4).setCellValue("Undergraduate");
            level.getCell(4).setCellStyle(styleData);

        }
        level.getCell(0).setCellStyle(styleData);
        level.getCell(3).setCellStyle(styleSymbol);

        int rowInfoDepartment = 10 ;
        Row department = sheet.createRow(rowInfoDepartment);
        department.createCell(0).setCellValue("Department");
        department.createCell(3).setCellValue(":");
        department.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdiEnglish());
        department.getCell(0).setCellStyle(styleData);
        department.getCell(3).setCellStyle(styleSymbol);
        department.getCell(4).setCellStyle(styleData);

        int rowInfoFaculty = 11 ;
        Row facultyy = sheet.createRow(rowInfoFaculty);
        facultyy.createCell(0).setCellValue("Faculty");
        facultyy.createCell(3).setCellValue(":");
        facultyy.createCell(4).setCellValue(mahasiswa.getIdProdi().getFakultas().getNamaFakultasEnglish());
        facultyy.getCell(0).setCellStyle(styleData);
        facultyy.getCell(3).setCellStyle(styleSymbol);
        facultyy.getCell(4).setCellStyle(styleData);

        int rowInfoNoAcred = 12 ;
        Row accreditation = sheet.createRow(rowInfoNoAcred);
        accreditation.createCell(0).setCellValue("No of Accreditation Decree");
        accreditation.createCell(3).setCellValue(":");
        accreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getNoSk());
        accreditation.getCell(0).setCellStyle(styleData);
        accreditation.getCell(3).setCellStyle(styleSymbol);
        accreditation.getCell(4).setCellStyle(styleData);


        int rowInfoDateAcred = 13 ;
        Row dateAccreditation = sheet.createRow(rowInfoDateAcred);
        dateAccreditation.createCell(0).setCellValue("Date of Accreditation Decree");
        dateAccreditation.createCell(3).setCellValue(":");
        int monthAccred = mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth();
        DateTimeFormatter formatterAccred = DateTimeFormatter.ofPattern(getPattern(monthAccred));
        String accredDate = mahasiswa.getIdProdi().getTanggalSk().format(formatterAccred);
        dateAccreditation.createCell(4).setCellValue(accredDate);
        dateAccreditation.getCell(4).setCellStyle(styleData);

        dateAccreditation.getCell(0).setCellStyle(styleData);
        dateAccreditation.getCell(3).setCellStyle(styleSymbol);

        int rowInfoGraduatedDate = 14 ;
        Row graduatedDate = sheet.createRow(rowInfoGraduatedDate);
        graduatedDate.createCell(0).setCellValue("Graduated Date");
        graduatedDate.createCell(3).setCellValue(":");
        int monthGraduate = mahasiswa.getTanggalLulus().getDayOfMonth();
        DateTimeFormatter formatterGraduate = DateTimeFormatter.ofPattern(getPattern(monthGraduate));
        String graduateDate = mahasiswa.getTanggalLulus().format(formatterGraduate);

        graduatedDate.createCell(4).setCellValue(graduateDate);
        graduatedDate.getCell(4).setCellStyle(styleData);

        graduatedDate.getCell(0).setCellStyle(styleData);
        graduatedDate.getCell(3).setCellStyle(styleSymbol);

        int rowInfoTranscript = 15 ;
        Row transcript = sheet.createRow(rowInfoTranscript);
        transcript.createCell(0).setCellValue("No of Transcript");
        transcript.createCell(3).setCellValue(":");
        transcript.createCell(4).setCellValue(mahasiswa.getNoTranskript());
        transcript.getCell(0).setCellStyle(styleData);
        transcript.getCell(3).setCellStyle(styleSymbol);
        transcript.getCell(4).setCellStyle(styleData);

        int rowNumSemester1 = 18 ;
        for (TranskriptDto sem1 : semester1) {
            Row row = sheet.createRow(rowNumSemester1);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,7,8));

            row.createCell(0).setCellValue(sem1.getKode());
            row.createCell(1).setCellValue(sem1.getCourses());
            row.createCell(5).setCellValue(sem1.getSks());
            row.createCell(6).setCellValue(sem1.getGrade());
            row.createCell(7).setCellValue(sem1.getBobot().toString());
            row.createCell(9).setCellValue(sem1.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester1++;
        }

        int rowNumSemester2 = 18+semester1.size() ;
        for (TranskriptDto sem2 : semester2) {
            Row row = sheet.createRow(rowNumSemester2);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,7,8));

            row.createCell(0).setCellValue(sem2.getKode());
            row.createCell(1).setCellValue(sem2.getCourses());
            row.createCell(5).setCellValue(sem2.getSks());
            row.createCell(6).setCellValue(sem2.getGrade());
            row.createCell(7).setCellValue(sem2.getBobot().toString());
            row.createCell(9).setCellValue(sem2.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester2++;
        }

        int rowNumSemester3 = 18+semester1.size()+semester2.size() ;
        for (TranskriptDto sem3 : semester3) {
            Row row = sheet.createRow(rowNumSemester3);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,7,8));

            row.createCell(0).setCellValue(sem3.getKode());
            row.createCell(1).setCellValue(sem3.getCourses());
            row.createCell(5).setCellValue(sem3.getSks());
            row.createCell(6).setCellValue(sem3.getGrade());
            row.createCell(7).setCellValue(sem3.getBobot().toString());
            row.createCell(9).setCellValue(sem3.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester3++;
        }

        int rowNumSemester4 = 18+semester1.size()+semester2.size()+semester3.size() ;
        for (TranskriptDto sem4 : semester4) {
            Row row = sheet.createRow(rowNumSemester4);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,7,8));

            row.createCell(0).setCellValue(sem4.getKode());
            row.createCell(1).setCellValue(sem4.getCourses());
            row.createCell(5).setCellValue(sem4.getSks());
            row.createCell(6).setCellValue(sem4.getGrade());
            row.createCell(7).setCellValue(sem4.getBobot().toString());
            row.createCell(9).setCellValue(sem4.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester4++;
        }

        int rowNumSemester5 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size() ;
        for (TranskriptDto sem5 : semester5) {
            Row row = sheet.createRow(rowNumSemester5);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,7,8));

            row.createCell(0).setCellValue(sem5.getKode());
            row.createCell(1).setCellValue(sem5.getCourses());
            row.createCell(5).setCellValue(sem5.getSks());
            row.createCell(6).setCellValue(sem5.getGrade());
            row.createCell(7).setCellValue(sem5.getBobot().toString());
            row.createCell(9).setCellValue(sem5.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester5++;
        }

        int rowNumSemester6 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size() ;
        for (TranskriptDto sem6 : semester6) {
            Row row = sheet.createRow(rowNumSemester6);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,7,8));

            row.createCell(0).setCellValue(sem6.getKode());
            row.createCell(1).setCellValue(sem6.getCourses());
            row.createCell(5).setCellValue(sem6.getSks());
            row.createCell(6).setCellValue(sem6.getGrade());
            row.createCell(7).setCellValue(sem6.getBobot().toString());
            row.createCell(9).setCellValue(sem6.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester6++;
        }

        int rowNumSemester7 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size();
        for (TranskriptDto sem7 : semester7) {
            Row row = sheet.createRow(rowNumSemester7);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,7,8));

            row.createCell(0).setCellValue(sem7.getKode());
            row.createCell(1).setCellValue(sem7.getCourses());
            row.createCell(5).setCellValue(sem7.getSks());
            row.createCell(6).setCellValue(sem7.getGrade());
            row.createCell(7).setCellValue(sem7.getBobot().toString());
            row.createCell(9).setCellValue(sem7.getMutu().toString());
            row.getCell(1).setCellStyle(styleData);
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester7++;
        }

        int rowNumSemester8 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size();
        for (TranskriptDto sem8 : semester8) {
            Row row = sheet.createRow(rowNumSemester8);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,7,8));

            row.createCell(0).setCellValue(sem8.getKode());
            row.createCell(1).setCellValue(sem8.getCourses());
            row.createCell(5).setCellValue(sem8.getSks());
            row.createCell(6).setCellValue(sem8.getGrade());
            row.createCell(7).setCellValue(sem8.getBobot().toString());
            row.createCell(9).setCellValue(sem8.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester8++;
        }

        int total = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size();
        Row rowTotal = sheet.createRow(total);
        sheet.addMergedRegion(new CellRangeAddress(total,total,1,4));
        rowTotal.createCell(1).setCellValue("Total");
        rowTotal.createCell(5).setCellValue(totalSKS.intValue());
        rowTotal.createCell(9).setCellValue(totalMuti.toString());
        rowTotal.getCell(1).setCellStyle(styleTotal);
        rowTotal.getCell(5).setCellStyle(styleDataKhs);
        rowTotal.getCell(9).setCellStyle(styleDataKhs);

        int ipKomulatif = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+2;
        Row rowIpk = sheet.createRow(ipKomulatif);
        sheet.addMergedRegion(new CellRangeAddress(ipKomulatif,ipKomulatif,0,2));
        rowIpk.createCell(0).setCellValue("Cumulative Grade Point Average");
        rowIpk.createCell(5).setCellValue(ipk.getIpk().toString());
        rowIpk.getCell(0).setCellStyle(styleTotal);
        rowIpk.getCell(5).setCellStyle(styleDataKhs);

        int predicate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+4;
        Row predicateRow = sheet.createRow(predicate);
        predicateRow.createCell(0).setCellValue("Predicate :");
        if (ipk.getIpk().compareTo(new BigDecimal(2.99)) <= 0){
            predicateRow.createCell(1).setCellValue("Satisfactory");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(3.49)) <= 0){
            predicateRow.createCell(1).setCellValue("Good");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.50)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(3.79)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);

            if (validate != null){
                predicateRow.createCell(1).setCellValue("Good");
                predicateRow.getCell(1).setCellStyle(styleData);
            }else {
                predicateRow.createCell(1).setCellValue("Very Good");
                predicateRow.getCell(1).setCellStyle(styleData);
            }

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.80)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(4.00)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);

            if (validate != null){
                predicateRow.createCell(1).setCellValue("Good");
                predicateRow.getCell(21).setCellStyle(styleData);
            }else {
                predicateRow.createCell(1).setCellValue("Excellent");
                predicateRow.getCell(1).setCellStyle(styleData);
            }


        }

        predicateRow.getCell(0).setCellStyle(styleSubHeader);

        int thesis = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+5;
        Row thesisRow = sheet.createRow(thesis);
        thesisRow.createCell(0).setCellValue("Thesis Title :");
        thesisRow.createCell(1).setCellValue(mahasiswa.getTitle());
        thesisRow.getCell(0).setCellStyle(styleSubHeader);
        thesisRow.getCell(1).setCellStyle(styleData);

        int keyResult = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8;
        Row resultRow = sheet.createRow(keyResult);
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,0,3));
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,5,9));
        resultRow.createCell(0).setCellValue("Key to Result");
        resultRow.createCell(5).setCellValue("Grading System");
        resultRow.getCell(0).setCellStyle(styleDataKhs);
        resultRow.getCell(5).setCellStyle(styleDataKhs);

        int remark = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10;
        Row remarkRow = sheet.createRow(remark);
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,0,3));
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,7,9));
        remarkRow.createCell(0).setCellValue("Remarks");
        remarkRow.createCell(5).setCellValue("Grade");
        remarkRow.createCell(6).setCellValue("Value");
        remarkRow.createCell(7).setCellValue("Meaning");
        remarkRow.getCell(0).setCellStyle(styleDataKhs);
        remarkRow.getCell(5).setCellStyle(styleIpk);
        remarkRow.getCell(6).setCellStyle(styleIpk);
        remarkRow.getCell(7).setCellStyle(styleIpk);

        int excellent = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11;
        Row excellentRow = sheet.createRow(excellent);
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,1,3));
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,7,9));
        excellentRow.createCell(0).setCellValue("3,80-4,00");
        excellentRow.createCell(1).setCellValue("Excellent (Minimum B)");
        excellentRow.createCell(5).setCellValue("A");
        excellentRow.createCell(6).setCellValue("4");
        excellentRow.createCell(7).setCellValue("Excellent");
        excellentRow.getCell(0).setCellStyle(styleProdi);
        excellentRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        excellentRow.getCell(5).setCellStyle(styleDataKhs);
        excellentRow.getCell(6).setCellStyle(styleDataKhs);
        excellentRow.getCell(7).setCellStyle(styleManajemen);

        int veryGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12;
        Row veryGoodRow = sheet.createRow(veryGood);
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,1,3));
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,7,9));
        veryGoodRow.createCell(0).setCellValue("3,50-3,79");
        veryGoodRow.createCell(1).setCellValue("Very Good (Minimum B)");
        veryGoodRow.createCell(5).setCellValue("A-");
        veryGoodRow.createCell(6).setCellValue("3,7");
        veryGoodRow.createCell(7).setCellValue("Very Good");
        veryGoodRow.getCell(0).setCellStyle(styleProdi);
        veryGoodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        veryGoodRow.getCell(5).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(6).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(7).setCellStyle(styleManajemen);

        int good = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13;
        Row goodRow = sheet.createRow(good);
        sheet.addMergedRegion(new CellRangeAddress(good,good,1,3));
        sheet.addMergedRegion(new CellRangeAddress(good,good,7,9));
        goodRow.createCell(0).setCellValue("3,00-3,49");
        goodRow.createCell(1).setCellValue("Good");
        goodRow.createCell(5).setCellValue("B+");
        goodRow.createCell(6).setCellValue("3,3");
        goodRow.createCell(7).setCellValue("Good");
        goodRow.getCell(0).setCellStyle(styleProdi);
        goodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        goodRow.getCell(5).setCellStyle(styleDataKhs);
        goodRow.getCell(6).setCellStyle(styleDataKhs);
        goodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactory = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14;
        Row satisfactoryRow = sheet.createRow(satisfactory);
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,1,3));
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,7,9));
        satisfactoryRow.createCell(0).setCellValue("2,75-2,99");
        satisfactoryRow.createCell(1).setCellValue("Satisfactory");
        satisfactoryRow.createCell(5).setCellValue("B");
        satisfactoryRow.createCell(6).setCellValue("3");
        satisfactoryRow.createCell(7).setCellValue("Good");
        satisfactoryRow.getCell(0).setCellStyle(styleProdi);
        satisfactoryRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        satisfactoryRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(7).setCellStyle(styleManajemen);

        int almostGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+15;
        Row almostGoodRow = sheet.createRow(almostGood);
        sheet.addMergedRegion(new CellRangeAddress(almostGood,almostGood,7,9));
        almostGoodRow.createCell(5).setCellValue("B-");
        almostGoodRow.createCell(6).setCellValue("2,7");
        almostGoodRow.createCell(7).setCellValue("Almost Good");
        almostGoodRow.getCell(5).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(6).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryCplus = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+16;
        Row satisfactoryCplusRow = sheet.createRow(satisfactoryCplus);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryCplus,satisfactoryCplus,7,9));
        satisfactoryCplusRow.createCell(5).setCellValue("C+");
        satisfactoryCplusRow.createCell(6).setCellValue("2,3");
        satisfactoryCplusRow.createCell(7).setCellValue("Satisfactory");
        satisfactoryCplusRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryC = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+17;
        Row satisfactoryCRow = sheet.createRow(satisfactoryC);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryC,satisfactoryC,7,9));
        satisfactoryCRow.createCell(5).setCellValue("C");
        satisfactoryCRow.createCell(6).setCellValue("2");
        satisfactoryCRow.createCell(7).setCellValue("Satisfactory");
        satisfactoryCRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(7).setCellStyle(styleManajemen);

        int poor = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+18;
        Row poorRow = sheet.createRow(poor);
        sheet.addMergedRegion(new CellRangeAddress(poor,poor,7,9));
        poorRow.createCell(5).setCellValue("D");
        poorRow.createCell(6).setCellValue("1");
        poorRow.createCell(7).setCellValue("Poor");
        poorRow.getCell(5).setCellStyle(styleDataKhs);
        poorRow.getCell(6).setCellStyle(styleDataKhs);
        poorRow.getCell(7).setCellStyle(styleManajemen);

        int fail = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+19;
        Row failRow = sheet.createRow(fail);
        sheet.addMergedRegion(new CellRangeAddress(fail,fail,7,9));
        failRow.createCell(5).setCellValue("E");
        failRow.createCell(6).setCellValue("0");
        failRow.createCell(7).setCellValue("Fail");
        failRow.getCell(5).setCellStyle(styleDataKhs);
        failRow.getCell(6).setCellStyle(styleDataKhs);
        failRow.getCell(7).setCellStyle(styleManajemen);

        int createDate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+24;
        Row createDateRow = sheet.createRow(createDate);
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        String namaBulanHijri = islamicDate.format(DateTimeFormatter.ofPattern("MMMM", new Locale("en")));
        String tanggalHijri = islamicDate.format(DateTimeFormatter.ofPattern("d", new Locale("en")));
        String tahunHijri = islamicDate.format(DateTimeFormatter.ofPattern("yyyy", new Locale("en")));

        int monthCreate = LocalDate.now().getDayOfMonth();
        DateTimeFormatter formatterCreate = DateTimeFormatter.ofPattern(getPattern(monthCreate));
        String createDatee = LocalDate.now().format(formatterCreate);

        if (namaBulanHijri.equals("Jumada I")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else if (namaBulanHijri.equals("Jumada II")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else if (namaBulanHijri.equals("Rabiʻ I")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else if (namaBulanHijri.equals("Rabiʻ II")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else{
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }


        int faculty = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+26;
        Row facultyRow = sheet.createRow(faculty);
        facultyRow.createCell(0).setCellValue("Dean of ");
        facultyRow.getCell(0).setCellStyle(styleData);
        facultyRow.createCell(5).setCellValue("Coordinator of ");
        facultyRow.getCell(5).setCellStyle(styleData);

        int faculty2 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+27;
        Row facultyRow2 = sheet.createRow(faculty2);
        facultyRow2.createCell(0).setCellValue(mahasiswa.getIdProdi().getFakultas().getNamaFakultasEnglish());
        facultyRow2.getCell(0).setCellStyle(styleData);
        facultyRow2.createCell(5).setCellValue(mahasiswa.getIdProdi().getNamaProdiEnglish());
        facultyRow2.getCell(5).setCellStyle(styleData);

        int lecture = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+32;
        Row lectureRow = sheet.createRow(lecture);
        lectureRow.createCell(0).setCellValue(mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(0).setCellStyle(styleDosen);
        lectureRow.createCell(5).setCellValue(mahasiswa.getIdProdi().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(5).setCellStyle(styleDosen);

        int nik = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+33;
        Row nikRow = sheet.createRow(nik);
        nikRow.createCell(0).setCellValue("NIK : " + mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNik());
        nikRow.getCell(0).setCellStyle(styleNik);
        nikRow.createCell(5).setCellValue("NIK : " + mahasiswa.getIdProdi().getDosen().getKaryawan().getNik());
        nikRow.getCell(5).setCellStyle(styleNik);





        PropertyTemplate propertyTemplate = new PropertyTemplate();
//        semester1
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        //        semester2
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

//        semester3
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester4
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester5
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester6
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester7
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester8
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 5, 9),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);

        propertyTemplate.applyBorders(sheet);


        String namaFile = "Transkript-" +mahasiswa.getNim()+"-"+mahasiswa.getNama();
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/studiesActivity/transcript/transkriptindo")
    public void transkriptExcelIndo (@RequestParam(required = false) String nim, HttpServletResponse response) throws IOException {


        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        List<TranskriptDto> semester1 = krsDetailDao.excelTranskript(mahasiswa.getId(),"1");
        List<TranskriptDto> semester2 = krsDetailDao.excelTranskript(mahasiswa.getId(),"2");
        List<TranskriptDto> semester3 = krsDetailDao.excelTranskript(mahasiswa.getId(),"3");
        List<TranskriptDto> semester4 = krsDetailDao.excelTranskript(mahasiswa.getId(),"4");
        List<TranskriptDto> semester5 = krsDetailDao.excelTranskript(mahasiswa.getId(),"5");
        List<TranskriptDto> semester6 = krsDetailDao.excelTranskript(mahasiswa.getId(),"6");
        List<TranskriptDto> semester7 = krsDetailDao.excelTranskript(mahasiswa.getId(),"7");
        List<TranskriptDto> semester8 = krsDetailDao.excelTranskript(mahasiswa.getId(),"8");

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());

//        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);
        IpkDto ipk = krsDetailDao.ipk(mahasiswa);


        InputStream file = contohExcelTranskriptIndo.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), mahasiswa.getNama());

        sheet.addMergedRegion(CellRangeAddress.valueOf("A7:C7"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A8:C8"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A9:C9"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A10:C10"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A11:C11"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A12:C12"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A13:C13"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A14:C14"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A15:C15"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A16:C16"));

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

        Font symbolFont = workbook.createFont();
        symbolFont.setFontHeightInPoints((short) 10);
        symbolFont.setFontName("Cambria");

        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 10);
        dataFont.setFontName("Cambria");

        Font prodiFont = workbook.createFont();
        prodiFont.setUnderline(XSSFFont.U_DOUBLE);
        prodiFont.setFontHeightInPoints((short) 10);
        prodiFont.setFontName("Cambria");

        Font ipFont = workbook.createFont();
        ipFont.setBold(true);
        ipFont.setItalic(true);
        ipFont.setFontHeightInPoints((short) 10);
        ipFont.setFontName("Cambria");

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

        CellStyle styleData = workbook.createCellStyle();
        styleData.setFont(dataFont);

        CellStyle styleDataKhs = workbook.createCellStyle();
        styleDataKhs.setAlignment(HorizontalAlignment.CENTER);
        styleDataKhs.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataKhs.setFont(dataFont);

        CellStyle styleSymbol = workbook.createCellStyle();
        styleSymbol.setAlignment(HorizontalAlignment.CENTER);
        styleSymbol.setFont(symbolFont);

        CellStyle styleTotal = workbook.createCellStyle();
        styleTotal.setAlignment(HorizontalAlignment.CENTER);
        styleTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTotal.setFont(ipFont);

        CellStyle stylePrestasiAkademik = workbook.createCellStyle();
        stylePrestasiAkademik.setAlignment(HorizontalAlignment.LEFT);
        stylePrestasiAkademik.setVerticalAlignment(VerticalAlignment.CENTER);
        stylePrestasiAkademik.setFont(dataFont);

        CellStyle styleSubHeader1 = workbook.createCellStyle();
        styleSubHeader1.setAlignment(HorizontalAlignment.CENTER);
        styleSubHeader1.setVerticalAlignment(VerticalAlignment.CENTER);
        styleSubHeader1.setFont(ipFont);

        CellStyle styleIpk = workbook.createCellStyle();
        styleIpk.setFont(prodiFont);
        styleIpk.setAlignment(HorizontalAlignment.CENTER);
        styleIpk.setVerticalAlignment(VerticalAlignment.CENTER);


        int rowInfoNama = 5 ;
        Row nama = sheet.createRow(rowInfoNama);
        nama.createCell(0).setCellValue("Nama Mahasiswa ");
        nama.createCell(3).setCellValue(":");
        nama.createCell(4).setCellValue(mahasiswa.getNama());
        nama.getCell(0).setCellStyle(styleData);
        nama.getCell(3).setCellStyle(styleSymbol);
        nama.getCell(4).setCellStyle(styleData);

        int rowInfoNim = 6 ;
        Row matricNo = sheet.createRow(rowInfoNim);
        matricNo.createCell(0).setCellValue("NIM");
        matricNo.createCell(3).setCellValue(":");
        matricNo.createCell(4).setCellValue(mahasiswa.getNim());
        matricNo.getCell(0).setCellStyle(styleData);
        matricNo.getCell(3).setCellStyle(styleSymbol);
        matricNo.getCell(4).setCellStyle(styleData);

        int rowInfoEntry = 7 ;
        Row entry = sheet.createRow(rowInfoEntry);
        entry.createCell(0).setCellValue("NIRM/NIRL");
        entry.createCell(3).setCellValue(":");
        entry.createCell(4).setCellValue(mahasiswa.getNirm() + " / " + mahasiswa.getNirl());
        entry.getCell(0).setCellStyle(styleData);
        entry.getCell(3).setCellStyle(styleSymbol);
        entry.getCell(4).setCellStyle(styleData);

        int rowInfoBirth = 8 ;
        Row birthDay = sheet.createRow(rowInfoBirth);
        birthDay.createCell(0).setCellValue("Tempat, Tanggal lahir");
        birthDay.createCell(3).setCellValue(":");

        if (mahasiswa.getTanggalLahir().getMonthValue() == 1){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Januari" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 2){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Februari" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 3){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Maret" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 4){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " April" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 5){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Mei" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 6){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Juni" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 7){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Juli" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 8){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Agustus" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 9){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " September" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 10){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Oktober" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 11){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " November" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 12){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Desember" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        birthDay.getCell(0).setCellStyle(styleData);
        birthDay.getCell(3).setCellStyle(styleSymbol);

        int rowInfoLevel = 9 ;
        Row level = sheet.createRow(rowInfoLevel);
        level.createCell(0).setCellValue("Program Pendidikan");
        level.createCell(3).setCellValue(":");

        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("01").get()){
            level.createCell(4).setCellValue("Sarjana");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("02").get()){
            level.createCell(4).setCellValue("Magister");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("03").get()){
            level.createCell(4).setCellValue("Sarjana");
            level.getCell(4).setCellStyle(styleData);

        }
        level.getCell(0).setCellStyle(styleData);
        level.getCell(3).setCellStyle(styleSymbol);

        int rowInfoDepartment = 10 ;
        Row department = sheet.createRow(rowInfoDepartment);
        department.createCell(0).setCellValue("Program Studi");
        department.createCell(3).setCellValue(":");
        department.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi() );
        department.getCell(0).setCellStyle(styleData);
        department.getCell(3).setCellStyle(styleSymbol);
        department.getCell(4).setCellStyle(styleData);

        int rowInfoFaculty = 11 ;
        Row faculty = sheet.createRow(rowInfoFaculty);
        faculty.createCell(0).setCellValue("Fakultas");
        faculty.createCell(3).setCellValue(":");
        faculty.createCell(4).setCellValue(mahasiswa.getIdProdi().getFakultas().getNamaFakultas() );
        faculty.getCell(0).setCellStyle(styleData);
        faculty.getCell(3).setCellStyle(styleSymbol);
        faculty.getCell(4).setCellStyle(styleData);

        int rowInfoNoAcred = 12 ;
        Row accreditation = sheet.createRow(rowInfoNoAcred);
        accreditation.createCell(0).setCellValue("No SK BAN - PT");
        accreditation.createCell(3).setCellValue(":");
        accreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getNoSk());
        accreditation.getCell(0).setCellStyle(styleData);
        accreditation.getCell(3).setCellStyle(styleSymbol);
        accreditation.getCell(4).setCellStyle(styleData);


        int rowInfoDateAcred = 13 ;
        Row dateAccreditation = sheet.createRow(rowInfoDateAcred);
        dateAccreditation.createCell(0).setCellValue("Tanggal SK BAN - PT ");
        dateAccreditation.createCell(3).setCellValue(":");
        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 1){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Januari" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 2){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Februari" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 3){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Maret" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 4){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " April" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 5){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Mei" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 6){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Juni" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 7){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Juli" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 8){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Agustus" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 9){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " September" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 10){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Oktober" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 11){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " November" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 12){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Desember" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        dateAccreditation.getCell(0).setCellStyle(styleData);
        dateAccreditation.getCell(3).setCellStyle(styleSymbol);

        int rowInfoGraduatedDate = 14 ;
        Row graduatedDate = sheet.createRow(rowInfoGraduatedDate);
        graduatedDate.createCell(0).setCellValue("Tanggal Kelulusan");
        graduatedDate.createCell(3).setCellValue(":");
        if (mahasiswa.getTanggalLulus().getMonthValue() == 1){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Januari" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 2){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Februari" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 3){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Maret" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 4){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " April" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 5){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Mei" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 6){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Juni" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 7){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Juli" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 8){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Agustus" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 9){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " September" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 10){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Oktober" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 11){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " November" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 12){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Desember" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }


        graduatedDate.getCell(0).setCellStyle(styleData);
        graduatedDate.getCell(3).setCellStyle(styleSymbol);

        int rowInfoTranscript = 15 ;
        Row transcript = sheet.createRow(rowInfoTranscript);
        transcript.createCell(0).setCellValue("No Transkrip ");
        transcript.createCell(3).setCellValue(":");
        transcript.createCell(4).setCellValue(mahasiswa.getNoTranskript());
        transcript.getCell(0).setCellStyle(styleData);
        transcript.getCell(3).setCellStyle(styleSymbol);
        transcript.getCell(4).setCellStyle(styleData);

        int rowNumSemester1 = 18 ;
        for (TranskriptDto sem1 : semester1) {
            Row row = sheet.createRow(rowNumSemester1);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,7,8));

            row.createCell(0).setCellValue(sem1.getKode());
            row.createCell(1).setCellValue(sem1.getMatakuliah());
            row.createCell(5).setCellValue(sem1.getSks());
            row.createCell(6).setCellValue(sem1.getGrade());
            row.createCell(7).setCellValue(sem1.getBobot().toString());
            row.createCell(9).setCellValue(sem1.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester1++;
        }

        int rowNumSemester2 = 18+semester1.size() ;
        for (TranskriptDto sem2 : semester2) {
            Row row = sheet.createRow(rowNumSemester2);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,7,8));

            row.createCell(0).setCellValue(sem2.getKode());
            row.createCell(1).setCellValue(sem2.getMatakuliah());
            row.createCell(5).setCellValue(sem2.getSks());
            row.createCell(6).setCellValue(sem2.getGrade());
            row.createCell(7).setCellValue(sem2.getBobot().toString());
            row.createCell(9).setCellValue(sem2.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester2++;
        }

        int rowNumSemester3 = 18+semester1.size()+semester2.size() ;
        for (TranskriptDto sem3 : semester3) {
            Row row = sheet.createRow(rowNumSemester3);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,7,8));

            row.createCell(0).setCellValue(sem3.getKode());
            row.createCell(1).setCellValue(sem3.getMatakuliah());
            row.createCell(5).setCellValue(sem3.getSks());
            row.createCell(6).setCellValue(sem3.getGrade());
            row.createCell(7).setCellValue(sem3.getBobot().toString());
            row.createCell(9).setCellValue(sem3.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester3++;
        }

        int rowNumSemester4 = 18+semester1.size()+semester2.size()+semester3.size() ;
        for (TranskriptDto sem4 : semester4) {
            Row row = sheet.createRow(rowNumSemester4);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,7,8));

            row.createCell(0).setCellValue(sem4.getKode());
            row.createCell(1).setCellValue(sem4.getMatakuliah());
            row.createCell(5).setCellValue(sem4.getSks());
            row.createCell(6).setCellValue(sem4.getGrade());
            row.createCell(7).setCellValue(sem4.getBobot().toString());
            row.createCell(9).setCellValue(sem4.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester4++;
        }

        int rowNumSemester5 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size() ;
        for (TranskriptDto sem5 : semester5) {
            Row row = sheet.createRow(rowNumSemester5);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,7,8));

            row.createCell(0).setCellValue(sem5.getKode());
            row.createCell(1).setCellValue(sem5.getMatakuliah());
            row.createCell(5).setCellValue(sem5.getSks());
            row.createCell(6).setCellValue(sem5.getGrade());
            row.createCell(7).setCellValue(sem5.getBobot().toString());
            row.createCell(9).setCellValue(sem5.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester5++;
        }

        int rowNumSemester6 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size() ;
        for (TranskriptDto sem6 : semester6) {
            Row row = sheet.createRow(rowNumSemester6);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,7,8));

            row.createCell(0).setCellValue(sem6.getKode());
            row.createCell(1).setCellValue(sem6.getMatakuliah());
            row.createCell(5).setCellValue(sem6.getSks());
            row.createCell(6).setCellValue(sem6.getGrade());
            row.createCell(7).setCellValue(sem6.getBobot().toString());
            row.createCell(9).setCellValue(sem6.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester6++;
        }

        int rowNumSemester7 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size();
        for (TranskriptDto sem7 : semester7) {
            Row row = sheet.createRow(rowNumSemester7);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,7,8));

            row.createCell(0).setCellValue(sem7.getKode());
            row.createCell(1).setCellValue(sem7.getMatakuliah());
            row.createCell(5).setCellValue(sem7.getSks());
            row.createCell(6).setCellValue(sem7.getGrade());
            row.createCell(7).setCellValue(sem7.getBobot().toString());
            row.createCell(9).setCellValue(sem7.getMutu().toString());
            row.getCell(1).setCellStyle(styleData);
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester7++;
        }

        int rowNumSemester8 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size();
        for (TranskriptDto sem8 : semester8) {
            Row row = sheet.createRow(rowNumSemester8);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,7,8));

            row.createCell(0).setCellValue(sem8.getKode());
            row.createCell(1).setCellValue(sem8.getMatakuliah());
            row.createCell(5).setCellValue(sem8.getSks());
            row.createCell(6).setCellValue(sem8.getGrade());
            row.createCell(7).setCellValue(sem8.getBobot().toString());
            row.createCell(9).setCellValue(sem8.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester8++;
        }

        int total = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size();
        Row rowTotal = sheet.createRow(total);
        sheet.addMergedRegion(new CellRangeAddress(total,total,1,4));
        rowTotal.createCell(1).setCellValue("Jumlah");
        rowTotal.createCell(5).setCellValue(totalSKS.intValue());
        rowTotal.createCell(9).setCellValue(totalMuti.toString());
        rowTotal.getCell(1).setCellStyle(styleTotal);
        rowTotal.getCell(5).setCellStyle(styleDataKhs);
        rowTotal.getCell(9).setCellStyle(styleDataKhs);

        int ipKomulatif = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+2;
        Row rowIpk = sheet.createRow(ipKomulatif);
        sheet.addMergedRegion(new CellRangeAddress(ipKomulatif,ipKomulatif,0,2));
        rowIpk.createCell(0).setCellValue("Indeks Prestasi Kumulatif");
        rowIpk.createCell(5).setCellValue(ipk.getIpk().toString());
        rowIpk.getCell(0).setCellStyle(styleTotal);
        rowIpk.getCell(5).setCellStyle(styleDataKhs);

        int predicate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+4;
        Row predicateRow = sheet.createRow(predicate);
        predicateRow.createCell(0).setCellValue("Predikat :");
        if (ipk.getIpk().compareTo(new BigDecimal(2.99)) <= 0){
            predicateRow.createCell(1).setCellValue("Memuaskan");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(3.49)) <= 0){
            predicateRow.createCell(1).setCellValue("Sangat Memuaskan");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.50)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(3.79)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);
            if (validate != null){
                predicateRow.createCell(1).setCellValue("Sangat Memuaskan");
                predicateRow.getCell(1).setCellStyle(styleData);
            }else {
                predicateRow.createCell(1).setCellValue("Pujian ");
                predicateRow.getCell(1).setCellStyle(styleData);
            }

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.80)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(4.00)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);
            if (validate != null){
                predicateRow.createCell(1).setCellValue("Sangat Memuaskan");
                predicateRow.getCell(1).setCellStyle(styleData);
            }else {
                predicateRow.createCell(1).setCellValue("Pujian Tertinggi");
                predicateRow.getCell(1).setCellStyle(styleData);
            }


        }

        predicateRow.getCell(0).setCellStyle(styleSubHeader);

        int thesis = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+5;
        Row thesisRow = sheet.createRow(thesis);
        if (mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("01").get()){
            thesisRow.createCell(0).setCellValue("Judul skripsi :");
            thesisRow.createCell(1).setCellValue(mahasiswa.getJudul());
            thesisRow.getCell(0).setCellStyle(styleSubHeader);
            thesisRow.getCell(1).setCellStyle(styleData);
        }

        if (mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("02").get()){
            thesisRow.createCell(0).setCellValue("Judul Tesis :");
            thesisRow.createCell(1).setCellValue(mahasiswa.getJudul());
            thesisRow.getCell(0).setCellStyle(styleSubHeader);
            thesisRow.getCell(1).setCellStyle(styleData);
        }


        int keyResult = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8;
        Row resultRow = sheet.createRow(keyResult);
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,0,3));
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,5,9));
        resultRow.createCell(0).setCellValue("Prestasi Akademik");
        resultRow.createCell(5).setCellValue("Sistem Penilaian");
        resultRow.getCell(0).setCellStyle(styleDataKhs);
        resultRow.getCell(5).setCellStyle(styleDataKhs);

        int remark = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10;
        Row remarkRow = sheet.createRow(remark);
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,0,3));
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,7,9));
        remarkRow.createCell(0).setCellValue("Keterangan");
        remarkRow.createCell(5).setCellValue("HM");
        remarkRow.createCell(6).setCellValue("AM");
        remarkRow.createCell(7).setCellValue("Arti");
        remarkRow.getCell(0).setCellStyle(styleDataKhs);
        remarkRow.getCell(5).setCellStyle(styleIpk);
        remarkRow.getCell(6).setCellStyle(styleIpk);
        remarkRow.getCell(7).setCellStyle(styleIpk);

        int excellent = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11;
        Row excellentRow = sheet.createRow(excellent);
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,1,3));
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,7,9));
        excellentRow.createCell(0).setCellValue("3,80-4,00");
        excellentRow.createCell(1).setCellValue("Pujian Tertinggi (Minimal B)");
        excellentRow.createCell(5).setCellValue("A");
        excellentRow.createCell(6).setCellValue("4");
        excellentRow.createCell(7).setCellValue("Baik Sekali");
        excellentRow.getCell(0).setCellStyle(styleProdi);
        excellentRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        excellentRow.getCell(5).setCellStyle(styleDataKhs);
        excellentRow.getCell(6).setCellStyle(styleDataKhs);
        excellentRow.getCell(7).setCellStyle(styleManajemen);

        int veryGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12;
        Row veryGoodRow = sheet.createRow(veryGood);
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,1,3));
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,7,9));
        veryGoodRow.createCell(0).setCellValue("3,50-3,79");
        veryGoodRow.createCell(1).setCellValue("Pujian (Minimal B)");
        veryGoodRow.createCell(5).setCellValue("A-");
        veryGoodRow.createCell(6).setCellValue("3,7");
        veryGoodRow.createCell(7).setCellValue("Baik Sekali");
        veryGoodRow.getCell(0).setCellStyle(styleProdi);
        veryGoodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        veryGoodRow.getCell(5).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(6).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(7).setCellStyle(styleManajemen);

        int good = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13;
        Row goodRow = sheet.createRow(good);
        sheet.addMergedRegion(new CellRangeAddress(good,good,1,3));
        sheet.addMergedRegion(new CellRangeAddress(good,good,7,9));
        goodRow.createCell(0).setCellValue("3,00-3,49");
        goodRow.createCell(1).setCellValue("Sangat Memuaskan");
        goodRow.createCell(5).setCellValue("B+");
        goodRow.createCell(6).setCellValue("3,3");
        goodRow.createCell(7).setCellValue("Baik");
        goodRow.getCell(0).setCellStyle(styleProdi);
        goodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        goodRow.getCell(5).setCellStyle(styleDataKhs);
        goodRow.getCell(6).setCellStyle(styleDataKhs);
        goodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactory = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14;
        Row satisfactoryRow = sheet.createRow(satisfactory);
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,1,3));
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,7,9));
        satisfactoryRow.createCell(0).setCellValue("2,75-2,99");
        satisfactoryRow.createCell(1).setCellValue("Memuaskan");
        satisfactoryRow.createCell(5).setCellValue("B");
        satisfactoryRow.createCell(6).setCellValue("3");
        satisfactoryRow.createCell(7).setCellValue("Baik");
        satisfactoryRow.getCell(0).setCellStyle(styleProdi);
        satisfactoryRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        satisfactoryRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(7).setCellStyle(styleManajemen);

        int almostGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+15;
        Row almostGoodRow = sheet.createRow(almostGood);
        sheet.addMergedRegion(new CellRangeAddress(almostGood,almostGood,7,9));
        almostGoodRow.createCell(5).setCellValue("B-");
        almostGoodRow.createCell(6).setCellValue("2,7");
        almostGoodRow.createCell(7).setCellValue("Baik");
        almostGoodRow.getCell(5).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(6).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryCplus = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+16;
        Row satisfactoryCplusRow = sheet.createRow(satisfactoryCplus);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryCplus,satisfactoryCplus,7,9));
        satisfactoryCplusRow.createCell(5).setCellValue("C+");
        satisfactoryCplusRow.createCell(6).setCellValue("2,3");
        satisfactoryCplusRow.createCell(7).setCellValue("Cukup");
        satisfactoryCplusRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryC = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+17;
        Row satisfactoryCRow = sheet.createRow(satisfactoryC);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryC,satisfactoryC,7,9));
        satisfactoryCRow.createCell(5).setCellValue("C");
        satisfactoryCRow.createCell(6).setCellValue("2");
        satisfactoryCRow.createCell(7).setCellValue("Cukup");
        satisfactoryCRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(7).setCellStyle(styleManajemen);

        int poor = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+18;
        Row poorRow = sheet.createRow(poor);
        sheet.addMergedRegion(new CellRangeAddress(poor,poor,7,9));
        poorRow.createCell(5).setCellValue("D");
        poorRow.createCell(6).setCellValue("1");
        poorRow.createCell(7).setCellValue("Kurang");
        poorRow.getCell(5).setCellStyle(styleDataKhs);
        poorRow.getCell(6).setCellStyle(styleDataKhs);
        poorRow.getCell(7).setCellStyle(styleManajemen);

        int fail = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+19;
        Row failRow = sheet.createRow(fail);
        sheet.addMergedRegion(new CellRangeAddress(fail,fail,7,9));
        failRow.createCell(5).setCellValue("E");
        failRow.createCell(6).setCellValue("0");
        failRow.createCell(7).setCellValue("Sangat Kurang");
        failRow.getCell(5).setCellStyle(styleDataKhs);
        failRow.getCell(6).setCellStyle(styleDataKhs);
        failRow.getCell(7).setCellStyle(styleManajemen);

        int createDate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+24;
        Row createDateRow = sheet.createRow(createDate);
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        String namaBulanHijri = islamicDate.format(DateTimeFormatter.ofPattern("MMMM", new Locale("en")));
        String tanggalHijri = islamicDate.format(DateTimeFormatter.ofPattern("dd", new Locale("en")));
        String tahunHijri = islamicDate.format(DateTimeFormatter.ofPattern("yyyy", new Locale("en")));

        if (LocalDate.now().getMonthValue() == 1){

            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari " + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 2){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 3){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 4){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 5){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 6){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 7){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 8){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 9){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 10){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 11){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 12){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        int facultyy = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+26;
        Row facultyRow = sheet.createRow(facultyy);
        facultyRow.createCell(0).setCellValue("Dekan ");
        facultyRow.getCell(0).setCellStyle(styleData);
        facultyRow.createCell(5).setCellValue("Koordinator ");
        facultyRow.getCell(5).setCellStyle(styleData);

        int faculty2 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+27;
        Row facultyRow2 = sheet.createRow(faculty2);
        facultyRow2.createCell(0).setCellValue("Fakultas " + mahasiswa.getIdProdi().getFakultas().getNamaFakultas());
        facultyRow2.getCell(0).setCellStyle(styleData);
        facultyRow2.createCell(5).setCellValue("Program Studi " + mahasiswa.getIdProdi().getNamaProdi());
        facultyRow2.getCell(5).setCellStyle(styleData);

        int lecture = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+32;
        Row lectureRow = sheet.createRow(lecture);
        lectureRow.createCell(0).setCellValue(mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(0).setCellStyle(styleDosen);
        lectureRow.createCell(5).setCellValue(mahasiswa.getIdProdi().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(5).setCellStyle(styleDosen);

        int nik = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+33;
        Row nikRow = sheet.createRow(nik);
        nikRow.createCell(0).setCellValue("NIK : " + mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNik());
        nikRow.getCell(0).setCellStyle(styleNik);
        nikRow.createCell(5).setCellValue("NIK : " + mahasiswa.getIdProdi().getDosen().getKaryawan().getNik());
        nikRow.getCell(5).setCellStyle(styleNik);



        PropertyTemplate propertyTemplate = new PropertyTemplate();
//        semester1
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        //        semester2
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

//        semester3
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester4
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester5
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester6
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester7
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester8
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 5, 9),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);

        propertyTemplate.applyBorders(sheet);


        String namaFile = "Transkript-" +mahasiswa.getNim()+"-"+mahasiswa.getNama();
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/studiesActivity/transcript/print")
    public void printTranskript(Model model, @RequestParam(required = false)String nim){
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        model.addAttribute("mhsw",mahasiswa);
        model.addAttribute("ipk", krsDetailDao.ipk(mahasiswa));


        model.addAttribute("sks", krsDetailDao.totalSks(mahasiswa));
        model.addAttribute("mutu", krsDetailDao.totalMutu(mahasiswa));
        model.addAttribute("transkrip1", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"1"));
        model.addAttribute("transkrip2", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"2"));
        model.addAttribute("transkrip3", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"3"));
        model.addAttribute("transkrip4", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"4"));
        model.addAttribute("transkrip5", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"5"));
        model.addAttribute("transkrip6", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"6"));
        model.addAttribute("transkrip7", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"7"));
        model.addAttribute("transkrip8", krsDetailDao.transkripSemesterWithoutWaiting(mahasiswa,"8"));
    }

    @GetMapping("/studiesActivity/transcript/print1")
    public void printTranskript1(Model model, @RequestParam(required = false)String nim){
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        model.addAttribute("mhsw",mahasiswa);

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        List<DataTranskript> listTranskript = krsDetailDao.listTranskript(mahasiswa);
        listTranskript.removeIf(e -> e.getGrade().equals("E"));

        int sks = listTranskript.stream().map(DataTranskript::getSks).mapToInt(Integer::intValue).sum();


        BigDecimal mutu = listTranskript.stream().map(DataTranskript::getMutu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        System.out.println(sks);
        System.out.println(mutu);

        BigDecimal ipk = mutu.divide(new BigDecimal(sks),2,BigDecimal.ROUND_HALF_UP);


        model.addAttribute("ipk", ipk);
        model.addAttribute("sks", sks);
        model.addAttribute("mutu", mutu);

        List<DataTranskript> semester1 = new ArrayList<>();
        List<DataTranskript> semester2 = new ArrayList<>();
        List<DataTranskript> semester3 = new ArrayList<>();
        List<DataTranskript> semester4 = new ArrayList<>();
        List<DataTranskript> semester5 = new ArrayList<>();
        List<DataTranskript> semester6 = new ArrayList<>();
        List<DataTranskript> semester7 = new ArrayList<>();
        List<DataTranskript> semester8 = new ArrayList<>();


        for (DataTranskript data : listTranskript){
            if (data.getSemester().equals("1")){
                semester1.add(data);
            }
            if (data.getSemester().equals("2")){
                semester2.add(data);
            }
            if (data.getSemester().equals("3")){
                semester3.add(data);
            }
            if (data.getSemester().equals("4")){
                semester4.add(data);
            }
            if (data.getSemester().equals("5")){
                semester5.add(data);
            }
            if (data.getSemester().equals("6")){
                semester6.add(data);
            }
            if (data.getSemester().equals("7")){
                semester7.add(data);
            }
            if (data.getSemester().equals("8")){
                semester8.add(data);
            }
        }


        model.addAttribute("transkrip", krsDetailDao.listTranskript(mahasiswa));
        model.addAttribute("transkrip1", semester1);
        model.addAttribute("transkrip2", semester2);
        model.addAttribute("transkrip3", semester3);
        model.addAttribute("transkrip4", semester4);
        model.addAttribute("transkrip5", semester5);
        model.addAttribute("transkrip6", semester6);
        model.addAttribute("transkrip7", semester7);
        model.addAttribute("transkrip8", semester8);

    }

//    Exam Validation

    @GetMapping("/studiesActivity/validation/list")
    public void validationQuestion(Model model, @RequestParam(required = false)TahunAkademik tahun, Pageable page,
                                   @RequestParam(required = false)StatusApprove status, @RequestParam(required = false)String search){

        model.addAttribute("akademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS)));

        if (tahun != null && status == null) {
            model.addAttribute("tahunAkademik",tahun);
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,search,search,page));
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun, page));
            }

        }

        if (tahun != null && status != null){
            model.addAttribute("tahunAkademik",tahun);
            model.addAttribute("status",status);
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUtsAndDosenKaryawanNamaKaryawanContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNullOrStatusAndStatusUtsAndTahunAkademikAndMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,status,search,StatusRecord.AKTIF,status,tahun,search,page));
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUtsAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun,status, page));
            }
        }

    }

    @GetMapping("/studiesActivity/validation/listuas")
    public void validationUas(Model model, @RequestParam(required = false)TahunAkademik tahun, Pageable page,
                              @RequestParam(required = false)StatusApprove status, @RequestParam(required = false)String search){
        model.addAttribute("akademik", tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(Arrays.asList(StatusRecord.HAPUS)));

        if (tahun != null && status != null){
            model.addAttribute("status",status);
            model.addAttribute("tahunAkademik",tahun);
            if (StringUtils.hasText(search)) {
                model.addAttribute("search", search);
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUasAndDosenKaryawanNamaKaryawanContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNullOrStatusAndStatusUasAndTahunAkademikAndMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,status,search,StatusRecord.AKTIF,status,tahun,search,page));
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndStatusUasAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun,status, page));
            }
        }

        if (tahun != null && status == null) {
            model.addAttribute("tahunAkademik",tahun);
            if (StringUtils.hasText(search)) {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndDosenKaryawanNamaKaryawanContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNullOrStatusAndTahunAkademikAndMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord.AKTIF,tahun,search,StatusRecord.AKTIF,tahun,search,page));
                model.addAttribute("search", search);
            } else {
                model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord.AKTIF, tahun, page));
            }

        }
    }

    @GetMapping("/studiesActivity/validation/approval")
    public void approval(Model model,@RequestParam Jadwal jadwal,@RequestParam StatusRecord status){
        model.addAttribute("jadwal",jadwal);
        model.addAttribute("soal",soalDao.findByJadwalAndStatusAndStatusApproveNotInAndStatusSoal(jadwal,StatusRecord.AKTIF,Arrays.asList(StatusApprove.REJECTED,StatusApprove.APPROVED),status));
    }

    @GetMapping("/studiesActivity/validation/detail")
    public void detailApprove (Model model,@RequestParam Jadwal jadwal,@RequestParam StatusRecord status){
        model.addAttribute("jadwal",jadwal);
        model.addAttribute("soal",soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal,StatusRecord.AKTIF,StatusApprove.APPROVED,status));
    }

    @PostMapping("/studiesActivity/validation/approval")
    public String prosesApprove(@RequestParam Soal soal,MultipartFile file) throws Exception {


        String namaFile = file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();

        System.out.println("Nama File : {}" + namaFile);
        System.out.println("Jenis File : {}" + jenisFile);
        System.out.println("Nama Asli File : {}" + namaAsli);
        System.out.println("Ukuran File : {}"+ ukuran);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }


//        String idFile = soal.getJadwal().getTahunAkademik().getKodeTahunAkademik()+"-"+soal.getStatusSoal()+"-"+soal.getJadwal().getKelas().getNamaKelas()+"-"+soal.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()+"-"+soal.getDosen().getKaryawan().getNamaKaryawan();
        String idFile = "VLD-"+soal.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()+"-"+ soal.getJadwal().getKelas().getNamaKelas();
        if (idFile.length() > 65){

        }

        idFile = idFile.replaceAll(" ", "-").toLowerCase();
        String lokasiUpload = uploadFolder + File.separator + soal.getJadwal().getId();
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        file.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());
        soal.setFileApprove(idFile + "." + extension);
        soal.setStatusApprove(StatusApprove.APPROVED);
        soal.setKeteranganApprove(soal.getKeterangan());
        soalDao.save(soal);
//        mailService.validasiSoal(soal,"APPROVE");


        Jadwal jadwal =jadwalDao.findById(soal.getJadwal().getId()).get();
        if (soal.getStatusSoal() == StatusRecord.UTS) {
            jadwal.setStatusUts(StatusApprove.APPROVED);
        }
        if (soal.getStatusSoal() == StatusRecord.UAS) {
            jadwal.setStatusUas(StatusApprove.APPROVED);
        }
        jadwalDao.save(jadwal);

        if (soal.getStatusSoal() == StatusRecord.UAS){
            return "redirect:listuas?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.APPROVED;
        }
        return "redirect:list?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.APPROVED;
    }

    @PostMapping("/studiesActivity/validation/rejected")
    public String prosesReject(@RequestParam Soal soal,@RequestParam(required = false) String keteranganApprove){
        soal.setStatusApprove(StatusApprove.REJECTED);
        soal.setKeteranganApprove(keteranganApprove);
        soalDao.save(soal);
//        mailService.validasiSoal(soal,"REJECT");

        Jadwal jadwal = jadwalDao.findById(soal.getJadwal().getId()).get();
        if (soal.getStatusSoal() == StatusRecord.UTS) {
            jadwal.setStatusUts(StatusApprove.REJECTED);
        }
        if (soal.getStatusSoal() == StatusRecord.UAS) {
            jadwal.setStatusUas(StatusApprove.REJECTED);
        }
        jadwalDao.save(jadwal);

        if (soal.getStatusSoal() == StatusRecord.UAS) {
            return "redirect:listuas?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.REJECTED;
        }


        return "redirect:list?tahun="+soal.getJadwal().getTahunAkademik().getId()+"&status="+StatusApprove.REJECTED;
    }

    @RequestMapping("/filedownload/")
    public void downloadSoal( HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam Soal soal)
    {
        //If user is not authorized - he should be thrown out from here itself
        String fileName = soal.getFileApprove();
        //Authorized user will download the file
        String lokasi = uploadFolder+File.separator+soal.getJadwal().getId();
        String dataDirectory = request.getServletContext().getRealPath(lokasi);
        Path file = Paths.get(lokasi, fileName);
        System.out.println(file);
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

    @RequestMapping("/file/{fileName}")
    public void downloadPDFResource( HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam Soal soal,
                                     @PathVariable("fileName") String fileName)
    {
        //If user is not authorized - he should be thrown out from here itself

        //Authorized user will download the file
        String lokasi = uploadFolder+File.separator+soal.getJadwal().getId();
        String dataDirectory = request.getServletContext().getRealPath(lokasi);
        Path file = Paths.get(lokasi, fileName);
        System.out.println(file);
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



    @PostMapping("/studiesActivity/validation/canceluts")
    public String cancel (@RequestParam Jadwal jadwal)
    {
        Soal soal = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal,StatusRecord.AKTIF,StatusApprove.APPROVED,StatusRecord.UTS);
        soal.setStatusApprove(StatusApprove.REJECTED);
        soal.setKeteranganApprove("Dibatalkan");
        soalDao.save(soal);

        jadwal.setStatusUts(StatusApprove.REJECTED);
        jadwalDao.save(jadwal);

        return "redirect:list?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.APPROVED;

    }

    @PostMapping("/studiesActivity/validation/canceluas")
    public String cancelUas (@RequestParam Jadwal jadwal)
    {
        Soal soal = soalDao.findByJadwalAndStatusAndStatusApproveAndStatusSoal(jadwal,StatusRecord.AKTIF,StatusApprove.APPROVED,StatusRecord.UAS);
        soal.setStatusApprove(StatusApprove.REJECTED);
        soal.setKeteranganApprove("Dibatalkan");
        soalDao.save(soal);

        jadwal.setStatusUas(StatusApprove.REJECTED);
        jadwalDao.save(jadwal);

        return "redirect:listuas?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.APPROVED;

    }

    @GetMapping("/studiesActivity/assesment/upload/rps")
    public void listRps(@RequestParam Jadwal jadwal, Model model){
        model.addAttribute("jadwal", jadwal);
        Iterable<Rps> rps = rpsDao.findByStatusAndJadwal(StatusRecord.AKTIF,jadwal);

        model.addAttribute("rps",rps);

    }


    @PostMapping("/studiesActivity/assesment/upload/rps")
    public String uploadRps(@Valid Rps rps,MultipartFile file, @RequestParam Jadwal jadwal) throws Exception {


        String namaFile = file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();

        System.out.println("Nama File : {}" + namaFile);
        System.out.println("Jenis File : {}" + jenisFile);
        System.out.println("Nama Asli File : {}" + namaAsli);
        System.out.println("Ukuran File : {}"+ ukuran);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }


        String idFile = "RPS-" + jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah();
        String lokasiUpload = uploadRps + File.separator + rps.getJadwal().getId();
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        file.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());


        rps.setJadwal(jadwal);
        rps.setStatus(StatusRecord.AKTIF);
        rps.setTanggalUpload(LocalDate.now());
        rps.setFilename(idFile + "." + extension);
        Rps rpsId = rpsDao.findByJadwalAndStatus(rps.getJadwal(),StatusRecord.AKTIF);

        if (rpsId == null){
            rpsDao.save(rps);
        }

        if (rpsId != null){
            rpsId.setStatus(StatusRecord.NONAKTIF);
            rpsDao.save(rpsId);
            rpsDao.save(rps);
        }

        return "redirect:/studiesActivity/assesment/upload/rps?jadwal=" + rps.getJadwal().getId();

    }

//    Pra KRS SP

    @GetMapping("/studiesActivity/sp/list")
    public void listSp(Model model){

        List<Object[]> p1 = praKrsSpDao.listKrsSp();
        model.addAttribute("list", p1);
        model.addAttribute("listDosen", dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("listProdi", prodiDao.findAll());
        model.addAttribute("listTahun", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        if (tahun == null) {
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        }
        model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademik(StatusRecord.AKTIF, tahun));

        model.addAttribute("matkulDetail1", praKrsSpDao.detailSp(tahun));


    }

    @Transactional
    @PostMapping("/studiesActivity/sp/approve")
    public String approveSp(HttpServletRequest request, Authentication authentication){

        List<Object[]> p1 = praKrsSpDao.listKrsSp();
        for (Object[] mk : p1){
            String pilihan = request.getParameter("dosen-"+mk[0].toString());
            if (pilihan != null && !pilihan.trim().isEmpty()) {
                User user = currentUserService.currentUser(authentication);
                Karyawan karyawan = karyawanDao.findByIdUser(user);
                MatakuliahKurikulum matkul = matakuliahKurikulumDao.findById(request.getParameter("matkur-"+mk[0])).get();
                TahunAkademik tahunAkademik = tahunAkademikDao.findById(request.getParameter("tahunAkademik-"+mk[0])).get();

                List<KrsSpDto> listSp = praKrsSpDao.cariMatakuliah(tahunAkademik, mk[1].toString(), mk[0].toString(), mk[6].toString(), mk[2].toString(), "WAITING");
                System.out.println("tes : " + listSp.toString());
                List<String> listId = new ArrayList<>();
                for (KrsSpDto pks : listSp){
                    listId.add(pks.getSp());
                }
                System.out.println("mhs : " + listId.toString());
                praKrsSpDao.updateStatus(karyawan,listId, "APPROVED");

                Prodi prodi = prodiDao.findById(request.getParameter("prodi-"+mk[0])).get();

                Kelas kelas = kelasDao.findById("SP-01").get();
                TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, prodi);
                System.out.println("prodi : " + request.getParameter("prodi-"+mk[0].toString()));
                System.out.println("tahun aka : " + request.getParameter("tahunAkademik-"+mk[0].toString()));

                Jadwal jadwal = new Jadwal();
                jadwal.setJumlahSesi(1);
                jadwal.setBobotUts(BigDecimal.ZERO);
                jadwal.setBobotUas(BigDecimal.ZERO);
                jadwal.setBobotTugas(BigDecimal.ZERO);
                jadwal.setBobotPresensi(BigDecimal.ZERO);
                jadwal.setKelas(kelas);
                jadwal.setProdi(prodi);
                jadwal.setDosen(dosenDao.findById(request.getParameter("dosen-"+mk[0].toString())).get());
                jadwal.setTahunAkademik(tahunAkademik);
                jadwal.setTahunAkademikProdi(tahunProdi);
                jadwal.setMatakuliahKurikulum(matkul);
                jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
                jadwal.setProgram(programDao.findById("01").get());
                jadwal.setAkses(Akses.UMUM);
                jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
                jadwalDao.save(jadwal);
                System.out.println(matkul.getId());

                JadwalDosen jadwalDosen = new JadwalDosen();
                jadwalDosen.setStatusJadwalDosen(StatusJadwalDosen.PENGAMPU);
                jadwalDosen.setJadwal(jadwal);
                jadwalDosen.setDosen(dosenDao.findById(request.getParameter("dosen-"+mk[0].toString())).get());
                jadwalDosen.setJumlahIzin(0);
                jadwalDosen.setJumlahKehadiran(0);
                jadwalDosen.setJumlahMangkir(0);
                jadwalDosen.setJumlahSakit(0);
                jadwalDosen.setJumlahTerlambat(0);
                jadwalDosenDao.save(jadwalDosen);

//
                List<Object[]> spLunas = praKrsSpDao.listLunasSpPerMatkul(tahunAkademik, mk[1].toString(), mk[0].toString(), mk[6].toString(), mk[2].toString());
                for (Object[] listLunas : spLunas){
                    Mahasiswa mhs = mahasiswaDao.findByNim(listLunas[2].toString());
                    TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mhs.getIdProdi());
                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndTahunAkademikAndJadwalAndStatus(mhs, tahunAkademik, jadwal, StatusRecord.AKTIF);
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahunAkademik, StatusRecord.AKTIF);
                    System.out.println("create krs : " + mhs.getNim());
                    if (krsDetail == null){
                        if (k == null){
                            Krs krs = new Krs();
                            krs.setTahunAkademik(tahunAkademik);
                            krs.setTahunAkademikProdi(tahunAkademikProdi);
                            krs.setProdi(mhs.getIdProdi());
                            krs.setMahasiswa(mhs);
                            krs.setNim(mhs.getNim());
                            krs.setTanggalTransaksi(LocalDateTime.now());
                            krs.setStatus(StatusRecord.AKTIF);
                            krsDao.save(krs);

                            KrsDetail kd = new KrsDetail();
                            kd.setKrs(krs);
                            kd.setMahasiswa(mhs);
                            kd.setJadwal(jadwal);
                            kd.setMatakuliahKurikulum(matkul);
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
                            kd.setStatus(StatusRecord.AKTIF);
                            kd.setTahunAkademik(tahunAkademik);
                            krsDetailDao.save(kd);

                        }else{

                            KrsDetail kd = new KrsDetail();
                            kd.setKrs(k);
                            kd.setMahasiswa(mhs);
                            kd.setJadwal(jadwal);
                            kd.setMatakuliahKurikulum(matkul);
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
                            kd.setStatus(StatusRecord.AKTIF);
                            kd.setTahunAkademik(tahunAkademik);
                            krsDetailDao.save(kd);
                        }
                    }
                }
                return "redirect:../../academic/schedule/list?tahunAkademik="+tahunAkademik.getId()+"&prodi="+prodi.getId();
            }
        }

        return "redirect:list";

    }

    @PostMapping("/studiesActivity/sp/reject")
    public String rejectSp(HttpServletRequest request, Authentication authentication){

        List<Object[]> p1 = praKrsSpDao.listKrsSp();
        for (Object[] mk : p1){
            String pilihan = request.getParameter("matkur-"+mk[0].toString());
            if (pilihan != null && !pilihan.trim().isEmpty()) {
                User user = currentUserService.currentUser(authentication);
                Karyawan karyawan = karyawanDao.findByIdUser(user);
                MatakuliahKurikulum matkul = matakuliahKurikulumDao.findById(request.getParameter("matkur-"+mk[0])).get();
                TahunAkademik tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
                if (tahunAkademik == null) {
                    tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
                }

                List<KrsSpDto> listSp = praKrsSpDao.cariMatakuliah(tahunAkademik, mk[1].toString(), mk[0].toString(), mk[6].toString(), mk[2].toString(), "WAITING");
                List<String> listId = new ArrayList<>();
                for (KrsSpDto pks : listSp){
                    listId.add(pks.getSp());
                }

                praKrsSpDao.updateReject(karyawan, listId);

            }
        }
        return "redirect:list";
    }

    @GetMapping("/studiesActivity/sp/custom")
    public void customKelas(Model model, @RequestParam String id, @RequestParam(required = false) String jenis, @RequestParam(required = false) String jumlah,
                            @RequestParam(required = false) String maks){

        MatakuliahKurikulum mk = matakuliahKurikulumDao.findById(id).get();
        model.addAttribute("matkul", mk);
        if (jenis != null){
            model.addAttribute("jadwal", jadwalDao.findByMatakuliahKurikulumAndStatus(mk, StatusRecord.PREVIEW));
            model.addAttribute("jenis", jenis);
            model.addAttribute("jumlah", jumlah);
            model.addAttribute("maks", maks);
            model.addAttribute("listProdi", prodiDao.findAll());
            model.addAttribute("listDosen", dosenDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS)));
            model.addAttribute("listTahun", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
            model.addAttribute("listKelas", kelasDao.findByStatusAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord.AKTIF, "SP"));
        }

    }

    @PostMapping("/studiesActivity/sp/custom")
    public String kelasCustom(@RequestParam String id, @RequestParam(required = false) String jenis, @RequestParam(required = false) String jumlah,
                              @RequestParam(required = false) String maks, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        MatakuliahKurikulum mk = matakuliahKurikulumDao.findById(id).get();
        List<Jadwal> cekPreview = jadwalDao.findByMatakuliahKurikulumAndStatus(mk, StatusRecord.PREVIEW);
        for (Jadwal j : cekPreview){
            jadwalDao.delete(j);
        }

        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        if (tahun == null) {
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        }

        SpDto m = praKrsSpDao.tampilPerMatkul(mk.getId());
        List<KrsSpDto> list = praKrsSpDao.cariMatakuliah(tahun, m.getIdMatakuliah(), m.getId(), m.getKode(), m.getNamaMatakuliah(), "WAITING");
        if (!list.isEmpty() || list != null) {
            System.out.println("cek list: " + list);
            List<String> listId = new ArrayList<>();
            for(KrsSpDto pks : list){
                listId.add(pks.getSp());
            }
            praKrsSpDao.updateSp(karyawan,listId);
            System.out.println("mhs : " + listId );
        }

        int j = Integer.parseInt(jumlah);
        for(int i = 1; i <= j; i++){
            Jadwal jadwal = new Jadwal();
            jadwal.setJumlahSesi(1);
            jadwal.setBobotTugas(BigDecimal.ZERO);
            jadwal.setBobotUas(BigDecimal.ZERO);
            jadwal.setBobotUts(BigDecimal.ZERO);
            jadwal.setBobotPresensi(BigDecimal.ZERO);
            jadwal.setMatakuliahKurikulum(mk);
            jadwal.setStatusUas(StatusApprove.NOT_UPLOADED_YET);
            jadwal.setProgram(programDao.findById("01").get());
            jadwal.setAkses(Akses.UMUM);
            jadwal.setStatusUts(StatusApprove.NOT_UPLOADED_YET);
            jadwal.setStatus(StatusRecord.PREVIEW);
            jadwalDao.save(jadwal);
        }

        return "redirect:custom?id="+id+"&jenis="+jenis+"&jumlah="+jumlah+"&maks="+maks;
    }

    @Transactional
    @PostMapping("/studiesActivity/sp/cancel")
    public String cancelKelas(@RequestParam String id, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        MatakuliahKurikulum mk = matakuliahKurikulumDao.findById(id).get();

        List<Jadwal> cekJadwal = jadwalDao.findByMatakuliahKurikulumAndStatus(mk, StatusRecord.PREVIEW);
        for (Jadwal j : cekJadwal){
            jadwalDao.delete(j);
        }

        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        if (tahun == null){
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        }

        SpDto m = praKrsSpDao.tampilPerMatkul(id);
        List<KrsSpDto> list = praKrsSpDao.cariMatakuliah(tahun, m.getIdMatakuliah(), m.getId(), m.getKode(), m.getNamaMatakuliah(), "APPROVED");
        if (!list.isEmpty() || list != null){
            System.out.println("cek list : " + list);
            List<String> listId = new ArrayList<>();
            for (KrsSpDto pks : list){
                listId.add(pks.getSp());
            }
            System.out.println("mhs : " + listId);
            praKrsSpDao.updateStatus(karyawan, listId, "WAITING");
        }

        return "redirect:list";

    }

    @Transactional
    @PostMapping("/studiesActivity/sp/custom/submit")
    public String submitCustom(@RequestParam String id, @RequestParam(required = false) String jenis, @RequestParam(required = false) String jumlah, @RequestParam(required = false) String maks,
                               HttpServletRequest request, Authentication authentication){

        MatakuliahKurikulum mk = matakuliahKurikulumDao.findById(id).get();
        List<Jadwal> jadwal = jadwalDao.findByMatakuliahKurikulumAndStatus(mk, StatusRecord.PREVIEW);
        for (Jadwal j : jadwal){
            String pilihan = request.getParameter("dosen-"+j.getId());
            if (pilihan != null && !pilihan.trim().isEmpty()) {
                User user = currentUserService.currentUser(authentication);
                Karyawan karyawan = karyawanDao.findByIdUser(user);
                TahunAkademik tahun = tahunAkademikDao.findById(request.getParameter("tahun-"+j.getId())).get();
                Prodi prodi = prodiDao.findById(request.getParameter("prodi-"+j.getId())).get();
                TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, prodi);
                SpDto m = praKrsSpDao.tampilPerMatkul(mk.getId());

                System.out.println("jenis : " + jenis);

                if (jenis.equals("campur")){

                    Kelas kelas = kelasDao.findById("SP-01").get();
                    Dosen dosen = dosenDao.findById(pilihan).get();

                    Jadwal jdwl = jadwalDao.findById(j.getId()).get();
                    jdwl.setKelas(kelas);
                    jdwl.setTahunAkademik(tahun);
                    jdwl.setProdi(prodi);
                    jdwl.setTahunAkademikProdi(tahunProdi);
                    jdwl.setDosen(dosen);
                    jdwl.setStatus(StatusRecord.AKTIF);
                    jadwalDao.save(jdwl);

                    JadwalDosen jd = new JadwalDosen();
                    jd.setStatusJadwalDosen(StatusJadwalDosen.PENGAMPU);
                    jd.setJadwal(jdwl);
                    jd.setDosen(dosen);
                    jd.setJumlahIzin(0);
                    jd.setJumlahKehadiran(0);
                    jd.setJumlahMangkir(0);
                    jd.setJumlahSakit(0);
                    jd.setJumlahTerlambat(0);
                    jadwalDosenDao.save(jd);

                    List<Object[]> spLunas = praKrsSpDao.listLunasSpPerMatkul(tahun, m.getIdMatakuliah(), m.getId(), m.getKode(), m.getNamaMatakuliah());
                    System.out.println("tes lunas : " + spLunas);
                    for (Object[] listLunas : spLunas){
                        Mahasiswa mhs = mahasiswaDao.findByNim(listLunas[2].toString());
                        TahunAkademikProdi tap = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());
                        KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndTahunAkademikAndJadwalAndStatus(mhs, tahun, jdwl, StatusRecord.AKTIF);
                        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                        if (krsDetail == null) {
                            if (k == null) {
                                Krs krs = new Krs();
                                krs.setTahunAkademikProdi(tap);
                                krs.setProdi(mhs.getIdProdi());
                                krs.setMahasiswa(mhs);
                                krs.setNim(mhs.getNim());
                                krs.setTanggalTransaksi(LocalDateTime.now());
                                krs.setStatus(StatusRecord.AKTIF);
                                krsDao.save(krs);

                                KrsDetail kd = new KrsDetail();
                                kd.setKrs(krs);
                                kd.setMahasiswa(mhs);
                                kd.setJadwal(jdwl);
                                kd.setMatakuliahKurikulum(mk);
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
                                kd.setStatus(StatusRecord.AKTIF);
                                kd.setTahunAkademik(tahun);
                                krsDetailDao.save(kd);

                            }else{
                                KrsDetail kd = new KrsDetail();
                                kd.setKrs(k);
                                kd.setMahasiswa(mhs);
                                kd.setJadwal(jdwl);
                                kd.setMatakuliahKurikulum(mk);
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
                                kd.setStatus(StatusRecord.AKTIF);
                                kd.setTahunAkademik(tahun);
                                krsDetailDao.save(kd);
                            }
                        }

                    }


                } else if (jenis.equals("pisah")) {
                    Kelas kelas = kelasDao.findById(request.getParameter("kelas-"+j.getId())).get();
                    Dosen dosen = dosenDao.findById(pilihan).get();

                    Jadwal jdwl = jadwalDao.findById(j.getId()).get();
                    jdwl.setKelas(kelas);
                    jdwl.setTahunAkademik(tahun);
                    jdwl.setProdi(prodi);
                    jdwl.setTahunAkademikProdi(tahunProdi);
                    jdwl.setDosen(dosen);
                    jdwl.setStatus(StatusRecord.AKTIF);
                    jadwalDao.save(jdwl);

                    JadwalDosen jd = new JadwalDosen();
                    jd.setStatusJadwalDosen(StatusJadwalDosen.PENGAMPU);
                    jd.setJadwal(jdwl);
                    jd.setDosen(dosen);
                    jd.setJumlahIzin(0);
                    jd.setJumlahKehadiran(0);
                    jd.setJumlahMangkir(0);
                    jd.setJumlahSakit(0);
                    jd.setJumlahTerlambat(0);
                    jadwalDosenDao.save(jd);

                    if (kelas.getKodeKelas().equals("SP-01-AKHWAT")){
                        List<Object[]> listLunas = praKrsSpDao.listLunasPisahKelas(tahun, m.getIdMatakuliah(), m.getId(), m.getKode(), m.getNamaMatakuliah(), "WANITA");
                        System.out.println("tes lunas : " + listLunas);
                        for (Object[] akhwatLunas : listLunas){
                            Mahasiswa mhs = mahasiswaDao.findByNim(akhwatLunas[2].toString());
                            TahunAkademikProdi tap = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());
                            KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndTahunAkademikAndJadwalAndStatus(mhs, tahun, jdwl, StatusRecord.AKTIF);
                            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                            if (krsDetail == null){
                                if (k == null) {
                                    Krs krs = new Krs();
                                    krs.setTahunAkademik(tahun);
                                    krs.setProdi(mhs.getIdProdi());
                                    krs.setTahunAkademikProdi(tap);
                                    krs.setMahasiswa(mhs);
                                    krs.setNim(mhs.getNim());
                                    krs.setTanggalTransaksi(LocalDateTime.now());
                                    krs.setStatus(StatusRecord.AKTIF);
                                    krsDao.save(krs);

                                    KrsDetail kd = new KrsDetail();
                                    kd.setKrs(krs);
                                    kd.setMahasiswa(mhs);
                                    kd.setJadwal(jdwl);
                                    kd.setMatakuliahKurikulum(mk);
                                    kd.setNilaiPresensi(BigDecimal.ZERO);
                                    kd.setNilaiUts(BigDecimal.ZERO);
                                    kd.setNilaiUas(BigDecimal.ZERO);
                                    kd.setNilaiTugas(BigDecimal.ZERO);
                                    kd.setFinalisasi("N");
                                    kd.setJumlahKehadiran(0);
                                    kd.setJumlahMangkir(0);
                                    kd.setJumlahTerlambat(0);
                                    kd.setJumlahIzin(0);
                                    kd.setJumlahSakit(0);
                                    kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setStatusEdom(StatusRecord.UNDONE);
                                    kd.setStatus(StatusRecord.AKTIF);
                                    kd.setTahunAkademik(tahun);
                                    krsDetailDao.save(kd);
                                }else{
                                    KrsDetail kd = new KrsDetail();
                                    kd.setKrs(k);
                                    kd.setMahasiswa(mhs);
                                    kd.setJadwal(jdwl);
                                    kd.setMatakuliahKurikulum(mk);
                                    kd.setNilaiPresensi(BigDecimal.ZERO);
                                    kd.setNilaiUts(BigDecimal.ZERO);
                                    kd.setNilaiUas(BigDecimal.ZERO);
                                    kd.setNilaiTugas(BigDecimal.ZERO);
                                    kd.setFinalisasi("N");
                                    kd.setJumlahKehadiran(0);
                                    kd.setJumlahMangkir(0);
                                    kd.setJumlahTerlambat(0);
                                    kd.setJumlahIzin(0);
                                    kd.setJumlahSakit(0);
                                    kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setStatusEdom(StatusRecord.UNDONE);
                                    kd.setStatus(StatusRecord.AKTIF);
                                    kd.setTahunAkademik(tahun);
                                    krsDetailDao.save(kd);
                                }
                            }

                        }

                    }else if (kelas.getKodeKelas().equals("SP-01-IKHWAN")){
                        List<Object[]> listLunas = praKrsSpDao.listLunasPisahKelas(tahun, m.getIdMatakuliah(), m.getId(), m.getKode(), m.getNamaMatakuliah(), "PRIA");
                        System.out.println("tes lunas : " + listLunas);
                        for (Object[] listIkhwan : listLunas){
                            Mahasiswa mhs = mahasiswaDao.findByNim(listIkhwan[2].toString());
                            TahunAkademikProdi tap = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());
                            KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndTahunAkademikAndJadwalAndStatus(mhs, tahun, jdwl, StatusRecord.AKTIF);
                            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                            if (krsDetail == null) {
                                if (k == null) {
                                    Krs krs = new Krs();
                                    krs.setMahasiswa(mhs);
                                    krs.setTahunAkademik(tahun);
                                    krs.setProdi(mhs.getIdProdi());
                                    krs.setTahunAkademikProdi(tap);
                                    krs.setNim(mhs.getNim());
                                    krs.setTanggalTransaksi(LocalDateTime.now());
                                    krs.setStatus(StatusRecord.AKTIF);
                                    krsDao.save(krs);

                                    KrsDetail kd = new KrsDetail();
                                    kd.setKrs(krs);
                                    kd.setMahasiswa(mhs);
                                    kd.setJadwal(jdwl);
                                    kd.setMatakuliahKurikulum(mk);
                                    kd.setNilaiPresensi(BigDecimal.ZERO);
                                    kd.setNilaiUas(BigDecimal.ZERO);
                                    kd.setNilaiUts(BigDecimal.ZERO);
                                    kd.setNilaiTugas(BigDecimal.ZERO);
                                    kd.setFinalisasi("N");
                                    kd.setJumlahKehadiran(0);
                                    kd.setJumlahMangkir(0);
                                    kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setJumlahTerlambat(0);
                                    kd.setJumlahIzin(0);
                                    kd.setJumlahSakit(0);
                                    kd.setStatusEdom(StatusRecord.UNDONE);
                                    kd.setStatus(StatusRecord.AKTIF);
                                    kd.setTahunAkademik(tahun);
                                    krsDetailDao.save(kd);

                                }else{
                                    KrsDetail kd = new KrsDetail();
                                    kd.setKrs(k);
                                    kd.setMahasiswa(mhs);
                                    kd.setJadwal(jdwl);
                                    kd.setMatakuliahKurikulum(mk);
                                    kd.setNilaiPresensi(BigDecimal.ZERO);
                                    kd.setNilaiUas(BigDecimal.ZERO);
                                    kd.setNilaiUts(BigDecimal.ZERO);
                                    kd.setNilaiTugas(BigDecimal.ZERO);
                                    kd.setFinalisasi("N");
                                    kd.setJumlahKehadiran(0);
                                    kd.setJumlahMangkir(0);
                                    kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setJumlahTerlambat(0);
                                    kd.setJumlahIzin(0);
                                    kd.setJumlahSakit(0);
                                    kd.setStatusEdom(StatusRecord.UNDONE);
                                    kd.setStatus(StatusRecord.AKTIF);
                                    kd.setTahunAkademik(tahun);
                                    krsDetailDao.save(kd);
                                }
                            }
                        }
                    }

                }
            }
        }
        return "redirect:../list";

    }

    @GetMapping("/studiesActivity/sp/detail")
    public void spDetail(Model model){

        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        if (tahun == null) {
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        }
        List<Object[]> detailSp = praKrsSpDao.allDetail(tahun);
        model.addAttribute("detailSp", detailSp);

    }

    @GetMapping("/download/detail/sp")
    public void listDetail(HttpServletResponse response) throws IOException{

        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        if (tahun == null) {
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        }
        List<Object[]> listDetail = praKrsSpDao.allDetail(tahun);

        String[] columns = {"No", "Nama", "NIM", "Prodi", "Matakuliah", "SKS", "Pembayaran", "Tanggal Pembayaran"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List detail mahasiswa request SP");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;
        int baris = 1;

        for(Object[] detail : listDetail){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(baris++);
            row.createCell(1).setCellValue(detail[3].toString());
            row.createCell(2).setCellValue(detail[2].toString());
            row.createCell(3).setCellValue(detail[4].toString());
            row.createCell(4).setCellValue(detail[6].toString());
            row.createCell(5).setCellValue(detail[7].toString());
            row.createCell(6).setCellValue(detail[9].toString());
            row.createCell(7).setCellValue(detail[10].toString());
        }

        for (int i = 1; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=Detail_MHS_SP_"+LocalDate.now()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }

    @GetMapping("/download/list")
    public void listPerMatkul(@RequestParam(required = false) String matkul, HttpServletResponse response) throws  IOException{

        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        if (tahun == null) {
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        }
        MatakuliahKurikulum matkur = matakuliahKurikulumDao.findById(matkul).get();
        SpDto m = praKrsSpDao.tampilPerMatkul(matkur.getId());
        List<Object[]> listDownload = praKrsSpDao.excelDownlaod(tahun, m.getId(), m.getIdMatakuliah(), m.getKode(), m.getNamaMatakuliah());

        String[] columns = {"No", "Nim", "Nama", "Prodi", "Nomor Telepon", "Status Pembayaran" , "Tanggal Pembayaran"};

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("List Mahasiswa Request SP");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        sheet.createRow(0).createCell(2).setCellValue("Matakuliah : " + matkur.getMatakuliah().getNamaMatakuliah());

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
            row.createCell(1).setCellValue(list[2].toString());
            row.createCell(2).setCellValue(list[3].toString());
            row.createCell(3).setCellValue(list[5].toString());
            row.createCell(4).setCellValue(list[4].toString());
            row.createCell(5).setCellValue(list[9].toString());
            row.createCell(6).setCellValue(list[10].toString());
        }

        for (int i = 0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }


        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=List_Request_SP_Matkul_"+matkur.getMatakuliah().getNamaMatakuliah()+"_"+LocalDate.now()+".xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();

    }


    @GetMapping("/studiesActivity/transcript/transkriptindo2")
    public void transkriptFormat2 (@RequestParam(required = false) String nim, HttpServletResponse response) throws IOException {


        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

//        Long totalSKS = krsDetailDao.totalSks(mahasiswa);
//        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

//        file
        List<DataTranskript> listTranskript = krsDetailDao.listTranskript(mahasiswa);
        listTranskript.removeIf(e -> e.getGrade().equals("E"));

        int totalSKS = listTranskript.stream().map(DataTranskript::getSks).mapToInt(Integer::intValue).sum();


        BigDecimal totalMuti = listTranskript.stream().map(DataTranskript::getMutu)
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        BigDecimal ipk = totalMuti.divide(new BigDecimal(totalSKS),2,BigDecimal.ROUND_HALF_UP);

        List<DataTranskript> semester1 = new ArrayList<>();
        List<DataTranskript> semester2 = new ArrayList<>();
        List<DataTranskript> semester3 = new ArrayList<>();
        List<DataTranskript> semester4 = new ArrayList<>();
        List<DataTranskript> semester5 = new ArrayList<>();
        List<DataTranskript> semester6 = new ArrayList<>();
        List<DataTranskript> semester7 = new ArrayList<>();
        List<DataTranskript> semester8 = new ArrayList<>();


        for (DataTranskript data : listTranskript){
            if (data.getSemester().equals("1")){
                semester1.add(data);
            }
            if (data.getSemester().equals("2")){
                semester2.add(data);
            }
            if (data.getSemester().equals("3")){
                semester3.add(data);
            }
            if (data.getSemester().equals("4")){
                semester4.add(data);
            }
            if (data.getSemester().equals("5")){
                semester5.add(data);
            }
            if (data.getSemester().equals("6")){
                semester6.add(data);
            }
            if (data.getSemester().equals("7")){
                semester7.add(data);
            }
            if (data.getSemester().equals("8")){
                semester8.add(data);
            }
        }

        InputStream file = contohExcelTranskriptIndo.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), mahasiswa.getNama());

        sheet.addMergedRegion(CellRangeAddress.valueOf("A7:C7"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A8:C8"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A9:C9"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A10:C10"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A11:C11"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A12:C12"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A13:C13"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A14:C14"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A15:C15"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A16:C16"));

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

        Font symbolFont = workbook.createFont();
        symbolFont.setFontHeightInPoints((short) 10);
        symbolFont.setFontName("Cambria");

        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 10);
        dataFont.setFontName("Cambria");

        Font prodiFont = workbook.createFont();
        prodiFont.setUnderline(XSSFFont.U_DOUBLE);
        prodiFont.setFontHeightInPoints((short) 10);
        prodiFont.setFontName("Cambria");

        Font ipFont = workbook.createFont();
        ipFont.setBold(true);
        ipFont.setItalic(true);
        ipFont.setFontHeightInPoints((short) 10);
        ipFont.setFontName("Cambria");

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

        CellStyle styleData = workbook.createCellStyle();
        styleData.setFont(dataFont);

        CellStyle styleDataKhs = workbook.createCellStyle();
        styleDataKhs.setAlignment(HorizontalAlignment.CENTER);
        styleDataKhs.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataKhs.setFont(dataFont);

        CellStyle stylePrestasiAkademik = workbook.createCellStyle();
        stylePrestasiAkademik.setAlignment(HorizontalAlignment.LEFT);
        stylePrestasiAkademik.setVerticalAlignment(VerticalAlignment.CENTER);
        stylePrestasiAkademik.setFont(dataFont);

        CellStyle styleSubHeader1 = workbook.createCellStyle();
        styleSubHeader1.setAlignment(HorizontalAlignment.LEFT);
        styleSubHeader1.setVerticalAlignment(VerticalAlignment.CENTER);
        styleSubHeader1.setFont(ipFont);

        CellStyle styleJudulSkripsi = workbook.createCellStyle();
        styleJudulSkripsi.setFont(dataFont);
        styleJudulSkripsi.setWrapText(true);

        CellStyle styleSymbol = workbook.createCellStyle();
        styleSymbol.setAlignment(HorizontalAlignment.CENTER);
        styleSymbol.setFont(symbolFont);

        CellStyle styleTotal = workbook.createCellStyle();
        styleTotal.setAlignment(HorizontalAlignment.CENTER);
        styleTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTotal.setFont(ipFont);

        CellStyle styleIpk = workbook.createCellStyle();
        styleIpk.setFont(prodiFont);
        styleIpk.setAlignment(HorizontalAlignment.CENTER);
        styleIpk.setVerticalAlignment(VerticalAlignment.CENTER);


        int rowInfoNama = 5 ;
        Row nama = sheet.createRow(rowInfoNama);
        nama.createCell(0).setCellValue("Nama Mahasiswa ");
        nama.createCell(3).setCellValue(":");
        nama.createCell(4).setCellValue(mahasiswa.getNama());
        nama.getCell(0).setCellStyle(styleData);
        nama.getCell(3).setCellStyle(styleSymbol);
        nama.getCell(4).setCellStyle(styleData);

        int rowInfoNim = 6 ;
        Row matricNo = sheet.createRow(rowInfoNim);
        matricNo.createCell(0).setCellValue("NIM");
        matricNo.createCell(3).setCellValue(":");
        matricNo.createCell(4).setCellValue(mahasiswa.getNim());
        matricNo.getCell(0).setCellStyle(styleData);
        matricNo.getCell(3).setCellStyle(styleSymbol);
        matricNo.getCell(4).setCellStyle(styleData);

        int rowInfoEntry = 7 ;
        Row entry = sheet.createRow(rowInfoEntry);
        entry.createCell(0).setCellValue("Penomoran Induk Nasional");
        entry.createCell(3).setCellValue(":");
        entry.createCell(4).setCellValue(mahasiswa.getIndukNasional());
        entry.getCell(0).setCellStyle(styleData);
        entry.getCell(3).setCellStyle(styleSymbol);
        entry.getCell(4).setCellStyle(styleData);

        int rowInfoBirth = 8 ;
        Row birthDay = sheet.createRow(rowInfoBirth);
        birthDay.createCell(0).setCellValue("Tempat, Tanggal lahir");
        birthDay.createCell(3).setCellValue(":");

        if (mahasiswa.getTanggalLahir().getMonthValue() == 1){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Januari" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 2){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Februari" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 3){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Maret" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 4){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " April" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 5){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Mei" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 6){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Juni" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 7){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Juli" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 8){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Agustus" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 9){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " September" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 10){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Oktober" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 11){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " November" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLahir().getMonthValue() == 12){
            birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir().getDayOfMonth() + " Desember" + " " + mahasiswa.getTanggalLahir().getYear());
            birthDay.getCell(4).setCellStyle(styleData);

        }

        birthDay.getCell(0).setCellStyle(styleData);
        birthDay.getCell(3).setCellStyle(styleSymbol);

        int rowInfoLevel = 9 ;
        Row level = sheet.createRow(rowInfoLevel);
        level.createCell(0).setCellValue("Program Pendidikan");
        level.createCell(3).setCellValue(":");

        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("01").get()){
            level.createCell(4).setCellValue("Sarjana");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("02").get()){
            level.createCell(4).setCellValue("Magister");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("03").get()){
            level.createCell(4).setCellValue("Sarjana");
            level.getCell(4).setCellStyle(styleData);

        }
        level.getCell(0).setCellStyle(styleData);
        level.getCell(3).setCellStyle(styleSymbol);

        int rowInfoDepartment = 10 ;
        Row department = sheet.createRow(rowInfoDepartment);
        department.createCell(0).setCellValue("Program Studi");
        department.createCell(3).setCellValue(":");
        department.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi() );
        department.getCell(0).setCellStyle(styleData);
        department.getCell(3).setCellStyle(styleSymbol);
        department.getCell(4).setCellStyle(styleData);

        int rowInfoFaculty = 11 ;
        Row faculty = sheet.createRow(rowInfoFaculty);
        faculty.createCell(0).setCellValue("Fakultas");
        faculty.createCell(3).setCellValue(":");
        faculty.createCell(4).setCellValue(mahasiswa.getIdProdi().getFakultas().getNamaFakultas() );
        faculty.getCell(0).setCellStyle(styleData);
        faculty.getCell(3).setCellStyle(styleSymbol);
        faculty.getCell(4).setCellStyle(styleData);

        int rowInfoNoAcred = 12 ;
        Row accreditation = sheet.createRow(rowInfoNoAcred);
        accreditation.createCell(0).setCellValue("No SK BAN - PT");
        accreditation.createCell(3).setCellValue(":");
        accreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getNoSk());
        accreditation.getCell(0).setCellStyle(styleData);
        accreditation.getCell(3).setCellStyle(styleSymbol);
        accreditation.getCell(4).setCellStyle(styleData);


        int rowInfoDateAcred = 13 ;
        Row dateAccreditation = sheet.createRow(rowInfoDateAcred);
        dateAccreditation.createCell(0).setCellValue("Tanggal SK BAN - PT ");
        dateAccreditation.createCell(3).setCellValue(":");
        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 1){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Januari" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 2){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Februari" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 3){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Maret" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 4){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " April" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 5){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Mei" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 6){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Juni" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 7){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Juli" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 8){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Agustus" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 9){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " September" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 10){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Oktober" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 11){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " November" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getIdProdi().getTanggalSk().getMonthValue() == 12){
            dateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth() + " Desember" + " " + mahasiswa.getIdProdi().getTanggalSk().getYear());
            dateAccreditation.getCell(4).setCellStyle(styleData);

        }

        dateAccreditation.getCell(0).setCellStyle(styleData);
        dateAccreditation.getCell(3).setCellStyle(styleSymbol);

        int rowInfoGraduatedDate = 14 ;
        Row graduatedDate = sheet.createRow(rowInfoGraduatedDate);
        graduatedDate.createCell(0).setCellValue("Tanggal Kelulusan");
        graduatedDate.createCell(3).setCellValue(":");
        if (mahasiswa.getTanggalLulus().getMonthValue() == 1){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Januari" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 2){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Februari" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 3){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Maret" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 4){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " April" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 5){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Mei" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 6){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Juni" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 7){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Juli" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 8){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Agustus" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 9){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " September" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 10){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Oktober" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 11){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " November" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }

        if (mahasiswa.getTanggalLulus().getMonthValue() == 12){
            graduatedDate.createCell(4).setCellValue(mahasiswa.getTanggalLulus().getDayOfMonth() + " Desember" + " " + mahasiswa.getTanggalLulus().getYear());
            graduatedDate.getCell(4).setCellStyle(styleData);

        }


        graduatedDate.getCell(0).setCellStyle(styleData);
        graduatedDate.getCell(3).setCellStyle(styleSymbol);

        int rowInfoTranscript = 15 ;
        Row transcript = sheet.createRow(rowInfoTranscript);
        transcript.createCell(0).setCellValue("No Transkrip ");
        transcript.createCell(3).setCellValue(":");
        transcript.createCell(4).setCellValue(mahasiswa.getNoTranskript());
        transcript.getCell(0).setCellStyle(styleData);
        transcript.getCell(3).setCellStyle(styleSymbol);
        transcript.getCell(4).setCellStyle(styleData);

        int rowNumSemester1 = 18 ;
        for (DataTranskript sem1 : semester1) {
            Row row = sheet.createRow(rowNumSemester1);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,7,8));

            row.createCell(0).setCellValue(sem1.getKode());
            row.createCell(1).setCellValue(sem1.getMatkul());
            row.createCell(5).setCellValue(sem1.getSks());
            row.createCell(6).setCellValue(sem1.getGrade());
            row.createCell(7).setCellValue(sem1.getBobot().toString());
            row.createCell(9).setCellValue(sem1.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester1++;
        }

        int rowNumSemester2 = 18+semester1.size() ;
        for (DataTranskript sem2 : semester2) {
            Row row = sheet.createRow(rowNumSemester2);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,7,8));

            row.createCell(0).setCellValue(sem2.getKode());
            row.createCell(1).setCellValue(sem2.getMatkul());
            row.createCell(5).setCellValue(sem2.getSks());
            row.createCell(6).setCellValue(sem2.getGrade());
            row.createCell(7).setCellValue(sem2.getBobot().toString());
            row.createCell(9).setCellValue(sem2.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester2++;
        }

        int rowNumSemester3 = 18+semester1.size()+semester2.size() ;
        for (DataTranskript sem3 : semester3) {
            Row row = sheet.createRow(rowNumSemester3);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,7,8));

            row.createCell(0).setCellValue(sem3.getKode());
            row.createCell(1).setCellValue(sem3.getMatkul());
            row.createCell(5).setCellValue(sem3.getSks());
            row.createCell(6).setCellValue(sem3.getGrade());
            row.createCell(7).setCellValue(sem3.getBobot().toString());
            row.createCell(9).setCellValue(sem3.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester3++;
        }

        int rowNumSemester4 = 18+semester1.size()+semester2.size()+semester3.size() ;
        for (DataTranskript sem4 : semester4) {
            Row row = sheet.createRow(rowNumSemester4);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,7,8));

            row.createCell(0).setCellValue(sem4.getKode());
            row.createCell(1).setCellValue(sem4.getMatkul());
            row.createCell(5).setCellValue(sem4.getSks());
            row.createCell(6).setCellValue(sem4.getGrade());
            row.createCell(7).setCellValue(sem4.getBobot().toString());
            row.createCell(9).setCellValue(sem4.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester4++;
        }

        int rowNumSemester5 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size() ;
        for (DataTranskript sem5 : semester5) {
            Row row = sheet.createRow(rowNumSemester5);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,7,8));

            row.createCell(0).setCellValue(sem5.getKode());
            row.createCell(1).setCellValue(sem5.getMatkul());
            row.createCell(5).setCellValue(sem5.getSks());
            row.createCell(6).setCellValue(sem5.getGrade());
            row.createCell(7).setCellValue(sem5.getBobot().toString());
            row.createCell(9).setCellValue(sem5.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester5++;
        }

        int rowNumSemester6 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size() ;
        for (DataTranskript sem6 : semester6) {
            Row row = sheet.createRow(rowNumSemester6);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,7,8));

            row.createCell(0).setCellValue(sem6.getKode());
            row.createCell(1).setCellValue(sem6.getMatkul());
            row.createCell(5).setCellValue(sem6.getSks());
            row.createCell(6).setCellValue(sem6.getGrade());
            row.createCell(7).setCellValue(sem6.getBobot().toString());
            row.createCell(9).setCellValue(sem6.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester6++;
        }

        int rowNumSemester7 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size();
        for (DataTranskript sem7 : semester7) {
            Row row = sheet.createRow(rowNumSemester7);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,7,8));

            row.createCell(0).setCellValue(sem7.getKode());
            row.createCell(1).setCellValue(sem7.getMatkul());
            row.createCell(5).setCellValue(sem7.getSks());
            row.createCell(6).setCellValue(sem7.getGrade());
            row.createCell(7).setCellValue(sem7.getBobot().toString());
            row.createCell(9).setCellValue(sem7.getMutu().toString());
            row.getCell(1).setCellStyle(styleData);
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester7++;
        }

        int rowNumSemester8 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size();
        for (DataTranskript sem8 : semester8) {
            Row row = sheet.createRow(rowNumSemester8);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,7,8));

            row.createCell(0).setCellValue(sem8.getKode());
            row.createCell(1).setCellValue(sem8.getMatkul());
            row.createCell(5).setCellValue(sem8.getSks());
            row.createCell(6).setCellValue(sem8.getGrade());
            row.createCell(7).setCellValue(sem8.getBobot().toString());
            row.createCell(9).setCellValue(sem8.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester8++;
        }

        int total = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size();
        Row rowTotal = sheet.createRow(total);
        sheet.addMergedRegion(new CellRangeAddress(total,total,1,4));
        rowTotal.createCell(1).setCellValue("Jumlah");
        rowTotal.createCell(5).setCellValue(totalSKS);
        rowTotal.createCell(9).setCellValue(totalMuti.toString());
        rowTotal.getCell(1).setCellStyle(styleTotal);
        rowTotal.getCell(5).setCellStyle(styleDataKhs);
        rowTotal.getCell(9).setCellStyle(styleDataKhs);

        int ipKomulatif = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+2;
        Row rowIpk = sheet.createRow(ipKomulatif);
        sheet.addMergedRegion(new CellRangeAddress(ipKomulatif,ipKomulatif,0,2));
        rowIpk.createCell(0).setCellValue("Indeks Prestasi Kumulatif");
        rowIpk.createCell(5).setCellValue(ipk.toString());
        rowIpk.getCell(0).setCellStyle(styleTotal);
        rowIpk.getCell(5).setCellStyle(styleDataKhs);

        int predicate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+4;
        Row predicateRow = sheet.createRow(predicate);
        predicateRow.createCell(0).setCellValue("Predikat :");
        if (ipk.compareTo(new BigDecimal(2.99)) <= 0){
            predicateRow.createCell(1).setCellValue("Memuaskan");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.compareTo(new BigDecimal(3.00)) >= 0 && ipk.compareTo(new BigDecimal(3.49)) <= 0){
            predicateRow.createCell(1).setCellValue("Sangat Memuaskan");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.compareTo(new BigDecimal(3.50)) >= 0 && ipk.compareTo(new BigDecimal(3.79)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);
            if (validate != null){
                predicateRow.createCell(2).setCellValue("Sangat Memuaskan");
                predicateRow.getCell(2).setCellStyle(styleData);
            }else {
                predicateRow.createCell(2).setCellValue("Pujian ");
                predicateRow.getCell(2).setCellStyle(styleData);
            }

        }

        if (ipk.compareTo(new BigDecimal(3.80)) >= 0 && ipk.compareTo(new BigDecimal(4.00)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);
            if (validate != null){
                predicateRow.createCell(2).setCellValue("Sangat Memuaskan");
                predicateRow.getCell(2).setCellStyle(styleData);
            }else {
                predicateRow.createCell(2).setCellValue("Pujian Tertinggi");
                predicateRow.getCell(2).setCellStyle(styleData);
            }


        }

        predicateRow.getCell(0).setCellStyle(styleSubHeader);

        int thesis = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+5;
        Row thesisRow = sheet.createRow(thesis);
        if (mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("01").get()){
            thesisRow.createCell(0).setCellValue("Judul skripsi :");
            thesisRow.createCell(1).setCellValue(mahasiswa.getJudul());
            thesisRow.getCell(0).setCellStyle(styleSubHeader);
            thesisRow.getCell(1).setCellStyle(styleData);
        }

        if (mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("02").get()){
            thesisRow.createCell(0).setCellValue("Judul Tesis :");
            thesisRow.createCell(1).setCellValue(mahasiswa.getJudul());
            thesisRow.getCell(0).setCellStyle(styleSubHeader);
            thesisRow.getCell(1).setCellStyle(styleData);
        }


        int keyResult = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8;
        Row resultRow = sheet.createRow(keyResult);
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,0,3));
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,5,9));
        resultRow.createCell(0).setCellValue("Prestasi Akademik");
        resultRow.createCell(5).setCellValue("Sistem Penilaian");
        resultRow.getCell(0).setCellStyle(styleDataKhs);
        resultRow.getCell(5).setCellStyle(styleDataKhs);

        int remark = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10;
        Row remarkRow = sheet.createRow(remark);
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,0,3));
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,7,9));
        remarkRow.createCell(0).setCellValue("Keterangan");
        remarkRow.createCell(5).setCellValue("HM");
        remarkRow.createCell(6).setCellValue("AM");
        remarkRow.createCell(7).setCellValue("Arti");
        remarkRow.getCell(0).setCellStyle(styleDataKhs);
        remarkRow.getCell(5).setCellStyle(styleIpk);
        remarkRow.getCell(6).setCellStyle(styleIpk);
        remarkRow.getCell(7).setCellStyle(styleIpk);

        int excellent = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11;
        Row excellentRow = sheet.createRow(excellent);
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,1,3));
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,7,9));
        excellentRow.createCell(0).setCellValue("3,80-4,00");
        excellentRow.createCell(1).setCellValue("Pujian Tertinggi (Minimal B)");
        excellentRow.createCell(5).setCellValue("A");
        excellentRow.createCell(6).setCellValue("4");
        excellentRow.createCell(7).setCellValue("Baik Sekali");
        excellentRow.getCell(0).setCellStyle(styleProdi);
        excellentRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        excellentRow.getCell(5).setCellStyle(styleDataKhs);
        excellentRow.getCell(6).setCellStyle(styleDataKhs);
        excellentRow.getCell(7).setCellStyle(styleManajemen);

        int veryGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12;
        Row veryGoodRow = sheet.createRow(veryGood);
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,1,3));
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,7,9));
        veryGoodRow.createCell(0).setCellValue("3,50-3,79");
        veryGoodRow.createCell(1).setCellValue("Pujian (Minimal B)");
        veryGoodRow.createCell(5).setCellValue("A-");
        veryGoodRow.createCell(6).setCellValue("3,7");
        veryGoodRow.createCell(7).setCellValue("Baik Sekali");
        veryGoodRow.getCell(0).setCellStyle(styleProdi);
        veryGoodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        veryGoodRow.getCell(5).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(6).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(7).setCellStyle(styleManajemen);

        int good = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13;
        Row goodRow = sheet.createRow(good);
        sheet.addMergedRegion(new CellRangeAddress(good,good,1,3));
        sheet.addMergedRegion(new CellRangeAddress(good,good,7,9));
        goodRow.createCell(0).setCellValue("3,00-3,49");
        goodRow.createCell(1).setCellValue("Sangat Memuaskan");
        goodRow.createCell(5).setCellValue("B+");
        goodRow.createCell(6).setCellValue("3,3");
        goodRow.createCell(7).setCellValue("Baik");
        goodRow.getCell(0).setCellStyle(styleProdi);
        goodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        goodRow.getCell(5).setCellStyle(styleDataKhs);
        goodRow.getCell(6).setCellStyle(styleDataKhs);
        goodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactory = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14;
        Row satisfactoryRow = sheet.createRow(satisfactory);
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,1,3));
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,7,9));
        satisfactoryRow.createCell(0).setCellValue("2,75-2,99");
        satisfactoryRow.createCell(1).setCellValue("Memuaskan");
        satisfactoryRow.createCell(5).setCellValue("B");
        satisfactoryRow.createCell(6).setCellValue("3");
        satisfactoryRow.createCell(7).setCellValue("Baik");
        satisfactoryRow.getCell(0).setCellStyle(styleProdi);
        satisfactoryRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        satisfactoryRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(7).setCellStyle(styleManajemen);

        int almostGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+15;
        Row almostGoodRow = sheet.createRow(almostGood);
        sheet.addMergedRegion(new CellRangeAddress(almostGood,almostGood,7,9));
        almostGoodRow.createCell(5).setCellValue("B-");
        almostGoodRow.createCell(6).setCellValue("2,7");
        almostGoodRow.createCell(7).setCellValue("Baik");
        almostGoodRow.getCell(5).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(6).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryCplus = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+16;
        Row satisfactoryCplusRow = sheet.createRow(satisfactoryCplus);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryCplus,satisfactoryCplus,7,9));
        satisfactoryCplusRow.createCell(5).setCellValue("C+");
        satisfactoryCplusRow.createCell(6).setCellValue("2,3");
        satisfactoryCplusRow.createCell(7).setCellValue("Cukup");
        satisfactoryCplusRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryC = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+17;
        Row satisfactoryCRow = sheet.createRow(satisfactoryC);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryC,satisfactoryC,7,9));
        satisfactoryCRow.createCell(5).setCellValue("C");
        satisfactoryCRow.createCell(6).setCellValue("2");
        satisfactoryCRow.createCell(7).setCellValue("Cukup");
        satisfactoryCRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(7).setCellStyle(styleManajemen);

        int poor = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+18;
        Row poorRow = sheet.createRow(poor);
        sheet.addMergedRegion(new CellRangeAddress(poor,poor,7,9));
        poorRow.createCell(5).setCellValue("D");
        poorRow.createCell(6).setCellValue("1");
        poorRow.createCell(7).setCellValue("Kurang");
        poorRow.getCell(5).setCellStyle(styleDataKhs);
        poorRow.getCell(6).setCellStyle(styleDataKhs);
        poorRow.getCell(7).setCellStyle(styleManajemen);

        int fail = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+19;
        Row failRow = sheet.createRow(fail);
        sheet.addMergedRegion(new CellRangeAddress(fail,fail,7,9));
        failRow.createCell(5).setCellValue("E");
        failRow.createCell(6).setCellValue("0");
        failRow.createCell(7).setCellValue("Sangat Kurang");
        failRow.getCell(5).setCellStyle(styleDataKhs);
        failRow.getCell(6).setCellStyle(styleDataKhs);
        failRow.getCell(7).setCellStyle(styleManajemen);

        int createDate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+24;
        Row createDateRow = sheet.createRow(createDate);
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        String namaBulanHijri = islamicDate.format(DateTimeFormatter.ofPattern("MMMM", new Locale("en")));
        String tanggalHijri = islamicDate.format(DateTimeFormatter.ofPattern("dd", new Locale("en")));
        String tahunHijri = islamicDate.format(DateTimeFormatter.ofPattern("yyyy", new Locale("en")));

        if (LocalDate.now().getMonthValue() == 1){

            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari " + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Januari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 2){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Februari" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 3){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Maret" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 4){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " April" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 5){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Mei" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 6){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juni" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 7){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Juli" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 8){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Agustus" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 9){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " September" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 10){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " Oktober" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 11){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor, " + LocalDate.now().getDayOfMonth() + " November" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        if (LocalDate.now().getMonthValue() == 12){
            if (namaBulanHijri.equals("Jumada I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Jumada II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ I")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else if (namaBulanHijri.equals("Rabiʻ II")){
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }else{
                createDateRow.createCell(0).setCellValue("Transkrip ini dibuat dengan sebenarnya dan telah disahkan di Bogor,  " + LocalDate.now().getDayOfMonth() + " Desember" + " " + LocalDate.now().getYear() + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
                createDateRow.getCell(0).setCellStyle(styleData);
            }

        }

        int facultyy = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+26;
        Row facultyRow = sheet.createRow(facultyy);
        facultyRow.createCell(0).setCellValue("Dekan ");
        facultyRow.getCell(0).setCellStyle(styleData);
        facultyRow.createCell(5).setCellValue("Koordinator ");
        facultyRow.getCell(5).setCellStyle(styleData);

        int faculty2 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+27;
        Row facultyRow2 = sheet.createRow(faculty2);
        facultyRow2.createCell(0).setCellValue("Fakultas " + mahasiswa.getIdProdi().getFakultas().getNamaFakultas());
        facultyRow2.getCell(0).setCellStyle(styleData);
        facultyRow2.createCell(5).setCellValue("Program Studi " + mahasiswa.getIdProdi().getNamaProdi());
        facultyRow2.getCell(5).setCellStyle(styleData);

        int lecture = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+32;
        Row lectureRow = sheet.createRow(lecture);
        lectureRow.createCell(0).setCellValue(mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(0).setCellStyle(styleDosen);
        lectureRow.createCell(5).setCellValue(mahasiswa.getIdProdi().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(5).setCellStyle(styleDosen);

        int nik = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+33;
        Row nikRow = sheet.createRow(nik);
        nikRow.createCell(0).setCellValue("NIK : " + mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNik());
        nikRow.getCell(0).setCellStyle(styleNik);
        nikRow.createCell(5).setCellValue("NIK : " + mahasiswa.getIdProdi().getDosen().getKaryawan().getNik());
        nikRow.getCell(5).setCellStyle(styleNik);



        PropertyTemplate propertyTemplate = new PropertyTemplate();
//        semester1
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        //        semester2
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

//        semester3
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester4
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester5
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester6
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester7
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester8
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 5, 9),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);

        propertyTemplate.applyBorders(sheet);


        String namaFile = "Transkript-" +mahasiswa.getNim()+"-"+mahasiswa.getNama();
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/studiesActivity/transcript/transkriptexcel2")
    public void transkriptExcel2 (@RequestParam(required = false) String nim, HttpServletResponse response) throws IOException {


        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        List<TranskriptDto> semester1 = krsDetailDao.excelTranskript(mahasiswa.getId(),"1");
        List<TranskriptDto> semester2 = krsDetailDao.excelTranskript(mahasiswa.getId(),"2");
        List<TranskriptDto> semester3 = krsDetailDao.excelTranskript(mahasiswa.getId(),"3");
        List<TranskriptDto> semester4 = krsDetailDao.excelTranskript(mahasiswa.getId(),"4");
        List<TranskriptDto> semester5 = krsDetailDao.excelTranskript(mahasiswa.getId(),"5");
        List<TranskriptDto> semester6 = krsDetailDao.excelTranskript(mahasiswa.getId(),"6");
        List<TranskriptDto> semester7 = krsDetailDao.excelTranskript(mahasiswa.getId(),"7");
        List<TranskriptDto> semester8 = krsDetailDao.excelTranskript(mahasiswa.getId(),"8");

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());

        IpkDto ipk = krsDetailDao.ipk(mahasiswa);

//        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);


        InputStream file = contohExcelTranskript.getInputStream();

        XSSFWorkbook workbook = new XSSFWorkbook(file);
        XSSFSheet sheet = workbook.getSheetAt(0);
        workbook.setSheetName(workbook.getSheetIndex(sheet), mahasiswa.getNama());

        sheet.addMergedRegion(CellRangeAddress.valueOf("A7:C7"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A8:C8"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A9:C9"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A10:C10"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A11:C11"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A12:C12"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A13:C13"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A14:C14"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A15:C15"));
        sheet.addMergedRegion(CellRangeAddress.valueOf("A16:C16"));

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

        Font symbolFont = workbook.createFont();
        symbolFont.setFontHeightInPoints((short) 10);
        symbolFont.setFontName("Cambria");

        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 10);
        dataFont.setFontName("Cambria");

        Font prodiFont = workbook.createFont();
        prodiFont.setUnderline(XSSFFont.U_DOUBLE);
        prodiFont.setFontHeightInPoints((short) 10);
        prodiFont.setFontName("Cambria");

        Font ipFont = workbook.createFont();
        ipFont.setBold(true);
        ipFont.setItalic(true);
        ipFont.setFontHeightInPoints((short) 10);
        ipFont.setFontName("Cambria");

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

        CellStyle styleData = workbook.createCellStyle();
        styleData.setFont(dataFont);

        CellStyle styleDataKhs = workbook.createCellStyle();
        styleDataKhs.setAlignment(HorizontalAlignment.CENTER);
        styleDataKhs.setVerticalAlignment(VerticalAlignment.CENTER);
        styleDataKhs.setFont(dataFont);

        CellStyle styleSymbol = workbook.createCellStyle();
        styleSymbol.setAlignment(HorizontalAlignment.CENTER);
        styleSymbol.setFont(symbolFont);

        CellStyle styleTotal = workbook.createCellStyle();
        styleTotal.setAlignment(HorizontalAlignment.CENTER);
        styleTotal.setVerticalAlignment(VerticalAlignment.CENTER);
        styleTotal.setFont(ipFont);

        CellStyle styleIpk = workbook.createCellStyle();
        styleIpk.setFont(prodiFont);
        styleIpk.setAlignment(HorizontalAlignment.CENTER);
        styleIpk.setVerticalAlignment(VerticalAlignment.CENTER);

        CellStyle stylePrestasiAkademik = workbook.createCellStyle();
        stylePrestasiAkademik.setAlignment(HorizontalAlignment.LEFT);
        stylePrestasiAkademik.setVerticalAlignment(VerticalAlignment.CENTER);
        stylePrestasiAkademik.setFont(dataFont);

        CellStyle styleSubHeader1 = workbook.createCellStyle();
        styleSubHeader1.setAlignment(HorizontalAlignment.CENTER);
        styleSubHeader1.setVerticalAlignment(VerticalAlignment.CENTER);
        styleSubHeader1.setFont(ipFont);


        int rowInfoNama = 5 ;
        Row nama = sheet.createRow(rowInfoNama);
        nama.createCell(0).setCellValue("Name");
        nama.createCell(3).setCellValue(":");
        nama.createCell(4).setCellValue(mahasiswa.getNama());
        nama.getCell(0).setCellStyle(styleData);
        nama.getCell(3).setCellStyle(styleSymbol);
        nama.getCell(4).setCellStyle(styleData);

        int rowInfoNim = 6 ;
        Row matricNo = sheet.createRow(rowInfoNim);
        matricNo.createCell(0).setCellValue("Student Matric No");
        matricNo.createCell(3).setCellValue(":");
        matricNo.createCell(4).setCellValue(mahasiswa.getNim());
        matricNo.getCell(0).setCellStyle(styleData);
        matricNo.getCell(3).setCellStyle(styleSymbol);
        matricNo.getCell(4).setCellStyle(styleData);

        int rowInfoEntry = 7 ;
        Row entry = sheet.createRow(rowInfoEntry);
        entry.createCell(0).setCellValue("National Certificate Number");
        entry.createCell(3).setCellValue(":");
        entry.createCell(4).setCellValue(mahasiswa.getIndukNasional());
        entry.getCell(0).setCellStyle(styleData);
        entry.getCell(3).setCellStyle(styleSymbol);
        entry.getCell(4).setCellStyle(styleData);

        int rowInfoBirth = 8 ;
        Row birthDay = sheet.createRow(rowInfoBirth);
        birthDay.createCell(0).setCellValue("Place and Date of Birth");
        birthDay.createCell(3).setCellValue(":");


        int month = mahasiswa.getTanggalLahir().getDayOfMonth();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getPattern(month));
        String birthdate = mahasiswa.getTanggalLahir().format(formatter);
        birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + birthdate);
        birthDay.getCell(4).setCellStyle(styleData);


        birthDay.getCell(0).setCellStyle(styleData);
        birthDay.getCell(3).setCellStyle(styleSymbol);

        int rowInfoLevel = 9 ;
        Row level = sheet.createRow(rowInfoLevel);
        level.createCell(0).setCellValue("Level");
        level.createCell(3).setCellValue(":");

        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("01").get()){
            level.createCell(4).setCellValue("Undergraduate");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("02").get()){
            level.createCell(4).setCellValue("Post Graduate");
            level.getCell(4).setCellStyle(styleData);
        }
        if(mahasiswa.getIdProdi().getIdJenjang() == jenjangDao.findById("03").get()){
            level.createCell(4).setCellValue("Undergraduate");
            level.getCell(4).setCellStyle(styleData);

        }
        level.getCell(0).setCellStyle(styleData);
        level.getCell(3).setCellStyle(styleSymbol);

        int rowInfoDepartment = 10 ;
        Row department = sheet.createRow(rowInfoDepartment);
        department.createCell(0).setCellValue("Department");
        department.createCell(3).setCellValue(":");
        department.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdiEnglish());
        department.getCell(0).setCellStyle(styleData);
        department.getCell(3).setCellStyle(styleSymbol);
        department.getCell(4).setCellStyle(styleData);

        int rowInfoFaculty = 11 ;
        Row facultyy = sheet.createRow(rowInfoFaculty);
        facultyy.createCell(0).setCellValue("Faculty");
        facultyy.createCell(3).setCellValue(":");
        facultyy.createCell(4).setCellValue(mahasiswa.getIdProdi().getFakultas().getNamaFakultasEnglish());
        facultyy.getCell(0).setCellStyle(styleData);
        facultyy.getCell(3).setCellStyle(styleSymbol);
        facultyy.getCell(4).setCellStyle(styleData);

        int rowInfoNoAcred = 12 ;
        Row accreditation = sheet.createRow(rowInfoNoAcred);
        accreditation.createCell(0).setCellValue("No of Accreditation Decree");
        accreditation.createCell(3).setCellValue(":");
        accreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getNoSk());
        accreditation.getCell(0).setCellStyle(styleData);
        accreditation.getCell(3).setCellStyle(styleSymbol);
        accreditation.getCell(4).setCellStyle(styleData);


        int rowInfoDateAcred = 13 ;
        Row dateAccreditation = sheet.createRow(rowInfoDateAcred);
        dateAccreditation.createCell(0).setCellValue("Date of Accreditation Decree");
        dateAccreditation.createCell(3).setCellValue(":");
        int monthAccred = mahasiswa.getIdProdi().getTanggalSk().getDayOfMonth();
        DateTimeFormatter formatterAccred = DateTimeFormatter.ofPattern(getPattern(monthAccred));
        String accredDate = mahasiswa.getIdProdi().getTanggalSk().format(formatterAccred);
        dateAccreditation.createCell(4).setCellValue(accredDate);
        dateAccreditation.getCell(4).setCellStyle(styleData);

        dateAccreditation.getCell(0).setCellStyle(styleData);
        dateAccreditation.getCell(3).setCellStyle(styleSymbol);

        int rowInfoGraduatedDate = 14 ;
        Row graduatedDate = sheet.createRow(rowInfoGraduatedDate);
        graduatedDate.createCell(0).setCellValue("Graduated Date");
        graduatedDate.createCell(3).setCellValue(":");
        int monthGraduate = mahasiswa.getTanggalLulus().getDayOfMonth();
        DateTimeFormatter formatterGraduate = DateTimeFormatter.ofPattern(getPattern(monthGraduate));
        String graduateDate = mahasiswa.getTanggalLulus().format(formatterGraduate);

        graduatedDate.createCell(4).setCellValue(graduateDate);
        graduatedDate.getCell(4).setCellStyle(styleData);

        graduatedDate.getCell(0).setCellStyle(styleData);
        graduatedDate.getCell(3).setCellStyle(styleSymbol);

        int rowInfoTranscript = 15 ;
        Row transcript = sheet.createRow(rowInfoTranscript);
        transcript.createCell(0).setCellValue("No of Transcript");
        transcript.createCell(3).setCellValue(":");
        transcript.createCell(4).setCellValue(mahasiswa.getNoTranskript());
        transcript.getCell(0).setCellStyle(styleData);
        transcript.getCell(3).setCellStyle(styleSymbol);
        transcript.getCell(4).setCellStyle(styleData);

        int rowNumSemester1 = 18 ;
        for (TranskriptDto sem1 : semester1) {
            Row row = sheet.createRow(rowNumSemester1);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester1,rowNumSemester1,7,8));

            row.createCell(0).setCellValue(sem1.getKode());
            row.createCell(1).setCellValue(sem1.getCourses());
            row.createCell(5).setCellValue(sem1.getSks());
            row.createCell(6).setCellValue(sem1.getGrade());
            row.createCell(7).setCellValue(sem1.getBobot().toString());
            row.createCell(9).setCellValue(sem1.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester1++;
        }

        int rowNumSemester2 = 18+semester1.size() ;
        for (TranskriptDto sem2 : semester2) {
            Row row = sheet.createRow(rowNumSemester2);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester2,rowNumSemester2,7,8));

            row.createCell(0).setCellValue(sem2.getKode());
            row.createCell(1).setCellValue(sem2.getCourses());
            row.createCell(5).setCellValue(sem2.getSks());
            row.createCell(6).setCellValue(sem2.getGrade());
            row.createCell(7).setCellValue(sem2.getBobot().toString());
            row.createCell(9).setCellValue(sem2.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester2++;
        }

        int rowNumSemester3 = 18+semester1.size()+semester2.size() ;
        for (TranskriptDto sem3 : semester3) {
            Row row = sheet.createRow(rowNumSemester3);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester3,rowNumSemester3,7,8));

            row.createCell(0).setCellValue(sem3.getKode());
            row.createCell(1).setCellValue(sem3.getCourses());
            row.createCell(5).setCellValue(sem3.getSks());
            row.createCell(6).setCellValue(sem3.getGrade());
            row.createCell(7).setCellValue(sem3.getBobot().toString());
            row.createCell(9).setCellValue(sem3.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester3++;
        }

        int rowNumSemester4 = 18+semester1.size()+semester2.size()+semester3.size() ;
        for (TranskriptDto sem4 : semester4) {
            Row row = sheet.createRow(rowNumSemester4);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester4,rowNumSemester4,7,8));

            row.createCell(0).setCellValue(sem4.getKode());
            row.createCell(1).setCellValue(sem4.getCourses());
            row.createCell(5).setCellValue(sem4.getSks());
            row.createCell(6).setCellValue(sem4.getGrade());
            row.createCell(7).setCellValue(sem4.getBobot().toString());
            row.createCell(9).setCellValue(sem4.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester4++;
        }

        int rowNumSemester5 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size() ;
        for (TranskriptDto sem5 : semester5) {
            Row row = sheet.createRow(rowNumSemester5);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester5,rowNumSemester5,7,8));

            row.createCell(0).setCellValue(sem5.getKode());
            row.createCell(1).setCellValue(sem5.getCourses());
            row.createCell(5).setCellValue(sem5.getSks());
            row.createCell(6).setCellValue(sem5.getGrade());
            row.createCell(7).setCellValue(sem5.getBobot().toString());
            row.createCell(9).setCellValue(sem5.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester5++;
        }

        int rowNumSemester6 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size() ;
        for (TranskriptDto sem6 : semester6) {
            Row row = sheet.createRow(rowNumSemester6);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester6,rowNumSemester6,7,8));

            row.createCell(0).setCellValue(sem6.getKode());
            row.createCell(1).setCellValue(sem6.getCourses());
            row.createCell(5).setCellValue(sem6.getSks());
            row.createCell(6).setCellValue(sem6.getGrade());
            row.createCell(7).setCellValue(sem6.getBobot().toString());
            row.createCell(9).setCellValue(sem6.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester6++;
        }

        int rowNumSemester7 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size();
        for (TranskriptDto sem7 : semester7) {
            Row row = sheet.createRow(rowNumSemester7);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester7,rowNumSemester7,7,8));

            row.createCell(0).setCellValue(sem7.getKode());
            row.createCell(1).setCellValue(sem7.getCourses());
            row.createCell(5).setCellValue(sem7.getSks());
            row.createCell(6).setCellValue(sem7.getGrade());
            row.createCell(7).setCellValue(sem7.getBobot().toString());
            row.createCell(9).setCellValue(sem7.getMutu().toString());
            row.getCell(1).setCellStyle(styleData);
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester7++;
        }

        int rowNumSemester8 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size();
        for (TranskriptDto sem8 : semester8) {
            Row row = sheet.createRow(rowNumSemester8);
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,1,4));
            sheet.addMergedRegion(new CellRangeAddress(rowNumSemester8,rowNumSemester8,7,8));

            row.createCell(0).setCellValue(sem8.getKode());
            row.createCell(1).setCellValue(sem8.getCourses());
            row.createCell(5).setCellValue(sem8.getSks());
            row.createCell(6).setCellValue(sem8.getGrade());
            row.createCell(7).setCellValue(sem8.getBobot().toString());
            row.createCell(9).setCellValue(sem8.getMutu().toString());
            row.getCell(0).setCellStyle(styleDataKhs);
            row.getCell(1).setCellStyle(styleData);
            row.getCell(5).setCellStyle(styleDataKhs);
            row.getCell(6).setCellStyle(styleDataKhs);
            row.getCell(7).setCellStyle(styleDataKhs);
            row.getCell(9).setCellStyle(styleDataKhs);

            rowNumSemester8++;
        }

        int total = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size();
        Row rowTotal = sheet.createRow(total);
        sheet.addMergedRegion(new CellRangeAddress(total,total,1,4));
        rowTotal.createCell(1).setCellValue("Total");
        rowTotal.createCell(5).setCellValue(totalSKS.intValue());
        rowTotal.createCell(9).setCellValue(totalMuti.toString());
        rowTotal.getCell(1).setCellStyle(styleTotal);
        rowTotal.getCell(5).setCellStyle(styleDataKhs);
        rowTotal.getCell(9).setCellStyle(styleDataKhs);

        int ipKomulatif = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+2;
        Row rowIpk = sheet.createRow(ipKomulatif);
        sheet.addMergedRegion(new CellRangeAddress(ipKomulatif,ipKomulatif,0,2));
        rowIpk.createCell(0).setCellValue("Cumulative Grade Point Average");
        rowIpk.createCell(5).setCellValue(ipk.getIpk().toString());
        rowIpk.getCell(0).setCellStyle(styleTotal);
        rowIpk.getCell(5).setCellStyle(styleDataKhs);

        int predicate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+4;
        Row predicateRow = sheet.createRow(predicate);
        predicateRow.createCell(0).setCellValue("Predicate :");
        if (ipk.getIpk().compareTo(new BigDecimal(2.99)) <= 0){
            predicateRow.createCell(1).setCellValue("Satisfactory");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(3.49)) <= 0){
            predicateRow.createCell(1).setCellValue("Good");
            predicateRow.getCell(1).setCellStyle(styleData);

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.50)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(3.79)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);

            if (validate != null){
                predicateRow.createCell(1).setCellValue("Good");
                predicateRow.getCell(1).setCellStyle(styleData);
            }else {
                predicateRow.createCell(1).setCellValue("Very Good");
                predicateRow.getCell(1).setCellStyle(styleData);
            }

        }

        if (ipk.getIpk().compareTo(new BigDecimal(3.80)) >= 0 && ipk.getIpk().compareTo(new BigDecimal(4.00)) <= 0){
            BigDecimal validate = krsDetailDao.validasiTranskrip(mahasiswa);

            if (validate != null){
                predicateRow.createCell(1).setCellValue("Good");
                predicateRow.getCell(1).setCellStyle(styleData);
            }else {
                predicateRow.createCell(1).setCellValue("Excellent");
                predicateRow.getCell(1).setCellStyle(styleData);
            }


        }

        predicateRow.getCell(0).setCellStyle(styleSubHeader);

        int thesis = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+5;
        Row thesisRow = sheet.createRow(thesis);
        thesisRow.createCell(0).setCellValue("Thesis Title :");
        thesisRow.createCell(1).setCellValue(mahasiswa.getTitle());
        thesisRow.getCell(0).setCellStyle(styleSubHeader);
        thesisRow.getCell(1).setCellStyle(styleData);

        int keyResult = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8;
        Row resultRow = sheet.createRow(keyResult);
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,0,3));
        sheet.addMergedRegion(new CellRangeAddress(keyResult,keyResult+1,5,9));
        resultRow.createCell(0).setCellValue("Key to Result");
        resultRow.createCell(5).setCellValue("Grading System");
        resultRow.getCell(0).setCellStyle(styleDataKhs);
        resultRow.getCell(5).setCellStyle(styleDataKhs);

        int remark = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10;
        Row remarkRow = sheet.createRow(remark);
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,0,3));
        sheet.addMergedRegion(new CellRangeAddress(remark,remark,7,9));
        remarkRow.createCell(0).setCellValue("Remarks");
        remarkRow.createCell(5).setCellValue("Grade");
        remarkRow.createCell(6).setCellValue("Value");
        remarkRow.createCell(7).setCellValue("Meaning");
        remarkRow.getCell(0).setCellStyle(styleDataKhs);
        remarkRow.getCell(5).setCellStyle(styleIpk);
        remarkRow.getCell(6).setCellStyle(styleIpk);
        remarkRow.getCell(7).setCellStyle(styleIpk);

        int excellent = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11;
        Row excellentRow = sheet.createRow(excellent);
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,1,3));
        sheet.addMergedRegion(new CellRangeAddress(excellent,excellent,7,9));
        excellentRow.createCell(0).setCellValue("3,80-4,00");
        excellentRow.createCell(1).setCellValue("Excellent (Minimum B)");
        excellentRow.createCell(5).setCellValue("A");
        excellentRow.createCell(6).setCellValue("4");
        excellentRow.createCell(7).setCellValue("Excellent");
        excellentRow.getCell(0).setCellStyle(styleProdi);
        excellentRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        excellentRow.getCell(5).setCellStyle(styleDataKhs);
        excellentRow.getCell(6).setCellStyle(styleDataKhs);
        excellentRow.getCell(7).setCellStyle(styleManajemen);

        int veryGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12;
        Row veryGoodRow = sheet.createRow(veryGood);
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,1,3));
        sheet.addMergedRegion(new CellRangeAddress(veryGood,veryGood,7,9));
        veryGoodRow.createCell(0).setCellValue("3,50-3,79");
        veryGoodRow.createCell(1).setCellValue("Very Good (Minimum B)");
        veryGoodRow.createCell(5).setCellValue("A-");
        veryGoodRow.createCell(6).setCellValue("3,7");
        veryGoodRow.createCell(7).setCellValue("Very Good");
        veryGoodRow.getCell(0).setCellStyle(styleProdi);
        veryGoodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        veryGoodRow.getCell(5).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(6).setCellStyle(styleDataKhs);
        veryGoodRow.getCell(7).setCellStyle(styleManajemen);

        int good = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13;
        Row goodRow = sheet.createRow(good);
        sheet.addMergedRegion(new CellRangeAddress(good,good,1,3));
        sheet.addMergedRegion(new CellRangeAddress(good,good,7,9));
        goodRow.createCell(0).setCellValue("3,00-3,49");
        goodRow.createCell(1).setCellValue("Good");
        goodRow.createCell(5).setCellValue("B+");
        goodRow.createCell(6).setCellValue("3,3");
        goodRow.createCell(7).setCellValue("Good");
        goodRow.getCell(0).setCellStyle(styleProdi);
        goodRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        goodRow.getCell(5).setCellStyle(styleDataKhs);
        goodRow.getCell(6).setCellStyle(styleDataKhs);
        goodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactory = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14;
        Row satisfactoryRow = sheet.createRow(satisfactory);
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,1,3));
        sheet.addMergedRegion(new CellRangeAddress(satisfactory,satisfactory,7,9));
        satisfactoryRow.createCell(0).setCellValue("2,75-2,99");
        satisfactoryRow.createCell(1).setCellValue("Satisfactory");
        satisfactoryRow.createCell(5).setCellValue("B");
        satisfactoryRow.createCell(6).setCellValue("3");
        satisfactoryRow.createCell(7).setCellValue("Good");
        satisfactoryRow.getCell(0).setCellStyle(styleProdi);
        satisfactoryRow.getCell(1).setCellStyle(stylePrestasiAkademik);
        satisfactoryRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryRow.getCell(7).setCellStyle(styleManajemen);

        int almostGood = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+15;
        Row almostGoodRow = sheet.createRow(almostGood);
        sheet.addMergedRegion(new CellRangeAddress(almostGood,almostGood,7,9));
        almostGoodRow.createCell(5).setCellValue("B-");
        almostGoodRow.createCell(6).setCellValue("2,7");
        almostGoodRow.createCell(7).setCellValue("Almost Good");
        almostGoodRow.getCell(5).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(6).setCellStyle(styleDataKhs);
        almostGoodRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryCplus = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+16;
        Row satisfactoryCplusRow = sheet.createRow(satisfactoryCplus);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryCplus,satisfactoryCplus,7,9));
        satisfactoryCplusRow.createCell(5).setCellValue("C+");
        satisfactoryCplusRow.createCell(6).setCellValue("2,3");
        satisfactoryCplusRow.createCell(7).setCellValue("Satisfactory");
        satisfactoryCplusRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCplusRow.getCell(7).setCellStyle(styleManajemen);

        int satisfactoryC = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+17;
        Row satisfactoryCRow = sheet.createRow(satisfactoryC);
        sheet.addMergedRegion(new CellRangeAddress(satisfactoryC,satisfactoryC,7,9));
        satisfactoryCRow.createCell(5).setCellValue("C");
        satisfactoryCRow.createCell(6).setCellValue("2");
        satisfactoryCRow.createCell(7).setCellValue("Satisfactory");
        satisfactoryCRow.getCell(5).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(6).setCellStyle(styleDataKhs);
        satisfactoryCRow.getCell(7).setCellStyle(styleManajemen);

        int poor = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+18;
        Row poorRow = sheet.createRow(poor);
        sheet.addMergedRegion(new CellRangeAddress(poor,poor,7,9));
        poorRow.createCell(5).setCellValue("D");
        poorRow.createCell(6).setCellValue("1");
        poorRow.createCell(7).setCellValue("Poor");
        poorRow.getCell(5).setCellStyle(styleDataKhs);
        poorRow.getCell(6).setCellStyle(styleDataKhs);
        poorRow.getCell(7).setCellStyle(styleManajemen);

        int fail = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+19;
        Row failRow = sheet.createRow(fail);
        sheet.addMergedRegion(new CellRangeAddress(fail,fail,7,9));
        failRow.createCell(5).setCellValue("E");
        failRow.createCell(6).setCellValue("0");
        failRow.createCell(7).setCellValue("Fail");
        failRow.getCell(5).setCellStyle(styleDataKhs);
        failRow.getCell(6).setCellStyle(styleDataKhs);
        failRow.getCell(7).setCellStyle(styleManajemen);

        int createDate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+24;
        Row createDateRow = sheet.createRow(createDate);
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        String namaBulanHijri = islamicDate.format(DateTimeFormatter.ofPattern("MMMM", new Locale("en")));
        String tanggalHijri = islamicDate.format(DateTimeFormatter.ofPattern("d", new Locale("en")));
        String tahunHijri = islamicDate.format(DateTimeFormatter.ofPattern("yyyy", new Locale("en")));

        int monthCreate = LocalDate.now().getDayOfMonth();
        DateTimeFormatter formatterCreate = DateTimeFormatter.ofPattern(getPattern(monthCreate));
        String createDatee = LocalDate.now().format(formatterCreate);

        if (namaBulanHijri.equals("Jumada I")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Jumadil Awal " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else if (namaBulanHijri.equals("Jumada II")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Jumadil Akhir " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else if (namaBulanHijri.equals("Rabiʻ I")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Rabi'ul Awal " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else if (namaBulanHijri.equals("Rabiʻ II")){
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " Rabi'ul Akhir " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }else{
            createDateRow.createCell(0).setCellValue("This is certified to be true and accurate statement, issued in Bogor on " + createDatee + " / " + tanggalHijri + " " + namaBulanHijri + " " + tahunHijri);
            createDateRow.getCell(0).setCellStyle(styleData);
        }


        int faculty = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+26;
        Row facultyRow = sheet.createRow(faculty);
        facultyRow.createCell(0).setCellValue("Dean of ");
        facultyRow.getCell(0).setCellStyle(styleData);
        facultyRow.createCell(5).setCellValue("Coordinator of ");
        facultyRow.getCell(5).setCellStyle(styleData);

        int faculty2 = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+27;
        Row facultyRow2 = sheet.createRow(faculty2);
        facultyRow2.createCell(0).setCellValue(mahasiswa.getIdProdi().getFakultas().getNamaFakultasEnglish());
        facultyRow2.getCell(0).setCellStyle(styleData);
        facultyRow2.createCell(5).setCellValue(mahasiswa.getIdProdi().getNamaProdiEnglish());
        facultyRow2.getCell(5).setCellStyle(styleData);

        int lecture = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+32;
        Row lectureRow = sheet.createRow(lecture);
        lectureRow.createCell(0).setCellValue(mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(0).setCellStyle(styleDosen);
        lectureRow.createCell(5).setCellValue(mahasiswa.getIdProdi().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(5).setCellStyle(styleDosen);

        int nik = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+33;
        Row nikRow = sheet.createRow(nik);
        nikRow.createCell(0).setCellValue("NIK : " + mahasiswa.getIdProdi().getFakultas().getDosen().getKaryawan().getNik());
        nikRow.getCell(0).setCellStyle(styleNik);
        nikRow.createCell(5).setCellValue("NIK : " + mahasiswa.getIdProdi().getDosen().getKaryawan().getNik());
        nikRow.getCell(5).setCellStyle(styleNik);





        PropertyTemplate propertyTemplate = new PropertyTemplate();
//        semester1
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18, 17+semester1.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        //        semester2
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

//        semester3
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester4
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester5
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester6
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester7
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
//        semester8
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 0, 0),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 6, 6),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 7, 8),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size(), 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);

        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 1, 4),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 5, 5),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size(), 17+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+1, 9, 9),
                BorderStyle.THIN, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+9, 5, 9),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+10, 0, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+11, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+12, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+13, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        propertyTemplate.drawBorders(new CellRangeAddress(18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+8, 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+14, 1, 3),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);

        propertyTemplate.applyBorders(sheet);


        String namaFile = "Transkript-" +mahasiswa.getNim()+"-"+mahasiswa.getNama();
        String extentionX = ".xlsx";
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition","attachment; filename=\""+ namaFile  + extentionX +  "\"");
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // BERKAS

    @GetMapping("/berkas/uploadSoal/list")
    public void listBerkas(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Karyawan k = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(k);

        model.addAttribute("dosen", dosen);
        model.addAttribute("dosenAkses", jadwalDosenDao.findByJadwalTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF)));

        List<Jadwal> listUts = jadwalDao.listUts(tahunAkademikDao.findByStatus(StatusRecord.AKTIF), dosen);
        List<Jadwal> listUas = jadwalDao.listUas(tahunAkademikDao.findByStatus(StatusRecord.AKTIF), dosen);

        model.addAttribute("listUts", listUts);
        model.addAttribute("listUas", listUas);

        List<Soal> soalUts = soalDao.findTopByStatusAndStatusSoalAndDosenOrderByTanggalUploadDesc(StatusRecord.AKTIF, StatusRecord.UTS, dosen);
        List<Soal> soalUas = soalDao.findTopByStatusAndStatusSoalAndDosenOrderByTanggalUploadDesc(StatusRecord.AKTIF, StatusRecord.UAS, dosen);
        model.addAttribute("soalUts", soalUts);
        model.addAttribute("soalUas", soalUas);

    }

}

