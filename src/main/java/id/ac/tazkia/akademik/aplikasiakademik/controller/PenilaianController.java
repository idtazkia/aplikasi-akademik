package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PenilaianController {
    @GetMapping("/penilaian/list")
    public void listPenilaian(){
    }

    @GetMapping("/penilaian/form")
    public void formPenilaian(){
    }
}
