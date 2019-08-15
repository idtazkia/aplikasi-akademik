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

                if (kelasMahasiswa == null){
                    List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(ta,Akses.UMUM,StatusRecord.AKTIF);
                    for (Jadwal j : jadwal){

                                List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                                if (kd.isEmpty()){

                                    if (prasyarat.isEmpty()){
                                        rekap.add(j);
                                    }else {

                                        for (Prasyarat pras : prasyarat) {
                                            KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                            if (cariPras != null) {
                                                System.out.println("prasayarat umum krs null kelas null");
                                                if (cariPras.getBobot().compareTo(pras.getNilai()) > 0) {
                                                    rekap.add(j);
                                                    break;
                                                }
                                            }

                                        }

                                    }

                                }else {

                                    for (KrsDetail krsDetail : kd) {
                                        if (krsDetail.getBobot() != null){

                                            if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                                if (prasyarat.isEmpty()){
                                                    rekap.add(j);
                                                }else {

                                                    for (Prasyarat pras : prasyarat) {
                                                        KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                                        if (cariPras != null) {
                                                            System.out.println("prasayarat umum kelas null");
                                                            if (cariPras.getBobot().compareTo(pras.getNilai()) > 0) {
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

                    List<Jadwal> jadwalProdi = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(ta,Akses.PRODI,StatusRecord.AKTIF);
                    for (Jadwal j : jadwalProdi){

                                List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                                if (kd.isEmpty()){

                                    if (prasyarat.isEmpty()){
                                        rekap.add(j);
                                    }else {

                                        for (Prasyarat prasProdi : prasyarat) {
                                            KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(prasProdi.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                            if (cariPras != null) {
                                                System.out.println("prasayarat prodi krs null kelas null");
                                                if (cariPras.getBobot().compareTo(prasProdi.getNilai()) > 0) {
                                                    rekap.add(j);
                                                    break;
                                                }
                                            }

                                        }

                                    }

                                }else {

                                    for (KrsDetail krsDetail : kd) {
                                        if (krsDetail.getBobot() != null){

                                            if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                                if (prasyarat.isEmpty()){
                                                    rekap.add(j);
                                                }else {

                                                    for (Prasyarat prasProdi : prasyarat) {
                                                        KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(prasProdi.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                                        if (cariPras != null) {
                                                            System.out.println("prasayarat prodi kelas null");
                                                            if (cariPras.getBobot().compareTo(prasProdi.getNilai()) > 0) {
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
                }




                List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(ta,Akses.UMUM,StatusRecord.AKTIF);
                for (Jadwal j : jadwal){

                    if (kelasMahasiswa != null){

                        if (j.getIdKelas() != kelasMahasiswa.getKelas()){

                            List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                            if (kd.isEmpty()){

                                if (prasyarat.isEmpty()){
                                    rekap.add(j);
                                }else {

                                    for (Prasyarat pras : prasyarat) {
                                        KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                        if (cariPras != null) {
                                            System.out.println("prasayarat umum krs null");
                                            if (cariPras.getBobot().compareTo(pras.getNilai()) > 0) {
                                                rekap.add(j);
                                                break;
                                            }
                                        }

                                    }

                                }

                            }else {

                                for (KrsDetail krsDetail : kd) {
                                    if (krsDetail.getBobot() != null){

                                        if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                            if (prasyarat.isEmpty()){
                                                rekap.add(j);
                                            }else {

                                                for (Prasyarat pras : prasyarat) {
                                                    KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                                    if (cariPras != null) {
                                                        System.out.println("prasayarat umum");
                                                        if (cariPras.getBobot().compareTo(pras.getNilai()) > 0) {
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
                    }
                }

                List<Jadwal> jadwalProdi = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(ta,Akses.PRODI,StatusRecord.AKTIF);
                for (Jadwal j : jadwalProdi){

                    if (kelasMahasiswa != null){

                        if (j.getIdKelas() != kelasMahasiswa.getKelas()){

                            List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                            if (kd.isEmpty()){

                                if (prasyarat.isEmpty()){
                                    rekap.add(j);
                                }else {

                                    for (Prasyarat prasProdi : prasyarat) {
                                        KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(prasProdi.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                        if (cariPras != null) {
                                            System.out.println("prasayarat prodi krs null");
                                            if (cariPras.getBobot().compareTo(prasProdi.getNilai()) > 0) {
                                                rekap.add(j);
                                                break;
                                            }
                                        }

                                    }

                                }

                            }else {

                                for (KrsDetail krsDetail : kd) {
                                    if (krsDetail.getBobot() != null){

                                        if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                            if (prasyarat.isEmpty()){
                                                rekap.add(j);
                                            }else {

                                                for (Prasyarat prasProdi : prasyarat) {
                                                    KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(prasProdi.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                                    if (cariPras != null) {
                                                        System.out.println("prasayarat prodi");
                                                        if (cariPras.getBobot().compareTo(prasProdi.getNilai()) > 0) {
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
                    }
                }
                if (kelasMahasiswa != null) {
                List<Jadwal> jadwalKelas = jadwalDao.findByTahunAkademikAndIdKelasAndStatusAndIdHariNotNull(ta, kelasMahasiswa.getKelas(), StatusRecord.AKTIF);
                if (!jadwalKelas.isEmpty()) {
                    for (Jadwal j : jadwalKelas) {

                            List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(), StatusRecord.AKTIF, mahasiswa);
                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                            if (kd.isEmpty()) {

                                if (prasyarat.isEmpty()) {
                                    rekap.add(j);
                                } else {

                                    for (Prasyarat prasKelas : prasyarat) {
                                        KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(prasKelas.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                        if (cariPras != null) {
                                            System.out.println("prasayarat kelas krs null");
                                            if (cariPras.getBobot().compareTo(prasKelas.getNilai()) > 0) {
                                                rekap.add(j);
                                                break;
                                            }
                                        }

                                    }

                                }

                            } else {

                                for (KrsDetail krsDetail : kd) {
                                    if (krsDetail.getBobot() != null) {

                                        if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0) {

                                            if (prasyarat.isEmpty()) {
                                                rekap.add(j);
                                            } else {

                                                for (Prasyarat prasKelas : prasyarat) {
                                                    KrsDetail cariPras = krsDetailDao.findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(prasKelas.getMatakuliahKurikulumPras(), mahasiswa, StatusRecord.AKTIF, k);

                                                    if (cariPras != null) {
                                                        System.out.println("prasayarat kelas");
                                                        if (cariPras.getBobot().compareTo(prasKelas.getNilai()) > 0) {
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
                    }
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
        Grade grade = gradeDao.findById("8").get();


//        Buat Semester Pendek
        if (tahunAkademik.getJenis() == StatusRecord.PENDEK) {
            if (data != null){
                if (krsDetails.size() < 2) {
                    for (String idJadwal : data) {
                        System.out.println(idJadwal);
                        int total = data.length + krsDetails.size();
                        Jadwal jadwal = jadwalDao.findById(idJadwal).get();
                        System.out.println("total :  " + total);
                        List<KrsDetail> krs = krsDetailDao.findByJadwalAndStatusAndKrsTahunAkademik(jadwal,StatusRecord.AKTIF,tahunAkademik);
                        System.out.println("kapasitas ruang  : "  +jadwal.getRuangan().getKapasitas().intValue());
                        System.out.println(krs.size() +  "   jumlaaah");
                            if (total <= 2) {
                                if (data.length + krs.size() < jadwal.getRuangan().getKapasitas().intValue()){
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
        id.setStatus(StatusRecord.HAPUS);
        krsDetailDao.save(id);

        return "redirect:/menumahasiswa/krs/list";
    }



}