package id.ac.tazkia.smilemahasiswa.api;

import groovyjarjarpicocli.CommandLine;
import id.ac.tazkia.smilemahasiswa.dao.JadwalDosenDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.PresensiMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dto.api.*;
import id.ac.tazkia.smilemahasiswa.entity.JadwalDosen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardSpmiApi {

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @GetMapping("/api/mahasiswa/aktif")
    @ResponseBody
    public ResponseEntity<List<JumlahMahasiswaAktifDto>> cariMahasiswaAktif(){

        List<MahasiswaAktifDto> mahasiswaAktifDtos = krsDetailDao.cariMahasiswaAktif();
        List<JumlahMahasiswaAktifDto> jumlahMahasiswaAktifDtos = new ArrayList<>();
        for(MahasiswaAktifDto mahasiswaAktifDto : mahasiswaAktifDtos){
            JumlahMahasiswaAktifDto jumlahMahasiswaAktifDto = new JumlahMahasiswaAktifDto();
            jumlahMahasiswaAktifDto.setTotal(mahasiswaAktifDto.getTotal());
            jumlahMahasiswaAktifDto.setPria(mahasiswaAktifDto.getPria());
            jumlahMahasiswaAktifDto.setWanita(mahasiswaAktifDto.getWanita());
            jumlahMahasiswaAktifDto.setKodeProdi(mahasiswaAktifDto.getKodeProdi());
            jumlahMahasiswaAktifDtos.add(jumlahMahasiswaAktifDto);
        }

//        return transaksiSetahunDuaDtos;

        final HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(jumlahMahasiswaAktifDtos, httpHeaders, HttpStatus.OK);
    }


    @GetMapping("/api/dosen/aktif")
    @ResponseBody
    public ResponseEntity<List<JumlahDosenAktifDto>> cariDosenAktifSemester(){

        List<DosenAktifSemesterDto> dosenAktifSemesterDtos = jadwalDosenDao.cariDosenAktifSemester();
        List<JumlahDosenAktifDto> jumlahDosenAktifDtos = new ArrayList<>();
        for(DosenAktifSemesterDto dosenAktifSemesterDto : dosenAktifSemesterDtos){
            JumlahDosenAktifDto jumlahDosenAktifDto = new JumlahDosenAktifDto();
            jumlahDosenAktifDto.setKodeProdi(dosenAktifSemesterDto.getKodeProdi());
            jumlahDosenAktifDto.setLb(dosenAktifSemesterDto.getLb());
            jumlahDosenAktifDto.setHb(dosenAktifSemesterDto.getHb());
            jumlahDosenAktifDto.setTotal(dosenAktifSemesterDto.getTotal());
            jumlahDosenAktifDtos.add(jumlahDosenAktifDto);
        }

//        return transaksiSetahunDuaDtos;

        final HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(jumlahDosenAktifDtos, httpHeaders, HttpStatus.OK);
    }


    @GetMapping("/api/performance/mahasiswa")
    @ResponseBody
    public ResponseEntity<RateMahasiswaDto> cariRateMahasiswa(){

        PerformanceMahasiswaDto performanceMahasiswaDto = presensiMahasiswaDao.performanceMahasiswa();
        RateMahasiswaDto rateMahasiswaDto = new RateMahasiswaDto();

        rateMahasiswaDto.setBulan(performanceMahasiswaDto.getBulan());
        rateMahasiswaDto.setRate(performanceMahasiswaDto.getRate());

        final HttpHeaders httpHeaders= new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(rateMahasiswaDto, httpHeaders, HttpStatus.OK);
    }



}
