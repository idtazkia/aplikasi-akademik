package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KonsentrasiController {
    @GetMapping("/konsentrasi/list")
    public void daftarFakultas(){
    }

    @GetMapping("/konsentrasi/form")
    public void  formFakultas(){
    }
}
