package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KurikulumController {
    @GetMapping("/kurikulum/list")
    public void daftarKurikulum(){
    }

    @GetMapping("/kurikulum/form")
    public void  formKurikulum(){
    }
}
