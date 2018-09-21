package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.LembagaDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

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
    public void  formLembaga(){
    }
}
