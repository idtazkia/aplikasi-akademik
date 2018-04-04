package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KebijakanNilaiController {
    @GetMapping("/kebijakannilai/list")
    public void daftarKebijakanNilai(){
    }

    @GetMapping("/kebijakannilai/form")
    public void  formKebijakanNilai(){
    }
}
