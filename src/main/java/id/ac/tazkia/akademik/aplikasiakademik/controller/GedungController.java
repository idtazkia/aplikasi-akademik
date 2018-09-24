package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GedungController {

    @GetMapping("/gedung/list")
    public void daftarGedung(){
    }

    @GetMapping("/gedung/form")
    public void  formGedung(){
    }

    @GetMapping("/kampus/list")
    public void  listKampus(){
    }

    @GetMapping("/kampus/form")
    public void  formKampus(){
    }
}
