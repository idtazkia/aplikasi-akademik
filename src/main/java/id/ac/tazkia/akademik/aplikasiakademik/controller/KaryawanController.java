package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KaryawanController {
    @GetMapping("/karyawan/list")
    public void daftarKaryawan(){
    }

    @GetMapping("/karyawan/form")
    public void  formKaryawan(){
    }
}
