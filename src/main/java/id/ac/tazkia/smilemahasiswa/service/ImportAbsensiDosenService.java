package id.ac.tazkia.smilemahasiswa.service;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ImportAbsensiDosenService {

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ProsesBackgroundDao prosesBackgroundDao;

    @Autowired
    private ProsesBackgroundAbsensiElearningDao prosesBackgroundAbsensiElearningDao;

    @Autowired
    private ProsesBackgroundAbsensiDosenDao prosesBackgroundAbsensiDosenDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private AttendanceImportBerhasilDao listImportBerhasilDao;

    @Autowired
    private SesiKuliahDao sesiKuliahDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private KrsDao krsDao;


    WebClient webClient1 = WebClient.builder()
            .baseUrl("https://elearning.tazkia.ac.id")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public List<MdlAttendanceLogDosenDto> getAttendanceDosen2() {
        return webClient1.get()
                .uri("/api/sessiondosen2")
                .retrieve().bodyToFlux(id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto.class)
                .collectList()
                .block();
    }

    public List<MdlAttendanceLogDosenDto> getAttendanceDosen3(@RequestParam String jadwal) {
        return webClient1.get()
                .uri("/api/sessiondosen3?jadwal=" + jadwal)
                .retrieve().bodyToFlux(id.ac.tazkia.smilemahasiswa.dto.elearning.MdlAttendanceLogDosenDto.class)
                .collectList()
                .block();
    }

    public List<MdlAttendanceLogMahasiswaDto> getAttendanceMahasiswa2(@RequestParam String id) {
        return webClient1.get()
                .uri("/api/sessionmahasiswa2?id=" + id)
                .retrieve().bodyToFlux(MdlAttendanceLogMahasiswaDto.class)
                .collectList()
                .block();

    }

    @Scheduled(fixedDelay = 1000)
    public void ambilDataProses(){

        ProsesBackgroundAbsensiDosen prosesBackgroundAbsensiDosen = prosesBackgroundAbsensiDosenDao.findFirstByStatusOrderByTanggalInputDesc(StatusRecord.WAITING);
        if (prosesBackgroundAbsensiDosen != null){
            if (prosesBackgroundAbsensiDosen.getNamaProses().equals("ABSENSI")){
                prosesBackgroundAbsensiDosen.setTanggalMulai(LocalDateTime.now());
                prosesBackgroundAbsensiDosen.setStatus(StatusRecord.ON_PROCESS);
                prosesBackgroundAbsensiDosenDao.save(prosesBackgroundAbsensiDosen);
                importAbsensiElearning(prosesBackgroundAbsensiDosen.getProdi(),prosesBackgroundAbsensiDosen.getJadwal(), prosesBackgroundAbsensiDosen);
            }else{
                throw new UnsupportedOperationException();
            }
        }

    }

    public void importAbsensiElearning(@RequestParam(required = true) String prodi,@RequestParam(required = false) String jadwal, @RequestParam(required = true) ProsesBackgroundAbsensiDosen prosesBackgroundAbsensiDosen) {

        Jadwal jadwalIdNum = jadwalDao.findByStatusAndId(StatusRecord.AKTIF, jadwal);
        List<MdlAttendanceLogDosenDto> daftarPresensiDosen = getAttendanceDosen3(jadwalIdNum.getIdNumberElearning());

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
                            for (Jadwal jadwal2 : jadwalList1){
                                pd.setTahunAkademik(tahunAkademikDao.findById(mdldos.getIdTahunAkademik()).get());
                                pd.setJadwal(jadwal2);
                                if (jadwal2.getJamMulai() != null){
                                    pd.setWaktuMasuk(LocalDateTime.of(timeIn,jadwal2.getJamMulai()));
                                }
                                if (jadwal2.getJamMulai() == null){
                                    pd.setWaktuMasuk(LocalDateTime.of(timeIn,jamNya ));
                                }
                                if (jadwal2.getJamSelesai() != null){
                                    pd.setWaktuSelesai(LocalDateTime.of(timeOut,jadwal2.getJamSelesai()));
                                }
                                if (jadwal2.getJamSelesai() == null){
                                    pd.setWaktuSelesai(LocalDateTime.of(timeOut,jamNya ));
                                }
                                pd.setStatusPresensi(StatusPresensi.valueOf(mdldos.getStatusPresensi()));
                                pd.setStatus(StatusRecord.valueOf(mdldos.getStatus()));
                                pd.setDosen(dosen);
                                presensiDosenDao.save(pd);
//                            update(mdldos.getIdLog());
                                System.out.println("INPUT DOSEN SUKSES  = " + mdldos.getIdDosen());
                                System.out.println("ID LOG  =  " + mdldos.getIdLog());
                                if (jadwal2.getJamMulai() != null){
                                    System.out.println(" JAM MULAI == " + LocalDateTime.of(timeIn,jadwal2.getJamMulai()));
                                }
                                if (jadwal2.getJamMulai() == null){
                                    System.out.println(" JAM MULAI NULL ");
                                }
                                if (jadwal2.getJamSelesai() != null){
                                    System.out.println(" JAM SELESAI == " + LocalDateTime.of(timeIn,jadwal2.getJamSelesai()));
                                }
                                if (jadwal2.getJamSelesai() == null){
                                    System.out.println(" JAM SELESAI NULL");
                                }

                                AttendanceImportBerhasil ib = new AttendanceImportBerhasil();
                                ib.setTahunAkademik(tahunAkademikDao.findById(mdldos.getIdTahunAkademik()).get());
                                ib.setJadwal(jadwal2);
                                ib.setKelas(jadwal2.getKelas().getNamaKelas());
                                ib.setDosen(dosen);
                                if (jadwal2.getJamMulai() != null){
                                    ib.setWaktuMasuk(LocalDateTime.of(timeIn,jadwal2.getJamMulai()));
                                }
                                if (jadwal2.getJamMulai() == null){
                                    ib.setWaktuMasuk(LocalDateTime.of(timeIn,jamNya ));
                                }
                                if (jadwal2.getJamSelesai() != null){
                                    ib.setWaktuSelesai(LocalDateTime.of(timeOut,jadwal2.getJamSelesai()));
                                }
                                if (jadwal2.getJamSelesai() == null){
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

        prosesBackgroundAbsensiDosen.setStatus(StatusRecord.DONE);
        prosesBackgroundAbsensiDosen.setTanggalSelesai(LocalDateTime.now());
        prosesBackgroundAbsensiDosenDao.save(prosesBackgroundAbsensiDosen);


    }

}
