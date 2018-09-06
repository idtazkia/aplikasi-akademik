package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KurikulumDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MataKuliahDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KurikulumController {

    @Autowired
    KurikulumDao kurikulumDao;

    @Autowired
    MataKuliahDao mataKuliahDao;

    @Autowired
    ProdiDao prodiDao;



    @GetMapping("/kurikulum/list")
    public void daftarKurikulum(){
    }

    @GetMapping("/kurikulum/form")
    public void  formKurikulum(){
    }
}
