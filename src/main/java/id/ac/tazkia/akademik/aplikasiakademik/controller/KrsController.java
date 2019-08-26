package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class KrsController {
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
    private KrsDetailDao krsDetailDao;
    @Autowired
    private ProgramDao programDao;
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private GradeDao gradeDao;
    @Autowired
    private PrasyaratDao prasyaratDao;
    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;
    @Autowired
    private IpkDao ipkDao;

    @ModelAttribute("tahun")
    public Iterable<TahunAkademik> tahun() {
        return tahunAkademikDao.findByStatusNotInOrderByTahunDesc(StatusRecord.HAPUS);
    }

    @GetMapping("/api/validasiKrs")
    @ResponseBody
    public Integer ipk(String nim,String tahun){
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(tahun).get();
        System.out.println(tahunAkademik.getNamaTahunAkademik());
        List<KrsDetail> krsDetail = krsDetailDao.findByMahasiswaAndKrsTahunAkademikAndStatus(mahasiswa,tahunAkademik,StatusRecord.AKTIF);
        System.out.println(krsDetail.size());

        if (tahunAkademik.getJenis() == StatusRecord.PENDEK){

            if (krsDetail.isEmpty()) {
                return new Integer(2);
            }else {
                return new Integer(2) - krsDetail.size();
            }

        }else {
            Ipk ipk = ipkDao.findByMahasiswa(mahasiswa);
            if (ipk.getIpk().toBigInteger().intValue() > new BigDecimal(3.00).toBigInteger().intValue()){
                if (krsDetail.isEmpty()) {
                    return new Integer(25);
                }else {
                    return new Integer(25) - krsDetail.size();
                }
            }

            if (krsDetail.isEmpty()) {
                return new Integer(23);
            }else {
                return new Integer(23) - krsDetail.size();
            }
        }

    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @GetMapping("/krs/list")
    public void krsList(@RequestParam(required = false)String nim, @RequestParam(required = false) TahunAkademik tahunAkademik, Model model, Pageable page){

        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByKodeTahunAkademikDesc(StatusRecord.HAPUS));

        if (nim != null && tahunAkademik != null) {
            model.addAttribute("nim",nim);
            model.addAttribute("tahun",tahunAkademik);


            Grade grade = gradeDao.findById("8").get();
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            if (mahasiswa != null) {
                model.addAttribute("mahasiswa", mahasiswa);
                KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);

                List<Jadwal> rekap = new ArrayList<>();
                Krs krs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa);

                if (krs!= null && LocalDate.now().compareTo(tahunAkademik.getTanggalMulaiKrs()) >= 0 == true && LocalDate.now().compareTo(tahunAkademik.getTanggalSelesaiKrs()) <= 0 == true) {
                    model.addAttribute("krsAktif", krs);

                    if (kelasMahasiswa == null){
                        List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(tahunAkademik,Akses.UMUM,StatusRecord.AKTIF);
                        for (Jadwal j : jadwal){

                            List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                            if (kd.isEmpty()){

                                if (prasyarat.isEmpty()){
                                    rekap.add(j);
                                }else {

                                    for (Prasyarat pras : prasyarat) {
                                        List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                        if (cariPras != null) {
                                            for (KrsDetail prasList : cariPras) {
                                                System.out.println("prasayarat umum krs null kelas null");
                                                if (prasList.getBobot() != null){
                                                    if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
                                                        rekap.add(j);
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

                                        if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                            if (prasyarat.isEmpty()){
                                                rekap.add(j);
                                            }else {

                                                for (Prasyarat pras : prasyarat) {
                                                    List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                                    if (cariPras != null) {
                                                        for (KrsDetail prasList : cariPras) {
                                                            System.out.println("prasayarat umum kelas null");
                                                            if (prasList.getBobot() != null){
                                                                if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
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

                        List<Jadwal> jadwalProdi = jadwalDao.findByTahunAkademikAndProdiAndAksesAndStatusAndIdHariNotNull(tahunAkademik,mahasiswa.getIdProdi(),Akses.PRODI,StatusRecord.AKTIF);
                        for (Jadwal j : jadwalProdi){

                            List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                            List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                            if (kd.isEmpty()){

                                if (prasyarat.isEmpty()){
                                    rekap.add(j);
                                }else {

                                    for (Prasyarat pras : prasyarat) {
                                        List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                        if (cariPras != null) {
                                            for (KrsDetail prasList : cariPras) {
                                                System.out.println("prasayarat prodi krs null kelas null");
                                                if (prasList.getBobot() != null){
                                                    if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
                                                        rekap.add(j);
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

                                        if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                            if (prasyarat.isEmpty()){
                                                rekap.add(j);
                                            }else {

                                                for (Prasyarat pras : prasyarat) {
                                                    List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                                    if (cariPras != null) {
                                                        for (KrsDetail prasList : cariPras) {
                                                            System.out.println("prasayarat prodi kelas null");
                                                            if (prasList.getBobot() != null){
                                                                if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
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




                    List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(tahunAkademik,Akses.UMUM,StatusRecord.AKTIF);
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
                                            List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                            if (cariPras != null) {
                                                for (KrsDetail prasList : cariPras) {
                                                    System.out.println("prasayarat umum krs null");
                                                    if (prasList.getBobot() != null){
                                                        if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
                                                            rekap.add(j);
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

                                            if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                                if (prasyarat.isEmpty()){
                                                    rekap.add(j);
                                                }else {

                                                    for (Prasyarat pras : prasyarat) {
                                                        List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                                        if (cariPras != null) {
                                                            for (KrsDetail prasList : cariPras) {
                                                                System.out.println("prasayarat umum");
                                                                if (prasList.getBobot() != null){
                                                                    if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
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
                    }

                    List<Jadwal> jadwalProdi = jadwalDao.findByTahunAkademikAndProdiAndAksesAndStatusAndIdHariNotNull(tahunAkademik,mahasiswa.getIdProdi(),Akses.PRODI,StatusRecord.AKTIF);
                    for (Jadwal j : jadwalProdi){

                        if (kelasMahasiswa != null){

                            if (j.getIdKelas() != kelasMahasiswa.getKelas()){

                                List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(),StatusRecord.AKTIF, mahasiswa);
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                                if (kd.isEmpty()){

                                    if (prasyarat.isEmpty()){
                                        rekap.add(j);
                                    }else {

                                        for (Prasyarat pras : prasyarat) {
                                            List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                            if (cariPras != null) {
                                                for (KrsDetail prasList : cariPras) {
                                                    System.out.println("prasayarat prodi krs null");
                                                    if (prasList.getBobot() != null){
                                                        if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
                                                            rekap.add(j);
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

                                            if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0){

                                                if (prasyarat.isEmpty()){
                                                    rekap.add(j);
                                                }else {

                                                    for (Prasyarat pras : prasyarat) {
                                                        List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                                        if (cariPras != null) {
                                                            for (KrsDetail prasList : cariPras) {
                                                                System.out.println("prasayarat prodi");
                                                                if (prasList.getBobot() != null){
                                                                    if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
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
                    }
                    if (kelasMahasiswa != null) {
                        List<Jadwal> jadwalKelas = jadwalDao.findByTahunAkademikAndIdKelasAndStatusAndIdHariNotNull(tahunAkademik, kelasMahasiswa.getKelas(), StatusRecord.AKTIF);
                        if (!jadwalKelas.isEmpty()) {
                            for (Jadwal j : jadwalKelas) {

                                List<KrsDetail> kd = krsDetailDao.findByMatakuliahKurikulumAndStatusAndMahasiswa(j.getMatakuliahKurikulum(), StatusRecord.AKTIF, mahasiswa);
                                List<Prasyarat> prasyarat = prasyaratDao.findByMatakuliahKurikulumAndStatus(j.getMatakuliahKurikulum(), StatusRecord.AKTIF);

                                if (kd.isEmpty()) {

                                    if (prasyarat.isEmpty()) {
                                        rekap.add(j);
                                    } else {

                                        for (Prasyarat pras : prasyarat) {
                                            List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                            if (cariPras != null) {
                                                for (KrsDetail prasList : cariPras) {
                                                    System.out.println("prasayarat kelas krs null");
                                                    if (prasList.getBobot() != null){
                                                        if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
                                                            rekap.add(j);
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

                                            if (krsDetail.getBobot().compareTo(grade.getBobot()) < 0) {

                                                if (prasyarat.isEmpty()) {
                                                    rekap.add(j);
                                                } else {

                                                    for (Prasyarat pras : prasyarat) {
                                                        List<KrsDetail> cariPras = krsDetailDao.findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(pras.getMatakuliahPras().getKodeMatakuliah(),mahasiswa,StatusRecord.AKTIF,krs);

                                                        if (cariPras != null) {
                                                            for (KrsDetail prasList : cariPras) {
                                                                System.out.println("prasayarat kelas");
                                                                if (prasList.getBobot() != null){
                                                                    if (prasList.getBobot().compareTo(pras.getNilai()) > 0) {
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
                    }




                }

                model.addAttribute("jadwal",rekap);
                model.addAttribute("krs", krs);

                model.addAttribute("detail", krsDetailDao.findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord.AKTIF,krs, mahasiswa));
            }
        }

    }

    @GetMapping("/krs/aktifasi")
    public void aktifasiKrs(Model model, @RequestParam(required = false) String mahasiswa, Pageable  pageable,
                            @RequestParam(required = false) TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi,
                            @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan,
                            @RequestParam(required = false) String nim){
        model.addAttribute("validasi", mahasiswa);

        if (nim == null){
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedProdi", prodi);
            model.addAttribute("selectedProgram", program);
            model.addAttribute("selectedAngkatan", angkatan);
            Page<Krs> krs = krsDao.findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(tahunAkademik,prodi,program,angkatan,pageable);
            model.addAttribute("krs",krs);
        }else {
            Mahasiswa mhsw = mahasiswaDao.findByNim(nim);
            model.addAttribute("selectedTahun", tahunAkademik);
            model.addAttribute("selectedNim", nim);
            Krs krs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mhsw);
            model.addAttribute("krsMahasiswa", krs);
        }
    }

    @PostMapping("/krs/prosesAktifasi")
    public String prosesKrs(@RequestParam TahunAkademik tahunAkademik, @RequestParam(required = false) Prodi prodi,
                            @RequestParam(required = false) Program program, @RequestParam(required = false) String angkatan,
                            @RequestParam(required = false) String nim){

        if (nim == null){
            List<Mahasiswa> mahasiswas = mahasiswaDao.findByStatusAndAngkatanAndIdProdiAndIdProgram(StatusRecord.AKTIF, angkatan, prodi, program);
            for (Mahasiswa mahasiswa : mahasiswas){
                Krs cariKrs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik,mahasiswa);
                if (cariKrs == null){
                    TahunAkademikProdi tahunAkademikProdi = tahunAkademikProdiDao.findByTahunAkademikAndProdi(tahunAkademik,prodi);
                    Krs krs = new Krs();
                    krs.setTanggalTransaksi(LocalDateTime.now());
                    krs.setStatus(StatusRecord.AKTIF);
                    krs.setTahunAkademik(tahunAkademik);
                    krs.setNim(mahasiswa.getNim());
                    krs.setProdi(prodi);
                    krs.setMahasiswa(mahasiswa);
                    krs.setTahunAkademikProdi(tahunAkademikProdi);
                    krsDao.save(krs);
                }
            }
            return "redirect:aktifasi?tahunAkademik=" + tahunAkademik.getId()+"&prodi="+prodi.getId()+"&program="+program.getId()+"&angkatan="+angkatan;

        } else {
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(nim);
            if (mahasiswa == null){
                return "redirect:list";
            }
            if (mahasiswa != null){
            Krs cariKrs = krsDao.findByTahunAkademikAndMahasiswa(tahunAkademik, mahasiswa);
            TahunAkademikProdi tahunAkademikProdi = tahunAkademikProdiDao.findByTahunAkademikAndProdi(tahunAkademik, mahasiswa.getIdProdi());
            if (cariKrs == null) {
                Krs krs = new Krs();
                krs.setTanggalTransaksi(LocalDateTime.now());
                krs.setStatus(StatusRecord.AKTIF);
                krs.setTahunAkademik(tahunAkademik);
                krs.setNim(mahasiswa.getNim());
                krs.setProdi(mahasiswa.getIdProdi());
                krs.setMahasiswa(mahasiswa);
                krs.setTahunAkademikProdi(tahunAkademikProdi);
                krsDao.save(krs);
            }
        }
            return "redirect:aktifasi?mahasiswa=AKTIF" + "&tahunAkademik=" + tahunAkademik.getId()+"&nim="+nim;
        }
    }

    @PostMapping("/krs/prosesKrs")
    public String addKrs(@RequestParam String[] data, @RequestParam Mahasiswa mahasiswa, RedirectAttributes attributes) {
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

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
//            Semeseter Genap/Ganjil
        }else {
            Ipk ipk = ipkDao.findByMahasiswa(mahasiswa);
            if (ipk.getIpk().toBigInteger().intValue() > new BigDecimal(3.00).toBigInteger().intValue()){

                if (data != null){
                    if (krsDetails.size() < 25) {
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
                                        krsDetailDao.save(kd);
                                }else {
                                    attributes.addFlashAttribute("batasRuang", jadwal);
                                }
                            } else {
                                System.out.println("batasnya 25");
                            }


                        }
                    }

                    if (krsDetails.size() == 25 || krsDetails.size() > 25) {
                        System.out.printf("Anda tidak bisa mengambil lagi matakuliah");
                    }

                } else {


                }

            }else {
                if (data != null){
                    if (krsDetails.size() < 23) {
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
                                        kd.setNilaiUts(BigDecimal.ZERO);
                                        kd.setFinalisasi("N");
                                        krsDetailDao.save(kd);
                                }else {
                                    attributes.addFlashAttribute("batasRuang", jadwal);
                                }
                            } else {
                                System.out.println("batasnya 23 ");
                            }


                        }
                    }

                    if (krsDetails.size() == 2 || krsDetails.size() > 2) {
                        System.out.printf("Anda tidak bisa mengambil lagi matakuliah");
                    }

                } else {


                }
            }

        }

        return "redirect:list?nim="+mahasiswa.getId()+"&tahunAkademik="+tahunAkademik.getId();
    }

    @PostMapping("/krs/deleteKrs")
    public String deleteKrs (@RequestParam KrsDetail id){
        id.setStatus(StatusRecord.HAPUS);
        krsDetailDao.save(id);

        return "redirect:list?nim="+id.getMahasiswa().getNim()+"&tahunAkademik="+id.getKrs().getTahunAkademik().getId();
    }

}
