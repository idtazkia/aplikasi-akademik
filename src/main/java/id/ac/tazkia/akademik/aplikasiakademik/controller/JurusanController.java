package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JurusanController {
    @GetMapping("/jurusan/list")
    public void daftarJurusan(){
    }

    @GetMapping("/jurusan/form")
    public void  formJurusan(){
    }
}
