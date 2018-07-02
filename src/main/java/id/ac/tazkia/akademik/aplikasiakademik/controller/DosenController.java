package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DosenController {
    @GetMapping("/dosen/list")
    public void daftarDosen(){
    }

    @GetMapping("/dosen/form")
    public void  formDosen(){
    }


    @GetMapping("/dosen/jurusan/list")
    public void  listJurusanDosen(){
    }
}
