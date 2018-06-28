package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PrasyaratController {

    @GetMapping("/prasyarat/form")
    public void formPrasyarat(){
    }

    @GetMapping("/prasyarat/list")
    public void listPrasyarat(){
    }
}
