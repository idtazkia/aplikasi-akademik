package id.ac.tazkia.smilemahasiswa.controller;


import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.courses.DetailJadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.courses.DetailJadwalIntDto;
import id.ac.tazkia.smilemahasiswa.dto.human.KaryawanDto;
import id.ac.tazkia.smilemahasiswa.dto.human.KaryawanIntDto;
import id.ac.tazkia.smilemahasiswa.dto.kelas.KelasDto;
import id.ac.tazkia.smilemahasiswa.dto.kelas.KelasIntDto;
import id.ac.tazkia.smilemahasiswa.dto.machine.*;
import id.ac.tazkia.smilemahasiswa.dto.tahunakademik.TahunAkademikDto;
import id.ac.tazkia.smilemahasiswa.dto.tahunakademik.TahunAkademikIntDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.NotifikasiService;
import id.ac.tazkia.smilemahasiswa.service.PresensiService;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class ApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private RuanganDao ruanganDao;
    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private HariDao hariDao;
    @Autowired
    private DosenDao dosenDao;
    @Autowired
    private JadwalDao jadwalDao;
    @Autowired
    private JadwalDosenDao jadwalDosenDao;
    @Autowired
    private PresensiDosenDao presensiDosenDao;
    @Autowired
    private SesiKuliahDao sesiKuliahDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private KaryawanDao karyawanDao;
    @Autowired
    private NotifikasiService notifikasiService;
    @Autowired
    private PresensiService presensiService;
    @Autowired
    private KelasDao kelasDao;

    @GetMapping("/api/tarikData")
    @ResponseBody
    public Iterable<JadwalDosenDto> tarikData (@RequestParam(required = false) String id){

        Ruangan ruangan = ruanganDao.findById(id).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Hari hari = hariDao.findById(String.valueOf(dayOfWeek-1)).get();
        List<JadwalDosenDto> jadwalDosenDtos = new ArrayList<>();

        LocalTime mulai = LocalTime.now().plusHours(7).minusMinutes(50);
        LocalTime sampai = LocalTime.now().plusHours(7).minusMinutes(5);
        LOGGER.debug("Mulai : {}", mulai);
        LOGGER.debug("Sampai : {}", sampai);

        Iterable<JadwalDosenDto> hasil = jadwalDosenDao.cariJadwal(tahunAkademik, ruangan, hari,StatusRecord.AKTIF, mulai, sampai);
        Integer jumlah = IterableUtils.size(hasil);
        for (JadwalDosenDto jadwalDosenDto : hasil){
            jadwalDosenDto.setJumlah(jumlah);
            jadwalDosenDtos.add(jadwalDosenDto);
        }
        return jadwalDosenDtos;
    }

    @GetMapping("/api/uploadMesin")
    @ResponseBody
    public Iterable<ApiJadwalDto> uploadData (@RequestParam(required = false) String id, @RequestParam(required = false) String idHari){

        Ruangan ruangan = ruanganDao.findById(id).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Hari hari = hariDao.findById(idHari).get();
        System.out.println(hari.getNamaHari());
        List<ApiJadwalDto> jadwalDosenDtos = new ArrayList<>();

        LocalTime mulai = LocalTime.now().plusHours(7);
        LocalTime sampai = LocalTime.now().plusHours(7).plusMinutes(9);
        LOGGER.debug("Mulai : {}", mulai);
        LOGGER.debug("Sampai : {}", sampai);

        Iterable<JadwalDosenDto> hasil = jadwalDosenDao.cariJadwal(tahunAkademik, ruangan, hari,StatusRecord.AKTIF ,mulai, sampai);
        Integer jumlah = IterableUtils.size(hasil);
        for (JadwalDosenDto jadwalDosenDto : hasil){
            ApiJadwalDto apiJadwalDto = new ApiJadwalDto();
            jadwalDosenDto.setJumlah(jumlah);
            BeanUtils.copyProperties(jadwalDosenDto,apiJadwalDto);
            Dosen dosen = dosenDao.findById(jadwalDosenDto.getIdDosen()).get();
            Jadwal jadwal = jadwalDao.findById(jadwalDosenDto.getJadwal()).get();
            apiJadwalDto.setMatakuliah(jadwal.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
            apiJadwalDto.setNamaDosen(dosen.getKaryawan().getNamaKaryawan());
            jadwalDosenDtos.add(apiJadwalDto);
        }


        return jadwalDosenDtos;


    }

    @GetMapping("/api/deleteMesin")
    @ResponseBody
    public Iterable<JadwalDosenDto> deleteMesin (@RequestParam(required = false) String id){

        Ruangan ruangan = ruanganDao.findById(id).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Hari hari = hariDao.findById(String.valueOf(dayOfWeek-1)).get();
        List<JadwalDosenDto> jadwalDosenDtos = new ArrayList<>();

        LocalTime mulai = LocalTime.now().plusHours(7).minusMinutes(50);
        LocalTime sampai = LocalTime.now().plusHours(7).minusMinutes(40);
        LOGGER.debug("Mulai : {}", mulai);
        LOGGER.debug("Sampai : {}", sampai);

        Iterable<JadwalDosenDto> hasil = jadwalDosenDao.cariJadwal(tahunAkademik, ruangan, hari,StatusRecord.AKTIF, mulai, sampai);
        Integer jumlah = IterableUtils.size(hasil);
        for (JadwalDosenDto jadwalDosenDto : hasil){
            jadwalDosenDto.setJumlah(jumlah);
            jadwalDosenDtos.add(jadwalDosenDto);
        }

        return jadwalDosenDtos;
    }


    @GetMapping("/api/akademikAktif")
    @ResponseBody
    public TahunAkademik tahunAkademik (){
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        return tahunAkademik;
    }

    @GetMapping("/api/cekpresensi")
    @ResponseBody
    public ApiPresensiDosenDto presensiDosen (@RequestParam String jadwal, @RequestParam(required = false) String dosen){

        LOGGER.info("Cek presensi by API : Jadwal : {}, Dosen : {}",jadwal, dosen);

        Optional<Jadwal> oj = jadwalDao.findById(jadwal);

        if (!oj.isPresent()) {
            recordTidakDitemukan("Jadwal", jadwal);
            return presensiError("Jadwal dengan id "+jadwal+" tidak ditemukan");
        }

        Jadwal j = oj.get();

        Optional<Dosen> od = dosenDao.findById(dosen);

        if (!od.isPresent()) {
            recordTidakDitemukan("Dosen", dosen);
            return presensiError("Jadwal dengan id "+jadwal+" tidak ditemukan");
        }

        Dosen d = od.get();

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Hari hari = hariDao.findById(String.valueOf(dayOfWeek-1)).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);

        List<PresensiDosen> presensiDosen = presensiDosenDao.findByJadwalAndDosenAndTahunAkademikAndJadwalHariAndStatus(j,d,tahunAkademik,hari,StatusRecord.AKTIF);

        for (PresensiDosen pd : presensiDosen){
            LocalDate date = pd.getWaktuMasuk().toLocalDate();
            LocalTime masuk = pd.getWaktuMasuk().toLocalTime();
            LocalTime minus5 = pd.getJadwal().getJamMulai().minusMinutes(10);
            if (date.isEqual(LocalDate.now())){
                if (masuk.compareTo(minus5) >= 0 && masuk .compareTo(pd.getJadwal().getJamSelesai()) <= 0){
                            SesiKuliah sesiKuliah = sesiKuliahDao.findByPresensiDosen(pd);
                            ApiPresensiDosenDto api = new ApiPresensiDosenDto();
                            api.setPresensiDosen(pd.getId());
                            api.setSesiKuliah(sesiKuliah.getId());
                            api.setJamMulai(pd.getWaktuMasuk().toLocalTime());
                            api.setJadwal(pd.getJadwal().getId());
                            api.setJamSelesai(pd.getWaktuSelesai().toLocalTime() );
                            api.setJumlah(1);
                            return api;
                }
            }

        }



        return result();
    }



    @PostMapping("/api/inputpresensi")
    @ResponseBody
    private ApiPresensiDosenDto input(@RequestParam String jadwal, @RequestParam String dosen, @RequestParam String hari, @RequestParam String jam){

        LOGGER.info("Input presensi by API : Jadwal : {}, Dosen : {}, Hari : {}, Jam : {}",jadwal, dosen, hari, jam);

        LocalDate localDate = LocalDate.parse(hari);
        LocalTime localTime = LocalTime.parse(jam);
        LocalDateTime dateTime = LocalDateTime.of(localDate,localTime);
        Optional<Jadwal> oj = jadwalDao.findById(jadwal);

        if (!oj.isPresent()) {
            recordTidakDitemukan("Jadwal", jadwal);
            return presensiError("Jadwal dengan id "+jadwal+" tidak ditemukan");
        }

        Jadwal j = oj.get();

        Optional<Dosen> od = dosenDao.findById(dosen);

        if (!od.isPresent()) {
            recordTidakDitemukan("Dosen", dosen);
            return presensiError("Jadwal dengan id "+jadwal+" tidak ditemukan");
        }

        Dosen d = od.get();

        PresensiDosen presensiDosen = presensiService.inputPresensi(d, j, dateTime);

        return presensiDosen(j.getId(),d.getId());

    }

    @GetMapping("/api/cekpresensi/mahasiswa")
    @ResponseBody
    public Iterable<ApiMahasiswaDto> mahasiswaDto (@RequestParam String jadwal){

        if (!StringUtils.hasText(jadwal)) {
            LOGGER.debug("Cek presensi mahasiswa by API : Jadwal tidak diisi");
            return Arrays.asList(apiMahasiswaError("Jadwal dengan id "+jadwal+" tidak ditemukan"));
        }

        LOGGER.info("Cek presensi mahasiswa by API : Jadwal : {}",jadwal);

        Optional<Jadwal> oj = jadwalDao.findById(jadwal);

        if (!oj.isPresent()) {
            recordTidakDitemukan("Jadwal", jadwal);
            return Arrays.asList(apiMahasiswaError("Jadwal dengan id "+jadwal+" tidak ditemukan"));
        }
        Jadwal j = oj.get();
        List<ApiMahasiswaDto> apiMahasiswaDtos = new ArrayList<>();

        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusAndKrsTahunAkademik(j,StatusRecord.AKTIF,tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        for (KrsDetail kd : krsDetail){
            ApiMahasiswaDto api = new ApiMahasiswaDto();
            api.setJumlah(krsDetail.size());
            api.setAbsen(kd.getMahasiswa().getIdAbsen());
            api.setJadwal(j.getId());
            api.setKrsDetail(kd.getId());
            api.setMahasiswa(kd.getMahasiswa().getId());
            api.setNim(kd.getMahasiswa().getNim());
            api.setRfid(kd.getMahasiswa().getRfid());
            apiMahasiswaDtos.add(api);
        }

        return apiMahasiswaDtos;

    }

    @PostMapping("/api/presensimahasiswa")
    @ResponseBody
    public String mahasiswa(@RequestParam String jadwal, @RequestParam String mahasiswa,
                            @RequestParam String sesi, @RequestParam String jam, @RequestParam StatusPresensi statusabsen){

        LOGGER.info("Presensi Mahasiswa by API : Jadwal : {}, Mahasiswa : {}, Sesi : {}, Jam : {}, Status Absen : {}",jadwal, mahasiswa, sesi, jam, statusabsen);

        Optional<Jadwal> oj = jadwalDao.findById(jadwal);

        if (!oj.isPresent()) {
            recordTidakDitemukan("Jadwal", jadwal);
            return "gagal";
        }
        Jadwal j = oj.get();

        Optional<Mahasiswa> om = mahasiswaDao.findById(mahasiswa);
        if (!om.isPresent()) {
            recordTidakDitemukan("Mahasiswa", mahasiswa);
            return "gagal";
        }

        Mahasiswa m = om.get();

        Optional<SesiKuliah> osk = sesiKuliahDao.findById(sesi);
        if (!osk.isPresent()) {
            recordTidakDitemukan("Sesi Kuliah", sesi);
            return "gagal";
        }
        SesiKuliah sesiKuliah = osk.get();

        PresensiMahasiswa presensiMahasiswa = presensiMahasiswaDao.findByMahasiswaAndSesiKuliahAndStatus(m,sesiKuliah,StatusRecord.AKTIF);
        presensiMahasiswa.setWaktuMasuk(LocalDateTime.of(LocalDate.now(),LocalTime.parse(jam)));
        presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),j.getJamSelesai()));
        presensiMahasiswa.setStatusPresensi(statusabsen);
        presensiMahasiswaDao.save(presensiMahasiswa);

        return "sukses";

    }

    @GetMapping("/api/getruangan")
    @ResponseBody
    public Ruangan ruangan(@RequestParam Ruangan id){
        return id;
    }

    private void recordTidakDitemukan(String entity, String id) {
        LOGGER.warn("Data {} dengan id {} tidak ditemukan", entity, id);
    }

    private ApiPresensiDosenDto presensiError(String errorMessage) {
        ApiPresensiDosenDto hasil = new ApiPresensiDosenDto();
        hasil.setSukses(false);
        hasil.setPesanError(errorMessage);
        return hasil;
    }

    private ApiPresensiDosenDto result() {
        ApiPresensiDosenDto hasil = new ApiPresensiDosenDto();
        hasil.setSukses(false);
        hasil.setPesanError("data kosong");
        return hasil;
    }

    private ApiJadwalDosen dataNull() {
        ApiJadwalDosen hasil = new ApiJadwalDosen();
        hasil.setSukses(false);
        hasil.setPesanError("data kosong");
        return hasil;
    }

    private ApiMahasiswaDto apiMahasiswaError(String errorMessage) {
        ApiMahasiswaDto hasil = new ApiMahasiswaDto();
        hasil.setSukses(false);
        hasil.setPesanError(errorMessage);
        return hasil;
    }

    private ApiPresensiMahasiswa presensiMahasiswaError(String errorMessage) {
        ApiPresensiMahasiswa hasil = new ApiPresensiMahasiswa();
        hasil.setSukses(false);
        hasil.setPesanError(errorMessage);
        return hasil;
    }

    @GetMapping("/api/getrfid")
    @ResponseBody
    public List<RfidDto> getRfid(){

        List<RfidDto> mahasiswa = mahasiswaDao.rfidMahasiswa();

        return mahasiswa;

    }

    @GetMapping("/api/rfidkaryawan")
    @ResponseBody
    public List<ApiRfidDto> rfidKaryawan(){

//        karyawanDao.updateIdAbsenKaryawan();

        List<ApiRfidDto> apiRfidDtos = new ArrayList<>();
        List<ApiRfidDto> karyawan = karyawanDao.rfidKaryawan(StatusRecord.AKTIF);
        for (ApiRfidDto api : karyawan){
            api.setJumlah(karyawan.size());
            apiRfidDtos.add(api);
        }


        return apiRfidDtos;

    }

    @GetMapping("/api/getjadwal")
    @ResponseBody()
    public ApiJadwalDosen jadwalDosen(@RequestParam String ruangan, @RequestParam String rfid){

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Hari hari = hariDao.findById(String.valueOf(dayOfWeek-1)).get();

        Dosen dosen = dosenDao.findByKaryawanRfid(rfid);


        JadwalDosen jadwalDosen = jadwalDosenDao.cari(dosen,tahunAkademikDao.findByStatus(StatusRecord.AKTIF),hari,ruanganDao.findById(ruangan).get(),LocalTime.now().plusHours(7));

        if (jadwalDosen != null) {


            ApiJadwalDosen apiJadwalDosen = new ApiJadwalDosen();
            apiJadwalDosen.setJadwal(jadwalDosen.getJadwal().getId());
            apiJadwalDosen.setDosen(jadwalDosen.getDosen().getId());
            apiJadwalDosen.setNamaDosen(jadwalDosen.getDosen().getKaryawan().getNamaKaryawan());
            apiJadwalDosen.setNamaMatakuliah(jadwalDosen.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());
            apiJadwalDosen.setNamaMatakuliahEng(jadwalDosen.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliahEnglish());
            apiJadwalDosen.setJamMulai(jadwalDosen.getJadwal().getJamMulai());
            apiJadwalDosen.setJamSelesai(jadwalDosen.getJadwal().getJamSelesai());
            apiJadwalDosen.setJumlah(1);
            return apiJadwalDosen;
        }

        return dataNull();

    }

    @GetMapping("/api/getdataMahasiswa")
    @ResponseBody
    public List<ApiPresensiMahasiswa> cekMahasiswa(@RequestParam String sesiKuliah){

        if (sesiKuliah.isEmpty()){
            return Arrays.asList(presensiMahasiswaError("Sesi Kuliah dengan id "+sesiKuliah+" tidak ditemukan"));
        }

        SesiKuliah s = sesiKuliahDao.findById(sesiKuliah).get();

        if (s.getJadwal() == null){
            return Arrays.asList(presensiMahasiswaError("Jadwal tidak ditemukan"));
        }

        List<ApiPresensiMahasiswa> mahasiswas = new ArrayList<>();

        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliahAndStatus(s,StatusRecord.AKTIF);

        if (presensiMahasiswa.isEmpty()){
            return Arrays.asList(presensiMahasiswaError("Presensi Mahasiswa Tidak Ada"));
        }
        for (PresensiMahasiswa pm : presensiMahasiswa){
            ApiPresensiMahasiswa apiPresensiMahasiswa = new ApiPresensiMahasiswa();
            apiPresensiMahasiswa.setCatatan(pm.getCatatan());
            apiPresensiMahasiswa.setJumlah(presensiMahasiswa.size());
            apiPresensiMahasiswa.setNim(pm.getMahasiswa().getNim());
            apiPresensiMahasiswa.setKrsDetail(pm.getKrsDetail().getId());
            apiPresensiMahasiswa.setMahasiswa(pm.getMahasiswa().getId());
            apiPresensiMahasiswa.setNamaMahasiswa(pm.getMahasiswa().getNama());
            apiPresensiMahasiswa.setPresensiMahasiswa(pm.getId());
            apiPresensiMahasiswa.setRating(pm.getRating());
            apiPresensiMahasiswa.setRfid(pm.getMahasiswa().getRfid());
            apiPresensiMahasiswa.setSesiKuliah(pm.getSesiKuliah().getId());
            apiPresensiMahasiswa.setStatusPresensi(pm.getStatusPresensi());
            if (pm.getWaktuKeluar() != null) {
                apiPresensiMahasiswa.setWaktuKeluar(pm.getWaktuKeluar().toLocalTime());
            }
            if (pm.getWaktuMasuk() != null) {
                apiPresensiMahasiswa.setWaktuMasuk(pm.getWaktuMasuk().toLocalTime());
            }
            mahasiswas.add(apiPresensiMahasiswa);
        }

        return mahasiswas;

    }



    //tahunAkademik
    @GetMapping("/api/getlistTahunAkademik")
    @ResponseBody
    public List<TahunAkademikDto> attendanceLogDosen(){

        List<TahunAkademikIntDto> alog = tahunAkademikDao.apiTahunAkademik();
        List<TahunAkademikDto> adto = new ArrayList<>();

        for (TahunAkademikIntDto TahunAkademikIntDto : alog){
            TahunAkademikDto TahunAkademikDto = new TahunAkademikDto();

            TahunAkademikDto.setIdTahunAkademik(TahunAkademikIntDto.getIdTahunAkademik());
            TahunAkademikDto.setKodeTahunAkademik(TahunAkademikIntDto.getKodeTahunAkademik());
            TahunAkademikDto.setNamaTahunAkademik(TahunAkademikIntDto.getNamaTahunAkademik());
            TahunAkademikDto.setTanggalMulai(TahunAkademikIntDto.getTanggalMulai());
            TahunAkademikDto.setTanggalSelesai(TahunAkademikIntDto.getTanggalSelesai());
            TahunAkademikDto.setTanggalMulaiKrs(TahunAkademikIntDto.getTanggalMulaiKrs());
            TahunAkademikDto.setTanggalSelesaiKrs(TahunAkademikIntDto.getTanggalSelesaiKrs());
            TahunAkademikDto.setTanggalMulaiKuliah(TahunAkademikIntDto.getTanggalMulaiKuliah());
            TahunAkademikDto.setTanggalSelesaiKuliah(TahunAkademikIntDto.getTanggalSelesaiKuliah());
            TahunAkademikDto.setTanggalMulaiUts(TahunAkademikIntDto.getTanggalMulaiUts());
            TahunAkademikDto.setTanggalSelesaiUts(TahunAkademikIntDto.getTanggalSelesaiUts());
            TahunAkademikDto.setTanggalMulaiUas(TahunAkademikIntDto.getTanggalMulaiUas());
            TahunAkademikDto.setTanggalSelesaiUas(TahunAkademikIntDto.getTanggalSelesaiUas());
            TahunAkademikDto.setTanggalMulaiNilai(TahunAkademikIntDto.getTanggalMulaiNilai());
            TahunAkademikDto.setTanggalSelesaiNilai(TahunAkademikIntDto.getTanggalSelesaiNilai());
            TahunAkademikDto.setTahun(TahunAkademikIntDto.getTahun());
            TahunAkademikDto.setStatus(TahunAkademikIntDto.getStatus());
            TahunAkademikDto.setJenis(TahunAkademikIntDto.getJenis());


            adto.add(TahunAkademikDto);
        }

        return adto;

    }

//    //detailJadwal
//    @GetMapping("/api/getDetailJadwal")
//    @ResponseBody
//    public List<DetailJadwalDto> getDetailJadwal(){
//
//        List<DetailJadwalIntDto> alog = jadwalDao.getDetailJadwal();
//        List<DetailJadwalDto> adto = new ArrayList<>();
//
//        for (DetailJadwalIntDto detailJadwalIntDto : alog){
//            DetailJadwalDto detailJadwalDto = new DetailJadwalDto();
//
//            detailJadwalDto.setId(detailJadwalIntDto.getId());
//            detailJadwalDto.setNamaProdi(detailJadwalIntDto.getNamaProdi());
//            detailJadwalDto.setNamaKelas(detailJadwalIntDto.getNamaKelas());
//            detailJadwalDto.setKodeMatakuliah(detailJadwalIntDto.getKodeMatakuliah());
//            detailJadwalDto.setNamaMatakuliah(detailJadwalIntDto.getNamaMatakuliah());
//            detailJadwalDto.setNamaMatakuliahEnglish(detailJadwalIntDto.getNamaMatakuliahEnglish());
//            detailJadwalDto.setIdDosen(detailJadwalIntDto.getIdDosen());
//            detailJadwalDto.setDosen(detailJadwalIntDto.getDosen());
//            detailJadwalDto.setJamMulai(detailJadwalIntDto.getJamMulai());
//            detailJadwalDto.setJamSelesai(detailJadwalIntDto.getJamSelesai());
//            detailJadwalDto.setIdNumberElearning(detailJadwalIntDto.getIdNumberElearning());
//            detailJadwalDto.setIdTahunAkademik(detailJadwalIntDto.getIdTahunAkademik());
//            detailJadwalDto.setStatus(detailJadwalIntDto.getStatus());
//
//            adto.add(detailJadwalDto);
//        }
//
//        return adto;
//
//    }
//
//
//    //kelas
//    @GetMapping("/api/getKelas")
//    @ResponseBody
//    public List<KelasDto> getKelas(){
//
//        List<KelasIntDto> alog = kelasDao.apiKelas();
//        List<KelasDto> adto = new ArrayList<>();
//
//        for (KelasIntDto kelasIntDto : alog){
//            KelasDto kelasDto = new KelasDto();
//
//            kelasDto.setIdKelas(kelasIntDto.getIdKelas());
//            kelasDto.setKodeKelas(kelasIntDto.getKodeKelas());
//            kelasDto.setNamaKelas(kelasIntDto.getNamaKelas());
//            kelasDto.setKeterangan(kelasIntDto.getKeterangan());
//            kelasDto.setIdProdi(kelasIntDto.getIdProdi());
//            kelasDto.setStatus(kelasIntDto.getStatus());
//            kelasDto.setKurikulum(kelasIntDto.getKurikulum());
//            kelasDto.setKonsentrasi(kelasIntDto.getKonsentrasi());
//            kelasDto.setAngkatan(kelasIntDto.getAngkatan());
//            kelasDto.setBahasa(kelasIntDto.getBahasa());
//
//
//            adto.add(kelasDto);
//        }
//
//        return adto;
//
//    }
//
//    //kelas
//    @GetMapping("/api/apiGetKaryawan")
//    @ResponseBody
//    public List<KaryawanDto> getKaryawan(){
//
//        List<KaryawanIntDto> alog = karyawanDao.apiGetKaryawan();
//        List<KaryawanDto> adto = new ArrayList<>();
//
//        for (KaryawanIntDto karyawanIntDto : alog){
//            KaryawanDto karyawanDto = new KaryawanDto();
//
//            karyawanDto.setId(karyawanIntDto.getId());
//            karyawanDto.setNik(karyawanIntDto.getNik());
//            karyawanDto.setNamaKaryawan(karyawanIntDto.getNamaKaryawan());
//            karyawanDto.setGelar(karyawanIntDto.getGelar());
//            karyawanDto.setJenisKelamin(karyawanIntDto.getJenisKelamin());
//            karyawanDto.setStatus(karyawanIntDto.getStatus());
//            karyawanDto.setIdUser(karyawanIntDto.getIdUser());
//            karyawanDto.setNidn(karyawanIntDto.getNidn());
//            karyawanDto.setEmail(karyawanIntDto.getEmail());
//            karyawanDto.setTanggalLahir(karyawanIntDto.getTanggalLahir());
//            karyawanDto.setRfid(karyawanIntDto.getRfid());
//            karyawanDto.setIdAbsen(karyawanIntDto.getIdAbsen());
//            karyawanDto.setFoto(karyawanIntDto.getFoto());
//
//            adto.add(karyawanDto);
//        }
//
//        return adto;
//
//    }

}
