package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.Kartu;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.util.*;

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
    @Autowired
    private IpkDao ipkDao;
    @Autowired
    private PresensiDosenDao presensiDosenDao;
    @Autowired
    private EnableFitureDao enableFitureDao;
    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @GetMapping("/api/sum")
    @ResponseBody
    public void sum(@RequestParam String sks,@RequestParam String hasil){
        int awal = Integer.parseInt(sks);
        int result = Integer.parseInt(hasil);

        System.out.println(awal+result);



    }

    @GetMapping("/api/krs")
    @ResponseBody
    public Integer ipk(Authentication authentication,Model model){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs krs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa);
        List<Integer> sksDiambil = new ArrayList<>();
        List<KrsDetail> krsDetail = krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF, krs, mahasiswa);
        for (KrsDetail kd : krsDetail){
            sksDiambil.add(kd.getMatakuliahKurikulum().getJumlahSks());
        }
        int sum = sksDiambil.stream().mapToInt(Integer::intValue).sum();

        if (tahunAkademik.getJenis() == StatusRecord.PENDEK){
            System.out.println("pendek");
            System.out.println(krsDetail.size());

            if (krsDetail.isEmpty()) {
                return new Integer(2);
            }else {
                return new Integer(2) -sum;
            }

        }else {
            Ipk ipk = ipkDao.findByMahasiswa(mahasiswa);
            System.out.println(ipk.getIpk().toBigInteger().intValue());
            System.out.println(new BigDecimal(3.00).toBigInteger().intValue());
            if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0){
                if (krsDetail.isEmpty()) {
                    return new Integer(24);

                }else {
                    return new Integer(24) - sum;
                }
            }

            if (krsDetail.isEmpty()) {
                return new Integer(21);
            }else {
                return new Integer(21) -sum;
            }
        }

    }

    @ModelAttribute("listTahunAkademik")
    public Iterable<TahunAkademik> daftarKonfig() {
        return tahunAkademikDao.findByStatusNotInOrderByNamaTahunAkademikDesc(StatusRecord.HAPUS);
    }


    @GetMapping("/menumahasiswa/krs/list")
    public void daftarKRS(Model model, Authentication authentication,Pageable page,
                          @RequestParam(required = false) TahunAkademik tahunAkademik){
        System.out.println(authentication);
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa,ta);
        List<Integer> sksDiambil = new ArrayList<>();
        List<KrsDetail> terpilih = krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF, k, mahasiswa);
        for (KrsDetail kd : terpilih){
            sksDiambil.add(kd.getMatakuliahKurikulum().getJumlahSks());
        }
        Map<String, Jadwal> matkulYangBisaDipilih = new LinkedHashMap<>();
        Grade grade = gradeDao.findById("8").get();
        model.addAttribute("tahun", ta);
        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);

        System.out.println("jumlah krs" + terpilih.size());

        if (k!= null && LocalDate.now().compareTo(ta.getTanggalMulaiKrs()) >= 0 == true && LocalDate.now().compareTo(ta.getTanggalSelesaiKrs()) <= 0 == true) {
            model.addAttribute("krsAktif", k);

            if (kelasMahasiswa == null){
                List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndAksesAndStatusAndHariNotNull(ta,Akses.UMUM,StatusRecord.AKTIF);
                for (Jadwal j : jadwal){

                    List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                    List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                    if (kd.isEmpty()){

                        if (prasyarat.isEmpty()){
                            matkulYangBisaDipilih.put(j.getId(), j);
                        }else {

                            for (Prasyarat pras : prasyarat) {
                                List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                if (cariPras != null) {
                                    for (KrsDetail prasList : cariPras) {
                                        System.out.println("prasayarat umum krs null kelas null");
                                        if (prasList.getBobot() != null){
                                            if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                matkulYangBisaDipilih.put(j.getId(), j);
                                                break;
                                            }
                                        }
                                    }
                                }

                            }

                        }

                    }else {

                        for (KrsDetail krsDetail : kd) {
                            if (krsDetail.getBobot() != null){

                                if (krsDetail.getBobot().compareTo(grade.getBobot()) <= 0){

                                    if (prasyarat.isEmpty()){
                                        matkulYangBisaDipilih.put(j.getId(), j);
                                    }else {

                                        for (Prasyarat pras : prasyarat) {
                                            List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                            if (cariPras != null) {
                                                for (KrsDetail prasList : cariPras) {
                                                    System.out.println("prasayarat umum kelas null");
                                                    if (prasList.getBobot() != null){
                                                        if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                            matkulYangBisaDipilih.put(j.getId(), j);
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

                List<Jadwal> jadwalProdi = jadwalDao.findByTahunAkademikAndProdiAndAksesAndStatusAndHariNotNull(ta,mahasiswa.getIdProdi(),Akses.PRODI,StatusRecord.AKTIF);
                for (Jadwal j : jadwalProdi){

                    List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                    List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                    if (kd.isEmpty()){

                        if (prasyarat.isEmpty()){
                            matkulYangBisaDipilih.put(j.getId(), j);
                        }else {

                            for (Prasyarat pras : prasyarat) {
                                List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                if (cariPras != null) {
                                    for (KrsDetail prasList : cariPras) {
                                        System.out.println("prasayarat prodi krs null kelas null");
                                        if (prasList.getBobot() != null){
                                            if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                matkulYangBisaDipilih.put(j.getId(), j);
                                                break;
                                            }
                                        }
                                    }
                                }

                            }

                        }

                    }else {

                        for (KrsDetail krsDetail : kd) {
                            if (krsDetail.getBobot() != null){

                                if (krsDetail.getBobot().compareTo(grade.getBobot()) <= 0){

                                    if (prasyarat.isEmpty()){
                                        matkulYangBisaDipilih.put(j.getId(), j);
                                    }else {

                                        for (Prasyarat pras : prasyarat) {
                                            List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                            if (cariPras != null) {
                                                for (KrsDetail prasList : cariPras) {
                                                    System.out.println("prasayarat prodi kelas null");
                                                    if (prasList.getBobot() != null){
                                                        if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                            matkulYangBisaDipilih.put(j.getId(), j);
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




            List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndAksesAndStatusAndHariNotNull(ta,Akses.UMUM,StatusRecord.AKTIF);
            for (Jadwal j : jadwal){

                if (kelasMahasiswa != null){

                    if (j.getKelas() != kelasMahasiswa.getKelas()){

                        List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                        List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                        if (kd.isEmpty()){

                            if (prasyarat.isEmpty()){
                                matkulYangBisaDipilih.put(j.getId(), j);
                            }else {

                                for (Prasyarat pras : prasyarat) {
                                    List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                    if (cariPras != null) {
                                        for (KrsDetail prasList : cariPras) {
                                            System.out.println("prasayarat umum krs null");
                                            if (prasList.getBobot() != null){
                                                if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                    matkulYangBisaDipilih.put(j.getId(), j);
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                }

                            }

                        }else {

                            for (KrsDetail krsDetail : kd) {
                                if (krsDetail.getBobot() != null){

                                    if (krsDetail.getBobot().compareTo(grade.getBobot()) <= 0){

                                        if (prasyarat.isEmpty()){
                                            matkulYangBisaDipilih.put(j.getId(), j);
                                        }else {

                                            for (Prasyarat pras : prasyarat) {
                                                List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                                if (cariPras != null) {
                                                    for (KrsDetail prasList : cariPras) {
                                                        System.out.println("prasayarat umum");
                                                        if (prasList.getBobot() != null){
                                                            if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                                matkulYangBisaDipilih.put(j.getId(), j);
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
            }

            List<Jadwal> jadwalProdi = jadwalDao.findByTahunAkademikAndProdiAndAksesAndStatusAndHariNotNull(ta,mahasiswa.getIdProdi(),Akses.PRODI,StatusRecord.AKTIF);
            for (Jadwal j : jadwalProdi){

                if (kelasMahasiswa != null){

                    if (j.getKelas() != kelasMahasiswa.getKelas()){

                        List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                        List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                        if (kd.isEmpty()){

                            if (prasyarat.isEmpty()){
                                matkulYangBisaDipilih.put(j.getId(), j);
                            }else {

                                for (Prasyarat pras : prasyarat) {
                                    List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                    if (cariPras != null) {
                                        for (KrsDetail prasList : cariPras) {
                                            System.out.println("prasayarat prodi krs null");
                                            if (prasList.getBobot() != null){
                                                if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                    matkulYangBisaDipilih.put(j.getId(), j);
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                }

                            }

                        }else {

                            for (KrsDetail krsDetail : kd) {
                                if (krsDetail.getBobot() != null){

                                    if (krsDetail.getBobot().compareTo(grade.getBobot()) <= 0){

                                        if (prasyarat.isEmpty()){
                                            matkulYangBisaDipilih.put(j.getId(), j);
                                        }else {

                                            for (Prasyarat pras : prasyarat) {
                                                List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                                if (cariPras != null) {
                                                    for (KrsDetail prasList : cariPras) {
                                                        System.out.println("prasayarat prodi");
                                                        if (prasList.getBobot() != null){
                                                            if (prasList.getBobot().compareTo(pras.getNilai()) >= 0) {
                                                                matkulYangBisaDipilih.put(j.getId(), j);
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
            }


            if (kelasMahasiswa != null) {
                List<Jadwal> jadwalKelas = jadwalDao.findByTahunAkademikAndKelasAndStatusAndHariNotNull(ta, kelasMahasiswa.getKelas(), StatusRecord.AKTIF);
                if (!jadwalKelas.isEmpty()) {
                    for (Jadwal j : jadwalKelas) {

                        List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(), StatusRecord.AKTIF, mahasiswa);
                        List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                        if (kd.isEmpty()) {

                            if (prasyarat.isEmpty()) {
                                matkulYangBisaDipilih.put(j.getId(), j);
                            } else {

                                for (Prasyarat pras : prasyarat) {
                                    List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                    if (cariPras != null) {
                                        for (KrsDetail prasList : cariPras) {
                                            System.out.println("prasayarat kelas krs null");
                                            if (prasList.getBobot() != null){
                                                if (prasList.getBobot().toBigInteger().intValue() >= pras.getNilai().toBigInteger().intValue()) {
                                                    matkulYangBisaDipilih.put(j.getId(), j);
                                                    break;
                                                }
                                            }
                                        }
                                    }

                                }

                            }

                        } else {

                            for (KrsDetail krsDetail : kd) {
                                if (krsDetail.getBobot() != null) {

                                    if (krsDetail.getBobot().toBigInteger().intValue() <= grade.getBobot().toBigInteger().intValue()) {

                                        if (prasyarat.isEmpty()) {
                                            matkulYangBisaDipilih.put(j.getId(), j);
                                        } else {

                                            for (Prasyarat pras : prasyarat) {
                                                List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,k);

                                                if (cariPras != null) {
                                                    for (KrsDetail prasList : cariPras) {
                                                        System.out.println("prasayarat kelas");
                                                        if (prasList.getBobot() != null){
                                                            if (prasList.getBobot().toBigInteger().intValue() >= pras.getNilai().toBigInteger().intValue()) {
                                                                matkulYangBisaDipilih.put(j.getId(), j);
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
            }




        }

        if (ta.getJenis() == StatusRecord.PENDEK){
            model.addAttribute("jumlah", 2);
            model.addAttribute("diambil", terpilih.size());

        }else{
            Ipk ipk = ipkDao.findByMahasiswa(mahasiswa);
            if (ipk.getIpk().compareTo(new BigDecimal(3.00)) >= 0){
                model.addAttribute("jumlah", 24);
                int sum = sksDiambil.stream().mapToInt(Integer::intValue).sum();
                model.addAttribute("diambil", sum);
            }else {
                model.addAttribute("jumlah", 21 );
                int sum = sksDiambil.stream().mapToInt(Integer::intValue).sum();
                model.addAttribute("diambil", sum);
            }
        }

        model.addAttribute("mahasiswa",mahasiswa);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF,mahasiswa.getIdProdi());


        model.addAttribute("jadwal",matkulYangBisaDipilih.values());

        if (tahunAkademik != null){
            model.addAttribute("search", tahunAkademik);
            Krs krs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa);
            model.addAttribute("krs",krs);
            EnableFiture utsFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa,StatusRecord.UTS,"1",tahunAkademik);

            model.addAttribute("uts",utsFiture);
            model.addAttribute("data",krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs,mahasiswa));
        }else {
            Krs krs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF,mahasiswa);
            model.addAttribute("krs",krs);
            EnableFiture utsFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mahasiswa,StatusRecord.UTS,"1",tahunAkademikDao.findByStatus(StatusRecord.AKTIF));

            model.addAttribute("uts",utsFiture);

            model.addAttribute("data",krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs,mahasiswa));

        }



    }

    @PostMapping("/proses/krs")
    public String addKrs(@RequestParam String[] data, Authentication authentication, RedirectAttributes attributes) {
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        TahunAkademikProdi tahunProdi = tahunAkademikProdiDao.findByTahunAkademikStatusAndProdi(StatusRecord.AKTIF, mahasiswa.getIdProdi());

        Krs cariKrs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF, mahasiswa);
        List<KrsDetail> krsDetails = krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF, cariKrs, mahasiswa);
        Grade grade = gradeDao.findById("8").get();

        List<Integer> sksDiambil = new ArrayList<>();
        for (KrsDetail kd : krsDetails){
            sksDiambil.add(kd.getMatakuliahKurikulum().getJumlahSks());
        }
        int sum = sksDiambil.stream().mapToInt(Integer::intValue).sum();


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
                                kd.setJumlahMangkir(0);
                                kd.setJumlahKehadiran(0);
                                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                kd.setJumlahTerlambat(0);
                                kd.setJumlahIzin(0);
                                kd.setJumlahSakit(0);
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
//            Semeseter Genap/Ganjil
        }else {
            Ipk ipk = ipkDao.findByMahasiswa(mahasiswa);
            if (ipk.getIpk().toBigInteger().intValue() > new BigDecimal(3.00).toBigInteger().intValue()){

                if (data != null){
                    if (sum < 24) {
                        for (String idJadwal : data) {
                            System.out.println(idJadwal);
                            int total = data.length + krsDetails.size();
                            Jadwal jadwal = jadwalDao.findById(idJadwal).get();
                            System.out.println("total :  " + total);
                            List<KrsDetail> krs = krsDetailDao.findByJadwalAndStatusAndKrsTahunAkademik(jadwal,StatusRecord.AKTIF,tahunAkademik);
                            System.out.println("kapasitas ruang  : "  +jadwal.getRuangan().getKapasitas().intValue());
                            System.out.println(krs.size() +  "   jumlaaah");
                            if (total <= 25) {
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
                                    kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setJumlahSakit(0);
                                    kd.setJumlahKehadiran(0);
                                    kd.setJumlahMangkir(0);
                                    kd.setJumlahTerlambat(0);
                                    kd.setJumlahIzin(0);
                                    krsDetailDao.save(kd);

                                }else {
                                    attributes.addFlashAttribute("batasRuang", jadwal);
                                }
                            } else {
                                System.out.println("batasnya 25");
                            }


                        }
                    }

                    if (sum == 24 || sum > 24) {
                        System.out.printf("Anda tidak bisa mengambil lagi matakuliah");
                    }

                } else {


                }

            }else {
                if (data != null){
                    if (sum < 21) {
                        for (String idJadwal : data) {
                            System.out.println(idJadwal);
                            int total = data.length + krsDetails.size();
                            Jadwal jadwal = jadwalDao.findById(idJadwal).get();
                            System.out.println("total :  " + total);
                            List<KrsDetail> krs = krsDetailDao.findByJadwalAndStatusAndKrsTahunAkademik(jadwal,StatusRecord.AKTIF,tahunAkademik);
                            System.out.println("kapasitas ruang  : "  +jadwal.getRuangan().getKapasitas().intValue());
                            System.out.println(krs.size() +  "   jumlaaah");
                            if (total <= 23) {
                                if (data.length + krs.size() < jadwal.getRuangan().getKapasitas().intValue()){

                                    KrsDetail kd = new KrsDetail();
                                    kd.setJadwal(jadwal);
                                    kd.setKrs(cariKrs);
                                    kd.setMahasiswa(mahasiswa);
                                    kd.setMatakuliahKurikulum(jadwal.getMatakuliahKurikulum());
                                    kd.setNilaiPresensi(BigDecimal.ZERO);
                                    kd.setNilaiTugas(BigDecimal.ZERO);
                                    kd.setNilaiUas(BigDecimal.ZERO);
                                    kd.setJumlahSakit(0);
                                    kd.setNilaiUts(BigDecimal.ZERO);
                                    kd.setFinalisasi("N");
                                    kd.setJumlahKehadiran(0);
                                    kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
                                    kd.setJumlahMangkir(0);
                                    kd.setJumlahTerlambat(0);
                                    kd.setJumlahIzin(0);
                                    krsDetailDao.save(kd);

                                }else {
                                    attributes.addFlashAttribute("batasRuang", jadwal);
                                }
                            } else {
                                System.out.println("batasnya 23 ");
                            }


                        }
                    }

                    if (sum == 21 || sum > 21) {
                        System.out.printf("Anda tidak bisa mengambil lagi matakuliah");
                    }

                } else {


                }
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

    @GetMapping("/menumahasiswa/krs/kartu")
    public void kartu(Model model,Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa",mahasiswa);


        Krs krs = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa,tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        List<KrsDetail> krsDetail = krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs,mahasiswa);
        List<Kartu> kartus = new ArrayList<>();

        for (KrsDetail kd : krsDetail){
            List<PresensiDosen> presensiDosen = presensiDosenDao.findByStatusAndJadwal(StatusRecord.AKTIF,kd.getJadwal());
            Long presensiMahasiswa = presensiMahasiswaDao.hitungAbsen(kd,StatusRecord.AKTIF,StatusPresensi.TERLAMBAT,StatusPresensi.MANGKIR);
            Integer absen = Math.toIntExact(presensiDosen.size() - presensiMahasiswa);

            if (absen > 3){
                LOGGER.info("Tidak masuk lebih dari 3");
            }else {
                Kartu kartu = new Kartu();
                kartu.setMatakuliah(kd.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
                kartu.setIdUjian(kd.getKodeUts());
                kartus.add(kartu);
            }

            model.addAttribute("kartu",kartus);
            model.addAttribute("tahun",tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            model.addAttribute("bulan",LocalDate.now().getMonth());
            model.addAttribute("tanggal",LocalDate.now().getLong(ChronoField.DAY_OF_MONTH));
            model.addAttribute("tahun",LocalDate.now().getLong(ChronoField.YEAR));

        }



    }



}