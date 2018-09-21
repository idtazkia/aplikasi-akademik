package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LembagaController {
    @GetMapping("/lembaga/list")
    public void daftarLembaga(){
    }

    @GetMapping("/lembaga/form")
    public void  formLembaga(){
    }
}
