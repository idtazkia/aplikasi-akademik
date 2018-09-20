package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProgramStudiController {
    @GetMapping("/programstudi/list")
    public void daftarProgramStudi(){
    }

    @GetMapping("/programstudi/form")
    public void  formProgramStudi(){
    }
}
