package id.ac.tazkia.smilemahasiswa.controller;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.GradeDto;
import id.ac.tazkia.smilemahasiswa.dto.ListJadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlGradeGradesDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlGradeItemsDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.ScoreService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class ElearningController {

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private BobotTugasDao bobotTugasDao;

    @Autowired
    private NilaiTugasDao nilaiTugasDao;

    @Autowired
    private JadwalBobotTugasMoodleDao jadwalBobotTugasMoodleDao;

    @Autowired
    private KrsNilaiTugasMoodleDao krsNilaiTugasMoodleDao;

    @Autowired
    private GradeDao gradeDao;


    @Autowired
    private ScoreService scoreService;

    WebClient webClient1 = WebClient.builder()
            .baseUrl("https://elearning.tazkia.ac.id")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public List<MdlGradeItemsDto> getBobotTugas(String jadwal) {
        return webClient1.get()
                .uri("/api/bobottugas?jadwal="+jadwal)
                .retrieve().bodyToFlux(MdlGradeItemsDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeItemsDto> getBobotTugasAsli(String jadwal) {
        return webClient1.get()
                .uri("/api/bobotaslitugas?jadwal="+jadwal)
                .retrieve().bodyToFlux(MdlGradeItemsDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeItemsDto> getBobotUts(String tahunAkademik) {
        return webClient1.get()
                .uri("/api/bobotuts?tahunAkademik="+tahunAkademik)
                .retrieve().bodyToFlux(MdlGradeItemsDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeItemsDto> getBobotUas(String tahunAkademik) {
        return webClient1.get()
                .uri("/api/bobotuas?tahunAkademik="+tahunAkademik)
                .retrieve().bodyToFlux(MdlGradeItemsDto.class)
                .collectList()
                .block();
    }


    public List<MdlGradeGradesDto> getNilaiTugas2(@RequestParam String jadwal) {
        return webClient1.get()
                .uri("/api/nilaitugas2?jadwal=" + jadwal)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUts2(@RequestParam String jadwal) {
        return webClient1.get()
                .uri("/api/nilaiuts2?jadwal=" + jadwal)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUas2(@RequestParam String jadwal) {
        return webClient1.get()
                .uri("/api/nilaiuas2?jadwal=" + jadwal)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }


    public List<MdlGradeGradesDto> getNilaiTugasPerMhs(@RequestParam String jadwal, @RequestParam String mahasiswa) {
        return webClient1.get()
                .uri("/api/nilaitugaspermhs" + jadwal + mahasiswa)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUtsPerMhs(@RequestParam String jadwal, @RequestParam String mahasiswa) {
        return webClient1.get()
                .uri("/api/nilaiutspermhs" + jadwal + mahasiswa)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUasPerMhs(@RequestParam String jadwal, @RequestParam String mahasiswa) {
        return webClient1.get()
                .uri("/api/nilaiuaspermhs" + jadwal + mahasiswa)
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }


    @GetMapping("/api/prodi2")
    @ResponseBody
    public List<Prodi> tahun() {

        List<Prodi> prodi = prodiDao.findByStatus(StatusRecord.AKTIF);


        return prodi;
    }

    @GetMapping("/api/tahun2")
    @ResponseBody
    public List<Jadwal> tahun(@RequestParam(required = false) String ta,
                              @RequestParam(required = false) String prodi) {

        TahunAkademik tahunAkademik = tahunAkademikDao.findById(ta).get();
        Prodi p = prodiDao.findById(prodi).get();
        List<Jadwal> jadwal = jadwalDao.findByTahunAkademikAndProdiAndHariNotNull(tahunAkademik, p);


        return jadwal;
    }

    @GetMapping("/api/jadwal2")
    @ResponseBody
    public List<KrsDetail> krsDetail(@RequestParam(required = false) String jadwal,
                               @RequestParam(required = false) String ta,
                               @RequestParam(required = false) String mahasiswa){

        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findById(ta).get();
//        Mahasiswa mhs = mahasiswaDao.findByNim(mahasiswa);
        List<KrsDetail> krsDetail = krsDetailDao.findByStatusAndJadwalOrderByMahasiswaNim(StatusRecord.AKTIF,jadwal1);

        return krsDetail;
    }


    @GetMapping("/elearning/importNilai")
    public void importNilai(Model model){

        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
    }

    @GetMapping("/elearning/importNilaiProdi")
    public void importNilaiProdi(Model model){

        model.addAttribute("tahunAkademik", tahunAkademikDao.findByStatusNotInOrderByTahunDesc(Arrays.asList(StatusRecord.HAPUS)));
        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
    }

    @PostMapping("/elearning/importNilai")
    public String inputForm(@RequestParam(required = false) String ta, @RequestParam(required = false) String prodi,
                            @RequestParam(required = false) String jadwal, @RequestParam(required = false) String nim,
                            @RequestParam(value="action", required=true) String action){

        System.out.println("Impor Jalan");
        TahunAkademik tahunAkademik1 = tahunAkademikDao.findById(ta).get();
        Prodi prodi1 = prodiDao.findById(prodi).get();
        System.out.println("Masih Jalan");

        if (action.equals("tugas")) {
            if (jadwal != null){
                Jadwal jadwal1 = jadwalDao.findByStatusAndId(StatusRecord.AKTIF, jadwal);
                if (nim != null) {
                    Mahasiswa mhs = mahasiswaDao.findByNim(nim);
                    System.out.println("Mahasiswa  == "  + mhs.getNim());
                    List<MdlGradeGradesDto> daftarNilaiCountTugas = getNilaiTugasPerMhs(jadwal1.getIdNumberElearning(),mhs.getNim());
                    if (daftarNilaiCountTugas != null){
                        for (MdlGradeGradesDto nilaiTugas : daftarNilaiCountTugas) {
                            System.out.println("nilai  == "  + nilaiTugas.getNilaiAkhir());
                            BigDecimal nilaiUas = BigDecimal.ZERO;
                            BigDecimal nilaiUts = BigDecimal.ZERO;
                            if(nilaiTugas.getNilaiAkhir() != null && nilaiTugas.getFinalGrade() != null){
                                KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
//                            System.out.println("krs_detail  == "  + krsDetail.getId());
                                if (krsDetail != null){
                                    if (krsDetail.getNilaiUasFinal() != null){
                                        nilaiUas = krsDetail.getNilaiUasFinal();
                                    }
                                    if (krsDetail.getNilaiUtsFinal() != null){
                                        nilaiUts = krsDetail.getNilaiUtsFinal();
                                    }
                                    krsDetail.setNilaiTugas(nilaiTugas.getNilaiAkhir());
                                    BigDecimal nilaiAkhir = nilaiUas.add(nilaiUts.add(nilaiTugas.getNilaiAkhir()));
                                    GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                    krsDetail.setNilaiAkhir(nilaiAkhir);
                                    krsDetail.setGrade(gradeDto.getGrade());
                                    krsDetail.setBobot(gradeDto.getBobot());
                                    krsDetailDao.save(krsDetail);
                                }
                            }
                        }
                    }
                }else{
                    List<MdlGradeGradesDto> daftarNilaiCountTugas = getNilaiTugas2(jadwal1.getIdNumberElearning());
                    if (daftarNilaiCountTugas != null){
                        for (MdlGradeGradesDto nilaiTugas : daftarNilaiCountTugas) {
                            System.out.println("Nilai_tugas  == "  + nilaiTugas.getNilaiAkhir());
                            Mahasiswa mhs = mahasiswaDao.findByNim(nilaiTugas.getMahasiswa());
                            BigDecimal nilaiUas = BigDecimal.ZERO;
                            BigDecimal nilaiUts = BigDecimal.ZERO;
                            if(nilaiTugas.getNilaiAkhir() != null && nilaiTugas.getFinalGrade() != null) {
                                KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
//                            System.out.println("Mahasiswa  == "  + krsDetail.getMahasiswa().getNim());
//                            System.out.println("krs  == "  + krsDetail.getId());
                                if (krsDetail != null) {
                                    if (krsDetail.getNilaiUasFinal() != null) {
                                        nilaiUas = krsDetail.getNilaiUasFinal();
                                    }
                                    if (krsDetail.getNilaiUtsFinal() != null) {
                                        nilaiUts = krsDetail.getNilaiUtsFinal();
                                    }
                                    krsDetail.setNilaiTugas(nilaiTugas.getNilaiAkhir());
                                    BigDecimal nilaiAkhir = nilaiUas.add(nilaiUts.add(nilaiTugas.getNilaiAkhir()));
                                    GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                    krsDetail.setNilaiAkhir(nilaiAkhir);
                                    krsDetail.setGrade(gradeDto.getGrade());
                                    krsDetail.setBobot(gradeDto.getBobot());
                                    krsDetailDao.save(krsDetail);
                                }
                            }
                        }
                    }
                }
            }else{
                System.out.println("Masuk Prodi Jalan");
                List<ListJadwalDto> listJadwalDtos = jadwalDao.listJadwalDto(prodi, ta);
                if (listJadwalDtos != null){
                    for (ListJadwalDto listJadwalDto : listJadwalDtos){
                        List<MdlGradeGradesDto> daftarNilaiTugas = getNilaiTugas2(listJadwalDto.getIdNumberElearning());
                        Jadwal jadwal2 = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, listJadwalDto.getId(), listJadwalDto.getIdNumberElearning());
                        if (daftarNilaiTugas != null){
                            for(MdlGradeGradesDto listNilaiTugas : daftarNilaiTugas){
                                System.out.println("Nilai_tugas  == "  + listNilaiTugas.getNilaiAkhir());
                                Mahasiswa mhs = mahasiswaDao.findByNim(listNilaiTugas.getMahasiswa());
                                BigDecimal nilaiUas = BigDecimal.ZERO;
                                BigDecimal nilaiUts = BigDecimal.ZERO;

                                if (listNilaiTugas.getNilaiAkhir() != null && listNilaiTugas.getFinalGrade() != null) {
                                    KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal2);
                                    if (krsDetail != null) {
                                        if (krsDetail.getNilaiUasFinal() != null) {
                                            nilaiUas = krsDetail.getNilaiUasFinal();
                                        }
                                        if (krsDetail.getNilaiUtsFinal() != null) {
                                            nilaiUts = krsDetail.getNilaiUtsFinal();
                                        }
                                        krsDetail.setNilaiTugas(listNilaiTugas.getNilaiAkhir());
                                        BigDecimal nilaiAkhir = nilaiUas.add(nilaiUts.add(listNilaiTugas.getNilaiAkhir()));
                                        GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                        krsDetail.setNilaiAkhir(nilaiAkhir);
                                        krsDetail.setGrade(gradeDto.getGrade());
                                        krsDetail.setBobot(gradeDto.getBobot());
                                        krsDetailDao.save(krsDetail);
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        if (action.equals("uts")) {
            if (jadwal != null) {
                Jadwal jadwal1 = jadwalDao.findByStatusAndId(StatusRecord.AKTIF, jadwal);
                if (nim != null) {
                    Mahasiswa mhs = mahasiswaDao.findByNim(nim);
                    List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUtsPerMhs(jadwal1.getIdNumberElearning(), mhs.getNim());
                    if (daftarNilaiUts != null) {
                        for (MdlGradeGradesDto getNilaiUts : daftarNilaiUts) {
                            BigDecimal nilaiTugas = BigDecimal.ZERO;
                            BigDecimal nilaiUas = BigDecimal.ZERO;
                            if(getNilaiUts.getNilaiAkhir() != null && getNilaiUts.getFinalGrade() != null) {
                                KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                                if (krsDetail != null) {
                                    if (krsDetail.getNilaiTugas() != null) {
                                        nilaiTugas = krsDetail.getNilaiTugas();
                                    }
                                    if (krsDetail.getNilaiUasFinal() != null) {
                                        nilaiUas = krsDetail.getNilaiUasFinal();
                                    }
                                    krsDetail.setNilaiUts(getNilaiUts.getFinalGrade());
                                    krsDetail.setNilaiUtsFinal(getNilaiUts.getNilaiAkhir());
                                    BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUas.add(getNilaiUts.getNilaiAkhir()));
                                    GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                    krsDetail.setNilaiAkhir(nilaiAkhir);
                                    krsDetail.setGrade(gradeDto.getGrade());
                                    krsDetail.setBobot(gradeDto.getBobot());
                                    krsDetailDao.save(krsDetail);
                                }
                            }
                        }
                    }

                } else {
                    List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts2(jadwal1.getIdNumberElearning());
                    if (daftarNilaiUts != null) {
                        for (MdlGradeGradesDto getNilaiUts : daftarNilaiUts) {
                            Mahasiswa mhs = mahasiswaDao.findByNim(getNilaiUts.getMahasiswa());
                            System.out.println("Nilai_uts  == "  + getNilaiUts.getNilaiAkhir());
                            BigDecimal nilaiTugas = BigDecimal.ZERO;
                            BigDecimal nilaiUas = BigDecimal.ZERO;
                            if (getNilaiUts.getNilaiAkhir() != null && getNilaiUts.getFinalGrade() != null) {
                                KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                                if (krsDetail != null) {
                                    if (krsDetail.getNilaiTugas() != null) {
                                        nilaiTugas = krsDetail.getNilaiTugas();
                                    }
                                    if (krsDetail.getNilaiUasFinal() != null) {
                                        nilaiUas = krsDetail.getNilaiUasFinal();
                                    }

                                    krsDetail.setNilaiUts(getNilaiUts.getFinalGrade());
                                    krsDetail.setNilaiUtsFinal(getNilaiUts.getNilaiAkhir());
                                    BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUas.add(getNilaiUts.getNilaiAkhir()));
                                    GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                    krsDetail.setNilaiAkhir(nilaiAkhir);
                                    krsDetail.setGrade(gradeDto.getGrade());
                                    krsDetail.setBobot(gradeDto.getBobot());
                                    krsDetailDao.save(krsDetail);
                                    System.out.println(" JADWAL == " + getNilaiUts.getIdJadwal());
                                    System.out.println(" Mahasiswa == " + getNilaiUts.getMahasiswa());
                                    System.out.println(" Nilai == " + getNilaiUts.getNilai());
                                    System.out.println(" NILAI UAS UPDATED == " + getNilaiUts.getId());
                                    System.out.println("  =======  ");
                                }
                            }
                        }
                    }
                }
            }else{
                List<ListJadwalDto> listJadwalDtos = jadwalDao.listJadwalDto(prodi, ta);
                if (listJadwalDtos != null){
                    for (ListJadwalDto listJadwalDto : listJadwalDtos){
                        List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts2(listJadwalDto.getIdNumberElearning());
                        Jadwal jadwal2 = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, listJadwalDto.getId(), listJadwalDto.getIdNumberElearning());
                        if (daftarNilaiUts != null){
                            for(MdlGradeGradesDto listNilaiUts : daftarNilaiUts){
                                System.out.println("Nilai_uts  == "  + listNilaiUts.getNilaiAkhir());
                                Mahasiswa mhs = mahasiswaDao.findByNim(listNilaiUts.getMahasiswa());
                                BigDecimal nilaiTugas = BigDecimal.ZERO;
                                BigDecimal nilaiUas = BigDecimal.ZERO;
                                if(listNilaiUts.getNilaiAkhir() != null && listNilaiUts.getFinalGrade() != null) {
                                    KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal2);
//                                System.out.println("Mahasiswa  == "  + krsDetail.getMahasiswa().getNim());
//                                System.out.println("krs  == "  + krsDetail.getId());

                                    if (krsDetail != null) {
                                        if (krsDetail.getNilaiTugas() != null) {
                                            nilaiTugas = krsDetail.getNilaiTugas();
                                        }
                                        if (krsDetail.getNilaiUasFinal() != null) {
                                            nilaiUas = krsDetail.getNilaiUasFinal();
                                        }
                                        krsDetail.setNilaiUts(listNilaiUts.getFinalGrade());
                                        krsDetail.setNilaiUtsFinal(listNilaiUts.getNilaiAkhir());
                                        BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUas.add(listNilaiUts.getNilaiAkhir()));
                                        GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                        krsDetail.setNilaiAkhir(nilaiAkhir);
                                        krsDetail.setGrade(gradeDto.getGrade());
                                        krsDetail.setBobot(gradeDto.getBobot());
                                        krsDetailDao.save(krsDetail);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (action.equals("uas")) {
            if (jadwal != null) {
                Jadwal jadwal1 = jadwalDao.findByStatusAndId(StatusRecord.AKTIF, jadwal);
                if (nim != null) {
                    Mahasiswa mhs = mahasiswaDao.findByNim(nim);
                    List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUasPerMhs(jadwal1.getIdNumberElearning(), mhs.getNim());
                    if (daftarNilaiUas != null) {
                        for (MdlGradeGradesDto nilaiUas : daftarNilaiUas) {
                            BigDecimal nilaiTugas = BigDecimal.ZERO;
                            BigDecimal nilaiUts = BigDecimal.ZERO;
                            if(nilaiUas.getNilaiAkhir() != null && nilaiUas.getFinalGrade() != null) {
                                KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                                if (krsDetail != null) {
                                    if (krsDetail.getNilaiTugas() != null) {
                                        nilaiTugas = krsDetail.getNilaiTugas();
                                    }
                                    if (krsDetail.getNilaiUtsFinal() != null) {
                                        nilaiUts = krsDetail.getNilaiUtsFinal();
                                    }

                                    krsDetail.setNilaiUas(nilaiUas.getFinalGrade());
                                    krsDetail.setNilaiUasFinal(nilaiUas.getNilaiAkhir());
                                    BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUts.add(nilaiUas.getNilaiAkhir()));
                                    GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                    krsDetail.setNilaiAkhir(nilaiAkhir);
                                    krsDetail.setGrade(gradeDto.getGrade());
                                    krsDetail.setBobot(gradeDto.getBobot());
                                    krsDetailDao.save(krsDetail);
                                }
                            }
                        }
                    }
                } else {
                    List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUas2(jadwal1.getIdNumberElearning());
                    if (daftarNilaiUas != null) {
                        for (MdlGradeGradesDto nilaiUas : daftarNilaiUas) {
                            Mahasiswa mhs = mahasiswaDao.findByNim(nilaiUas.getMahasiswa());
                            System.out.println("Nilai_UAS  == "  + nilaiUas.getNilaiAkhir());
                            BigDecimal nilaiTugas = BigDecimal.ZERO;
                            BigDecimal nilaiUts = BigDecimal.ZERO;
                            if (nilaiUas.getNilaiAkhir() != null && nilaiUas.getFinalGrade() != null) {
                                KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                                if (krsDetail != null) {
                                    if (krsDetail.getNilaiTugas() != null) {
                                        nilaiTugas = krsDetail.getNilaiTugas();
                                    }
                                    if (krsDetail.getNilaiUtsFinal() != null) {
                                        nilaiUts = krsDetail.getNilaiUtsFinal();
                                    }

                                    krsDetail.setNilaiUas(nilaiUas.getFinalGrade());
                                    krsDetail.setNilaiUasFinal(nilaiUas.getNilaiAkhir());
                                    BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUts.add(nilaiUas.getNilaiAkhir()));
                                    GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                    krsDetail.setNilaiAkhir(nilaiAkhir);
                                    krsDetail.setGrade(gradeDto.getGrade());
                                    krsDetail.setBobot(gradeDto.getBobot());
                                    krsDetailDao.save(krsDetail);
                                }
                            }
                        }
                    }
                }
            }else{
                List<ListJadwalDto> listJadwalDtos = jadwalDao.listJadwalDto(prodi, ta);
                if (listJadwalDtos != null){
                    for (ListJadwalDto listJadwalDto : listJadwalDtos){
                        List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUas2(listJadwalDto.getIdNumberElearning());
                        Jadwal jadwal2 = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, listJadwalDto.getId(), listJadwalDto.getIdNumberElearning());
                        if (daftarNilaiUas != null){
                            for(MdlGradeGradesDto listNilaiUas : daftarNilaiUas){
                                System.out.println("Nilai_UAS  == "  + listNilaiUas.getNilaiAkhir());
                                Mahasiswa mhs = mahasiswaDao.findByNim(listNilaiUas.getMahasiswa());
                                BigDecimal nilaiUts = BigDecimal.ZERO;
                                BigDecimal nilaiTugas = BigDecimal.ZERO;
                                if(listNilaiUas.getNilaiAkhir() != null && listNilaiUas.getFinalGrade() != null) {
                                    KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal2);
//                                System.out.println("Mahasiswa  == "  + krsDetail.getMahasiswa().getNim());
//                                System.out.println("krs  == "  + krsDetail.getId());

                                    if (krsDetail != null) {
                                        if (krsDetail.getNilaiUtsFinal() != null) {
                                            nilaiUts = krsDetail.getNilaiUtsFinal();
                                        }
                                        if (krsDetail.getNilaiTugas() != null) {
                                            nilaiTugas = krsDetail.getNilaiTugas();
                                        }
                                        krsDetail.setNilaiUas(listNilaiUas.getFinalGrade());
                                        krsDetail.setNilaiUasFinal(listNilaiUas.getNilaiAkhir());
                                        BigDecimal nilaiAkhir = nilaiUts.add(nilaiTugas.add(listNilaiUas.getNilaiAkhir()));
                                        GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                                        krsDetail.setNilaiAkhir(nilaiAkhir);
                                        krsDetail.setGrade(gradeDto.getGrade());
                                        krsDetail.setBobot(gradeDto.getBobot());
                                        krsDetailDao.save(krsDetail);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        return "redirect:importNilai";

    }


    @PostMapping("/elearning/importNilai3")
    public String inputForm2(@RequestParam(required = false) String ta, @RequestParam(required = false) String prodi,
                            @RequestParam(required = false) String jadwal,@RequestParam(required = false) String nim,
                            @RequestParam(value="action", required=true) String action){

        TahunAkademik tahunAkademik1 = tahunAkademikDao.findById(ta).get();
        Prodi prodi1 = prodiDao.findById(prodi).get();
        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();


        if (action.equals("tugas")) {
            if (StringUtils.hasText(nim)) {
                Mahasiswa mhs = mahasiswaDao.findByNim(nim);
                System.out.println("Mahasiswa  == "  + mhs.getNim());
                List<MdlGradeGradesDto> daftarNilaiCountTugas = getNilaiTugasPerMhs(jadwal1.getIdNumberElearning(),mhs.getNim());
                if (daftarNilaiCountTugas != null){
                    for (MdlGradeGradesDto nilaiTugas : daftarNilaiCountTugas) {
                        System.out.println("nilai  == "  + nilaiTugas.getNilaiAkhir());
                        BigDecimal nilaiUas = BigDecimal.ZERO;
                        BigDecimal nilaiUts = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                        System.out.println("krs_detail  == "  + krsDetail.getId());
                        if (krsDetail != null){
                            if(krsDetail.getNilaiUasFinal() == null){
                                nilaiUas = krsDetail.getNilaiUasFinal();
                            }
                            if(krsDetail.getNilaiUtsFinal() == null){
                                nilaiUts = krsDetail.getNilaiUtsFinal();
                            }
                            krsDetail.setNilaiTugas(nilaiTugas.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiUas.add(nilaiUts).add(nilaiTugas.getNilaiAkhir());
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                        }
                    }
                }
            }else{
                List<MdlGradeGradesDto> daftarNilaiCountTugas = getNilaiTugas2(jadwal1.getIdNumberElearning());
                if (daftarNilaiCountTugas != null){
                    for (MdlGradeGradesDto nilaiTugas : daftarNilaiCountTugas) {
                        System.out.println("Nilai_tugas  == "  + nilaiTugas.getNilaiAkhir());
                        Mahasiswa mhs = mahasiswaDao.findByNim(nilaiTugas.getMahasiswa());
                        BigDecimal nilaiUas = BigDecimal.ZERO;
                        BigDecimal nilaiUts = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                        System.out.println("Mahasiswa  == "  + krsDetail.getMahasiswa().getNim());
                        System.out.println("krs  == "  + krsDetail.getId());
                        if (krsDetail != null){
                            if(krsDetail.getNilaiUasFinal() == null){
                                nilaiUas = krsDetail.getNilaiUasFinal();
                            }
                            if(krsDetail.getNilaiUtsFinal() == null){
                                nilaiUts = krsDetail.getNilaiUtsFinal();
                            }
                            krsDetail.setNilaiTugas(nilaiTugas.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiUas.add(nilaiUts).add(nilaiTugas.getNilaiAkhir());
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                        }
                    }
                }
            }
        }

        if (action.equals("uts")) {
            if (StringUtils.hasText(nim)) {
                Mahasiswa mhs = mahasiswaDao.findByNim(nim);
                List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUtsPerMhs(jadwal1.getIdNumberElearning(),mhs.getNim());
                if (daftarNilaiUts != null){
                    for (MdlGradeGradesDto getNilaiUts : daftarNilaiUts) {
                        BigDecimal nilaiTugas = BigDecimal.ZERO;
                        BigDecimal nilaiUas = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                        if (krsDetail != null){
                            if(krsDetail.getNilaiUasFinal() == null){
                                nilaiTugas = krsDetail.getNilaiTugas();
                            }
                            if(krsDetail.getNilaiUtsFinal() == null){
                                nilaiUas = krsDetail.getNilaiUasFinal();
                            }
                            krsDetail.setNilaiUts(getNilaiUts.getFinalGrade());
                            krsDetail.setNilaiUtsFinal(getNilaiUts.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUas).add(getNilaiUts.getNilaiAkhir());
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                        }
                    }
                }

            }else{
                List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts2(jadwal1.getIdNumberElearning());
                if (daftarNilaiUts != null){
                    for (MdlGradeGradesDto getNilaiUts : daftarNilaiUts) {
                        Mahasiswa mhs = mahasiswaDao.findByNim(getNilaiUts.getMahasiswa());
                        BigDecimal nilaiTugas = BigDecimal.ZERO;
                        BigDecimal nilaiUas = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                        if (krsDetail != null){
                            if(krsDetail.getNilaiUasFinal() == null){
                                nilaiTugas = krsDetail.getNilaiTugas();
                            }
                            if(krsDetail.getNilaiUtsFinal() == null){
                                nilaiUas = krsDetail.getNilaiUtsFinal();
                            }

                            krsDetail.setNilaiUts(getNilaiUts.getFinalGrade());
                            krsDetail.setNilaiUtsFinal(getNilaiUts.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUas).add(getNilaiUts.getNilaiAkhir());
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                        }
                    }
                }
            }
        }

        if (action.equals("uas")) {
            if (StringUtils.hasText(nim)) {
                Mahasiswa mhs = mahasiswaDao.findByNim(nim);
                List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUasPerMhs(jadwal1.getIdNumberElearning(),mhs.getNim());
                if (daftarNilaiUas != null){
                    for (MdlGradeGradesDto nilaiUas : daftarNilaiUas) {
                        BigDecimal nilaiTugas = BigDecimal.ZERO;
                        BigDecimal nilaiUts = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                        if (krsDetail != null){
                            if(krsDetail.getNilaiUasFinal() == null){
                                nilaiTugas = krsDetail.getNilaiTugas();
                            }
                            if(krsDetail.getNilaiUtsFinal() == null){
                                nilaiUts = krsDetail.getNilaiUtsFinal();
                            }

                            krsDetail.setNilaiUas(nilaiUas.getFinalGrade());
                            krsDetail.setNilaiUasFinal(nilaiUas.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUts).add(nilaiUas.getNilaiAkhir());
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                        }
                    }
                }
            }else{
                List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUas2(jadwal1.getIdNumberElearning());
                if (daftarNilaiUas != null){
                    for (MdlGradeGradesDto nilaiUas : daftarNilaiUas) {
                        Mahasiswa mhs = mahasiswaDao.findByNim(nilaiUas.getMahasiswa());
                        BigDecimal nilaiTugas = BigDecimal.ZERO;
                        BigDecimal nilaiUts = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaAndJadwal(StatusRecord.AKTIF, mhs, jadwal1);
                        if (krsDetail != null){
                            if(krsDetail.getNilaiUasFinal() == null){
                                nilaiTugas = krsDetail.getNilaiTugas();
                            }
                            if(krsDetail.getNilaiUtsFinal() == null){
                                nilaiUts = krsDetail.getNilaiUtsFinal();
                            }

                            krsDetail.setNilaiUas(nilaiUas.getFinalGrade());
                            krsDetail.setNilaiUasFinal(nilaiUas.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUts).add(nilaiUas.getNilaiAkhir());
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                        }
                    }
                }
            }
        }

        return "redirect:importNilai";

    }




    @PostMapping("/elearning/importNilai2")
    public String inputForm1(@RequestParam(required = false) String ta, @RequestParam(required = false) String prodi,
                            @RequestParam(required = false) String jadwal,@RequestParam(required = false) String nim,
                            @RequestParam(value="action", required=true) String action){

        TahunAkademik tahunAkademik1 = tahunAkademikDao.findById(ta).get();
        Prodi prodi1 = prodiDao.findById(prodi).get();
        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();
        Mahasiswa mhs = mahasiswaDao.findByNim(nim);

        if (action.equals("tugas")) {
            // do something here
            //tugas per jadwal
            List<MdlGradeGradesDto> daftarNilaiCountTugas = getNilaiTugas2(jadwal1.getIdNumberElearning());
            for (MdlGradeGradesDto mdlnilcounttugas : daftarNilaiCountTugas) {
                Jadwal j = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, mdlnilcounttugas.getIdNumber(), jadwal);

                if (mdlnilcounttugas.getEmail() != null) {
                    User user = userDao.findByUsername(mdlnilcounttugas.getEmail());
                        System.out.println("USER  == "  + user.getId());
                        System.out.println("USER Email  == "  + user.getUsername());

                    if (user != null) {
                        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                        System.out.println("Mahasiswa  == "  + mahasiswa.getNim());
                        if (mahasiswa != null){
                            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik1, StatusRecord.AKTIF);
                            System.out.println("KRS  == "  + k.getId());
                            if (k != null) {
                                Long jmlData = krsDetailDao.countByJadwalIdAndKrsAndStatusAndTahunAkademik(mdlnilcounttugas.getIdNumber(), k, StatusRecord.AKTIF, tahunAkademik1);
                                System.out.println("JML  == "  + jmlData);
                                if (jmlData.compareTo(Long.valueOf(1)) > 0) {
                                    Object idKrsDetail = krsDetailDao.getKrsDetailId(jadwal1, mahasiswa);
                                    System.out.println("idKrsDetailOjject  == "  + idKrsDetail);
                                    List<KrsDetail> cariDouble = krsDetailDao.findByStatusAndJadwalAndMahasiswaAndIdNotIn(StatusRecord.AKTIF, jadwal1, mahasiswa, idKrsDetail);
                                    System.out.println("caridouble  == "  + cariDouble);

                                    if (cariDouble != null) {
                                        for (KrsDetail thekrsDetail : cariDouble) {
                                            thekrsDetail.setStatus(StatusRecord.HAPUS);
                                            krsDetailDao.save(thekrsDetail);
                                            System.out.println("KRS DETAIL DOUBLE TERHAPUS == " + thekrsDetail.getId());
                                        }
                                    }

                                    KrsDetail krsDetail1 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, jadwal1, StatusRecord.AKTIF, k, tahunAkademik1);
                                    if (krsDetail1 != null) {
                                        krsDetail1.setNilaiTugas(mdlnilcounttugas.getNilaiAkhir());
                                        krsDetailDao.save(krsDetail1);
                                        System.out.println(" NILAI TUGAS UPDATED == " + mdlnilcounttugas.getId());
                                    }

                                }

                                if (jmlData.compareTo(Long.valueOf(1)) == 0) {

//                                KrsDetail krsDetail1 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, jadwal1, StatusRecord.AKTIF, k, tahunAkademik1);
                                    KrsDetail krsDetail1 = krsDetailDao.findByTahunAkademikAndJadwalAndMahasiswaAndKrsAndStatus(tahunAkademik1, jadwal1, mahasiswa, k, StatusRecord.AKTIF);
                                    if (krsDetail1 != null) {
                                        krsDetail1.setNilaiTugas(mdlnilcounttugas.getNilaiAkhir());
                                        krsDetailDao.save(krsDetail1);
                                        System.out.println(" JADWAL == " + mdlnilcounttugas.getIdJadwal());
                                        System.out.println(" Mahasiswa == " + mdlnilcounttugas.getMahasiswa());
                                        System.out.println(" Nilai == " + mdlnilcounttugas.getNilai());
                                        System.out.println(" NILAI TUGAS UPDATED == " + mdlnilcounttugas.getId());

                                        BigDecimal finalUts = krsDetail1.getNilaiUtsFinal();
                                        BigDecimal finalUas = krsDetail1.getNilaiUasFinal();
                                        if (finalUts != null && finalUas != null){
                                            krsDetail1.setNilaiAkhir(krsDetail1.getNilaiTugas().add(krsDetail1.getNilaiUtsFinal()).add(krsDetail1.getNilaiUasFinal()).add(krsDetail1.getNilaiPresensi()));
                                            scoreService.hitungNilaiAkhir(krsDetail1);
                                            System.out.println("SEMUA NILAI TERHITUNG == " + krsDetail1.getNilaiAkhir());
                                            System.out.println("  =======  ");
                                        }
                                    }
                                }

                            }
                        }

                    }
                }

            }
        }



        if (action.equals("uts")) {
            // do something here
            //uts per jadwal
            List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts2(jadwal1.getIdNumberElearning());
            for (MdlGradeGradesDto mdlniluts : daftarNilaiUts){
                Jadwal j = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, mdlniluts.getIdNumber(),jadwal);

//            System.out.println("TA  =" + ta);
//            System.out.println("PRODI =" + prodi);
//            System.out.println("JADWAL =" + jadwal);

                if (mdlniluts.getEmail() != null) {
                    User user = userDao.findByUsername(mdlniluts.getEmail());
                    System.out.println("USER  == "  + user.getId());
                    System.out.println("USER Email  == "  + user.getUsername());


                    if (user != null) {
                        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                        if (mahasiswa != null){
                            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik1, StatusRecord.AKTIF);

                            if (k != null) {


//                        Object krsDetail2 = krsDetailDao.getKrsDetailId3(tahunAkademik1, prodi, jadwal1, StatusRecord.AKTIF);
                                KrsDetail krsDetail2 = krsDetailDao.findByTahunAkademikAndJadwalAndMahasiswaAndKrsAndStatus(tahunAkademik1, jadwal1,mahasiswa,k, StatusRecord.AKTIF);
                                if (krsDetail2 != null) {
                                    krsDetail2.setNilaiUts(mdlniluts.getNilai());
                                    krsDetail2.setNilaiUtsFinal(mdlniluts.getNilaiAkhir());
                                    krsDetailDao.save(krsDetail2);
                                    System.out.println(" JADWAL == " + mdlniluts.getIdJadwal());
                                    System.out.println(" Mahasiswa == " + mdlniluts.getMahasiswa());
                                    System.out.println(" Nilai == " + mdlniluts.getNilai());
                                    System.out.println(" NILAI UTS UPDATED == " + mdlniluts.getId());


//                                BigDecimal nilaiUas = krsDetail2.getNilaiUas().multiply(krsDetail2.getJadwal().getBobotUas()).divide(new BigDecimal(100));
//                                BigDecimal nilaiUts = krsDetail2.getNilaiUts().multiply(krsDetail2.getJadwal().getBobotUts()).divide(new BigDecimal(100));
//                                krsDetail2.setNilaiAkhir(krsDetail2.getNilaiTugas().add(nilaiUts).add(krsDetail2.getNilaiPresensi()).add(nilaiUas));

                                    //                                if (krsDetail2.getNilaiUtsFinal().compareTo(BigDecimal.ZERO) == 0){
//
//                                }

                                    BigDecimal finalUts = krsDetail2.getNilaiUtsFinal();
                                    BigDecimal finalUas = krsDetail2.getNilaiUasFinal();
                                    if (finalUts != null && finalUas != null){
                                        krsDetail2.setNilaiAkhir(krsDetail2.getNilaiTugas().add(krsDetail2.getNilaiUtsFinal()).add(krsDetail2.getNilaiUasFinal()).add(krsDetail2.getNilaiPresensi()));
                                        scoreService.hitungNilaiAkhir(krsDetail2);
                                        System.out.println("SEMUA NILAI TERHITUNG == " + krsDetail2.getNilaiAkhir());
                                        System.out.println("  =======  ");
                                    }




                                }
                            }
                        }

                    }
                }

            }
        }

        if (action.equals("uas")) {
            //uas per jadwal
            List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUas2(jadwal1.getIdNumberElearning());
            for (MdlGradeGradesDto mdlniluas : daftarNilaiUas){
                Jadwal j = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, mdlniluas.getIdNumber(),jadwal);

//            System.out.println("TA  =" + ta);
//            System.out.println("PRODI =" + prodi);
//            System.out.println("JADWAL =" + jadwal);

                if (mdlniluas.getEmail() != null) {
                    User user = userDao.findByUsername(mdlniluas.getEmail());
                    System.out.println("USER  == "  + user.getId());
                    System.out.println("USER Email  == "  + user.getUsername());


                    if (user != null) {
                        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                        if (mahasiswa != null){
                            Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, tahunAkademik1, StatusRecord.AKTIF);

                            if (k != null) {


//                        Object krsDetail2 = krsDetailDao.getKrsDetailId3(tahunAkademik1, prodi, jadwal1, StatusRecord.AKTIF);
                                KrsDetail krsDetail2 = krsDetailDao.findByTahunAkademikAndJadwalAndMahasiswaAndKrsAndStatus(tahunAkademik1, jadwal1,mahasiswa,k, StatusRecord.AKTIF);
                                if (krsDetail2 != null) {
                                    krsDetail2.setNilaiUas(mdlniluas.getNilai());
                                    krsDetail2.setNilaiUasFinal(mdlniluas.getNilaiAkhir());
                                    krsDetailDao.save(krsDetail2);
                                    System.out.println(" JADWAL == " + mdlniluas.getIdJadwal());
                                    System.out.println(" Mahasiswa == " + mdlniluas.getMahasiswa());
                                    System.out.println(" Nilai == " + mdlniluas.getNilai());
                                    System.out.println(" NILAI UAS UPDATED == " + mdlniluas.getId());
                                    System.out.println("  =======  ");

                                    BigDecimal finalUts = krsDetail2.getNilaiUtsFinal();
                                    BigDecimal finalUas = krsDetail2.getNilaiUasFinal();
                                    if (finalUts != null && finalUas != null){
                                        krsDetail2.setNilaiAkhir(krsDetail2.getNilaiTugas().add(krsDetail2.getNilaiUtsFinal()).add(krsDetail2.getNilaiUasFinal()).add(krsDetail2.getNilaiPresensi()));
                                        scoreService.hitungNilaiAkhir(krsDetail2);
                                        System.out.println("SEMUA NILAI TERHITUNG == " + krsDetail2.getNilaiAkhir());
                                        System.out.println("  =======  ");
                                    }

                                }
                            }
                        }

                    }
                }

            }
        }

        return "redirect:importNilai";

    }


}
