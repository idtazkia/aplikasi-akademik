package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.dto.user.MahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Controller
public class StudyActivityController {

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private IpkDao ipkDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

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

        if (tahunAkademikProdi.getMulaiKrs().compareTo(LocalDate.now()) > 0){
            Long day = ChronoUnit.DAYS.between(LocalDate.now(),tahunAkademikProdi.getMulaiKrs());

            model.addAttribute("krs", tahunAkademikProdi);
            model.addAttribute("hari", day);
            return "study/comingsoon";

        }else {

            return "redirect:krs";

        }

    }

    @GetMapping("/study/krs")
    public void krs(Model model, Authentication authentication){

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByStatusAndProdi(StatusRecord.AKTIF, mahasiswa.getIdProdi());

//        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        TahunAkademik ta = tahunAkademikDao.findById(tahunAkademikProdi.getTahunAkademik().getId()).get();

        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);

        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta,StatusRecord.AKTIF);

        Long jumlahSks = krsDetailDao.jumlahSks(StatusRecord.AKTIF, k);

        if (jumlahSks == null){
            jumlahSks = Long.valueOf(0);
        }

        if (ta.getTanggalMulaiKrs().compareTo(LocalDate.now()) >= 0) {
            model.addAttribute("validasi", ta);
        }

        Integer semester = krsDetailDao.cariSemester(mahasiswa.getId(), ta.getId());
        Integer semesterSekarang = krsDetailDao.cariSemesterSekarang(mahasiswa.getId(), ta.getId());

        if(semester == null){
            semester = 0;
        }

        if(semesterSekarang == null){
            semesterSekarang = 0;
        }

        Integer semesterTotal = semester + semesterSekarang;

        model.addAttribute("semester", semesterTotal);
        model.addAttribute("mahasiswa", mahasiswa);
        model.addAttribute("kelas",kelasMahasiswa);
        model.addAttribute("sks", jumlahSks);
        model.addAttribute("tahunAkademikProdi", tahunAkademikProdi);

        model.addAttribute("listKrs", krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,k,mahasiswa));


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
                model.addAttribute("kosong", "21");
            }else {

                if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0) {
                    model.addAttribute("full", "23");
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
                model.addAttribute("kosong", "21");
            }else {

                if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0) {
                    model.addAttribute("full", "23");
                }
            }
            model.addAttribute("lebih", lebih);
            model.addAttribute("sks", sks);
        }



        List<Object[]> krsDetail = krsDetailDao.pilihanKrs(ta,kelasMahasiswa.getKelas(),mahasiswa.getIdProdi(),mahasiswa);
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

        Integer semester = krsDao.countSemester(nim);
        if (semester == 5){
            model.addAttribute("mahasiswa", mahasiswa);
            return "du/konsentrasi";
        }

        return "/du/kelas";



    }

    @PostMapping("/du/konsentrasi")
    public String prosesKonsentrasi(@ModelAttribute @Valid MahasiswaDto mahasiswaDto, Authentication authentication){
        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        mahasiswa.setIdKonsentrasi(mahasiswaDto.getIdKonsentrasi());
        mahasiswaDao.save(mahasiswa);

        return "redirect:/du/kelas";
    }

    @GetMapping("/du/alert")
    public void alertDu(){

    }

    @GetMapping("/du/kelas")
    public void getKelas(Model model,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
        model.addAttribute("kelasMahasiswa", kelasMahasiswa);

    }

    @GetMapping("/pendaftaran/form")
    public void formPendaftaran(){

    }


}
