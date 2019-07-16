package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class KrsMahasiswaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KrsMahasiswaController.class);

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private KrsDao krsDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private PrasyaratDao prasyaratDao;
    @Autowired
    private GradeDao gradeDao;
    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @ModelAttribute("listTahunAkademik")
    public Iterable<TahunAkademik> daftarKonfig() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS);
    }


    @GetMapping("/menumahasiswa/krs/list")
    public void daftarKRS(Model model, Authentication authentication,Pageable page,
                          @RequestParam(required = false) TahunAkademik tahunAkademik){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa,ta);
        List<Jadwal> rekap = new ArrayList<>();
        Grade grade = gradeDao.findById("8").get();
        model.addAttribute("tahun", ta);
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);


        if (k!= null && LocalDate.now().compareTo(ta.getTanggalMulaiKrs()) >= 0 == true && LocalDate.now().compareTo(ta.getTanggalSelesaiKrs()) <= 0 == true) {
            model.addAttribute("krsAktif", k);


            List<Jadwal> jadwalUmum = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(ta,Akses.UMUM,StatusRecord.AKTIF);
            if (jadwalUmum != null){
                if (kelasMahasiswa != null){
                    for (Jadwal jadwal : jadwalUmum){
                        if (jadwal.getIdKelas() != kelasMahasiswa.getKelas()) {
                                KrsDetail kd = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(jadwal.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);
                            if (kd == null){
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(jadwal.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                if (prasyarat.isEmpty()) {
                                    System.out.printf("gaada pras");
                                    rekap.add(jadwal);
                                } else {
                                    for (Prasyarat pras : prasyarat) {
                                        System.out.println(pras.getMatakuliahPras().getNamaMatakuliah());
                                        KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);
                                        if (krsDetail != null) {
                                            if (krsDetail.getBobot().compareTo(pras.getNilai()) > 0) {
                                                rekap.add(jadwal);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (kd != null){
                                if (kd.getBobot() != null){
                                    if (kd.getBobot().compareTo(grade.getBobot()) < 0){
                                        List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(jadwal.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                        if (prasyarat.isEmpty()) {
                                            System.out.printf("gaada pras");
                                            rekap.add(jadwal);
                                        } else {
                                            for (Prasyarat pras : prasyarat) {
                                                KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);
                                                System.out.println(pras.getMatakuliahPras().getNamaMatakuliah());
                                                if (krsDetail != null) {
                                                    if (krsDetail.getBobot().compareTo(pras.getNilai()) > 0) {
                                                        rekap.add(jadwal);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }

            List<Jadwal> JadwalProdi = jadwalDao.findByTahunAkademikAndProdiAndAksesAndStatusAndIdHariNotNull(ta,mahasiswa.getIdProdi(),Akses.PRODI,StatusRecord.AKTIF);
            if (JadwalProdi != null){
                if (kelasMahasiswa != null){
                    for (Jadwal jadwal : JadwalProdi){
                        if (jadwal.getIdKelas() != kelasMahasiswa.getKelas()) {
                            KrsDetail kd = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(jadwal.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);
                            if (kd == null){
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(jadwal.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                if (prasyarat.isEmpty()) {
                                    System.out.printf("gaada pras");
                                    rekap.add(jadwal);
                                } else {
                                    for (Prasyarat pras : prasyarat) {
                                        System.out.println(pras.getMatakuliahPras().getNamaMatakuliah());
                                        KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);
                                        if (krsDetail != null) {
                                            if (krsDetail.getBobot().compareTo(pras.getNilai()) > 0) {
                                                rekap.add(jadwal);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            if (kd != null){
                                if (kd.getBobot() != null){
                                    if (kd.getBobot().compareTo(grade.getBobot()) < 0){
                                        List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(jadwal.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                        if (prasyarat.isEmpty()) {
                                            rekap.add(jadwal);
                                        } else {
                                            for (Prasyarat pras : prasyarat) {
                                                KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);
                                                if (krsDetail != null) {
                                                    if (krsDetail.getBobot().compareTo(pras.getNilai()) > 0) {
                                                        rekap.add(jadwal);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }

            if (kelasMahasiswa != null) {
                List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndIdKelasAndStatusAndIdHariNotNull(ta, kelasMahasiswa.getKelas(), StatusRecord.AKTIF);

                if (jadwal != null){
                    for (Jadwal j : jadwal){
                        KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(j.getMatakuliahKurikulum(),mahasiswa,StatusRecord.AKTIF);
                        if (krsDetail == null){
                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                            if (prasyarat.isEmpty()) {
                                System.out.printf("gaada pras");
                                rekap.add(j);
                            }

                            if (!prasyarat.isEmpty()){
                                for (Prasyarat pras : prasyarat) {
                                    KrsDetail kd = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);
                                    if (kd != null) {
                                        if (kd.getBobot().compareTo(pras.getNilai()) > 0) {
                                            rekap.add(j);
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (krsDetail != null){
                            if (krsDetail.getBobot() != null){
                                if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){
                                    List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);
                                    if (prasyarat.isEmpty()) {
                                        System.out.printf("gaada pras");
                                        rekap.add(j);
                                    } else {
                                        for (Prasyarat pras : prasyarat) {
                                            KrsDetail kd = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);
                                            if (kd != null) {
                                                if (kd.getBobot().compareTo(pras.getNilai()) > 0) {
                                                    System.out.printf("bisa mengambil matkul");
                                                    rekap.add(j);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }

                    }
                }

                System.out.println("jadwal size   :  "  + jadwal.size());
            }



        }
        model.addAttribute("mahasiswa",mahasiswa);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF,mahasiswa.getIdProdi());



        model.addAttribute("jadwal",rekap);

        if (tahunAkademik != null){
            model.addAttribute("search", tahunAkademik);
            Krs krs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa);
            model.addAttribute("krs",krs);


            model.addAttribute("data",krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs,mahasiswa));
        }else {
            Krs krs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF,mahasiswa);
            model.addAttribute("krs",krs);

            model.addAttribute("data",krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs,mahasiswa));

        }



    }

    @PostMapping("/proses/krs")
    public String addKrs(@RequestParam String[] data, Authentication authentication, RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF, mahasiswa.getIdProdi());

        Krs cariKrs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF, mahasiswa);
        List<KrsDetail> krsDetails = krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF, cariKrs, mahasiswa);

//        Buat Semester Pendek
        if (tahunAkademik.getJenis() == StatusRecord.PENDEK) {
            if (data != null){
                if (krsDetails.size() < 2) {
                    for (String idJadwal : data) {
                        System.out.println(idJadwal);
                        int total = data.length + krsDetails.size();
                        Jadwal jadwal = jadwalDao.findById(idJadwal).get();
                        KrsDetail krsDetail = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatus(jadwal.getMatakuliahKurikulum(), mahasiswa, StatusRecord.AKTIF);
                        System.out.println("total :  " + total);
                        List<KrsDetail> krs = krsDetailDao.findByJadwalAndStatusAndKrsTahunAkademik(jadwal,StatusRecord.AKTIF,tahunAkademik);
                        System.out.println("kapasitas ruang  : "  +jadwal.getRuangan().getKapasitas().intValue());
                        System.out.println(krs.size() +  "   jumlaaah");
                        if (krsDetail == null) {
                            if (total <= 2) {
                                if (46 + krs.size() < jadwal.getRuangan().getKapasitas().intValue()){
                                    KrsDetail kd = new KrsDetail();
                                    kd.setJadwal(jadwal);
                                    kd.setKrs(cariKrs);
                                    kd.setMahasiswa(mahasiswa);
                                    kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                                    kd.setNilaiPresensi(BigDecimal.ZERO);
                                    kd.setNilaiTugas(BigDecimal.ZERO);
                                    kd.setNilaiUas(BigDecimal.ZERO);
                                    kd.setNilaiUts(BigDecimal.ZERO);
                                    kd.setFinalisasi("N");
                                    krsDetailDao.save(kd);
                                 }else {
                                    attributes.addFlashAttribute("batasRuang", jadwal);
                                }
                            } else {
                                System.out.println("batasnya 2");
                            }
                        } else {
                            System.out.println("sudah ada");
                        }

                    }
                }

                if (krsDetails.size() == 2 || krsDetails.size() > 2) {
                    System.out.printf("Anda tidak bisa mengambil lagi matakuliah");
                }

            } else {


            }
        }

        return "redirect:/menumahasiswa/krs/list";
    }

    @PostMapping("/krs/delete")
    public String deleteKrs (@RequestParam KrsDetail id){
        krsDetailDao.delete(id);

        return "redirect:/menumahasiswa/krs/list";
    }



}