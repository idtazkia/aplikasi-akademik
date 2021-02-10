package id.ac.tazkia.smilemahasiswa.controller;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreHitungDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.ScoreService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class ElearningWebClientController {

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private SesiKuliahDao sesiKuliahDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private KrsDao krsDao;

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


    public List<MdlGradeItemsDto> getBobotTugas(String tahunAkademik) {
        return webClient1.get()
                .uri("/api/bobottugas?tahunAkademik="+tahunAkademik)
                .retrieve().bodyToFlux(MdlGradeItemsDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeItemsDto> getBobotTugasAsli(String tahunAkademik) {
        return webClient1.get()
                .uri("/api/bobotaslitugas?tahunAkademik="+tahunAkademik)
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

    public List<MdlGradeGradesDto> getNilaiTugas() {
        return webClient1.get()
                .uri("/api/nilaitugas")
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiCountTugas() {
        return webClient1.get()
                .uri("/api/nilaicounttugas")
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUts() {
        return webClient1.get()
                .uri("/api/nilaiuts")
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }

    public List<MdlGradeGradesDto> getNilaiUas() {
        return webClient1.get()
                .uri("/api/nilaiuas")
                .retrieve().bodyToFlux(MdlGradeGradesDto.class)
                .collectList()
                .block();
    }


    @GetMapping("/assessment/updatenilai")
    public String updateNilai(){

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        //krs detail edit
        //tugas edit
        List<MdlGradeGradesDto> daftarNilaiCountTugas = getNilaiCountTugas();
        for (MdlGradeGradesDto mdlnilcounttugas : daftarNilaiCountTugas){
            Jadwal j = jadwalDao.findById(mdlnilcounttugas.getIdJadwal()).get();

            if (mdlnilcounttugas.getMahasiswa() != null) {
                User user = userDao.findByUsername(mdlnilcounttugas.getMahasiswa());

                if (user != null) {
                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
                    if (k != null){
                        Long jmlData = krsDetailDao.countByJadwalIdAndKrsAndStatusAndTahunAkademik(mdlnilcounttugas.getIdJadwal(), k, StatusRecord.AKTIF, ta);
                        if (jmlData.compareTo(Long.valueOf(1)) > 0) {
                            Object idKrsDetail = krsDetailDao.getKrsDetailId(j, mahasiswa);
                            List<KrsDetail> cariDouble = krsDetailDao.findByStatusAndJadwalAndMahasiswaAndIdNotIn(StatusRecord.AKTIF, j, mahasiswa, idKrsDetail);
                            for (KrsDetail thekrsDetail : cariDouble) {
                                thekrsDetail.setStatus(StatusRecord.HAPUS);
                                krsDetailDao.save(thekrsDetail);
                                System.out.println("KRS DETAIL DOUBLE TERHAPUS == " + thekrsDetail.getId());
                            }

                            KrsDetail krsDetail1 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
                            if (krsDetail1 != null){
                                krsDetail1.setNilaiTugas(mdlnilcounttugas.getNilaiAkhir());
                                krsDetailDao.save(krsDetail1);
                                System.out.println(" NILAI TUGAS UPDATED == " + mdlnilcounttugas.getId());
                            }

                        }

                        if (jmlData.compareTo(Long.valueOf(1)) == 0) {

                            KrsDetail krsDetail1 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
                            if (krsDetail1 != null){
                                krsDetail1.setNilaiTugas(mdlnilcounttugas.getNilaiAkhir());
                                krsDetailDao.save(krsDetail1);
                                System.out.println(" NILAI TUGAS UPDATED == " + mdlnilcounttugas.getId());
                            }
                        }

                    }
                }
            }

        }


//        //uts
        List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts();
        for (MdlGradeGradesDto mdlniluts : daftarNilaiUts){
            Jadwal j = jadwalDao.findById(mdlniluts.getIdJadwal()).get();

            if (mdlniluts.getMahasiswa() != null) {
                User user = userDao.findByUsername(mdlniluts.getMahasiswa());

                if (user != null) {
                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
                    if (k != null){

                        KrsDetail krsDetail2 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
                        if  (krsDetail2 != null){
                            krsDetail2.setNilaiUts(mdlniluts.getNilai());
                            krsDetailDao.save(krsDetail2);
                            System.out.println(" NILAI UTS UPDATED == " + mdlniluts.getId());
                        }
                    }

                }
            }

        }





        //uas
        List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUas();
        for (MdlGradeGradesDto mdlniluas : daftarNilaiUas){
            Jadwal j = jadwalDao.findById(mdlniluas.getIdJadwal()).get();

            if (mdlniluas.getMahasiswa() != null) {
                User user = userDao.findByUsername(mdlniluas.getMahasiswa());

                if (user != null) {
                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
                    if (k != null){

                        KrsDetail krsDetail2 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
                        if  (krsDetail2 != null){
                            krsDetail2.setNilaiUas(mdlniluas.getNilai());
                            krsDetailDao.save(krsDetail2);
                            System.out.println(" NILAI UAS UPDATED == " + mdlniluas.getId());

                            BigDecimal nilaiUas = krsDetail2.getNilaiUas().multiply(krsDetail2.getJadwal().getBobotUas()).divide(new BigDecimal(100));
                            BigDecimal nilaiUts = krsDetail2.getNilaiUts().multiply(krsDetail2.getJadwal().getBobotUts()).divide(new BigDecimal(100));
                            krsDetail2.setNilaiAkhir(krsDetail2.getNilaiTugas().add(nilaiUts).add(krsDetail2.getNilaiPresensi()).add(nilaiUas));
                            scoreService.hitungNilaiAkhir(krsDetail2);
                            System.out.println("SEMUA NILAI TERHITUNG == " + krsDetail2.getNilaiAkhir());
                        }


                    }

                }
            }

        }


        return "redirect:admin";

    }

    @GetMapping("/assessment/krsnilaitugas")
    public String krsNilaiTugas(){

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        //krs nilai tugas
        List<MdlGradeGradesDto> daftarNilaiTugas = getNilaiTugas();
        for (MdlGradeGradesDto mdlniltug : daftarNilaiTugas){
            Jadwal j = jadwalDao.findById(mdlniltug.getIdJadwal()).get();

            if (mdlniltug.getMahasiswa() != null) {
                User user = userDao.findByUsername(mdlniltug.getMahasiswa());

                if (user != null) {
                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
//                    KrsDetail idKrsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
                    Object idKrsDetail = krsDetailDao.getKrsDetailId2(j, mahasiswa, k, ta);


                    KrsNilaiTugasMoodle nt = new KrsNilaiTugasMoodle();
                    if (idKrsDetail != null){
                        nt.setId(mdlniltug.getId());
                        nt.setKrsDetail(krsDetailDao.findById(idKrsDetail.toString()).get());
                        nt.setBobotTugas(bobotTugasDao.findById(mdlniltug.getIdBobotTugas().toString()).get());
                        nt.setNilai(mdlniltug.getNilai());
                        nt.setStatus(StatusRecord.valueOf(mdlniltug.getStatus()));
                        nt.setNilaiAkhir(mdlniltug.getNilaiAkhir());
                        krsNilaiTugasMoodleDao.save(nt);
                        System.out.println("NILAI TUGAS AWAL TERSIMPAN == " + mdlniltug.getId());
                    }

                }

//                if (user != null) {
//                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
//                    Object idKrsDetail = krsDetailDao.getKrsDetailId(j, mahasiswa);
//                    KrsNilaiTugasMoodle nt = new KrsNilaiTugasMoodle();
//
//                    if (idKrsDetail != null){
//                        nt.setId(mdlniltug.getId());
//                        nt.setKrsDetail(krsDetailDao.findById(idKrsDetail.toString()).get());
//                        nt.setBobotTugas(bobotTugasDao.findById(mdlniltug.getIdBobotTugas().toString()).get());
//                        nt.setNilai(mdlniltug.getFinalGrade());
//                        nt.setStatus(StatusRecord.valueOf(mdlniltug.getStatus()));
//                        nt.setNilaiAkhir(mdlniltug.getNilaiAkhir());
//                        krsNilaiTugasMoodleDao.save(nt);
//                        System.out.println(" NILAI TERSIMPAN == " + mdlniltug.getId());
//                    }
//
//                }

            }
        }

        return "redirect:admin";

    }




    @GetMapping("/assessment/import")
    public String importAssessmentElearning(String tahunAkademik){

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        // bobot tugas
        List<MdlGradeItemsDto> daftarTugas = getBobotTugas(tahunAkademik);
        for (MdlGradeItemsDto mdltug : daftarTugas){

            JadwalBobotTugasMoodle bt = new JadwalBobotTugasMoodle();

            bt.setId(mdltug.getId());
            bt.setJadwal(jadwalDao.findById(mdltug.getIdJadwal()).get());
            bt.setNamaTugas(mdltug.getNamaTugas());
            bt.setBobot(mdltug.getBobot());
            bt.setStatus(StatusRecord.valueOf(mdltug.getStatusA()));
            bt.setPertemuan(mdltug.getPertemuan().toString());
            jadwalBobotTugasMoodleDao.save(bt);
            System.out.println(" BOBOT TERSIMPAN == " + mdltug.getId());


        }


//        //jadwal edit
        //tugas edit
        List<MdlGradeItemsDto> daftarTugasAsli = getBobotTugasAsli(tahunAkademik);
        for (MdlGradeItemsDto mdltugasasli : daftarTugasAsli){
            Jadwal j = jadwalDao.findById(mdltugasasli.getIdJadwal()).get();

                j.setBobotTugas(mdltugasasli.getBobot());
                jadwalDao.save(j);
                System.out.println(" BOBOT TUGAS ASLI UPDATED == " + mdltugasasli.getId());
        }

        //uts edit
        List<MdlGradeItemsDto> daftarUts = getBobotUts(tahunAkademik);
        for (MdlGradeItemsDto mdluts : daftarUts){
            Jadwal j = jadwalDao.findById(mdluts.getIdJadwal()).get();

                j.setBobotUts(mdluts.getBobot());
                jadwalDao.save(j);
                System.out.println(" BOBOT UTS UPDATED == " + mdluts.getId());
        }

        //uas edit
        List<MdlGradeItemsDto> daftarUas = getBobotUas(tahunAkademik);
        for (MdlGradeItemsDto mdluas : daftarUas){
            Jadwal j = jadwalDao.findById(mdluas.getIdJadwal()).get();

                j.setBobotUas(mdluas.getBobot());
                jadwalDao.save(j);
                System.out.println(" BOBOT UAS UPDATED == " + mdluas.getId());
        }


        return "redirect:admin";
    }


}
