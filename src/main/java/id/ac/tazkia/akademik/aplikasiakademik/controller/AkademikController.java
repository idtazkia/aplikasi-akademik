package id.ac.tazkia.akademik.aplikasiakademik.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AkademikController {

    @GetMapping("/akademik/list")
    public void akademikList(){}

    @GetMapping("/akademik/form")
    public void akademikForm(){}
}
