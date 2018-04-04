package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MahasiswaController {
    @GetMapping("/mahasiswa/list")
    public void daftarMahasiswa(){
    }

    @GetMapping("/mahasiswa/form")
    public void  formMahasiswa(){
    }
}
