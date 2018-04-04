package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FakultasController {
    @GetMapping("/fakultas/list")
    public void daftarFakultas(){
    }

    @GetMapping("/fakultas/form")
    public void  formFakultas(){
    }
}
