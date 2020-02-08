package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreInput;
import id.ac.tazkia.smilemahasiswa.dto.attendance.JadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.PresensiService;
import id.ac.tazkia.smilemahasiswa.service.ScoreService;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.ss.usermodel.*;
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
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
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
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private IpkDao ipkDao;

    @Autowired
    private BobotTugasDao bobotTugasDao;

    @Autowired
    private NilaiTugasDao nilaiTugasDao;

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

    @Autowired private ScoreService scoreService;

    @Value("classpath:sample/soal.doc")
    private Resource contohSoal;

    @Value("classpath:sample/uas.doc")
    private Resource contohSoalUas;

    @Value("${upload.soal}")
    private String uploadFolder;

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
        model.addAttribute("jadwal", jadwal);

    }

    @PostMapping("/studiesActivity/attendance/detail")
    public String createPresensi(@ModelAttribute @Valid JadwalDto jadwalDto){

        presensiService.inputPresensi(jadwalDto.getDosen(),
                jadwalDto.getJadwal(), jadwalDto.getBeritaAcara(),
                LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamMulai()));

        return "redirect:detail?jadwal=" + jadwalDto.getJadwal().getId();

    }

    @GetMapping("/studiesActivity/attendance/mahasiswa")
    public void attendance(@RequestParam(name = "id",value = "id") SesiKuliah sesiKuliah, Model model){
        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliahAndStatus(sesiKuliah,StatusRecord.AKTIF);

        Map<String, String> statusPresensi = new HashMap<>();
        for(PresensiMahasiswa pm : presensiMahasiswa){
            statusPresensi.put(pm.getId(), pm.getStatusPresensi().toString());
        }

        model.addAttribute("statusPresensi", StatusPresensi.values());
        model.addAttribute("status", statusPresensi);
        model.addAttribute("presensi", presensiMahasiswa);
        model.addAttribute("jadwal", sesiKuliah.getJadwal().getId());
        model.addAttribute("sesi",sesiKuliah.getId());
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
    public String prosesEdit(@ModelAttribute @Valid JadwalDto jadwalDto){
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(jadwalDto.getId()).get();
        sesiKuliah.setBeritaAcara(jadwalDto.getBeritaAcara());
        sesiKuliah.setWaktuMulai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamMulai()));
        sesiKuliah.setWaktuSelesai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamSelesai()));

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
    public void formKrs(@RequestParam(required = false)String nim, @RequestParam(required = false) String tahunAkademik,Model model){
        model.addAttribute("nim", nim);
        model.addAttribute("tahun", tahunAkademik);

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);


        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Ipk ipk = ipkDao.findByMahasiswa(mahasiswa);
        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);

        Long sks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

        if (ipk == null){
            model.addAttribute("kosong", "21");
        }

        if (ipk != null && ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0){
            model.addAttribute("full", "23");
        }
        model.addAttribute("sks", sks);

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
    public void listPenilaianDosen(Model model,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        model.addAttribute("jadwal", jadwalDao.lecturerAssesment(dosen,StatusRecord.AKTIF, tahun));

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
    public String tambahTugas(@ModelAttribute @Valid BobotTugas bobotTugas){
        bobotTugasDao.save(bobotTugas);
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatus(bobotTugas.getJadwal(),StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail){
            NilaiTugas nilaiTugas = new NilaiTugas();
            nilaiTugas.setNilai(BigDecimal.ZERO);
            nilaiTugas.setNilaiAkhir(BigDecimal.ZERO);
            nilaiTugas.setKrsDetail(kd);
            nilaiTugas.setBobotTugas(bobotTugas);
            nilaiTugas.setStatus(StatusRecord.AKTIF);
            nilaiTugasDao.save(nilaiTugas);
        }
        return "redirect:weight?jadwal="+bobotTugas.getJadwal().getId();

    }

    @GetMapping("/studiesActivity/assesment/score")
    public String assesmentScore(@RequestParam Jadwal jadwal, Model model, RedirectAttributes attributes){
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


        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=PenilaianTugas.xlsx");
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


        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=PenilaianUTS.xlsx");
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


        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=PenilaianUAS.xlsx");
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
            List <KrsDetail> krsDetailAt = krsDetailDao.findByJadwalAndStatus(bobotTugas.getJadwal(),StatusRecord.AKTIF);
            int jmlMhs = krsDetailAt.size();
            for (int i = 0; i < jmlMhs;i++){
                Row baris = sheetPertama.getRow(row + i);

                String nim = baris.getCell(3).getStringCellValue();
                Cell nilai = baris.getCell(5 );

                List<StatusPresensi> statusp = Arrays.asList(StatusPresensi.MANGKIR,StatusPresensi.TERLAMBAT);

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

                    Long jmlhMhsw = presensiMahasiswaDao.countByStatusPresensiNotInAndKrsDetailAndStatus(statusp,krsDetail,StatusRecord.AKTIF);

                    Long jmlhDsn = presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF,bobotTugas.getJadwal());

                    Long jmlhSds = presensiMahasiswaDao.countByStatusAndSesiKuliahPresensiDosenStatusAndStatusPresensiNotInAndMahasiswaAndKrsDetailJadwalMatakuliahKurikulumMatakuliahKodeMatakuliahContainingIgnoreCase(StatusRecord.AKTIF,StatusRecord.AKTIF,statusp,krsDetail.getMahasiswa(),"SDS");


                    BigDecimal nilaiPresensi = BigDecimal.valueOf(new Long(jmlhMhsw*100/jmlhDsn));

                    if (bobotTugas.getJadwal().getMatakuliahKurikulum().getSds() == null){
                        BigDecimal nilaiPresensiSds = BigDecimal.ZERO;
                        if (nilaiPresensiSds == null && jmlhSds == null){
                            if (bobotTugas.getJadwal().getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(BigDecimal.ZERO));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(BigDecimal.ZERO));
                            }
                        }else {
                            if (bobotTugas.getJadwal().getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(nilaiPresensiSds));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(nilaiPresensiSds));
                            }
                        }
                    }else {
                        BigDecimal nilaiPresensiSds = BigDecimal.valueOf(new Long(jmlhSds * 10 * krsDetail.getMatakuliahKurikulum().getSds() / 100));
                        if (nilaiPresensiSds == null && jmlhSds == null){
                            if (bobotTugas.getJadwal().getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(BigDecimal.ZERO));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(BigDecimal.ZERO));
                            }
                        }else {
                            if (bobotTugas.getJadwal().getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(nilaiPresensiSds));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(nilaiPresensiSds));
                            }
                        }
                    }
                    krsDetail.setNilaiPresensi(nilaiPresensi);
                    scoreService.hitungNilaiAkhir(krsDetail);
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
            List <KrsDetail> krsDetailAt = krsDetailDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
            int jmlMhs = krsDetailAt.size();
            for (int i = 0; i < jmlMhs;i++) {
                Row baris = sheetPertama.getRow(row + i);

                String nim = baris.getCell(3).getStringCellValue();
                Cell nilai = baris.getCell(5);

                List<StatusPresensi> statusp = Arrays.asList(StatusPresensi.MANGKIR,StatusPresensi.TERLAMBAT);

                if(nilai != null) {
                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            jadwal, StatusRecord.AKTIF);

                    if (krsDetail == null) {
                        LOGGER.warn("KRS Detail untuk nim {} dan UTS {} tidak ditemukan", nim, jadwal.getStatusUts());
                        return "redirect:/penilaian/list";
                    }

                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);

                    krsDetail.setNilaiUts(new BigDecimal(nilai.getNumericCellValue()));

                    BigDecimal nilaiUts = new BigDecimal(nilai.getNumericCellValue()).multiply(krsDetail.getJadwal().getBobotUts())
                            .divide(new BigDecimal(100));


                    Long jmlhDsn = presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF,jadwal);
                    Long jmlhMhsw = presensiMahasiswaDao.countByStatusPresensiNotInAndKrsDetailAndStatus(statusp,krsDetail,StatusRecord.AKTIF);

                    Long jmlhSds = presensiMahasiswaDao.countByStatusAndSesiKuliahPresensiDosenStatusAndStatusPresensiNotInAndMahasiswaAndKrsDetailJadwalMatakuliahKurikulumMatakuliahKodeMatakuliahContainingIgnoreCase(StatusRecord.AKTIF,StatusRecord.AKTIF,statusp,krsDetail.getMahasiswa(),"SDS");

                    BigDecimal nilaiPresensi = BigDecimal.valueOf(new Long(jmlhMhsw*100/jmlhDsn));
                    if (jadwal.getMatakuliahKurikulum().getSds() == null){
                        BigDecimal nilaiPresensiSds = BigDecimal.ZERO;
                        if (nilaiPresensiSds == null && jmlhSds == null){
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(BigDecimal.ZERO));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(BigDecimal.ZERO));
                            }
                        }else {
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(nilaiPresensiSds));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(nilaiPresensiSds));
                            }
                        }
                    }else {
                        BigDecimal nilaiPresensiSds = BigDecimal.valueOf(new Long(jmlhSds * 10 * krsDetail.getMatakuliahKurikulum().getSds() / 100));
                        if (nilaiPresensiSds == null && jmlhSds == null){
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(BigDecimal.ZERO));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(BigDecimal.ZERO));
                            }
                        }else {
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(nilaiPresensiSds));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(nilaiPresensiSds));
                            }
                        }
                    }
                    krsDetail.setNilaiPresensi(nilaiPresensi);
                    scoreService.hitungNilaiAkhir(krsDetail);

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
            List <KrsDetail> krsDetailAt = krsDetailDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
            int jmlMhs = krsDetailAt.size();
            for (int i = 0; i < jmlMhs;i++) {
                Row baris = sheetPertama.getRow(row + i);

                Cell nilai = baris.getCell(5);
                String nim = baris.getCell(3).getStringCellValue();

                List<StatusPresensi> statusp = Arrays.asList(StatusPresensi.MANGKIR,StatusPresensi.TERLAMBAT);

                if (nilai != null) {
                    LOGGER.info("NIM : {}, Nilai : {}", nim, nilai);

                    KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatus(mahasiswaDao.findByNim(nim),
                            jadwal, StatusRecord.AKTIF);

                    if (krsDetail == null) {
                        LOGGER.warn("KRS Detail untuk nim {} dan UAS {} tidak ditemukan", nim, jadwal.getStatusUas());
                        return "redirect:/penilaian/list";
                    }

                    krsDetail.setNilaiUas(new BigDecimal(nilai.getNumericCellValue()));

                    BigDecimal nilaiUas = new BigDecimal(nilai.getNumericCellValue()).multiply(krsDetail.getJadwal().getBobotUts())
                            .divide(new BigDecimal(100));

                    Long jmlhMhsw = presensiMahasiswaDao.countByStatusPresensiNotInAndKrsDetailAndStatus(statusp,krsDetail,StatusRecord.AKTIF);
                    Long jmlhDsn = presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF,jadwal);

                    Long jmlhSds = presensiMahasiswaDao.countByStatusAndSesiKuliahPresensiDosenStatusAndStatusPresensiNotInAndMahasiswaAndKrsDetailJadwalMatakuliahKurikulumMatakuliahKodeMatakuliahContainingIgnoreCase(StatusRecord.AKTIF,StatusRecord.AKTIF,statusp,krsDetail.getMahasiswa(),"SDS");

                    BigDecimal nilaiPresensi = BigDecimal.valueOf(new Long(jmlhMhsw*100/jmlhDsn));
                    if (jadwal.getMatakuliahKurikulum().getSds() == null){
                        BigDecimal nilaiPresensiSds = BigDecimal.ZERO;
                        if (nilaiPresensiSds == null && jmlhSds == null){
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(BigDecimal.ZERO));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(BigDecimal.ZERO));
                            }
                        }else {
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(nilaiPresensiSds));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(nilaiPresensiSds));
                            }
                        }
                    }else {
                        BigDecimal nilaiPresensiSds = BigDecimal.valueOf(new Long(jmlhSds * 10 * krsDetail.getMatakuliahKurikulum().getSds() / 100));
                        if (nilaiPresensiSds == null && jmlhSds == null){
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(BigDecimal.ZERO));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(BigDecimal.ZERO));
                            }
                        }else {
                            if (jadwal.getBobotPresensi().toBigInteger().intValue() == 0){
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(BigDecimal.ZERO).add(nilaiPresensiSds));
                            }else {
                                krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts().multiply(krsDetail.getJadwal().getBobotUts().divide(new BigDecimal(100)))).add(krsDetail.getNilaiUas().multiply(krsDetail.getJadwal().getBobotUas().divide(new BigDecimal(100)))).add(nilaiPresensi.divide(krsDetail.getJadwal().getBobotPresensi())).add(nilaiPresensiSds));
                            }
                        }
                    }
                    krsDetail.setNilaiPresensi(nilaiPresensi);
                    scoreService.hitungNilaiAkhir(krsDetail);


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
            } else {
                model.addAttribute("selectedNim" , nim);
                List<DataKhsDto> krsDetail = krsDetailDao.getKhs(tahunAkademikDao.findByStatus(StatusRecord.AKTIF),mahasiswa);
                model.addAttribute("khs",krsDetail);
            }
        }
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


}
