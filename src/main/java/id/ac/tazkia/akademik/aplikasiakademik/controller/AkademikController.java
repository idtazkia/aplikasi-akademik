package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.TahunAkademikDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class AkademikController {
    
    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @GetMapping("/akademik/list")
    public void akademikList(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", tahunAkademikDao.findByStatusNotInAndNamaTahunAkademikContainingIgnoreCaseOrderByStatusAsc(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",tahunAkademikDao.findByStatusNotInOrderByStatusAsc(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/akademik/form")
    public void akademikForm(Model model, @RequestParam(required = false) String id){
        model.addAttribute("tahunAkademik", new TahunAkademik());

        if (id != null && !id.isEmpty()) {
            TahunAkademik tahunAkademik = tahunAkademikDao.findById(id).get();
            if (tahunAkademik != null) {
                model.addAttribute("tahunAkademik", tahunAkademik);
            }
        }
    }

    @PostMapping("/akademik/form")
    public String prosesForm(@Valid TahunAkademik tahunAkademik){

        tahunAkademikDao.save(tahunAkademik);
        return "redirect:list";
    }

    @PostMapping("/akademik/delete")
    public String deleteJenjang(@RequestParam TahunAkademik tahunAkademik){
        tahunAkademik.setStatus(StatusRecord.HAPUS);
        tahunAkademikDao.save(tahunAkademik);

        return "redirect:list";
    }

    @PostMapping("/akademik/aktif")
    public String aktifAkademik(@RequestParam TahunAkademik tahunAkademik){
        TahunAkademik thnAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        if (thnAkademik != null) {
            thnAkademik.setStatus(StatusRecord.NONAKTIF);
            tahunAkademikDao.save(thnAkademik);
        }

        tahunAkademik.setStatus(StatusRecord.AKTIF);

        tahunAkademikDao.save(tahunAkademik);

        return "redirect:list";
    }
}
