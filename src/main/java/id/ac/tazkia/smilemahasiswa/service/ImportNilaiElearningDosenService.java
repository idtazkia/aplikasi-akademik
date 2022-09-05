package id.ac.tazkia.smilemahasiswa.service;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.GradeDto;
import id.ac.tazkia.smilemahasiswa.dto.ListJadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.NilaiAbsenSdsDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlGradeGradesDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@EnableScheduling
public class ImportNilaiElearningDosenService {


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
    private TagihanDao tagihanDao;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ProsesBackgroundDosenDao prosesBackgroundDosenDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    WebClient webClient1 = WebClient.builder()
            .baseUrl("https://elearning.tazkia.ac.id")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

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



    @Scheduled(fixedDelay = 1000)
    public void amblilDataProses(){

        ProsesBackgroundDosen prosesBackgroundDosen = prosesBackgroundDosenDao.findFirstByStatusOrderByTanggalInputDesc(StatusRecord.WAITING);
        if (prosesBackgroundDosen != null){
            if (prosesBackgroundDosen.getNamaProses().equals("TUGAS")){
                prosesBackgroundDosen.setTanggalMulai(LocalDateTime.now());
                prosesBackgroundDosen.setStatus(StatusRecord.ON_PROCESS);
                prosesBackgroundDosenDao.save(prosesBackgroundDosen);
                imporNilaiTugas(prosesBackgroundDosen.getProdi(), prosesBackgroundDosen.getTahunAkademik(),prosesBackgroundDosen.getJadwal(), prosesBackgroundDosen);
            }else if (prosesBackgroundDosen.getNamaProses().equals("UTS")){
                prosesBackgroundDosen.setTanggalMulai(LocalDateTime.now());
                prosesBackgroundDosen.setStatus(StatusRecord.ON_PROCESS);
                prosesBackgroundDosenDao.save(prosesBackgroundDosen);
                imporNilaiUts(prosesBackgroundDosen.getProdi(), prosesBackgroundDosen.getTahunAkademik(),prosesBackgroundDosen.getJadwal(), prosesBackgroundDosen);
            }else if (prosesBackgroundDosen.getNamaProses().equals("UAS")){
                prosesBackgroundDosen.setTanggalMulai(LocalDateTime.now());
                prosesBackgroundDosen.setStatus(StatusRecord.ON_PROCESS);
                prosesBackgroundDosenDao.save(prosesBackgroundDosen);
                imporNilaiUas(prosesBackgroundDosen.getProdi(), prosesBackgroundDosen.getTahunAkademik(),prosesBackgroundDosen.getJadwal(), prosesBackgroundDosen);
            }else if (prosesBackgroundDosen.getNamaProses().equals("SDS")){
                prosesBackgroundDosen.setTanggalMulai(LocalDateTime.now());
                prosesBackgroundDosen.setStatus(StatusRecord.ON_PROCESS);
                prosesBackgroundDosenDao.save(prosesBackgroundDosen);
                imporNilaiSds(prosesBackgroundDosen.getProdi(), prosesBackgroundDosen.getTahunAkademik(), prosesBackgroundDosen);
            }else{
                throw new UnsupportedOperationException();
            }
        }

    }


    public void imporNilaiTugas(@RequestParam(required = true) String prodi,@RequestParam(required = true) String ta,@RequestParam(required = false) String jadwal, @RequestParam(required = true) ProsesBackgroundDosen prosesBackgroundDosen) {

        System.out.println("Masuk Impott Jalan");
        List<ListJadwalDto> listJadwalDtos = jadwalDao.listJadwalDto(prodi, ta);
        if (listJadwalDtos != null){
            for (ListJadwalDto listJadwalDto : listJadwalDtos){
                List<MdlGradeGradesDto> daftarNilaiTugas = getNilaiTugas2(listJadwalDto.getIdNumberElearning());
                Jadwal jadwal2 = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, listJadwalDto.getIdNumberElearning(), listJadwalDto.getId());
                if (daftarNilaiTugas != null){
                    for(MdlGradeGradesDto listNilaiTugas : daftarNilaiTugas){
                        System.out.println("NIM : " + listNilaiTugas.getMahasiswa());
                        System.out.println("Nilai_tugas  : "  + listNilaiTugas.getNilaiAkhir());
                        BigDecimal nilaiUas = BigDecimal.ZERO;
                        BigDecimal nilaiUts = BigDecimal.ZERO;
                        BigDecimal nilaiTugas = BigDecimal.ZERO;
                        BigDecimal nilaiUtsAsli = BigDecimal.ZERO;
                        BigDecimal nilaiUasAsli = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaNimAndJadwal(StatusRecord.AKTIF, listNilaiTugas.getMahasiswa(), jadwal2);
                        if (krsDetail != null) {
                            if (krsDetail.getNilaiUasFinal() != null) {
                                nilaiUas = krsDetail.getNilaiUasFinal();
                            }
                            if (krsDetail.getNilaiUtsFinal() != null) {
                                nilaiUts = krsDetail.getNilaiUtsFinal();
                            }
                            if (krsDetail.getNilaiTugas() != null){
                                nilaiTugas = krsDetail.getNilaiTugas();
                            }
                            if (listNilaiTugas.getNilaiAkhir() != null){
                                nilaiTugas = listNilaiTugas.getNilaiAkhir();
                            }
                            if(krsDetail.getNilaiUts() != null){
                                nilaiUtsAsli = krsDetail.getNilaiUts();
                            }
                            if(krsDetail.getNilaiUas() != null){
                                nilaiUasAsli = krsDetail.getNilaiUas();
                            }
                            krsDetail.setNilaiTugas(nilaiTugas);
                            krsDetail.setNilaiUts(nilaiUtsAsli);
                            krsDetail.setNilaiUas(nilaiUasAsli);
                            BigDecimal nilaiAkhir = nilaiUas.add(nilaiUts.add(nilaiTugas));
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                            System.out.println("Status  : DONE");
                        }
                    }
                }
            }
        }

        prosesBackgroundDosen.setStatus(StatusRecord.DONE);
        prosesBackgroundDosen.setTanggalSelesai(LocalDateTime.now());
        prosesBackgroundDosenDao.save(prosesBackgroundDosen);

    }

    public void imporNilaiUts(@RequestParam(required = true) String prodi,@RequestParam(required = true) String ta,@RequestParam(required = false) String jadwal, @RequestParam(required = true) ProsesBackgroundDosen prosesBackgroundDosen) {
//
        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();
//        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        List<ListJadwalDto> listJadwalDtos = jadwalDao.byJadwal2Dosen(prodi,ta, jadwal1.getIdNumberElearning());
        if (listJadwalDtos != null){
            for (ListJadwalDto listJadwalDto : listJadwalDtos){
                List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts2(listJadwalDto.getIdNumberElearning());
                Jadwal jadwal2 = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, listJadwalDto.getIdNumberElearning(), listJadwalDto.getId());
                if (daftarNilaiUts != null){
                    for(MdlGradeGradesDto listNilaiUts : daftarNilaiUts){
                        System.out.println("NIM : " + listNilaiUts.getMahasiswa());
                        System.out.println("Nilai_uts  : "  + listNilaiUts.getNilaiAkhir());
                        BigDecimal nilaiTugas = BigDecimal.ZERO;
                        BigDecimal nilaiUas = BigDecimal.ZERO;
                        BigDecimal nilaiUts = BigDecimal.ZERO;
                        BigDecimal nilaiUtsFinal = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaNimAndJadwal(StatusRecord.AKTIF, listNilaiUts.getMahasiswa(), jadwal2);
                        if (krsDetail != null) {
                            if (krsDetail.getNilaiTugas() != null) {
                                nilaiTugas = krsDetail.getNilaiTugas();
                            }
                            if (krsDetail.getNilaiUasFinal() != null) {
                                nilaiUas = krsDetail.getNilaiUasFinal();
                            }
                            if (krsDetail.getNilaiUts() != null) {
                                nilaiUts = krsDetail.getNilaiUts();
                            }
                            if (listNilaiUts.getFinalgrade() != null){
                                nilaiUts = listNilaiUts.getFinalgrade();
                            }
                            if (listNilaiUts.getNilaiAkhir() != null){
                                nilaiUtsFinal = listNilaiUts.getNilaiAkhir();
                            }
                            krsDetail.setNilaiUts(nilaiUts);
                            krsDetail.setNilaiUtsFinal(listNilaiUts.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiTugas.add(nilaiUas.add(nilaiUtsFinal));
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                            System.out.println("Status  : DONE");
                        }

                    }
                }
            }
        }

        prosesBackgroundDosen.setStatus(StatusRecord.DONE);
        prosesBackgroundDosen.setTanggalSelesai(LocalDateTime.now());
        prosesBackgroundDosenDao.save(prosesBackgroundDosen);

    }

    public void imporNilaiUas(@RequestParam(required = true) String prodi,@RequestParam(required = true) String ta,@RequestParam(required = false) String jadwal, @RequestParam(required = true) ProsesBackgroundDosen prosesBackgroundDosen) {

        Jadwal jadwal1 = jadwalDao.findById(jadwal).get();

        List<ListJadwalDto> listJadwalDtos = jadwalDao.byJadwal2Dosen(prodi,ta, jadwal1.getIdNumberElearning());
        if (listJadwalDtos != null){
            for (ListJadwalDto listJadwalDto : listJadwalDtos){
                List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUas2(listJadwalDto.getIdNumberElearning());
                Jadwal jadwal2 = jadwalDao.findByStatusAndIdNumberElearningAndId(StatusRecord.AKTIF, listJadwalDto.getIdNumberElearning(), listJadwalDto.getId());
                if (daftarNilaiUas != null){
                    for(MdlGradeGradesDto listNilaiUas : daftarNilaiUas){
                        System.out.println("NIM : " + listNilaiUas.getMahasiswa());
                        System.out.println("Nilai_UAS  == "  + listNilaiUas.getNilaiAkhir());
                        BigDecimal nilaiUts = BigDecimal.ZERO;
                        BigDecimal nilaiTugas = BigDecimal.ZERO;
                        BigDecimal nilaiUas = BigDecimal.ZERO;
                        BigDecimal nilaiUasFinal = BigDecimal.ZERO;
                        KrsDetail krsDetail = krsDetailDao.findByStatusAndMahasiswaNimAndJadwal(StatusRecord.AKTIF, listNilaiUas.getMahasiswa(), jadwal2);
                        if (krsDetail != null) {
                            if (krsDetail.getNilaiUtsFinal() != null) {
                                nilaiUts = krsDetail.getNilaiUtsFinal();
                            }
                            if (krsDetail.getNilaiTugas() != null) {
                                nilaiTugas = krsDetail.getNilaiTugas();
                            }
                            if (krsDetail.getNilaiUas() != null) {
                                nilaiUas = krsDetail.getNilaiUas();
                            }
                            if (listNilaiUas.getFinalgrade() != null){
                                nilaiUas = listNilaiUas.getFinalgrade();
                            }
                            if (listNilaiUas.getNilaiAkhir() != null){
                                nilaiUasFinal = listNilaiUas.getNilaiAkhir();
                            }
                            krsDetail.setNilaiUas(nilaiUas);
                            krsDetail.setNilaiUasFinal(listNilaiUas.getNilaiAkhir());
                            BigDecimal nilaiAkhir = nilaiUts.add(nilaiTugas.add(nilaiUasFinal));
                            GradeDto gradeDto = gradeDao.cariGradeNilai(nilaiAkhir);
                            krsDetail.setNilaiAkhir(nilaiAkhir);
                            krsDetail.setGrade(gradeDto.getGrade());
                            krsDetail.setBobot(gradeDto.getBobot());
                            krsDetailDao.save(krsDetail);
                            System.out.println("Status  : DONE");
                        }
                    }
                }
            }
        }

        prosesBackgroundDosen.setStatus(StatusRecord.DONE);
        prosesBackgroundDosen.setTanggalSelesai(LocalDateTime.now());
        prosesBackgroundDosenDao.save(prosesBackgroundDosen);

    }

    public void imporNilaiSds(@RequestParam(required = true) String prodi, @RequestParam(required = true) String ta, @RequestParam(required = true) ProsesBackgroundDosen prosesBackgroundDosen) {
        System.out.println("Masuk SDS Jalan");
        //Cari Jadwal per tahun akademik dan per prodi yang ada potongan SDS nya
        List<String> listSds = jadwalDao.findSds1(ta,prodi);
        if (listSds != null) {
            //Looping jadwal
            for (String listSds1 : listSds) {
                System.out.println("Jadwal : " + listSds1);
                //Cari krs Mahasiswa per jadwal
                List<KrsDetail> krsDetail1 = krsDetailDao.findByStatusAndJadwalId(StatusRecord.AKTIF,listSds1);
                if(krsDetail1 != null) {
                    //looping krs mahasiswa per jadwal
                    for (KrsDetail krsDetail : krsDetail1) {
                        System.out.println("NIM : " + krsDetail.getMahasiswa().getNim());
                        System.out.println("Mahasiswa : " + krsDetail.getMahasiswa().getNama());
                        NilaiAbsenSdsDto nilaiAbsenSdsDto = presensiMahasiswaDao.listNilaiAbsenSds(krsDetail.getMahasiswa().getId(), tahunAkademikDao.findByStatusNotAndId(StatusRecord.HAPUS, ta).getKodeTahunAkademik());
                        if(nilaiAbsenSdsDto != null) {
                            if(nilaiAbsenSdsDto.getNilai() == null) {
                                System.out.println("Nilai SDS : 0.00");
                                krsDetailDao.updateNilaiSds(BigDecimal.ZERO, krsDetail.getId());
                                krsDetailDao.updateGradeNilai(krsDetail.getId());
                            }else{
                                if (nilaiAbsenSdsDto.getNilai().compareTo(BigDecimal.TEN) > 0) {
                                    System.out.println("Nilai SDS : " + BigDecimal.TEN);
                                    krsDetailDao.updateNilaiSds(BigDecimal.TEN, krsDetail.getId());
                                    krsDetailDao.updateGradeNilai(krsDetail.getId());
                                }else{
                                    System.out.println("Nilai SDS : " + nilaiAbsenSdsDto.getNilai());
                                    krsDetailDao.updateNilaiSds(nilaiAbsenSdsDto.getNilai(), krsDetail.getId());
                                    krsDetailDao.updateGradeNilai(krsDetail.getId());
                                }
                            }
                        }else{
                            System.out.println("Nilai SDS : 0.00");
                            krsDetailDao.updateNilaiSds(BigDecimal.ZERO, krsDetail.getId());
                            krsDetailDao.updateGradeNilai(krsDetail.getId());
                        }
                        System.out.println("DONE");
                    }
                }
            }
        }

        prosesBackgroundDosen.setStatus(StatusRecord.DONE);
        prosesBackgroundDosen.setTanggalSelesai(LocalDateTime.now());
        prosesBackgroundDosenDao.save(prosesBackgroundDosen);

    }


}
