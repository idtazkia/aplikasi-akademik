package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dto.ImportMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Controller
public class ImportDataController {

    @Autowired
    private MahasiswaService mahasiswaService;

    @Autowired
    private MahasiswaDao mahasiswaDao;


    @GetMapping("/api/saveMahasiswa")
    @ResponseBody
    public String createMahasiswa(Authentication authentication, @RequestParam String nim) {

        RestTemplate restTemplate = new RestTemplate();
        ImportMahasiswaDto importMahasiswaDto = restTemplate
                .getForObject("https://spmb.tazkia.ac.id/api/mahasiswanim?nim="+nim, ImportMahasiswaDto.class,2);

        Mahasiswa mahasiswa = mahasiswaDao.findByNim(importMahasiswaDto.getNim());
        if (mahasiswa != null) {

        }
        mahasiswaService.importMahasiswa(importMahasiswaDto);

        return "Data Berhasil Tersimpan";

    }


}
