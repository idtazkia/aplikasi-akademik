package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.LembagaDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jenjang;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Lembaga;
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
public class LembagaController {
    @Autowired
    private LembagaDao lembagaDao;


    @GetMapping("/lembaga/list")
    public void daftarLembaga(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", lembagaDao.findByStatusNotInAndNamaLembagaContainingIgnoreCaseOrderByNamaLembaga(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",lembagaDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/lembaga/form")
    public void  formLembaga(Model model,@RequestParam(required = false) String id){
        model.addAttribute("lembaga", new Lembaga());

        if (id != null && !id.isEmpty()) {
            Lembaga lembaga = lembagaDao.findById(id).get();
            if (lembaga != null) {
                model.addAttribute("lembaga", lembaga);
                if (lembaga.getStatus() == null){
                    lembaga.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/lembaga/form")
    public String prosesForm(@Valid Lembaga lembaga){
        if (lembaga.getStatus() == null){
            lembaga.setStatus(StatusRecord.NONAKTIF);
        }
        lembagaDao.save(lembaga);
        return "redirect:list";
    }


}
