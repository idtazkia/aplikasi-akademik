package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.GedungDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.KampusDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Gedung;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jenjang;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class GedungController {
    @Autowired
    private GedungDao gedungDao;
    @Autowired
    private KampusDao kampusDao;

    @GetMapping("/gedung/list")
    public void daftarGedung(Model model, @RequestParam(required = false) String search, Pageable page){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", gedungDao.findByStatusNotInAndAndNamaGedungContainingIgnoreCaseOrderByNamaGedung(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",gedungDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/gedung/form")
    public void gedungForm(Model model,@RequestParam(required = false) String id){
        model.addAttribute("gedung", new Gedung());
        model.addAttribute("kampus", kampusDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Gedung gedung = gedungDao.findById(id).get();
            if (gedung != null) {
                model.addAttribute("gedung", gedung);
                if (gedung.getStatus() == null){
                    gedung.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/gedung/form")
    public String prosesForm(@Valid Gedung gedung){
        if (gedung.getStatus() == null){
            gedung.setStatus(StatusRecord.NONAKTIF);
        }
        gedungDao.save(gedung);
        return "redirect:list";
    }

}
