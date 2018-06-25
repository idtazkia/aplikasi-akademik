package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProgramController {
    @GetMapping("/program/list")
    public void daftarProgram(){
    }

    @GetMapping("/program/form")
    public void  formProgram(){
    }
}
