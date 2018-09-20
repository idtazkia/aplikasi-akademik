package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JenjangController {
    @GetMapping("/jenjang/list")
    public void daftarJenjang(){
    }
    @GetMapping("/jenjang/form")
    public void jenjangForm(){
    }
}

