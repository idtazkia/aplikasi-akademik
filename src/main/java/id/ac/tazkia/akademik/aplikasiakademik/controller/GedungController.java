package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.GedungDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.KampusDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GedungController {
    @Autowired
    private GedungDao gedungDao;

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
    public void  formGedung(){
    }

}
