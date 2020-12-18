package id.ac.tazkia.smilemahasiswa.controller;

import fr.opensagres.xdocreport.core.io.IOUtils;
import id.ac.tazkia.smilemahasiswa.SmilemahasiswaApplication;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.KrsNilaiTugasDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreHitungDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreInput;
import id.ac.tazkia.smilemahasiswa.dto.attendance.JadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.dto.room.KelasMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.transkript.TranskriptDto;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
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
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private JadwalDao jadwalDao;

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

    @Autowired private ScoreService scoreService;

    @Value("classpath:sample/soal.doc")
    private Resource contohSoal;

    @Value("classpath:/sample/khs.xlsx")
    private Resource contohExcelKhs;

    @Value("classpath:/sample/transkript.xlsx")
    private Resource contohExcelTranskript;

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
    public void listLectureAttendance(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Iterable<JadwalDosen> jadwal = jadwalDosenDao.findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(Arrays.asList(StatusRecord.HAPUS), tahunAkademik,dosen);
        model.addAttribute("jadwal", jadwal);

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
            model.addAttribute("jadwal", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hari));
        }


        if (prodi != null && tahunAkademik != null && hari == null) {
            model.addAttribute("minggu", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hariDao.findById("0").get()));
            model.addAttribute("ploting", jadwalDao.ploting(prodi, tahunAkademik));
            model.addAttribute("senin", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hariDao.findById("1").get()));
            model.addAttribute("selasa", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hariDao.findById("2").get()));
            model.addAttribute("rabu", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hariDao.findById("3").get()));
            model.addAttribute("kamis", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hariDao.findById("4").get()));
            model.addAttribute("jumat", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hariDao.findById("5").get()));
            model.addAttribute("sabtu", jadwalDao.schedule(prodi, Arrays.asList(StatusRecord.HAPUS), tahunAkademik, hariDao.findById("6").get()));
        }
    }

    @GetMapping("/studiesActivity/attendance/detail")
    public void detailAttendance(Model model, @RequestParam Jadwal jadwal, Pageable page) {
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
        List<KrsDetail> krsDetails = krsDetailDao.findByMahasiswaNotInAndJadwalAndStatus(mahasiswas,sesiKuliah.getJadwal(),StatusRecord.AKTIF);

        model.addAttribute("statusPresensi", StatusPresensi.values());
        model.addAttribute("status", statusPresensi);
        model.addAttribute("presensi", presensiMahasiswa);
        model.addAttribute("detail", krsDetails);
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
            model.addAttribute("nim", nim);
            model.addAttribute("tahun", tahunAkademik);
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

            TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

            if (ta.getTanggalMulaiKrs().compareTo(LocalDate.now()) <= 0) {
                model.addAttribute("validasi", ta);
            }

            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik,StatusRecord.AKTIF);

            model.addAttribute("listKrs", krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,k,mahasiswa));
            model.addAttribute("mahasiswa", mahasiswa);

        }

    }

    @GetMapping("/studiesActivity/krs/form")
    public void formKrs(@RequestParam(required = false)String nim, @RequestParam(required = false) String tahunAkademik,Model model, @RequestParam(required = false) String lebih){
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
                model.addAttribute("kosong", "21");
            }else {

                if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0) {
                    model.addAttribute("full", "23");
                }
            }
            model.addAttribute("sks", sks);
            model.addAttribute("lebih", lebih);
        }

        if (ta.getJenis() == StatusRecord.GANJIL){
            Integer prosesKode = Integer.valueOf(firstFourChars)-1;
            String kode = prosesKode.toString()+"2";

            TahunAkademik tahun = tahunAkademikDao.findByKodeTahunAkademikAndJenis(kode,StatusRecord.GENAP);
            IpkDto ipk = krsDetailDao.ip(mahasiswa,tahun);
            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);
            Long sks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

            if (ipk == null){
                model.addAttribute("kosong", "21");
            }else {

                if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0) {
                    model.addAttribute("full", "23");
                }
            }
            model.addAttribute("sks", sks);
            model.addAttribute("lebih", lebih);
        }

        List<Object[]> krsDetail = krsDetailDao.pilihanKrs(ta,kelasMahasiswa.getKelas(),mahasiswa.getIdProdi(),mahasiswa);
        model.addAttribute("pilihanKrs", krsDetail);
    }

    @PostMapping("/studiesActivity/krs/form")
    public String prosesKrs(Authentication authentication, @RequestParam String jumlah, @RequestParam(required = false) String[] selected,
                            RedirectAttributes attributes,@RequestParam String nim){

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik,StatusRecord.AKTIF);

        Long krsDetail = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

        if (selected == null){

        }else {
            Long jadwal = jadwalDao.totalSks(selected);
            if (krsDetail == null){
                if (jadwal > Integer.valueOf(jumlah)) {
                    System.out.println("lebih kosong");
                    return "redirect:form?lebih=true";
                }else {
                    for (String idJadwal : selected) {
                        Jadwal j = jadwalDao.findById(idJadwal).get();
                        if (krsDetailDao.cariKrs(j,tahunAkademik,mahasiswa) == null) {
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

            if (krsDetail != null){
                if (jadwal + krsDetail > Integer.valueOf(jumlah)) {
                    System.out.println("lebih");
                    return "redirect:form?lebih=true";
                }else {
                    for (String idJadwal : selected) {
                        Jadwal j = jadwalDao.findById(idJadwal).get();
                        if (krsDetailDao.cariKrs(j,tahunAkademik,mahasiswa) == null) {

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

        return "redirect:list?nim="+nim+"&tahunAkademik="+tahunAkademik.getId();


    }

    @PostMapping("/studiesActivity/krs/delete")
    public String deleteKrs(@RequestParam(name = "id", value = "id") KrsDetail krsDetail){
        krsDetail.setStatus(StatusRecord.HAPUS);
        krsDetailDao.save(krsDetail);

        return "redirect:list?nim="+krsDetail.getMahasiswa().getNim()+"&tahunAkademik="+krsDetail.getKrs().getTahunAkademik().getId();

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
        }else{
            TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            model.addAttribute("jadwal", jadwalDao.lecturerAssesment(dosen,StatusRecord.AKTIF, tahun));
        }


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


        String idFile = UUID.randomUUID().toString();
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

        Soal s = soalDao.findByJadwalAndStatusAndStatusApproveNotIn(soal.getJadwal(),StatusRecord.AKTIF,Arrays.asList(StatusApprove.REJECTED));
        if (s == null){
            soalDao.save(soal);
        }

        if (s != null){
            s.setStatus(StatusRecord.NONAKTIF);
            soalDao.save(s);
            soalDao.save(soal);
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

        return null;

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
    public void upload(Model model,@RequestParam Jadwal jadwal){

        List<BobotTugas> listTugas = bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("listTugas",listTugas);

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
                model.addAttribute("khs",krsDetail);
                model.addAttribute("ipk", krsDetailDao.ipkTahunAkademik(mahasiswa,tahunAkademik.getKodeTahunAkademik()));
                model.addAttribute("ip", krsDetailDao.ip(mahasiswa,tahunAkademik));
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
    public void hasilEdom(Model model, @RequestParam Jadwal jadwal) {

        model.addAttribute("jadwal", jadwal);
        model.addAttribute("jumlahMahasiswa", krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF).size());

        List<KrsDetail> mahasiswa = krsDetailDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);

        long e1Long =krsDetailDao.jumlahE1(jadwal);
        long e2Long =krsDetailDao.jumlahE2(jadwal);
        long e3Long =krsDetailDao.jumlahE3(jadwal);
        long e4Long =krsDetailDao.jumlahE4(jadwal);
        long e5Long =krsDetailDao.jumlahE5(jadwal);

        BigDecimal e1 = BigDecimal.valueOf(e1Long).divide(BigDecimal.valueOf(mahasiswa.size()),2, RoundingMode.HALF_UP);
        BigDecimal e2 = BigDecimal.valueOf(e2Long).divide(BigDecimal.valueOf(mahasiswa.size()),2, RoundingMode.HALF_UP);
        BigDecimal e3 = BigDecimal.valueOf(e3Long).divide(BigDecimal.valueOf(mahasiswa.size()),2, RoundingMode.HALF_UP);
        BigDecimal e4 = BigDecimal.valueOf(e4Long).divide(BigDecimal.valueOf(mahasiswa.size()),2, RoundingMode.HALF_UP);
        BigDecimal e5 = BigDecimal.valueOf(e5Long).divide(BigDecimal.valueOf(mahasiswa.size()),2, RoundingMode.HALF_UP);

        BigDecimal rata = e1.add(e2).add(e3).add(e4).add(e5);


        model.addAttribute("e1", e1);
        model.addAttribute("e2", e2);
        model.addAttribute("e3", e3);
        model.addAttribute("e4", e4);
        model.addAttribute("e5", e5);
        model.addAttribute("rata",rata.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP));


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
            model.addAttribute("mhsw",mahasiswa);

            //tampilsemua
            model.addAttribute("transkrip", krsDetailDao.transkrip(mahasiswa));
            model.addAttribute("sks", krsDetailDao.totalSks(mahasiswa));
            model.addAttribute("mutu", krsDetailDao.totalMutu(mahasiswa));
            model.addAttribute("semesterTranskript", krsDao.semesterTranskript(mahasiswa.getId()));
            model.addAttribute("transkriptTampil", krsDetailDao.transkriptTampil(mahasiswa.getId()));
//            model.addAttribute("transkrip1", krsDetailDao.transkripSem(mahasiswa,"1"));
//            model.addAttribute("transkrip2", krsDetailDao.transkripSem(mahasiswa,"2"));
//            model.addAttribute("transkrip3", krsDetailDao.transkripSem(mahasiswa,"3"));
//            model.addAttribute("transkrip4", krsDetailDao.transkripSem(mahasiswa,"4"));
//            model.addAttribute("transkrip5", krsDetailDao.transkripSem(mahasiswa,"5"));
//            model.addAttribute("transkrip6", krsDetailDao.transkripSem(mahasiswa,"6"));
//            model.addAttribute("transkrip7", krsDetailDao.transkripSem(mahasiswa,"7"));
//            model.addAttribute("transkrip8", krsDetailDao.transkripSem(mahasiswa,"8"));
        }




    }

    @GetMapping("/studiesActivity/transcript/cetaktranscript")
    public void cetakTranscript(Model model,@RequestParam(required = false) String nim){
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        if (mahasiswa != null){
            model.addAttribute("mahasiswa",mahasiswa);

        }
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

        BigDecimal ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);


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


        int rowInfoNama = 6 ;
        Row nama = sheet.createRow(rowInfoNama);
        nama.createCell(0).setCellValue("Name");
        nama.createCell(3).setCellValue(":");
        nama.createCell(4).setCellValue(mahasiswa.getNama());
        nama.getCell(0).setCellStyle(styleData);
        nama.getCell(3).setCellStyle(styleSymbol);
        nama.getCell(4).setCellStyle(styleData);

        int rowInfoNim = 7 ;
        Row matricNo = sheet.createRow(rowInfoNim);
        matricNo.createCell(0).setCellValue("Student Matric No");
        matricNo.createCell(3).setCellValue(":");
        matricNo.createCell(4).setCellValue(mahasiswa.getNim());
        matricNo.getCell(0).setCellStyle(styleData);
        matricNo.getCell(3).setCellStyle(styleSymbol);
        matricNo.getCell(4).setCellStyle(styleData);

        int rowInfoEntry = 8 ;
        Row entry = sheet.createRow(rowInfoEntry);
        entry.createCell(0).setCellValue("Entry Matric No / Graduated Matric No");
        entry.createCell(3).setCellValue(":");
        entry.createCell(4).setCellValue("-");
        entry.getCell(0).setCellStyle(styleData);
        entry.getCell(3).setCellStyle(styleSymbol);
        entry.getCell(4).setCellStyle(styleData);

        int rowInfoBirth = 9 ;
        Row birthDay = sheet.createRow(rowInfoBirth);
        birthDay.createCell(0).setCellValue("Place and Date of Birth");
        birthDay.createCell(3).setCellValue(":");
        birthDay.createCell(4).setCellValue(mahasiswa.getTempatLahir()+"," + " " + mahasiswa.getTanggalLahir());
        birthDay.getCell(0).setCellStyle(styleData);
        birthDay.getCell(3).setCellStyle(styleSymbol);
        birthDay.getCell(4).setCellStyle(styleData);

        int rowInfoLevel = 10 ;
        Row level = sheet.createRow(rowInfoLevel);
        level.createCell(0).setCellValue("Level");
        level.createCell(3).setCellValue(":");
        level.createCell(4).setCellValue(mahasiswa.getIdProgram().getNamaProgram());
        level.getCell(0).setCellStyle(styleData);
        level.getCell(3).setCellStyle(styleSymbol);
        level.getCell(4).setCellStyle(styleData);

        int rowInfoDepartment = 11 ;
        Row department = sheet.createRow(rowInfoDepartment);
        department.createCell(0).setCellValue("Department");
        department.createCell(3).setCellValue(":");
        department.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi());
        department.getCell(0).setCellStyle(styleData);
        department.getCell(3).setCellStyle(styleSymbol);
        department.getCell(4).setCellStyle(styleData);

        int rowInfoNoAcred = 12 ;
        Row accreditation = sheet.createRow(rowInfoNoAcred);
        accreditation.createCell(0).setCellValue("No of Accreditation Decree");
        accreditation.createCell(3).setCellValue(":");
        accreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi());
        accreditation.getCell(0).setCellStyle(styleData);
        accreditation.getCell(3).setCellStyle(styleSymbol);
        accreditation.getCell(4).setCellStyle(styleData);

        int rowInfoDateAcred = 13 ;
        Row DateAccreditation = sheet.createRow(rowInfoDateAcred);
        DateAccreditation.createCell(0).setCellValue("Date of Accreditation Decree");
        DateAccreditation.createCell(3).setCellValue(":");
        DateAccreditation.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi());
        DateAccreditation.getCell(0).setCellStyle(styleData);
        DateAccreditation.getCell(3).setCellStyle(styleSymbol);
        DateAccreditation.getCell(4).setCellStyle(styleData);

        int rowInfoGraduatedDate = 14 ;
        Row graduatedDate = sheet.createRow(rowInfoGraduatedDate);
        graduatedDate.createCell(0).setCellValue("Graduated Date");
        graduatedDate.createCell(3).setCellValue(":");
        graduatedDate.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi());
        graduatedDate.getCell(0).setCellStyle(styleData);
        graduatedDate.getCell(3).setCellStyle(styleSymbol);
        graduatedDate.getCell(4).setCellStyle(styleData);

        int rowInfoTranscript = 15 ;
        Row transcript = sheet.createRow(rowInfoTranscript);
        transcript.createCell(0).setCellValue("No of Transcript");
        transcript.createCell(3).setCellValue(":");
        transcript.createCell(4).setCellValue(mahasiswa.getIdProdi().getNamaProdi());
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
        rowIpk.createCell(5).setCellValue(ipk.toString());
        rowIpk.getCell(0).setCellStyle(styleTotal);
        rowIpk.getCell(5).setCellStyle(styleDataKhs);

        int predicate = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+4;
        Row predicateRow = sheet.createRow(predicate);
        predicateRow.createCell(0).setCellValue("Predicate :");
        predicateRow.createCell(2).setCellValue("Good");
        predicateRow.getCell(0).setCellStyle(styleSubHeader);
        predicateRow.getCell(2).setCellStyle(styleData);

        int thesis = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+5;
        Row thesisRow = sheet.createRow(thesis);
        thesisRow.createCell(0).setCellValue("Thesis Title :");
        thesisRow.createCell(2).setCellValue("Good");
        thesisRow.getCell(0).setCellStyle(styleSubHeader);
        thesisRow.getCell(2).setCellStyle(styleData);

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
        excellentRow.getCell(1).setCellStyle(styleManajemen);
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
        veryGoodRow.getCell(1).setCellStyle(styleManajemen);
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
        goodRow.getCell(1).setCellStyle(styleManajemen);
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
        satisfactoryRow.getCell(1).setCellStyle(styleManajemen);
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
        poorRow.createCell(7).setCellValue("Poor/Passed Conditionally");
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
        createDateRow.createCell(0).setCellValue("This is Certified to be true and accurate statement, issued in Bogor on 17 November 2015 H");
        createDateRow.getCell(0).setCellStyle(styleData);

        int faculty = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+26;
        Row facultyRow = sheet.createRow(faculty);
        facultyRow.createCell(0).setCellValue("Dean of " + mahasiswa.getIdProdi().getIdJurusan().getIdFakultas().getNamaFakultasEnglish());
        facultyRow.getCell(0).setCellStyle(styleData);
        facultyRow.createCell(5).setCellValue("Coordinator of " + mahasiswa.getIdProdi().getNamaProdiEnglish());
        facultyRow.getCell(5).setCellStyle(styleData);

        int lecture = 18+semester1.size()+semester2.size()+semester3.size()+semester4.size()+semester5.size()+semester6.size()+semester7.size()+semester8.size()+32;
        Row lectureRow = sheet.createRow(lecture);
        lectureRow.createCell(0).setCellValue(mahasiswa.getIdProdi().getIdJurusan().getIdFakultas().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(0).setCellStyle(styleDosen);
        lectureRow.createCell(5).setCellValue(mahasiswa.getIdProdi().getDosen().getKaryawan().getNamaKaryawan());
        lectureRow.getCell(5).setCellStyle(styleDosen);





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
//        model.addAttribute("ipk", krsDetailDao.ipkTranskript(mahasiswa));

        BigDecimal totalSKS = krsDetailDao.totalSksAkhir(mahasiswa.getId());
        BigDecimal totalMuti = krsDetailDao.totalMutuAkhir(mahasiswa.getId());

        BigDecimal Ipk = totalMuti.divide(totalSKS,2,BigDecimal.ROUND_HALF_DOWN);

        Long totalSKS2 = totalSKS.longValue();
        Long totalMuti1 = totalMuti.longValue();

        model.addAttribute("sks", totalSKS2);
        model.addAttribute("mutu", totalMuti1);
        model.addAttribute("ipk", Ipk);

//        model.addAttribute("transkriptPrint", krsDetailDao.transkriptAkhir(mahasiswa.getId()));
//        model.addAttribute("semesterTranskriptPrint1", krsDetailDao.semesterTraskripPrint1(mahasiswa.getId()));
//        model.addAttribute("semesterTranskriptPrint1", krsDetailDao.transkriptAkhir(mahasiswa.getId()));
//        model.addAttribute("transkrip", krsDetailDao.transkripAKhir(mahasiswa));

        model.addAttribute("transkrip1", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"1"));
        model.addAttribute("transkrip2", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"2"));
        model.addAttribute("transkrip3", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"3"));
        model.addAttribute("transkrip4", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"4"));
        model.addAttribute("transkrip5", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"5"));
        model.addAttribute("transkrip6", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"6"));
        model.addAttribute("transkrip7", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"7"));
        model.addAttribute("transkrip8", krsDetailDao.transkriptAkhir(mahasiswa.getId(),"8"));

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


        String idFile = soal.getJadwal().getTahunAkademik().getKodeTahunAkademik()+"-"+soal.getStatusSoal()+"-"+soal.getJadwal().getKelas().getNamaKelas()+"-"+soal.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah()+"-"+soal.getDosen().getKaryawan().getNamaKaryawan();

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

        return "redirect:list?tahun="+jadwal.getTahunAkademik().getId()+"&status="+StatusApprove.REJECTED;
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


}

