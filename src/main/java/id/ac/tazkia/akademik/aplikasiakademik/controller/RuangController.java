package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RuangController {

    @GetMapping("/ruang/list")
    public void daftarRuang(){
    }

    @GetMapping("/ruang/form")
    public void  formRuang(){
    }

}
