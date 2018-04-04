package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MataKuliahController {
    @GetMapping("/matakuliah/list")
    public void daftarMataKuliah(){
    }

    @GetMapping("/matakuliah/form")
    public void  formMataKuliah(){
    }
}
