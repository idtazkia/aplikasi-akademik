package id.ac.tazkia.smilemahasiswa.controller;

import com.sun.org.apache.xpath.internal.operations.Mod;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.TagihanService;
import jdk.net.SocketFlow;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.common.metrics.Stat;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sun.security.krb5.internal.KRBCred;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class StudyActivityController {

    public static final List<String> TAGIHAN_KRS = Arrays.asList("14", "22", "40");

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private CutiDao cutiDao;

    @Autowired
    private PraKrsSpDao praKrsSpDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private JenisTagihanDao jenisTagihanDao;

    @Autowired
    private NilaiJenisTagihanDao nilaiJenisTagihanDao;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private TagihanService tagihanService;

    @Autowired
    private BiayaSksSpDao biayaSksDao;

    @Autowired
    private RefundSpDao refundSpDao;

    @Autowired
    private PembayaranDao pembayaranDao;

    @Autowired
    private KuotaOfflineDao kuotaOfflineDao;

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired
    private RequestPenangguhanDao requestPenangguhanDao;

    @Autowired
    private DaftarUlangDao daftarUlangDao;

    @ModelAttribute("konsentrasi")
    public Iterable<Konsentrasi> konsentrasis() {
        return konsentrasiDao.findByStatus(StatusRecord.AKTIF);
    }

    @GetMapping("/study/comingsoon")
    public String comingsoon(Model model,
                             Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);


        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
//        TahunAkademikProdi tahunAkademikProdi1 = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mahasiswa.getIdProdi());
        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByStatusAndProdi(StatusRecord.AKTIF, mahasiswa.getIdProdi());

        DaftarUlang daftarUlang = daftarUlangDao.findByStatusAndMahasiswaAndTahunAkademik(StatusRecord.AKTIF, mahasiswa, tahunAkademik);


        if (daftarUlang == null){
            Long day = ChronoUnit.DAYS.between(LocalDate.now(),tahunAkademikProdi.getMulaiKrs());

            model.addAttribute("krs", tahunAkademikProdi);
            model.addAttribute("hari", day);
            return "study/comingsoon";

        }else {

            return "redirect:krs";

        }


//        String jenisTahunAkademik = tahunAkademik.getJenis().toString();
//
//        String kodeTahunAkademik = tahunAkademik.getKodeTahunAkademik();
//        String beforeKode = kodeTahunAkademik.substring(0, 4);
//        Integer nextKode = (Integer.valueOf(beforeKode)) + 1;
//        String tahunAkademikKode = (String.valueOf(nextKode)) + 1;
//
//        if (jenisTahunAkademik == "PENDEK"){
//            TahunAkademik tahunAkademik1 = tahunAkademikDao.findByKodeTahunAkademikAndJenis(tahunAkademikKode, StatusRecord.GANJIL);
//            TahunAkademikProdi tahunAkademikProdi1 = tahunProdiDao.findByTahunAkademikAndProdi(tahunAkademik1, mahasiswa.getIdProdi());
//            Long day = ChronoUnit.DAYS.between(LocalDate.now(),tahunAkademik1.getTanggalMulaiKrs());
//            model.addAttribute("krs", tahunAkademikProdi1);
//            model.addAttribute("hari", day);
//            return "study/comingsoon";
//        } else if (tahunAkademikProdi.getMulaiKrs().compareTo(LocalDate.now()) > 0){
//            Long day = ChronoUnit.DAYS.between(LocalDate.now(),tahunAkademikProdi.getMulaiKrs());
//
//            model.addAttribute("krs", tahunAkademikProdi);
//            model.addAttribute("hari", day);
//            return "study/comingsoon";
//        } else {
//
//            return "redirect:krs";
//
//        }

    }

    @GetMapping("/study/krs")
    public String krs(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        Konsentrasi idKonstrasi = mahasiswa.getIdKonsentrasi();
        String prodi = mahasiswa.getIdProdi().getId();
        if (idKonstrasi == null){
            Integer semester = krsDao.countSemester(mahasiswa.getNim());
            if (semester == 5){
                // Manajemen Bisnis Syariah
                if (prodi.equals("01")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    return "redirect:/du/konsentrasi";
                }else if (prodi.equals("03")){
                    //ekomomi Syariah
                    model.addAttribute("mahasiswa", mahasiswa);
                    return "redirect:/du/konsentrasi";
                }
            }else if (semester == 6){
                // Akuntasi Syariah
                if (prodi.equals("02")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    return "redirect:/du/konsentrasi";
                }
            }else if (semester == 2){
                // Magister Ekonomi Syariah
                if (prodi.equals("05")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    return "redirect:/du/konsentrasi";
                } else if (prodi.equals("4f8e1779-4d46-4365-90df-996fab83b47c")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    return "redirect:/du/konsentrasi";
                }
            }
        }

        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByStatusAndProdi(StatusRecord.AKTIF, mahasiswa.getIdProdi());

//        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        TahunAkademik ta = tahunAkademikDao.findById(tahunAkademikProdi.getTahunAkademik().getId()).get();

        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);

        model.addAttribute("mahasiswa", mahasiswa);
        if (kelasMahasiswa != null){
            model.addAttribute("kelas", kelasMahasiswa);
        }


        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);

        if (k == null){
            Long jumlahSks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

            if (jumlahSks == null) {
                jumlahSks = Long.valueOf(0);
            }

            model.addAttribute("sks", jumlahSks);
            model.addAttribute("tahunAkademikProdi", tahunAkademikProdi);

            model.addAttribute("kosong", "kosong");
        }

        if(k != null) {

            Long jumlahSks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

            if (jumlahSks == null) {
                jumlahSks = Long.valueOf(0);
            }

            if (tahunAkademikProdi.getSelesaiKrs().compareTo(LocalDate.now()) >= 0) {
                model.addAttribute("validasi", ta);
            }

            Integer semester = krsDetailDao.cariSemester(mahasiswa.getId(), ta.getId());
            Integer semesterSekarang = krsDetailDao.cariSemesterSekarang(mahasiswa.getId(), ta.getId());

            if (semester == null) {
                semester = 0;
            }

            if (semesterSekarang == null) {
                semesterSekarang = 0;
            }

            Integer semesterTotal = semester + semesterSekarang;

            model.addAttribute("semester", semesterTotal);
            model.addAttribute("sks", jumlahSks);
            model.addAttribute("tahunAkademikProdi", tahunAkademikProdi);

            model.addAttribute("listKrs", krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF, k, mahasiswa));
        }

        return "study/krs";


    }

    @GetMapping("/study/form")
    public void getForm(Model model, Authentication authentication, @RequestParam(required = false) String lebih){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);


        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
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
            model.addAttribute("lebih", lebih);
            model.addAttribute("sks", sks);
        }

        if (ta.getJenis() == StatusRecord.GANJIL){
            Integer prosesKode = Integer.valueOf(firstFourChars)-1;
            String kode = prosesKode.toString()+"2";

            TahunAkademik tahun = tahunAkademikDao.findByKodeTahunAkademikAndJenis(kode,StatusRecord.GENAP);
            IpkDto ipk = krsDetailDao.ip(mahasiswa,tahun);
            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);
//            System.out.println(tahun.getKodeTahunAkademik());
//            System.out.println(ipk.getIpk());
            Long sks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

            if (ipk == null){
                model.addAttribute("kosong", "24");
            }else {

                if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0) {
                    model.addAttribute("full", "24");
                }
            }
            model.addAttribute("lebih", lebih);
            model.addAttribute("sks", sks);
        }



//        List<Object[]> krsDetail = krsDetailDao.pilihKrs(ta,kelasMahasiswa.getKelas(),mahasiswa.getIdProdi(),mahasiswa);
//        List<Object[]> krsDetail = krsDetailDao.pilihKrsMahasiswa(ta, mahasiswa.getIdProdi(), mahasiswa);
        List<Object[]> krsDetail = krsDetailDao.newPilihKrs(ta, kelasMahasiswa.getKelas(), mahasiswa.getIdProdi(), mahasiswa);
        model.addAttribute("pilihanKrs", krsDetail);

    }

    @GetMapping("/study/alert")
    public void alert(){

    }

    @PostMapping("/study/form")
    public String prosesKrs(Authentication authentication,@RequestParam String jumlah, @RequestParam(required = false) String[] selected,
                            RedirectAttributes attributes){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik,StatusRecord.AKTIF);

        if (k == null){
            System.out.println("Bayar");
            return "redirect:alert";
        }

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
                            kd.setNilaiTugas(BigDecimal.ZERO);
                            kd.setNilaiUas(BigDecimal.ZERO);
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
                            kd.setTahunAkademik(tahunAkademik);
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
                            kd.setTahunAkademik(tahunAkademik);
                            kd.setJumlahSakit(0);
                            kd.setStatusEdom(StatusRecord.UNDONE);
                            krsDetailDao.save(kd);
                        }
                    }
                }
            }



        }

            return "redirect:krs";


    }

    @PostMapping("/study/deleteKrs")
    public String deleteKrs(@RequestParam(name = "id", value = "id") KrsDetail krsDetail,
                            RedirectAttributes redirectAttributes){


        Integer jmlpresensi = presensiMahasiswaDao.jumlahPresensi(krsDetail.getId());

        if (jmlpresensi == null){
            jmlpresensi = 0;
        }

        if(jmlpresensi > 0){

            redirectAttributes.addFlashAttribute("gagal", "Save Data Berhasil");

        }else {

            krsDetail.setStatus(StatusRecord.HAPUS);
            krsDetailDao.save(krsDetail);
            redirectAttributes.addFlashAttribute("success", "Save Data Berhasil");

        }

        return "redirect:krs";

    }

    @GetMapping("/study/krs/form")
    public void listKrs(Model model,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        model.addAttribute("listKrs", krsDao.listKrs(tahunAkademik, mahasiswa));
    }

    @GetMapping("/study/jadwal")
    public void listJadwal(){

    }

    @GetMapping("/study/detail")
    public void detail(Authentication authentication,Model model,@RequestParam(name = "id",value = "id") Jadwal jadwal){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        model.addAttribute("detail", krsDetailDao.detailPresensi(mahasiswa,tahunAkademik,jadwal));
        model.addAttribute("jadwal", jadwal);

    }

//    Daftar Ulang

    @GetMapping("/du/konsentrasi")
    public String formKonsentrasi(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        String nim = mahasiswa.getNim();
        String prodi = mahasiswa.getIdProdi().getId();

        Integer semester = krsDao.countSemester(nim);
        if (semester == 5){
            // Manajemen Bisnis Syariah
            if (prodi.equals("01")){
                if (mahasiswa.getIdKonsentrasi().getId().equals("0")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    model.addAttribute("konstrasiProdi", konsentrasiDao.konsentrasiProdi(prodi));
                    return "du/konsentrasi";
                }else {
                    return "redirect:/du/alert";
                }
            }else if (prodi.equals("03")){
                // Ekonomi Syariah
                if (mahasiswa.getIdKonsentrasi().getId().equals("0")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    model.addAttribute("konstrasiProdi", konsentrasiDao.konsentrasiProdi(prodi));
                    return "du/konsentrasi";
                }else {
                    return "redirect:/du/alert";
                }
            }
        }else if (semester == 6){
            // Akuntasi Syariah
            if (mahasiswa.getIdKonsentrasi().getId().equals("0")){
                model.addAttribute("mahasiswa", mahasiswa);
                model.addAttribute("konstrasiProdi", konsentrasiDao.konsentrasiProdi(prodi));
                return "du/konsentrasi";
            }else {
                return "redirect:/du/alert";
            }
        }else if (semester == 2){
            // Magister Ekonomi Syariah
            if (prodi.equals("05")){
                if (mahasiswa.getIdKonsentrasi().getId().equals("0")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    model.addAttribute("konstrasiProdi", konsentrasiDao.konsentrasiProdi(prodi));
                    return "du/konsentrasi";
                }else {
                    return "redirect:/du/alert";
                }
            } else if (prodi.equals("4f8e1779-4d46-4365-90df-996fab83b47c")){
                if (mahasiswa.getIdKonsentrasi().getId().equals("0")){
                    model.addAttribute("mahasiswa", mahasiswa);
                    model.addAttribute("konstrasiProdi", konsentrasiDao.konsentrasiProdi(prodi));
                    return "du/konsentrasi";
                }else {
                    return "redirect:/du/alert";
                }
            }
        }

        return "redirect:/du/alert";


    }

    @PostMapping("/du/konsentrasi")
    public String prosesKonsentrasi(@ModelAttribute @Valid MahasiswaDto mahasiswaDto, Authentication authentication){
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        mahasiswa.setIdKonsentrasi(mahasiswaDto.getIdKonsentrasi());
        mahasiswaDao.save(mahasiswa);

        return "redirect:/du/alert";
    }

    @GetMapping("/du/alert")
    public void alertDu(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa m = mahasiswaDao.findByUser(user);

//        if (m.getStatusAktif().equals("BEASISWA")) {
//            model.addAttribute("message", "mahasiswa beasiswa");
//        }
//
//        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
//        if (ta.getJenis() == StatusRecord.PENDEK){
//            Integer tahun = new Integer(ta.getTahun());
//            Integer jadi = tahun + 1;
//            String tahunJadi = jadi + "1";
//            ta = tahunAkademikDao.findByKodeTahunAkademikAndJenis(tahunJadi, StatusRecord.GANJIL);
//            model.addAttribute("tahun", ta);
//        }else{
//            model.addAttribute("tahun", ta);
//        }
//
//        Tagihan t = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanKodeInAndTahunAkademikAndStatus(m, TAGIHAN_KRS, ta, StatusRecord.AKTIF);
//        if (t != null) {
//            model.addAttribute("sudahAda", t);
//        }

//        List<Tagihan> tagihans = tagihanDao.findByMahasiswaAndStatus(m, StatusRecord.AKTIF);
//        List<String> listId = new ArrayList<>();
//        for (Tagihan nilai : tagihans){
//            listId.add(nilai.getNilaiJenisTagihan().getId());
//        }
//        KuotaTagihanOffline kto = kuotaOfflineDao.findByAngkatanAndTahunAkademik(m.getAngkatan(), ta);
//        if (kto.getJumlah().equals("0")) {
//            model.addAttribute("habis", "kuota offline sudah habis");
//            List<NilaiJenisTagihan> njt = nilaiJenisTagihanDao.findByProdiAndProgramAndAngkatanAndTahunAkademikAndIdNotInAndStatusAndKategori(m.getIdProdi(), m.getIdProgram(), m.getAngkatan(), ta, listId, StatusRecord.AKTIF, StatusTagihan.ONLINE);
//            model.addAttribute("selectNilai", njt);
//        }else {
//            model.addAttribute("kuota", kto);
//            List<NilaiJenisTagihan> njt = nilaiJenisTagihanDao.findByProdiAndProgramAndAngkatanAndTahunAkademikAndIdNotInAndStatus(m.getIdProdi(), m.getIdProgram(), m.getAngkatan(), ta, listId, StatusRecord.AKTIF);
//            model.addAttribute("selectNilai", njt);
//        }

        model.addAttribute("listTagihan", tagihanDao.findByStatusAndStatusTagihanNotInAndMahasiswaAndLunas(StatusRecord.AKTIF, Arrays.asList(StatusTagihan.LUNAS), m, false));
        model.addAttribute("tes", m);

    }

    @PostMapping("/bill/create")
    public String mhsCreateBill(Authentication authentication, @RequestParam String nilaiJenisTagihan, @RequestParam String tahun){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa m = mahasiswaDao.findByUser(user);
        NilaiJenisTagihan nTagihan = nilaiJenisTagihanDao.findById(nilaiJenisTagihan).get();
        TahunAkademik ta = tahunAkademikDao.findById(tahun).get();
        TahunAkademik taAktif = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Integer jadi = null;
        if (taAktif.getJenis() == StatusRecord.PENDEK) {
            Integer k = new Integer(taAktif.getKodeTahunAkademik());
            jadi = k - 1;
        }else{
            Integer k = new Integer(taAktif.getTahun());
            Integer a = k-1;
            String j = a + "2";
            jadi = new Integer(j);
        }
        TahunAkademik tahunBefore = tahunAkademikDao.findByKodeTahunAkademikAndJenis(jadi.toString(), StatusRecord.GENAP);
        System.out.println("test: " + tahunBefore);

//        NilaiJenisTagihan njt = nilaiJenisTagihanDao.findByJenisTagihanAndTahunAkademikNotAndProgramAndProdiAndAngkatanAndStatus(nTagihan.getJenisTagihan(), ta, m.getIdProgram(), m.getIdProdi(), m.getAngkatan(), StatusRecord.AKTIF);
//        Tagihan t = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(m, nTagihan.getJenisTagihan(), tahunBefore, false, StatusRecord.AKTIF);
//        String keteranganTagihan = "Tagihan " + nTagihan.getJenisTagihan().getNama()
//                + " a.n. " + m.getNama();
//        if (t == null) {
//
//            Tagihan tagihan = new Tagihan();
//            tagihan.setMahasiswa(m);
//            tagihan.setNilaiJenisTagihan(nTagihan);
//            tagihan.setKeterangan(keteranganTagihan);
//            tagihan.setNilaiTagihan(nTagihan.getNilai());
//            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//            tagihan.setTanggalPembuatan(LocalDate.now());
//            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//            tagihan.setTahunAkademik(ta);
//            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//            tagihan.setStatus(StatusRecord.AKTIF);
//            tagihanDao.save(tagihan);
//            tagihanService.requestCreateTagihan(tagihan);
//
//            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(m, StatusRecord.KRS, false, ta);
//            if (ef == null) {
//                EnableFiture enableFiture = new EnableFiture();
//                enableFiture.setMahasiswa(m);
//                enableFiture.setTahunAkademik(ta);
//                enableFiture.setFitur(StatusRecord.KRS);
//                enableFiture.setEnable(false);
//                enableFiture.setKeterangan("-");
//                enableFitureDao.save(enableFiture);
//            }
//
//        }else{
//            Integer sisaCicilan = t.getNilaiTagihan().intValue() - t.getAkumulasiPembayaran().intValue();
//
//            Tagihan tagihan = new Tagihan();
//            tagihan.setIdTagihanSebelumnya(t.getId());
//            tagihan.setMahasiswa(m);
//            tagihan.setNilaiJenisTagihan(nTagihan);
//            tagihan.setKeterangan(keteranganTagihan);
//            tagihan.setNilaiTagihan(nTagihan.getNilai().add(new BigDecimal(sisaCicilan)));
//            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//            tagihan.setTanggalPembuatan(LocalDate.now());
//            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//            tagihan.setTahunAkademik(ta);
//            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//            tagihan.setStatus(StatusRecord.AKTIF);
//            tagihanDao.save(tagihan);
//            tagihanService.requestCreateTagihan(tagihan);
//
//            t.setStatus(StatusRecord.NONAKTIF);
//            t.setStatusTagihan(StatusTagihan.NONAKTIF);
//            tagihanDao.save(t);
//
//            if (t.getStatusTagihan() == StatusTagihan.DICICIL) {
//                List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(t, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
//                if (cekSisaCicilan != null) {
//                    for (RequestCicilan listCicilan : cekSisaCicilan){
//                        listCicilan.setStatus(StatusRecord.HAPUS);
//                        listCicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
//                        listCicilan.setStatusApprove(StatusApprove.HAPUS);
//                        requestCicilanDao.save(listCicilan);
//                    }
//                }
//            }
//
//            if (t.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
//                RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(t, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
//                if (rp != null) {
//                    rp.setStatus(StatusRecord.HAPUS);
//                    rp.setStatusApprove(StatusApprove.HAPUS);
//                    requestPenangguhanDao.save(rp);
//                }
//            }
//
//            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(m, StatusRecord.KRS, false, ta);
//            if (ef == null) {
//                EnableFiture enableFiture = new EnableFiture();
//                enableFiture.setMahasiswa(m);
//                enableFiture.setTahunAkademik(ta);
//                enableFiture.setFitur(StatusRecord.KRS);
//                enableFiture.setEnable(false);
//                enableFiture.setKeterangan("-");
//                enableFitureDao.save(enableFiture);
//            }
//
//        }

//        if (nTagihan.getKategori() == StatusTagihan.OFFLINE) {
//            KuotaTagihanOffline kto = kuotaOfflineDao.findByAngkatanAndTahunAkademik(m.getAngkatan(), ta);
//            if (kto != null) {
//                Integer total = new Integer(kto.getJumlah());
//                total = total - 1;
//                kto.setJumlah(total.toString());
//                kuotaOfflineDao.save(kto);
//            }
//        }

        return "redirect:../user/profile";

    }

    @PostMapping("/bill/beasiswa")
    public String mhsBeasiswa(Authentication authentication, @RequestParam String tahun){

//        User user = currentUserService.currentUser(authentication);
//        Mahasiswa m = mahasiswaDao.findByUser(user);
//        TahunAkademik ta = tahunAkademikDao.findById(tahun).get();
//
//        String kode = null;
//        if (m.getAngkatan().equals("2018") || m.getAngkatan().equals("2019") || m.getAngkatan().equals("2021")) {
//            kode = "14";
//        } else if (m.getAngkatan().equals("2020")) {
//            kode = "40";
//        }
//
//        NilaiJenisTagihan nTagihan = nilaiJenisTagihanDao.findByJenisTagihanKodeAndTahunAkademikAndProgramAndProdiAndAngkatanAndStatusAndKategori(kode, ta, m.getIdProgram(), m.getIdProdi(), m.getAngkatan(), StatusRecord.AKTIF, StatusTagihan.OFFLINE);
//        if (nTagihan == null) {
//            System.out.println("Tidak ada nilai jenis tagihan untuk mahasiswa ini. ");
//            return "redirect:/du/form";
//        }else{
//
//            Tagihan tagihan = new Tagihan();
//            tagihan.setMahasiswa(m);
//            tagihan.setNilaiJenisTagihan(nTagihan);
//            tagihan.setKeterangan("Tagihan UKT Beasiswa");
//            tagihan.setNilaiTagihan(BigDecimal.ZERO);
//            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//            tagihan.setTahunAkademik(ta);
//            tagihan.setTanggalPembuatan(LocalDate.now());
//            tagihan.setTanggalJatuhTempo(LocalDate.now());
//            tagihan.setTanggalPenangguhan(LocalDate.now());
//            tagihan.setStatusTagihan(StatusTagihan.LUNAS);
//            tagihan.setStatus(StatusRecord.AKTIF);
//            tagihan.setLunas(true);
//            tagihanDao.save(tagihan);
//
//            Pembayaran pembayaran = new Pembayaran();
//            pembayaran.setTagihan(tagihan);
//            pembayaran.setWaktuBayar(LocalDateTime.now());
//            pembayaran.setNomorRekening("-");
//            pembayaran.setAmount(tagihan.getNilaiTagihan());
//            pembayaran.setReferensi("-");
//            pembayaran.setStatus(StatusRecord.AKTIF);
//            pembayaranDao.save(pembayaran);
//
//            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(m, StatusRecord.KRS, false, ta);
//            if (enableFiture == null) {
//                enableFiture = new EnableFiture();
//                enableFiture.setMahasiswa(m);
//                enableFiture.setTahunAkademik(ta);
//                enableFiture.setFitur(StatusRecord.KRS);
//                enableFiture.setEnable(true);
//                enableFiture.setKeterangan("-");
//            }
//            enableFiture.setEnable(true);
//            enableFitureDao.save(enableFiture);
//
//        }

        return "redirect:../user/profile";

    }

    @GetMapping("/du/kelas")
    public void getKelas(Model model,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        String angkatan = mahasiswa.getAngkatan();
        String idProdi = mahasiswa.getIdProdi().getId();

        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);

        model.addAttribute("kelasMahasiswa", kelasMahasiswa);
        model.addAttribute("kelasSelected", kelasDao.kelasAngktanProdi(idProdi, angkatan));

    }

    @PostMapping("/du/kelas")
    public String prosesKelas(Authentication authentication, @RequestParam Kelas kelas){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndKelas(mahasiswa, kelas);
        if (kelasMahasiswa != null){
            KelasMahasiswa kelasMhsw = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
            if (kelasMhsw != null){
                kelasMhsw.setStatus(StatusRecord.NONAKTIF);
                kelasMahasiswaDao.save(kelasMhsw);
            }
            kelasMahasiswa.setStatus(StatusRecord.AKTIF);
            kelasMahasiswaDao.save(kelasMahasiswa);
        }else{
            KelasMahasiswa kelasMhsw = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
            if (kelasMhsw != null){
                kelasMhsw.setStatus(StatusRecord.NONAKTIF);
                kelasMahasiswaDao.save(kelasMhsw);
            }
            KelasMahasiswa km = new KelasMahasiswa();
            km.setStatus(StatusRecord.AKTIF);
            km.setKelas(kelas);
            km.setMahasiswa(mahasiswa);
            kelasMahasiswaDao.save(km);
        }

        return "redirect:/du/alert";

    }

    @GetMapping("/pendaftaran/form")
    public void formPendaftaran(){

    }

    @GetMapping("/du/form")
    public String formDaftarUlang(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        Cuti cuti = cutiDao.findCutiByStatusAndMahasiswa(StatusRecord.AKTIF, mahasiswa);
        if (cuti == null){
            return "du/form";
        }else {
            model.addAttribute("cuti", cutiDao.findCutiByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF));
            return "redirect:/du/cuti/list";
        }
    }

//    Cuti

    @GetMapping("/du/cuti/form")
    public void formCuti(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        model.addAttribute("mahasiswa", mahasiswa);

    }

    @PostMapping("/du/cuti/form")
    public String prosesCuti(@Valid Cuti cuti, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        cuti.setMahasiswa(mahasiswa);
        cuti.setTanggalPengajuaan(LocalDate.now());
        cuti.setStatusPengajuaan("WAITING");
        cutiDao.save(cuti);

        return "redirect:/du/cuti/list";

    }

    @GetMapping("/du/cuti/list")
    public String listCuti(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        Cuti cuti = cutiDao.findCutiByStatusAndMahasiswa(StatusRecord.AKTIF, mahasiswa);
        if (cuti == null){
            return "redirect:/du/cuti/form";
        }else {
            model.addAttribute("cuti", cutiDao.findCutiByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF));
            return "/du/cuti/list";
        }

    }

//    PRA KRS SEMESTER PENDEK

    @GetMapping("/study/prakrssp")
    public void prioritasSp(Model model, @RequestParam(required = false) String[] checkBox, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.PRAAKTIF);

        model.addAttribute("cekbox", checkBox.length);

        if (checkBox.length == 1){
            model.addAttribute("cekCheckbox1", matakuliahKurikulumDao.findById(checkBox[0]).get());

            model.addAttribute("cekCheckbox1", matakuliahKurikulumDao.findById(checkBox[0]).get());
            model.addAttribute("jumlahSks1", praKrsSpDao.jumlahSks1(checkBox[0]));
            model.addAttribute("total1", bs.getBiaya().multiply(new BigDecimal(praKrsSpDao.jumlahSks1(checkBox[0]))));
            model.addAttribute("jumlahPerMatkul1", praKrsSpDao.countMahasiswaByMatakuliahKurikulumIdAndTahunAkademikAndStatus(checkBox[0], tahun, StatusRecord.AKTIF));

            Integer totalSks = praKrsSpDao.jumlahSks1(checkBox[0]);
            BigDecimal total = bs.getBiaya().multiply(new BigDecimal(totalSks));
            model.addAttribute("jumlahSks", totalSks);
            model.addAttribute("totalTagihan", total);
        }else{
            model.addAttribute("cekCheckbox1", matakuliahKurikulumDao.findById(checkBox[0]).get());
            model.addAttribute("cekCheckbox2", matakuliahKurikulumDao.findById(checkBox[1]).get());

            model.addAttribute("jumlahSks1", praKrsSpDao.jumlahSks1(checkBox[0]));
            model.addAttribute("jumlahSks2", praKrsSpDao.jumlahSks1(checkBox[1]));
            model.addAttribute("total1", bs.getBiaya().multiply(new BigDecimal(praKrsSpDao.jumlahSks1(checkBox[0]))));
            model.addAttribute("total2", bs.getBiaya().multiply(new BigDecimal(praKrsSpDao.jumlahSks1(checkBox[1]))));
            model.addAttribute("jumlahPerMatkul1", praKrsSpDao.countMahasiswaByMatakuliahKurikulumIdAndTahunAkademikAndStatus(checkBox[0], tahun, StatusRecord.AKTIF));
            model.addAttribute("jumlahPerMatkul2", praKrsSpDao.countMahasiswaByMatakuliahKurikulumIdAndTahunAkademikAndStatus(checkBox[1], tahun, StatusRecord.AKTIF));

            Integer totalSks = praKrsSpDao.jumlahSks2(checkBox[0], checkBox[1]);
            BigDecimal total = bs.getBiaya().multiply(new BigDecimal(totalSks));
            model.addAttribute("jumlahSks", totalSks);
            model.addAttribute("totalTagihan", total);
        }

        model.addAttribute("mhs", mahasiswa);
        model.addAttribute("biayaSks", biayaSksDao.findByStatus(StatusRecord.AKTIF).getBiaya());

    }

    @PostMapping("/prakrs/request/sp")
    public String requestSp(@RequestParam(required = false) String idMatkul1, @RequestParam(required = false) String idMatkul2,
                            @RequestParam(required = false) String nomorTelepon, @RequestParam(required = false) String jumlahSks,
                            @RequestParam(required = false) String jumlahMatkul, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        JenisTagihan jt = jenisTagihanDao.findByKodeAndStatus("23", StatusRecord.AKTIF);

        if (jumlahMatkul.equals("2")) {
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
            MatakuliahKurikulum mk1 = matakuliahKurikulumDao.findById(idMatkul1).get();
            MatakuliahKurikulum mk2 = matakuliahKurikulumDao.findById(idMatkul2).get();

            PraKrsSp pks1 = new PraKrsSp();
            pks1.setMahasiswa(mahasiswa);
            pks1.setMatakuliahKurikulum(mk1);
            pks1.setNomorTelepon(nomorTelepon);
            pks1.setStatus(StatusRecord.AKTIF);
            pks1.setTahunAkademik(tahunAkademik);
            praKrsSpDao.save(pks1);

            PraKrsSp pks2 = new PraKrsSp();
            pks2.setMahasiswa(mahasiswa);
            pks2.setMatakuliahKurikulum(mk2);
            pks2.setNomorTelepon(nomorTelepon);
            pks2.setStatus(StatusRecord.AKTIF);
            pks2.setTahunAkademik(tahunAkademik);
            praKrsSpDao.save(pks2);

            if (mahasiswa.getTeleponSeluler() != nomorTelepon) {
                mahasiswa.setTeleponSeluler(nomorTelepon);
                mahasiswaDao.save(mahasiswa);
            }

            NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(),
                    mahasiswa.getAngkatan(), tahunAkademik, mahasiswa.getIdProgram(), StatusRecord.AKTIF, jt);
            if (nilaiJenisTagihan == null){

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(jumlahSks));

                NilaiJenisTagihan nilaiTagihan = new NilaiJenisTagihan();
                nilaiTagihan.setJenisTagihan(jt);
                nilaiTagihan.setNilai(bs.getBiaya());
                nilaiTagihan.setTahunAkademik(tahunAkademik);
                nilaiTagihan.setProdi(mahasiswa.getIdProdi());
                nilaiTagihan.setProgram(mahasiswa.getIdProgram());
                nilaiTagihan.setAngkatan(mahasiswa.getAngkatan());
                nilaiTagihan.setStatus(StatusRecord.AKTIF);
                nilaiJenisTagihanDao.save(nilaiTagihan);

                String keteranganTagihan = "Tagihan " + nilaiTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan tagihan = new Tagihan();
                tagihan.setMahasiswa(mahasiswa);
                tagihan.setNilaiJenisTagihan(nilaiTagihan);
                tagihan.setKeterangan(keteranganTagihan);
                tagihan.setNilaiTagihan(total);
                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                tagihan.setTanggalPembuatan(LocalDate.now());
                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                tagihan.setTahunAkademik(tahunAkademik);
                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                tagihan.setStatus(StatusRecord.AKTIF);
                tagihanDao.save(tagihan);
                tagihanService.requestCreateTagihan(tagihan);

            } else{

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(jumlahSks));

                String keteranganTagihan = "Tagihan " + nilaiJenisTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, true);
                if (t != null) {
                    if (total.compareTo(t.getNilaiTagihan()) > 0) {
                        Tagihan tagihan = new Tagihan();
                        tagihan.setMahasiswa(mahasiswa);
                        tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                        tagihan.setKeterangan(keteranganTagihan);
                        tagihan.setNilaiTagihan(new BigDecimal(total.intValue() - t.getNilaiTagihan().intValue()));
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahunAkademik);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihanDao.save(tagihan);
                        tagihanService.requestCreateTagihan(tagihan);
                    }
                }else{
                    Tagihan tagihan = new Tagihan();
                    tagihan.setMahasiswa(mahasiswa);
                    tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                    tagihan.setKeterangan(keteranganTagihan);
                    tagihan.setNilaiTagihan(total);
                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                    tagihan.setTanggalPembuatan(LocalDate.now());
                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                    tagihan.setTahunAkademik(tahunAkademik);
                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                    tagihan.setStatus(StatusRecord.AKTIF);
                    tagihanDao.save(tagihan);
                    tagihanService.requestCreateTagihan(tagihan);
                }

            }
        }else{
            TahunAkademik tahunAkademik = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
            MatakuliahKurikulum mk1 = matakuliahKurikulumDao.findById(idMatkul1).get();

            PraKrsSp pks1 = new PraKrsSp();
            pks1.setMahasiswa(mahasiswa);
            pks1.setMatakuliahKurikulum(mk1);
            pks1.setNomorTelepon(nomorTelepon);
            pks1.setStatus(StatusRecord.AKTIF);
            pks1.setTahunAkademik(tahunAkademik);
            praKrsSpDao.save(pks1);

            if (mahasiswa.getTeleponSeluler() != nomorTelepon) {
                mahasiswa.setTeleponSeluler(nomorTelepon);
                mahasiswaDao.save(mahasiswa);
            }

            NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(),
                    mahasiswa.getAngkatan(), tahunAkademik, mahasiswa.getIdProgram(), StatusRecord.AKTIF, jt);
            if (nilaiJenisTagihan == null){

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(jumlahSks));

                NilaiJenisTagihan nilaiTagihan = new NilaiJenisTagihan();
                nilaiTagihan.setJenisTagihan(jt);
                nilaiTagihan.setNilai(bs.getBiaya());
                nilaiTagihan.setTahunAkademik(tahunAkademik);
                nilaiTagihan.setProdi(mahasiswa.getIdProdi());
                nilaiTagihan.setProgram(mahasiswa.getIdProgram());
                nilaiTagihan.setAngkatan(mahasiswa.getAngkatan());
                nilaiTagihan.setStatus(StatusRecord.AKTIF);
                nilaiJenisTagihanDao.save(nilaiTagihan);

                String keteranganTagihan = "Tagihan " + nilaiTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan tagihan = new Tagihan();
                tagihan.setMahasiswa(mahasiswa);
                tagihan.setNilaiJenisTagihan(nilaiTagihan);
                tagihan.setKeterangan(keteranganTagihan);
                tagihan.setNilaiTagihan(total);
                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                tagihan.setTanggalPembuatan(LocalDate.now());
                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                tagihan.setTahunAkademik(tahunAkademik);
                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                tagihan.setStatus(StatusRecord.AKTIF);
                tagihanDao.save(tagihan);
                tagihanService.requestCreateTagihan(tagihan);

            } else{

                BiayaSksSp bs = biayaSksDao.findByStatus(StatusRecord.AKTIF);
                BigDecimal total = bs.getBiaya().multiply(new BigDecimal(jumlahSks));
                List<PraKrsSp> cekSp = praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mahasiswa, StatusRecord.HAPUS, StatusApprove.HAPUS, tahunAkademik);
                System.out.println("sp kosong : " + cekSp);

                String keteranganTagihan = "Tagihan " + nilaiJenisTagihan.getJenisTagihan().getNama()
                        + " a.n. " + mahasiswa.getNama();

                Tagihan t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, false);
                if (t == null) {
                    t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, true);
                    if (t == null) {
                        Tagihan tagihan = new Tagihan();
                        tagihan.setMahasiswa(mahasiswa);
                        tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                        tagihan.setKeterangan(keteranganTagihan);
                        tagihan.setNilaiTagihan(total);
                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                        tagihan.setTanggalPembuatan(LocalDate.now());
                        tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                        tagihan.setTahunAkademik(tahunAkademik);
                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                        tagihan.setStatus(StatusRecord.AKTIF);
                        tagihanDao.save(tagihan);
                        tagihanService.requestCreateTagihan(tagihan);
                    }else{
                        if (cekSp.isEmpty() || cekSp == null) {
                            Tagihan tagihan = new Tagihan();
                            tagihan.setMahasiswa(mahasiswa);
                            tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                            tagihan.setKeterangan(keteranganTagihan);
                            tagihan.setNilaiTagihan(total);
                            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                            tagihan.setTanggalPembuatan(LocalDate.now());
                            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                            tagihan.setTahunAkademik(tahunAkademik);
                            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                            tagihan.setStatus(StatusRecord.AKTIF);
                            tagihanDao.save(tagihan);
                            tagihanService.requestCreateTagihan(tagihan);
                        }else{
                            if (total.compareTo(t.getNilaiTagihan()) > 0) {
                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mahasiswa);
                                tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
                                tagihan.setKeterangan(keteranganTagihan);
                                tagihan.setNilaiTagihan(new BigDecimal(total.intValue() - t.getNilaiTagihan().intValue()));
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahunAkademik);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihanDao.save(tagihan);
                                tagihanService.requestCreateTagihan(tagihan);
                            }

                        }
                    }
                }else{
                    t.setNilaiTagihan(t.getNilaiTagihan().add(total));
                    tagihanDao.save(t);
                    tagihanService.editTagihan(t);
                }


//                if (tg != null) {
//                    if (tg.getLunas() == false) {
//                        tg.setNilaiTagihan(tg.getNilaiTagihan().add(total));
//                        tagihanDao.save(tg);
//                        tagihanService.editTagihan(tg);
//                    } else if (tg.getLunas() == true) {
//                        List<PraKrsSp> cekSp = praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mahasiswa, StatusRecord.AKTIF, StatusApprove.WAITING, tahunAkademik);
//                        if (cekSp != null) {
//                            Tagihan tagihan = new Tagihan();
//                            tagihan.setMahasiswa(mahasiswa);
//                            tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
//                            tagihan.setKeterangan(keteranganTagihan);
//                            tagihan.setNilaiTagihan(total);
//                            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//                            tagihan.setTanggalPembuatan(LocalDate.now());
//                            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//                            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//                            tagihan.setTahunAkademik(tahunAkademik);
//                            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//                            tagihan.setStatus(StatusRecord.AKTIF);
//                            tagihanDao.save(tagihan);
//                            tagihanService.requestCreateTagihan(tagihan);
//                        }else{
//                            if (total.compareTo(tg.getNilaiTagihan()) > 0) {
//                                Tagihan tagihan = new Tagihan();
//                                tagihan.setMahasiswa(mahasiswa);
//                                tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
//                                tagihan.setKeterangan(keteranganTagihan);
//                                tagihan.setNilaiTagihan(new BigDecimal(total.intValue() - tg.getNilaiTagihan().intValue()));
//                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//                                tagihan.setTanggalPembuatan(LocalDate.now());
//                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//                                tagihan.setTahunAkademik(tahunAkademik);
//                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//                                tagihan.setStatus(StatusRecord.AKTIF);
//                                tagihanDao.save(tagihan);
//                                tagihanService.requestCreateTagihan(tagihan);
//                            }
//                        }
//                    }
//                }else{
//                    Tagihan tagihan = new Tagihan();
//                    tagihan.setMahasiswa(mahasiswa);
//                    tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
//                    tagihan.setKeterangan(keteranganTagihan);
//                    tagihan.setNilaiTagihan(total);
//                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//                    tagihan.setTanggalPembuatan(LocalDate.now());
//                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//                    tagihan.setTahunAkademik(tahunAkademik);
//                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//                    tagihan.setStatus(StatusRecord.AKTIF);
//                    tagihanDao.save(tagihan);
//                    tagihanService.requestCreateTagihan(tagihan);
//                }

//                if (tg != null) {
//                    if (total.compareTo(tg.getNilaiTagihan()) > 0) {
//                        Tagihan tagihan = new Tagihan();
//                        tagihan.setMahasiswa(mahasiswa);
//                        tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
//                        tagihan.setKeterangan(keteranganTagihan);
//                        tagihan.setNilaiTagihan(new BigDecimal(total.intValue() - tg.getNilaiTagihan().intValue()));
//                        tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//                        tagihan.setTanggalPembuatan(LocalDate.now());
//                        tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//                        tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//                        tagihan.setTahunAkademik(tahunAkademik);
//                        tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//                        tagihan.setStatus(StatusRecord.AKTIF);
//                        tagihanDao.save(tagihan);
//                        tagihanService.requestCreateTagihan(tagihan);
//                    }
//                }else{
//                    Tagihan tagihan = new Tagihan();
//                    tagihan.setMahasiswa(mahasiswa);
//                    tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
//                    tagihan.setKeterangan(keteranganTagihan);
//                    tagihan.setNilaiTagihan(total);
//                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//                    tagihan.setTanggalPembuatan(LocalDate.now());
//                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//                    tagihan.setTahunAkademik(tahunAkademik);
//                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//                    tagihan.setStatus(StatusRecord.AKTIF);
//                    tagihanDao.save(tagihan);
//                    tagihanService.requestCreateTagihan(tagihan);
//                }
//
//                Tagihan t = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, false);
//                if (t == null) {
//                    Tagihan tagihan = new Tagihan();
//                    tagihan.setMahasiswa(mahasiswa);
//                    tagihan.setNilaiJenisTagihan(nilaiJenisTagihan);
//                    tagihan.setKeterangan(keteranganTagihan);
//                    tagihan.setNilaiTagihan(total);
//                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
//                    tagihan.setTanggalPembuatan(LocalDate.now());
//                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
//                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
//                    tagihan.setTahunAkademik(tahunAkademik);
//                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
//                    tagihan.setStatus(StatusRecord.AKTIF);
//                    tagihanDao.save(tagihan);
//                    tagihanService.requestCreateTagihan(tagihan);
//                }else{
//                    t.setNilaiTagihan(t.getNilaiTagihan().add(total));
//                    tagihanDao.save(t);
//                    tagihanService.editTagihan(t);
//                }


            }
        }


        return "redirect:../../report/transcript";
    }

    @PostMapping("/prakrs/sp/refund")
    public String refund(HttpServletRequest request, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mhs = mahasiswaDao.findByUser(user);
        TahunAkademik tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.PRAAKTIF, StatusRecord.PENDEK);
        if (tahun == null) {
            tahun = tahunAkademikDao.findByStatusAndJenis(StatusRecord.AKTIF, StatusRecord.PENDEK);
        }

        List<PraKrsSp> listReject = praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(mhs, StatusRecord.AKTIF, StatusApprove.REJECTED, tahun);
        for (PraKrsSp mk : listReject ){
            String pilihan = request.getParameter("matkur-"+mk.getMatakuliahKurikulum().getId());
            if (pilihan != null && !pilihan.trim().isEmpty()) {
                Tagihan tagihan = tagihanDao.tagihanSp(mk.getMahasiswa().getId(), mk.getTahunAkademik().getId());
                Pembayaran pembayaran = pembayaranDao.findByStatusAndTagihan(StatusRecord.AKTIF, tagihan);
                RefundSp refund = new RefundSp();
                refund.setMahasiswa(mk.getMahasiswa());
                refund.setTagihan(tagihan);
                refund.setPembayaran(pembayaran);
                refund.setPraKrsSp(mk);
                refund.setNomorRekening(request.getParameter("nomorRekening-"+mk.getMatakuliahKurikulum().getId()));
                refund.setNamaBank(request.getParameter("namaBank-"+mk.getMatakuliahKurikulum().getId()));
                refund.setJumlah(new BigDecimal(request.getParameter("jumlah-"+mk.getMatakuliahKurikulum().getId())));
                refund.setNomorTelepon(request.getParameter("nomorTelepon-"+mk.getMatakuliahKurikulum().getId()));
                refund.setStatusPengembalian(StatusRecord.UNDONE);
                refundSpDao.save(refund);

            }
        }

        return "redirect:/dashboard";
    }

}
