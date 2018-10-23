package id.ac.tazkia.akademik.aplikasiakademik.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AbsensiMahasiswaController {

    @GetMapping("/menumahasiswa/absensi/list")
    public void daftarAbsensi(){

    }

    @GetMapping("/menumahasiswa/absensi/form")
    public void  formAbsensi(){

    }
}
