package id.ac.tazkia.akademik.aplikasiakademik.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TagihanMahasiswaController {

    @GetMapping("/menumahasiswa/tagihan/list")
    public void daftarTagihan(){

    }

    @GetMapping("/menumahasiswa/tagihan/form")
    public void  formTagihan(){

    }

}
