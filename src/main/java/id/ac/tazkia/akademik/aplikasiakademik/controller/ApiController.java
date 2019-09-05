package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.ApiJadwalDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.ApiMahasiswaDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.ApiPresensiDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Controller
public class ApiController {

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
        System.out.println(mulai);
        System.out.println(sampai);

        Iterable<JadwalDosenDto> hasil = jadwalDosenDao.cariJadwal(tahunAkademik, ruangan, hari, mulai, sampai);
        Integer jumlah = IterableUtils.size(hasil);
        for (JadwalDosenDto jadwalDosenDto : hasil){
            jadwalDosenDto.setJumlah(jumlah);
            jadwalDosenDtos.add(jadwalDosenDto);
        }


        return jadwalDosenDtos;


    }

    @GetMapping("/api/uploadMesin")
    @ResponseBody
    public Iterable<ApiJadwalDto> uploadData (@RequestParam(required = false) String id){

        Ruangan ruangan = ruanganDao.findById(id).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Hari hari = hariDao.findById(String.valueOf(dayOfWeek-1)).get();
        List<ApiJadwalDto> jadwalDosenDtos = new ArrayList<>();

        LocalTime mulai = LocalTime.now().plusHours(7).minusMinutes(5);
        LocalTime sampai = LocalTime.now().plusHours(7).plusMinutes(5);
        System.out.println(mulai);
        System.out.println(sampai);

        Iterable<JadwalDosenDto> hasil = jadwalDosenDao.cariJadwal(tahunAkademik, ruangan, hari, mulai, sampai);
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
        System.out.println(mulai);
        System.out.println(sampai);

        Iterable<JadwalDosenDto> hasil = jadwalDosenDao.cariJadwal(tahunAkademik, ruangan, hari, mulai, sampai);
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
    public ApiPresensiDosenDto presensiDosen (@RequestParam String jadwal,@RequestParam(required = false) String dosen){

        Jadwal j = jadwalDao.findById(jadwal).get();
        Dosen d = dosenDao.findById(dosen).get();

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        Hari hari = hariDao.findById(String.valueOf(dayOfWeek-1)).get();
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);


        PresensiDosen presensiDosen = presensiDosenDao.findByJadwalAndDosenAndTahunAkademikAndJadwalHari(j,d,tahunAkademik,hari);

        SesiKuliah sesiKuliah = sesiKuliahDao.findByPresensiDosen(presensiDosen);
        ApiPresensiDosenDto api = new ApiPresensiDosenDto();
        api.setPresensiDosen(presensiDosen.getId());
        api.setSesiKuliah(sesiKuliah.getId());
        api.setJamMulai(presensiDosen.getWaktuMasuk().toLocalTime());
        api.setJadwal(presensiDosen.getJadwal().getId());
        api.setJamSelesai(presensiDosen.getWaktuSelesai().toLocalTime() );
        api.setJumlah(1);

        return api;



    }



    @PostMapping("/api/inputpresensi")
    @ResponseBody
    private ApiPresensiDosenDto input(@RequestParam String jadwal, @RequestParam String dosen,@RequestParam String hari,@RequestParam String jam){
        System.out.println(LocalDateTime.now());
        LocalDate localDate = LocalDate.parse(hari);
        LocalTime localTime = LocalTime.parse(jam);
        LocalDateTime dateTime = LocalDateTime.of(localDate,localTime);
        Jadwal j = jadwalDao.findById(jadwal).get();
        Dosen d = dosenDao.findById(dosen).get();

        PresensiDosen presensiDosen = new PresensiDosen();
        presensiDosen.setDosen(d);
        presensiDosen.setWaktuSelesai(LocalDateTime.of(LocalDate.now(),j.getJamSelesai()));
        presensiDosen.setWaktuMasuk(dateTime);
        presensiDosen.setStatusPresensi(StatusPresensi.HADIR);
        presensiDosen.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        presensiDosen.setJadwal(j);
        presensiDosenDao.save(presensiDosen);

        SesiKuliah sesiKuliah = new SesiKuliah();
        sesiKuliah.setJadwal(j);
        sesiKuliah.setPresensiDosen(presensiDosen);
        sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
        sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
        sesiKuliahDao.save(sesiKuliah);

        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusAndKrsTahunAkademik(j,StatusRecord.AKTIF,tahunAkademikDao.findByStatus(StatusRecord.AKTIF));

        for (KrsDetail kd : krsDetail){
            PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
            presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
            presensiMahasiswa.setSesiKuliah(sesiKuliah);
            presensiMahasiswa.setKrsDetail(kd);
            presensiMahasiswa.setCatatan("-");
            presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
            presensiMahasiswa.setRating(0);
            presensiMahasiswaDao.save(presensiMahasiswa);
        }


        return presensiDosen(j.getId(),d.getId());


    }

    @GetMapping("/api/cekpresensi/mahasiswa")
    @ResponseBody
    public Iterable<ApiMahasiswaDto> mahasiswaDto (@RequestParam String jadwal){

        Jadwal j = jadwalDao.findById(jadwal).get();
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
    public String mahasiswa(@RequestParam String jadwal,@RequestParam String mahasiswa,
                          @RequestParam String sesi,@RequestParam String jam,@RequestParam StatusPresensi statusabsen){

            Jadwal j = jadwalDao.findById(jadwal).get();
            Mahasiswa m = mahasiswaDao.findById(mahasiswa).get();
            SesiKuliah sesiKuliah = sesiKuliahDao.findById(sesi).get();

            PresensiMahasiswa presensiMahasiswa = presensiMahasiswaDao.findByMahasiswaAndSesiKuliahAndStatus(m,sesiKuliah,StatusRecord.AKTIF);
            presensiMahasiswa.setWaktuMasuk(LocalDateTime.of(LocalDate.now(),LocalTime.parse(jam)));
            presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),j.getJamSelesai()));
            presensiMahasiswa.setStatusPresensi(statusabsen);
            presensiMahasiswaDao.save(presensiMahasiswa);

            return "sukses";

    }


}
