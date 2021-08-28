package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.UserDao;
import id.ac.tazkia.smilemahasiswa.dto.ImportMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.MdlGradeItemsDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.service.MahasiswaService;
import id.ac.tazkia.smilemahasiswa.utility.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(path = UrlUtil.Import.MAHASISWA)
@RestController
public class ImportDataController  {

    @Value("${spmb.mahaiswa.url}") private String apiUrl;

    @Autowired
    private MahasiswaService mahasiswaService;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private UserDao userDao;


    @GetMapping(value = "/{nim}")
    @ResponseBody
    public ResponseEntity<Object> getMahasiswaBaru(@PathVariable String nim) {

        RestTemplate restTemplate = new RestTemplate();
        ImportMahasiswaDto response = restTemplate
                .getForObject(apiUrl+"?nim="+nim, ImportMahasiswaDto.class);

        if (response.getNim() == null){
            return new ResponseEntity<>("Data Tidak ditemukan", HttpStatus.NOT_FOUND);
        }else {
            Mahasiswa mahasiswa = mahasiswaDao.findByNim(response.getNim());
            if (mahasiswa != null) {
                return new ResponseEntity<>("Data Sudah Dilaporkan", HttpStatus.ALREADY_REPORTED);
            }else {
                save(response);
                return new ResponseEntity<>("Data Tersimpan", HttpStatus.CREATED);
            }
        }

    }

    @PostMapping
    public ResponseEntity<Object> save(@Valid @RequestBody ImportMahasiswaDto request){
        Mahasiswa response = mahasiswaService.importMahasiswa(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/proses")
    @ResponseBody
    public ResponseEntity<Object> getMahasiswaBaru(@RequestBody ImportMahasiswaDto request) {

        User user = userDao.findById(request.getId()).get();


        if (user != null) {
            if (request.getNim() == null || request.getNim().equals("null")) {
                System.out.println("Request Null");

                return new ResponseEntity<>("Data Tidak ditemukan", HttpStatus.NOT_FOUND);
            } else {
                Mahasiswa mahasiswa = mahasiswaDao.findByNim(request.getNim());
                if (mahasiswa != null) {
                    System.out.println("Data Sudah Dilaporkan");

                    return new ResponseEntity<>("Data Sudah Dilaporkan", HttpStatus.ALREADY_REPORTED);
                } else {

                    Mahasiswa response = mahasiswaService.importMahasiswa(request);
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                }
            }
        }else {
            return new ResponseEntity<>("Access Denied", HttpStatus.FORBIDDEN);

        }
    }


}
