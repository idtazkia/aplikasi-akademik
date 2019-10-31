package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TugasAkhirController {
    @GetMapping("/tugasakhir/register")
    public void register(){

    }

    @GetMapping("/tugasakhir/alertpage")
    public void alertPage(){

    }

    @GetMapping("tugasakhir/konsepnote")
    public void formNote(){

    }

    @GetMapping("tugasakhir/waitingpage")
    public void waitingPage(){

    }
}
