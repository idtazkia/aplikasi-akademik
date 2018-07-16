package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KebijakanPresensiController {
    @GetMapping("/kebijakanpresensi/list")
    public void daftarKebijakanPresensi(){
    }

    @GetMapping("/kebijakanpresensi/form")
    public void  formKebijakanPresensi(){
    }
}
