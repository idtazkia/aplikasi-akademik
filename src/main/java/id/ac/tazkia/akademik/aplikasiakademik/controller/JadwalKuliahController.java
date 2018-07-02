package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JadwalKuliahController {

    @GetMapping("/jadwalkuliah/list")
    public void jadwalList(){}
}
