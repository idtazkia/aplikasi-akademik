package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KampusDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Kampus;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
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
public class KampusController {

    @Autowired
    private KampusDao kampusDao;

    @GetMapping("/kampus/list")
    public void  listKampus(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listKampus", kampusDao.findByStatusNotInAndNamaKampusContainingIgnoreCaseOrderByNamaKampus(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("listKampus",kampusDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }

    }

    @GetMapping("/kampus/form")
    public void formKampus(Model model,@RequestParam(required = false) String id){
        model.addAttribute("kampus", new Kampus());

        if (id != null && !id.isEmpty()) {
            Kampus kampus = kampusDao.findById(id).get();
            if (kampus != null) {
                model.addAttribute("kampus", kampus);
                if (kampus.getStatus() == null){
                    kampus.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/kampus/form")
    public String prosesForm(@Valid Kampus kampus){
        if (kampus.getStatus() == null){
            kampus.setStatus(StatusRecord.NONAKTIF);
        }
        kampusDao.save(kampus);
        return "redirect:list";
    }

    @PostMapping("/kampus/delete")
    public String deleteKampus(@RequestParam Kampus kampus){
        kampus.setStatus(StatusRecord.HAPUS);
        kampusDao.save(kampus);

        return "redirect:list";
    }

}
