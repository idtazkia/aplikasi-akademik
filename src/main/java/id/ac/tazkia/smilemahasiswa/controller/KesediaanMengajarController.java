package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.KesediaanMengajarJawabanDao;
import id.ac.tazkia.smilemahasiswa.dao.KesediaanMengajarPertanyaanDao;
import id.ac.tazkia.smilemahasiswa.dao.KesediaanMengajarSubPertanyaanDao;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class KesediaanMengajarController {
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    KesediaanMengajarPertanyaanDao kesediaanMengajarPertanyaanDao;

    @Autowired
    KesediaanMengajarSubPertanyaanDao kesediaanMengajarSubPertanyaanDao;

    @Autowired
    KesediaanMengajarJawabanDao kesediaanMengajarJawabanDao;

    @GetMapping("/kesediaanMengajar/list")
    public void listKesediaanMengajar(Model model){
        model.addAttribute("listData", kesediaanMengajarPertanyaanDao.findByStatusOrderByUrutanAsc(StatusRecord.AKTIF));
        model.addAttribute("subPertanyaan", kesediaanMengajarSubPertanyaanDao.findByStatus(StatusRecord.AKTIF));
    }

    @GetMapping("/kesediaanMengajar/form")
    public void formKesediaanMengajar(){}

    @PostMapping("/kesediaanMengajar/form")
    public String saveSoalKesediaanMengajar(KesediaanMengajarPertanyaan kesediaanMengajar){
        kesediaanMengajar.setStatus(StatusRecord.AKTIF);
        kesediaanMengajarPertanyaanDao.save(kesediaanMengajar);
        System.out.println("test 1 : " + kesediaanMengajar);
        if (kesediaanMengajar.getTipeSoal().equals("pilihan")){
            KesediaanMengajarPertanyaan kesediaanMengajarPertanyaan = kesediaanMengajarPertanyaanDao.findById(kesediaanMengajar.getId()).get();
            KesediaanMengajarSubPertanyaan kesediaanMengajarSubPertanyaan = new KesediaanMengajarSubPertanyaan();
            kesediaanMengajarSubPertanyaan.setKesediaanMengajarPertanyaan(kesediaanMengajarPertanyaan);
            kesediaanMengajarSubPertanyaan.setPertanyaan("-");
            kesediaanMengajarSubPertanyaan.setStatus(StatusRecord.AKTIF);
            kesediaanMengajarSubPertanyaanDao.save(kesediaanMengajarSubPertanyaan);
            System.out.println("test 2 : " + kesediaanMengajarSubPertanyaan);
        }

        return "redirect:list";
    }

    @PostMapping("/kesediaanMengajar/subPertanyaan")
    public String saveSubPertanyaan(@RequestParam String[] pertanyaan, @RequestParam KesediaanMengajarPertanyaan kesediaanMengajarPertanyaan){
        for (String subPertanyaan : pertanyaan){
            KesediaanMengajarSubPertanyaan kesediaanMengajarSubPertanyaan = new KesediaanMengajarSubPertanyaan();

            kesediaanMengajarSubPertanyaan.setPertanyaan(subPertanyaan);
            kesediaanMengajarSubPertanyaan.setKesediaanMengajarPertanyaan(kesediaanMengajarPertanyaan);
            kesediaanMengajarSubPertanyaan.setStatus(StatusRecord.AKTIF);
            System.out.println("test : " + subPertanyaan);
            kesediaanMengajarSubPertanyaanDao.save(kesediaanMengajarSubPertanyaan);
        }

        return "redirect:list";
    }

    @PostMapping("/kesediaanMengajar/hapus")
    public String hapusPertanyaan(@RequestParam KesediaanMengajarPertanyaan id){
        id.setStatus(StatusRecord.HAPUS);
        kesediaanMengajarPertanyaanDao.save(id);
        return "redirect:list";
    }

    @PostMapping("/kesediaanMengajar/delete")
    public String deleteSubPertanyaan(@RequestParam KesediaanMengajarSubPertanyaan id){
        id.setStatus(StatusRecord.HAPUS);
        kesediaanMengajarSubPertanyaanDao.save(id);
        return "redirect:list";
    }

    @GetMapping("/kesediaanMengajar/dosen")
    public void kesediaanMengajarDosen(Model model){
        model.addAttribute("pertanyaan", kesediaanMengajarPertanyaanDao.findByStatusOrderByUrutanAsc(StatusRecord.AKTIF));
        model.addAttribute("subPertanyaan", kesediaanMengajarSubPertanyaanDao.findByStatus(StatusRecord.AKTIF));
    }

    @PostMapping("/kesediaanMengajar/menjawab")
    public String menjawabKesedianMengajar(@RequestParam String[] kesediaanMengajarSubPertanyaan, HttpServletRequest request, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        for (String pertanyaan : kesediaanMengajarSubPertanyaan){
            KesediaanMengajarJawaban kesediaanMengajarJawaban = new KesediaanMengajarJawaban();
            KesediaanMengajarSubPertanyaan kesediaanMengajarSubPertanyaan1 = kesediaanMengajarSubPertanyaanDao.findById(pertanyaan).get();
            String jawaban = request.getParameter(kesediaanMengajarSubPertanyaan1.getId() + "jwb");

            kesediaanMengajarJawaban.setKesediaanMengajarSubPertanyaan(kesediaanMengajarSubPertanyaan1);
            kesediaanMengajarJawaban.setJawaban(jawaban);
            kesediaanMengajarJawaban.setStatus(StatusRecord.AKTIF);
            kesediaanMengajarJawaban.setUser(user);

            kesediaanMengajarJawabanDao.save(kesediaanMengajarJawaban);

        }
        return "redirect:dosen";
    }
}
