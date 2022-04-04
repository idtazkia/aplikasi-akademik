package id.ac.tazkia.smilemahasiswa.service;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlGradeGradesDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class ElearningWebClientService {



    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private SesiKuliahDao sesiKuliahDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private JadwalBobotTugasMoodleDao jadwalBobotTugasMoodleDao;

    @Autowired
    private KrsNilaiTugasMoodleDao krsNilaiTugasMoodleDao;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private BobotTugasDao bobotTugasDao;

    @Autowired
    private AttendanceImportBerhasilDao listImportBerhasilDao;

    WebClient webClient1 = WebClient.builder()
            .baseUrl("https://elearning.tazkia.ac.id")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public List<MdlAttendanceLogDosenDto> getAttendanceDosen(@RequestParam LocalDate tanggalImport) {
        return webClient1.get()
                .uri("/api/sessiondosen?tanggalImport=" + tanggalImport)
                .retrieve().bodyToFlux(id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto.class)
                .collectList()
                .block();
    }

    public List<MdlAttendanceLogDosenDto> getAttendanceDosen2() {
        return webClient1.get()
                .uri("/api/sessiondosen2")
                .retrieve().bodyToFlux(id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto.class)
                .collectList()
                .block();
    }

    public List<MdlAttendanceLogMahasiswaDto> getAttendanceMahasiswa() {
        return webClient1.get()
                .uri("/api/sessionmahasiswa")
                .retrieve().bodyToFlux(MdlAttendanceLogMahasiswaDto.class)
                .collectList()
                .block();
    }

    public List<MdlAttendanceLogMahasiswaDto> getAttendanceMahasiswa2(@RequestParam String id) {
        return webClient1.get()
                .uri("/api/sessionmahasiswa2?id="+id)
                .retrieve().bodyToFlux(MdlAttendanceLogMahasiswaDto.class)
                .collectList()
                .block();
    }

    public List<MdlAttendanceLogDto> setStatusImport() {
        return webClient1.get()
                .uri("/api/changestatus")
                .retrieve().bodyToFlux(MdlAttendanceLogDto.class)
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


//    @GetMapping("/api/changestatus")
//    @ResponseBody
//    public void changeImportStatus(@RequestParam String id){
//        List<MdlAttendanceLogDosenDto> daftarPresensiDosen = getAttendanceDosen();
//        for (MdlAttendanceLogDosenDto mdldos : daftarPresensiDosen){
//            mdldos.getIdLog(id);
//        }
//    }

//    @Scheduled(cron = "0 45 19 * * ? ", zone = "Asia/Jakarta")
//    public void penilaian() {
//
//        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
//
//        //krs detail edit
//        //tugas edit
//        List<MdlGradeGradesDto> daftarNilaiCountTugas = getNilaiCountTugas();
//        for (MdlGradeGradesDto mdlnilcounttugas : daftarNilaiCountTugas){
//            Jadwal j = jadwalDao.findById(mdlnilcounttugas.getIdJadwal()).get();
//
//            if (mdlnilcounttugas.getMahasiswa() != null) {
//                User user = userDao.findByUsername(mdlnilcounttugas.getMahasiswa());
//
//                if (user != null) {
//                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
//                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
//                    if (k != null){
//                        Long jmlData = krsDetailDao.countByJadwalIdAndKrsAndStatusAndTahunAkademik(mdlnilcounttugas.getIdJadwal(), k, StatusRecord.AKTIF, ta);
//                        if (jmlData.compareTo(Long.valueOf(1)) > 0) {
//                            Object idKrsDetail = krsDetailDao.getKrsDetailId(j, mahasiswa);
//                            List<KrsDetail> cariDouble = krsDetailDao.findByStatusAndJadwalAndMahasiswaAndIdNotIn(StatusRecord.AKTIF, j, mahasiswa, idKrsDetail);
//                            for (KrsDetail thekrsDetail : cariDouble) {
//                                thekrsDetail.setStatus(StatusRecord.HAPUS);
//                                krsDetailDao.save(thekrsDetail);
//                                System.out.println("KRS DETAIL DOUBLE TERHAPUS == " + thekrsDetail.getId());
//                            }
//
//                            KrsDetail krsDetail1 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
//                            if (krsDetail1 != null){
//                                krsDetail1.setNilaiTugas(mdlnilcounttugas.getNilaiAkhir());
//                                krsDetailDao.save(krsDetail1);
//                                System.out.println(" NILAI TUGAS UPDATED == " + mdlnilcounttugas.getId());
//                            }
//
//                        }
//
//                        if (jmlData.compareTo(Long.valueOf(1)) == 0) {
//
//                            KrsDetail krsDetail1 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
//                            if (krsDetail1 != null){
//                                krsDetail1.setNilaiTugas(mdlnilcounttugas.getNilaiAkhir());
//                                krsDetailDao.save(krsDetail1);
//                                System.out.println(" NILAI TUGAS UPDATED == " + mdlnilcounttugas.getId());
//                            }
//                        }
//
//                    }
//                }
//            }
//
//        }
//
//
////        //uts
//        List<MdlGradeGradesDto> daftarNilaiUts = getNilaiUts();
//        for (MdlGradeGradesDto mdlniluts : daftarNilaiUts){
//            Jadwal j = jadwalDao.findById(mdlniluts.getIdJadwal()).get();
//
//            if (mdlniluts.getMahasiswa() != null) {
//                User user = userDao.findByUsername(mdlniluts.getMahasiswa());
//
//                if (user != null) {
//                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
//                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
//                    if (k != null){
//
//                        KrsDetail krsDetail2 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
//                        if  (krsDetail2 != null){
//                            krsDetail2.setNilaiUts(mdlniluts.getNilai());
//                            krsDetailDao.save(krsDetail2);
//                            System.out.println(" NILAI UTS UPDATED == " + mdlniluts.getId());
//                        }
//                    }
//
//                }
//            }
//
//        }
//
//
//
//
//
//        //uas
//        List<MdlGradeGradesDto> daftarNilaiUas = getNilaiUas();
//        for (MdlGradeGradesDto mdlniluas : daftarNilaiUas){
//            Jadwal j = jadwalDao.findById(mdlniluas.getIdJadwal()).get();
//
//            if (mdlniluas.getMahasiswa() != null) {
//                User user = userDao.findByUsername(mdlniluas.getMahasiswa());
//
//                if (user != null) {
//                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
//                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
//                    if (k != null){
//
//                        KrsDetail krsDetail2 = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
//                        if  (krsDetail2 != null){
//                            krsDetail2.setNilaiUas(mdlniluas.getNilai());
//                            krsDetailDao.save(krsDetail2);
//                            System.out.println(" NILAI UAS UPDATED == " + mdlniluas.getId());
//
//                            BigDecimal nilaiUas = krsDetail2.getNilaiUas().multiply(krsDetail2.getJadwal().getBobotUas()).divide(new BigDecimal(100));
//                            BigDecimal nilaiUts = krsDetail2.getNilaiUts().multiply(krsDetail2.getJadwal().getBobotUts()).divide(new BigDecimal(100));
//                            krsDetail2.setNilaiAkhir(krsDetail2.getNilaiTugas().add(nilaiUts).add(krsDetail2.getNilaiPresensi()).add(nilaiUas));
//                            scoreService.hitungNilaiAkhir(krsDetail2);
//                            System.out.println("SEMUA NILAI TERHITUNG == " + krsDetail2.getNilaiAkhir());
//                        }
//
//
//                    }
//
//                }
//            }
//
//        }
//
//
//
//
//        //krs nilai tugas
//        List<MdlGradeGradesDto> daftarNilaiTugas = getNilaiTugas();
//        for (MdlGradeGradesDto mdlniltug : daftarNilaiTugas){
//            Jadwal j = jadwalDao.findById(mdlniltug.getIdJadwal()).get();
//
//            if (mdlniltug.getMahasiswa() != null) {
//                User user = userDao.findByUsername(mdlniltug.getMahasiswa());
//
//                if (user != null) {
//                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
//                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
////                    KrsDetail idKrsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, j, StatusRecord.AKTIF, k, ta);
//                    Object idKrsDetail = krsDetailDao.getKrsDetailId2(j, mahasiswa, k, ta);
//
//
//                    KrsNilaiTugasMoodle nt = new KrsNilaiTugasMoodle();
//                    if (idKrsDetail != null){
//                        nt.setId(mdlniltug.getId());
//                        nt.setKrsDetail(krsDetailDao.findById(idKrsDetail.toString()).get());
//                        nt.setBobotTugas(bobotTugasDao.findById(mdlniltug.getIdBobotTugas().toString()).get());
//                        nt.setNilai(mdlniltug.getNilai());
//                        nt.setStatus(StatusRecord.valueOf(mdlniltug.getStatus()));
//                        nt.setNilaiAkhir(mdlniltug.getNilaiAkhir());
//                        krsNilaiTugasMoodleDao.save(nt);
//                        System.out.println("NILAI TUGAS AWAL TERSIMPAN == " + mdlniltug.getId());
//                    }
//
//                }
//
////                if (user != null) {
////                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
////                    Object idKrsDetail = krsDetailDao.getKrsDetailId(j, mahasiswa);
////                    KrsNilaiTugasMoodle nt = new KrsNilaiTugasMoodle();
////
////                    if (idKrsDetail != null){
////                        nt.setId(mdlniltug.getId());
////                        nt.setKrsDetail(krsDetailDao.findById(idKrsDetail.toString()).get());
////                        nt.setBobotTugas(bobotTugasDao.findById(mdlniltug.getIdBobotTugas().toString()).get());
////                        nt.setNilai(mdlniltug.getFinalGrade());
////                        nt.setStatus(StatusRecord.valueOf(mdlniltug.getStatus()));
////                        nt.setNilaiAkhir(mdlniltug.getNilaiAkhir());
////                        krsNilaiTugasMoodleDao.save(nt);
////                        System.out.println(" NILAI TERSIMPAN == " + mdlniltug.getId());
////                    }
////
////                }
//
//            }
//        }
//
//    }

    public void syncPresensiMoodle(@RequestParam LocalDate tanggalImport){
        // 1. Cari kelas yang jadwalnya sekarang

        // 2. Loop masing-masing jadwal,
        //    - kalau sudah pernah ditarik, skip
        //    - kalau masih dalam toleransi telat,
        //      ngga usah ditarik dulu data presensinya

        // 3. Insert data presensi ke database smile

        List<MdlAttendanceLogDosenDto> daftarPresensiDosen = getAttendanceDosen(tanggalImport);

        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        for (MdlAttendanceLogDosenDto mdldos : daftarPresensiDosen) {
            System.out.println("PROGRESS INPUT DOSEN");
            System.out.println(mdldos.getIdDosen());
            if (mdldos.getIdDosen() != null) {
                Karyawan karyawan = karyawanDao.findByEmail(mdldos.getIdDosen());
                Dosen dosen = dosenDao.findByKaryawan(karyawan);
                if (karyawan != null) {
                    if (dosen != null) {

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        LocalDate timeIn = LocalDate.parse(mdldos.getWaktuMasuk(), formatter);
                        LocalDate timeOut = LocalDate.parse(mdldos.getWaktuSelesai(), formatter);
                        LocalDate timeIp = LocalDate.parse(mdldos.getTanggalInput(), formatter);

                        LocalTime jamNya = LocalTime.of(00,00,00);


                        PresensiDosen pd = new PresensiDosen();

                        SesiKuliah sesiKuliah = new SesiKuliah();

//                        Jadwal jadwal = jadwalDao.findById(mdldos.getIdJadwal()).get();
//                        Jadwal jadwal = jadwalDao.findByIdNumberElearningAndTahunAkademikAndStatus(mdldos.getIdJadwal(),ta, StatusRecord.AKTIF);
                        List<Jadwal> jadwalList1 = jadwalDao.findByTahunAkademikAndIdNumberElearningAndStatus(ta,mdldos.getIdJadwal(),StatusRecord.AKTIF);


                        if (jadwalList1 != null) {
                            for (Jadwal jadwal : jadwalList1){
                                pd.setTahunAkademik(tahunAkademikDao.findById(mdldos.getIdTahunAkademik()).get());
                                pd.setJadwal(jadwal);
                                if (jadwal.getJamMulai() != null){
                                    pd.setWaktuMasuk(LocalDateTime.of(timeIn,jadwal.getJamMulai()));
                                }
                                if (jadwal.getJamMulai() == null){
                                    pd.setWaktuMasuk(LocalDateTime.of(timeIn,jamNya ));
                                }
                                if (jadwal.getJamSelesai() != null){
                                    pd.setWaktuSelesai(LocalDateTime.of(timeOut,jadwal.getJamSelesai()));
                                }
                                if (jadwal.getJamSelesai() == null){
                                    pd.setWaktuSelesai(LocalDateTime.of(timeOut,jamNya ));
                                }
                                pd.setStatusPresensi(StatusPresensi.valueOf(mdldos.getStatusPresensi()));
                                pd.setStatus(StatusRecord.valueOf(mdldos.getStatus()));
                                pd.setDosen(dosen);
                                presensiDosenDao.save(pd);
//                            update(mdldos.getIdLog());
                                System.out.println("INPUT DOSEN SUKSES  = " + mdldos.getIdDosen());
                                System.out.println("ID LOG  =  " + mdldos.getIdLog());
                                if (jadwal.getJamMulai() != null){
                                    System.out.println(" JAM MULAI == " + LocalDateTime.of(timeIn,jadwal.getJamMulai()));
                                }
                                if (jadwal.getJamMulai() == null){
                                    System.out.println(" JAM MULAI NULL ");
                                }
                                if (jadwal.getJamSelesai() != null){
                                    System.out.println(" JAM SELESAI == " + LocalDateTime.of(timeIn,jadwal.getJamSelesai()));
                                }
                                if (jadwal.getJamSelesai() == null){
                                    System.out.println(" JAM SELESAI NULL");
                                }

                                AttendanceImportBerhasil ib = new AttendanceImportBerhasil();
                                ib.setTahunAkademik(tahunAkademikDao.findById(mdldos.getIdTahunAkademik()).get());
                                ib.setJadwal(jadwal);
                                ib.setKelas(jadwal.getKelas().getNamaKelas());
                                ib.setDosen(dosen);
                                if (jadwal.getJamMulai() != null){
                                    ib.setWaktuMasuk(LocalDateTime.of(timeIn,jadwal.getJamMulai()));
                                }
                                if (jadwal.getJamMulai() == null){
                                    ib.setWaktuMasuk(LocalDateTime.of(timeIn,jamNya ));
                                }
                                if (jadwal.getJamSelesai() != null){
                                    ib.setWaktuSelesai(LocalDateTime.of(timeOut,jadwal.getJamSelesai()));
                                }
                                if (jadwal.getJamSelesai() == null){
                                    ib.setWaktuSelesai(LocalDateTime.of(timeOut,jamNya ));
                                }
                                ib.setTanggalImport(LocalDateTime.of(timeIp, jamNya));
                                ib.setStatus(StatusRecord.AKTIF);
                                listImportBerhasilDao.save(ib);
                                System.out.println("Tanggal Input =" + LocalDateTime.of(timeIp,jamNya));

                            }


                            //remove html tag
                            String strHTML = mdldos.getBeritaAcara();
                            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(strHTML);

                            org.jsoup.nodes.Document.OutputSettings outputSettings = new org.jsoup.nodes.Document.OutputSettings();
                            outputSettings.prettyPrint(false);
                            jsoupDoc.outputSettings(outputSettings);
                            jsoupDoc.select("br").before("\\n");
                            jsoupDoc.select("p").before("\\n");

                            String str = jsoupDoc.html().replaceAll("\\\\n", "");
                            String strWithNewLines = Jsoup.clean(str, "", Whitelist.none(), outputSettings);




                            if (jadwalList1 != null) {
                                sesiKuliah.setJadwal(pd.getJadwal());
                                sesiKuliah.setPresensiDosen(pd);
                                sesiKuliah.setWaktuMulai(pd.getWaktuMasuk());
                                sesiKuliah.setWaktuSelesai(pd.getWaktuSelesai());
                                sesiKuliah.setBeritaAcara(strWithNewLines);
                                sesiKuliahDao.save(sesiKuliah);
                                System.out.println("INPUT SESI KULIAH SUKSES  = " + pd.getJadwal());
                                }
                            }


                        List<MdlAttendanceLogMahasiswaDto> daftarPresensiMahasiswa = getAttendanceMahasiswa2(mdldos.getIdSession());
                        for (MdlAttendanceLogMahasiswaDto mdlmah : daftarPresensiMahasiswa) {

//                            Jadwal j = jadwalDao.findById(mdldos.getIdJadwal()).get();
                            Jadwal j = jadwalDao.findByIdNumberElearningAndTahunAkademikAndStatus(mdldos.getIdJadwal(), ta, StatusRecord.AKTIF);


                        if (j != null) {
                            if (mdlmah.getMahasiswa() != null) {
                                DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                LocalDateTime mahasiswaIn = LocalDateTime.parse(mdlmah.getWaktuMasuk(), formatter2);
                                LocalDateTime mahasiswaOut = LocalDateTime.parse(mdlmah.getWaktuSelesai(), formatter2);
                                User user = userDao.findByUsername(mdlmah.getMahasiswa());
                                if (user != null) {
                                    Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);

                                    if (k != null) {
                                        Long jmlData = krsDetailDao.countKrsDetail2(mdlmah.getIdJadwal(), mahasiswa, ta, StatusRecord.AKTIF);
//                                        Long jmlData = krsDetailDao.countByJadwalIdAndKrsAndStatusAndTahunAkademik(mdlmah.getIdJadwal(), k, StatusRecord.AKTIF, ta);
                                        System.out.println("INPUT MAHASISWA PROGRESS");
                                        System.out.println(" JUMLAH KRS = " + jmlData);
                                        System.out.println(" NIM = " + mahasiswa.getId() + "     " + " JADWAL = " + mdlmah.getIdJadwal());


                                        if (jmlData.compareTo(Long.valueOf(1)) > 0) {
                                            Object idKrsDetail = krsDetailDao.getKrsDetailId(j, mahasiswa);
                                            List<KrsDetail> cariDouble = krsDetailDao.findByStatusAndJadwalAndMahasiswaAndIdNot(StatusRecord.AKTIF, j, mahasiswa, idKrsDetail);
                                            for (KrsDetail thekrsDetail : cariDouble) {
                                                thekrsDetail.setStatus(StatusRecord.HAPUS);
                                                krsDetailDao.save(thekrsDetail);
                                            }

                                            PresensiMahasiswa pm = new PresensiMahasiswa();
                                            pm.setMahasiswa(mahasiswa);
                                            pm.setKrsDetail(krsDetailDao.findById(idKrsDetail.toString()).get());
                                            pm.setSesiKuliah(sesiKuliah);
                                            pm.setWaktuMasuk(mahasiswaIn);
                                            pm.setWaktuKeluar(mahasiswaOut);
                                            if (mdlmah.getStatusPresensi().equals("Present")) {
                                                pm.setStatusPresensi(StatusPresensi.HADIR);
                                            }
                                            if (mdlmah.getStatusPresensi().equals("Late")) {
                                                pm.setStatusPresensi(StatusPresensi.TERLAMBAT);
                                            }

                                            if (mdlmah.getStatusPresensi().equals("Absent")) {
                                                pm.setStatusPresensi(StatusPresensi.MANGKIR);
                                            }

                                            if (mdlmah.getStatusPresensi().equals("Excused")) {
                                                pm.setStatusPresensi(StatusPresensi.IZIN);
                                            }
                                            pm.setStatus(StatusRecord.valueOf(mdlmah.getStatus()));
                                            presensiMahasiswaDao.save(pm);
                                            System.out.println("INPUT MAHASISWA SUKSES  =" + "NIM = " + mahasiswa.getId() + "     " + " JADWAL = " + mdlmah.getIdJadwal());

                                        }

                                        if (jmlData.compareTo(Long.valueOf(1)) == 0) {
                                            PresensiMahasiswa pm = new PresensiMahasiswa();
                                            pm.setMahasiswa(mahasiswa);
                                            KrsDetail krsDetail = krsDetailDao.getKrsDetail4(mdlmah.getIdJadwal(), mahasiswa, k, ta, StatusRecord.AKTIF);
//                                            KrsDetail krsDetail = krsDetailDao.findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(mahasiswa, jadwalDao.findById(mdlmah.getIdJadwal()).get(), StatusRecord.AKTIF, k, ta);
                                            pm.setKrsDetail(krsDetail);
                                            pm.setSesiKuliah(sesiKuliah);
                                            pm.setWaktuMasuk(mahasiswaIn);
                                            pm.setWaktuKeluar(mahasiswaOut);
                                            if (mdlmah.getStatusPresensi().equals("Present")) {
                                                pm.setStatusPresensi(StatusPresensi.HADIR);
                                            }
                                            if (mdlmah.getStatusPresensi().equals("Late")) {
                                                pm.setStatusPresensi(StatusPresensi.TERLAMBAT);
                                            }

                                            if (mdlmah.getStatusPresensi().equals("Absent")) {
                                                pm.setStatusPresensi(StatusPresensi.MANGKIR);
                                            }

                                            if (mdlmah.getStatusPresensi().equals("Excused")) {
                                                pm.setStatusPresensi(StatusPresensi.IZIN);
                                            }
                                            pm.setStatus(StatusRecord.valueOf(mdlmah.getStatus()));
                                            presensiMahasiswaDao.save(pm);
                                            System.out.println("INPUT MAHASISWA SUKSES  =" + "NIM = " + mahasiswa.getId() + "     " + " JADWAL = " + mdlmah.getIdJadwal());

                                        }


//                                        if (jmlData.compareTo(Long.valueOf(1)) < 0) {
//                                            KrsDetail kd = new KrsDetail();
//                                            kd.setJadwal(j);
//                                            kd.setKrs(k);
//                                            kd.setMahasiswa(mahasiswa);
//                                            kd.setMatakuliahKurikulum(j.getMatakuliahKurikulum());
//                                            kd.setNilaiPresensi(BigDecimal.ZERO);
//                                            kd.setNilaiTugas(BigDecimal.ZERO);
//                                            kd.setNilaiUas(BigDecimal.ZERO);
//                                            kd.setNilaiUts(BigDecimal.ZERO);
//                                            kd.setFinalisasi("N");
//                                            kd.setJumlahMangkir(0);
//                                            kd.setJumlahKehadiran(0);
//                                            kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
//                                            kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
//                                            kd.setJumlahTerlambat(0);
//                                            kd.setJumlahIzin(0);
//                                            kd.setJumlahSakit(0);
//                                            kd.setStatusEdom(StatusRecord.UNDONE);
//                                            kd.setTahunAkademik(ta);
//                                            krsDetailDao.save(kd);
//
//                                            PresensiMahasiswa pm = new PresensiMahasiswa();
//                                            pm.setMahasiswa(mahasiswa);
//                                            pm.setKrsDetail(kd);
//                                            pm.setSesiKuliah(sesiKuliah);
//                                            pm.setWaktuMasuk(mahasiswaIn);
//                                            pm.setWaktuKeluar(mahasiswaOut);
//                                            if (mdlmah.getStatusPresensi().equals("Present")) {
//                                                pm.setStatusPresensi(StatusPresensi.HADIR);
//                                            }
//                                            if (mdlmah.getStatusPresensi().equals("Late")) {
//                                                pm.setStatusPresensi(StatusPresensi.TERLAMBAT);
//                                            }
//
//                                            if (mdlmah.getStatusPresensi().equals("Absent")) {
//                                                pm.setStatusPresensi(StatusPresensi.MANGKIR);
//                                            }
//
//                                            if (mdlmah.getStatusPresensi().equals("Excused")) {
//                                                pm.setStatusPresensi(StatusPresensi.IZIN);
//                                            }
//                                            pm.setStatus(StatusRecord.valueOf(mdlmah.getStatus()));
//                                            presensiMahasiswaDao.save(pm);
//                                        }
                                    } else {
                                        System.out.printf("Belum bayaran  > ");
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

//    //manual1
//
////    @Scheduled(cron = "0 32 10 * * ? ", zone = "Asia/Jakarta")
////    @Scheduled(fixedDelay = 60*60)
//    public void syncPresensiMoodle2(){
//        // 1. Cari kelas yang jadwalnya sekarang
//
//        // 2. Loop masing-masing jadwal,
//        //    - kalau sudah pernah ditarik, skip
//        //    - kalau masih dalam toleransi telat,
//        //      ngga usah ditarik dulu data presensinya
//
//        // 3. Insert data presensi ke database smile
//
//        List<MdlAttendanceLogDosenDto> daftarPresensiDosen = getAttendanceDosen2();
//
//        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
//
//        for (MdlAttendanceLogDosenDto mdldos : daftarPresensiDosen) {
//            System.out.println("PROGRESS INPUT DOSEN");
//            System.out.println(mdldos.getIdDosen());
//            if (mdldos.getIdDosen() != null) {
//                Karyawan karyawan = karyawanDao.findByEmail(mdldos.getIdDosen());
//                Dosen dosen = dosenDao.findByKaryawan(karyawan);
//                if (karyawan != null) {
//                    if (dosen != null) {
//
//                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                        LocalDate timeIn = LocalDate.parse(mdldos.getWaktuMasuk(), formatter);
//                        LocalDate timeOut = LocalDate.parse(mdldos.getWaktuSelesai(), formatter);
//
//                        LocalTime jamNya = LocalTime.of(00,00,00);
//
//
//                        PresensiDosen pd = new PresensiDosen();
//
//                        SesiKuliah sesiKuliah = new SesiKuliah();
//
////                        Jadwal jadwal = jadwalDao.findByIdNumberElearningAndTahunAkademikAndStatus(mdldos.getIdJadwal(),ta, StatusRecord.AKTIF);
//                        List<Jadwal> jadwalList1 = jadwalDao.findByTahunAkademikAndIdNumberElearningAndStatus(ta,mdldos.getIdJadwal(),StatusRecord.AKTIF);
//
//
//                        if (jadwalList1 != null) {
//                        for (Jadwal jadwal : jadwalList1){
//                                pd.setTahunAkademik(jadwal.getTahunAkademik());
//                                pd.setJadwal(jadwal);
////                            pd.setJadwal(jadwalDao.findByIdNumberElearning(mdldos.getIdJadwal()));
//                                if (jadwal.getJamMulai() != null){
//                                    pd.setWaktuMasuk(LocalDateTime.of(timeIn,jadwal.getJamMulai()));
//                                }
//                                if (jadwal.getJamMulai() == null){
//                                    pd.setWaktuMasuk(LocalDateTime.of(timeIn,jamNya ));
//                                }
//                                if (jadwal.getJamSelesai() != null){
//                                    pd.setWaktuSelesai(LocalDateTime.of(timeOut,jadwal.getJamSelesai()));
//                                }
//                                if (jadwal.getJamSelesai() == null){
//                                    pd.setWaktuSelesai(LocalDateTime.of(timeOut,jamNya ));
//                                }
//                                pd.setStatusPresensi(StatusPresensi.valueOf(mdldos.getStatusPresensi()));
//                                pd.setStatus(StatusRecord.valueOf(mdldos.getStatus()));
//                                pd.setDosen(dosen);
//                                presensiDosenDao.save(pd);
////                            update(mdldos.getIdLog());
//                                System.out.println("INPUT DOSEN SUKSES  = " + mdldos.getIdDosen());
//                                System.out.println("ID LOG  =  " + mdldos.getIdLog());
//                                if (jadwal.getJamMulai() != null){
//                                    System.out.println(" JAM MULAI == " + LocalDateTime.of(timeIn,jadwal.getJamMulai()));
//                                }
//                                if (jadwal.getJamMulai() == null){
//                                    System.out.println(" JAM MULAI NULL ");
//                                }
//                                if (jadwal.getJamSelesai() != null){
//                                    System.out.println(" JAM SELESAI == " + LocalDateTime.of(timeIn,jadwal.getJamSelesai()));
//                                }
//                                if (jadwal.getJamSelesai() == null){
//                                    System.out.println(" JAM SELESAI NULL");
//                                }
//
//                            //remove html tag
//                            String strHTML = mdldos.getBeritaAcara();
//                            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(strHTML);
//
//                            org.jsoup.nodes.Document.OutputSettings outputSettings = new org.jsoup.nodes.Document.OutputSettings();
//                            outputSettings.prettyPrint(false);
//                            jsoupDoc.outputSettings(outputSettings);
//                            jsoupDoc.select("br").before("\\n");
//                            jsoupDoc.select("p").before("\\n");
//
//                            String str = jsoupDoc.html().replaceAll("\\\\n", "");
//                            String strWithNewLines = Jsoup.clean(str, "", Whitelist.none(), outputSettings);
//
//
//
//
//
//                            if (jadwalList1 != null) {
//                                sesiKuliah.setJadwal(pd.getJadwal());
//                                sesiKuliah.setPresensiDosen(pd);
//                                sesiKuliah.setWaktuMulai(pd.getWaktuMasuk());
//                                sesiKuliah.setWaktuSelesai(pd.getWaktuSelesai());
//                                sesiKuliah.setBeritaAcara(strWithNewLines);
//                                sesiKuliahDao.save(sesiKuliah);
//                                System.out.println("INPUT SESI KULIAH SUKSES  = " + pd.getJadwal());
//                            }
//
//
//
//                            }
//                        }
//
//
//
//
//
//                        List<MdlAttendanceLogMahasiswaDto> daftarPresensiMahasiswa = getAttendanceMahasiswa2(mdldos.getIdSession());
//                        for (MdlAttendanceLogMahasiswaDto mdlmah : daftarPresensiMahasiswa) {
//
////                            Jadwal j = jadwalDao.findById(mdldos.getIdJadwal()).get();
//                            Jadwal j = jadwalDao.findByIdNumberElearningAndTahunAkademikAndStatus(mdldos.getIdJadwal(), ta, StatusRecord.AKTIF);
//
//
//                            if (j != null) {
//                                if (mdlmah.getMahasiswa() != null) {
//                                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//                                    LocalDateTime mahasiswaIn = LocalDateTime.parse(mdlmah.getWaktuMasuk(), formatter2);
//                                    LocalDateTime mahasiswaOut = LocalDateTime.parse(mdlmah.getWaktuSelesai(), formatter2);
//                                    User user = userDao.findByUsername(mdlmah.getMahasiswa());
//                                    if (user != null) {
//                                        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
//                                        Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mahasiswa, ta, StatusRecord.AKTIF);
//
//                                        if (k != null) {
//                                            System.out.println("krs ==" + k.getId());
//                                            Long jmlData = krsDetailDao.countKrsDetail2(mdlmah.getIdJadwal(), mahasiswa, ta, StatusRecord.AKTIF);
//                                            System.out.println("INPUT MAHASISWA PROGRESS");
//                                            System.out.println(" JUMLAH KRS = " + jmlData);
//                                            System.out.println(" NIM = " + mahasiswa.getId() + "     " + " JADWAL = " + mdlmah.getIdJadwal());
//
//
//                                            if (jmlData.compareTo(Long.valueOf(1)) > 0) {
//                                                Object idKrsDetail = krsDetailDao.getKrsDetailId(j, mahasiswa);
//                                                List<KrsDetail> cariDouble = krsDetailDao.findByStatusAndJadwalAndMahasiswaAndIdNotIn(StatusRecord.AKTIF, j, mahasiswa, idKrsDetail);
//                                                for (KrsDetail thekrsDetail : cariDouble) {
//                                                    thekrsDetail.setStatus(StatusRecord.HAPUS);
//                                                    krsDetailDao.save(thekrsDetail);
//                                                }
//
//                                                PresensiMahasiswa pm = new PresensiMahasiswa();
//                                                pm.setMahasiswa(mahasiswa);
//                                                pm.setKrsDetail(krsDetailDao.findById(idKrsDetail.toString()).get());
//                                                pm.setSesiKuliah(sesiKuliah);
//                                                pm.setWaktuMasuk(mahasiswaIn);
//                                                pm.setWaktuKeluar(mahasiswaOut);
//                                                if (mdlmah.getStatusPresensi().equals("Present")) {
//                                                    pm.setStatusPresensi(StatusPresensi.HADIR);
//                                                }
//                                                if (mdlmah.getStatusPresensi().equals("Late")) {
//                                                    pm.setStatusPresensi(StatusPresensi.TERLAMBAT);
//                                                }
//
//                                                if (mdlmah.getStatusPresensi().equals("Absent")) {
//                                                    pm.setStatusPresensi(StatusPresensi.MANGKIR);
//                                                }
//
//                                                if (mdlmah.getStatusPresensi().equals("Excused")) {
//                                                    pm.setStatusPresensi(StatusPresensi.IZIN);
//                                                }
//                                                pm.setStatus(StatusRecord.valueOf(mdlmah.getStatus()));
//                                                presensiMahasiswaDao.save(pm);
//                                                System.out.println("INPUT MAHASISWA SUKSES  =" + "NIM = " + mahasiswa.getId() + "     " + " JADWAL = " + mdlmah.getIdJadwal());
//
//                                            }
//
//                                            if (jmlData.compareTo(Long.valueOf(1)) == 0) {
////                                            Jadwal j2 = jadwalDao.findByIdNumberElearningAndStatus(mdlmah.getIdJadwal(), StatusRecord.AKTIF);
//                                                PresensiMahasiswa pm = new PresensiMahasiswa();
//                                                pm.setMahasiswa(mahasiswa);
//                                                KrsDetail krsDetail = krsDetailDao.getKrsDetail4(mdlmah.getIdJadwal(), mahasiswa, k, ta, StatusRecord.AKTIF);
////                                                System.out.println("THE KRS DETAILA ==" + krsDetail.toString());
//                                                pm.setKrsDetail(krsDetail);
//                                                pm.setSesiKuliah(sesiKuliah);
//                                                pm.setWaktuMasuk(mahasiswaIn);
//                                                pm.setWaktuKeluar(mahasiswaOut);
//                                                if (mdlmah.getStatusPresensi().equals("Present")) {
//                                                    pm.setStatusPresensi(StatusPresensi.HADIR);
//                                                }
//                                                if (mdlmah.getStatusPresensi().equals("Late")) {
//                                                    pm.setStatusPresensi(StatusPresensi.TERLAMBAT);
//                                                }
//
//                                                if (mdlmah.getStatusPresensi().equals("Absent")) {
//                                                    pm.setStatusPresensi(StatusPresensi.MANGKIR);
//                                                }
//
//                                                if (mdlmah.getStatusPresensi().equals("Excused")) {
//                                                    pm.setStatusPresensi(StatusPresensi.IZIN);
//                                                }
//                                                pm.setStatus(StatusRecord.valueOf(mdlmah.getStatus()));
//                                                presensiMahasiswaDao.save(pm);
//                                                System.out.println("INPUT MAHASISWA SUKSES  =" + "NIM = " + mahasiswa.getId() + "     " + " JADWAL = " + mdlmah.getIdJadwal());
//                                            }
//
//
//                                            if (jmlData.compareTo(Long.valueOf(1)) < 0) {
//                                                KrsDetail kd = new KrsDetail();
//                                                kd.setJadwal(j);
//                                                kd.setKrs(k);
//                                                kd.setMahasiswa(mahasiswa);
//                                                kd.setMatakuliahKurikulum(j.getMatakuliahKurikulum());
//                                                kd.setNilaiPresensi(BigDecimal.ZERO);
//                                                kd.setNilaiTugas(BigDecimal.ZERO);
//                                                kd.setNilaiUas(BigDecimal.ZERO);
//                                                kd.setNilaiUts(BigDecimal.ZERO);
//                                                kd.setFinalisasi("N");
//                                                kd.setJumlahMangkir(0);
//                                                kd.setJumlahKehadiran(0);
//                                                kd.setKodeUts(RandomStringUtils.randomAlphanumeric(5));
//                                                kd.setKodeUas(RandomStringUtils.randomAlphanumeric(5));
//                                                kd.setJumlahTerlambat(0);
//                                                kd.setJumlahIzin(0);
//                                                kd.setJumlahSakit(0);
//                                                kd.setStatusEdom(StatusRecord.UNDONE);
//                                                kd.setTahunAkademik(ta);
//                                                krsDetailDao.save(kd);
//
//                                                PresensiMahasiswa pm = new PresensiMahasiswa();
//                                                pm.setMahasiswa(mahasiswa);
//                                                pm.setKrsDetail(kd);
//                                                pm.setSesiKuliah(sesiKuliah);
//                                                pm.setWaktuMasuk(mahasiswaIn);
//                                                pm.setWaktuKeluar(mahasiswaOut);
//                                                if (mdlmah.getStatusPresensi().equals("Present")) {
//                                                    pm.setStatusPresensi(StatusPresensi.HADIR);
//                                                }
//                                                if (mdlmah.getStatusPresensi().equals("Late")) {
//                                                    pm.setStatusPresensi(StatusPresensi.TERLAMBAT);
//                                                }
//
//                                                if (mdlmah.getStatusPresensi().equals("Absent")) {
//                                                    pm.setStatusPresensi(StatusPresensi.MANGKIR);
//                                                }
//
//                                                if (mdlmah.getStatusPresensi().equals("Excused")) {
//                                                    pm.setStatusPresensi(StatusPresensi.IZIN);
//                                                }
//                                                pm.setStatus(StatusRecord.valueOf(mdlmah.getStatus()));
//                                                presensiMahasiswaDao.save(pm);
//                                            }
//                                        } else {
//                                            System.out.printf("Belum bayaran  > ");
//                                        }
//
//
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//
//                }
//
//            }
//        }
//
//
//    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Jakarta")
    public void syncBeritaAcaraMoodle(){
        // 1. Query ambil jadwal hari ini
        // 2. Tarik berita acara untuk tiap jadwal
        // 3. Insert ke smile
    }



}
