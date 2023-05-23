package id.ac.tazkia.smilemahasiswa.api;

import groovyjarjarpicocli.CommandLine;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dto.api.JumlahMahasiswaAktifDto;
import id.ac.tazkia.smilemahasiswa.dto.api.MahasiswaAktifDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardSpmiApi {

    @Autowired
    private KrsDetailDao krsDetailDao;

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

}
