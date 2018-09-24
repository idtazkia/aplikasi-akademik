package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.FakultasDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.LembagaDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
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
public class FakultasController {
    
    @Autowired
    private FakultasDao fakultasDao;

    @Autowired
    private LembagaDao lembagaDao;
    
    
    @GetMapping("/fakultas/list")
    public void daftarFakultas(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", fakultasDao.findByStatusNotInAndAndNamaFakultasContainingIgnoreCaseOrderByNamaFakultas(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",fakultasDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/fakultas/form")
    public void  formFakultas(Model model,@RequestParam(required = false) String id){
        model.addAttribute("fakultas", new Fakultas());
        model.addAttribute("lembaga",lembagaDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Fakultas fakultas = fakultasDao.findById(id).get();
            if (fakultas != null) {
                model.addAttribute("fakultas", fakultas);
                if (fakultas.getStatus() == null){
                    fakultas.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/fakultas/form")
    public String prosesForm(@Valid Fakultas fakultas){
        if (fakultas.getStatus() == null){
            fakultas.setStatus(StatusRecord.NONAKTIF);
        }
        fakultasDao.save(fakultas);
        return "redirect:list";
    }

    @PostMapping("/fakultas/delete")
    public String deletefakultas(@RequestParam Fakultas fakultas){
        fakultas.setStatus(StatusRecord.HAPUS);
        fakultasDao.save(fakultas);

        return "redirect:list";
    }
}
