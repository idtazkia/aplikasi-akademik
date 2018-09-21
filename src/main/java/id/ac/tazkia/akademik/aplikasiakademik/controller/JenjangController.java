package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.JenjangDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jenjang;
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
import java.time.LocalDateTime;

@Controller
public class JenjangController {
    @Autowired
    private JenjangDao jenjangDao;

    @GetMapping("/jenjang/list")
    public void daftarJenjang(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", jenjangDao.findByStatusNotInAndNamaJenjangContainingIgnoreCaseOrderByNamaJenjang(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",jenjangDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/jenjang/form")
    public void jenjangForm(Model model,@RequestParam(required = false) String id){
        model.addAttribute("jenjang", new Jenjang());

        if (id != null && !id.isEmpty()) {
            Jenjang jenjang = jenjangDao.findById(id).get();
            if (jenjang != null) {
                model.addAttribute("jenjang", jenjang);
                if (jenjang.getStatus() == null){
                    jenjang.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/jenjang/form")
    public String prosesForm(@Valid Jenjang jenjang){
        if (jenjang.getStatus() == null){
            jenjang.setStatus(StatusRecord.NONAKTIF);
        }
        jenjangDao.save(jenjang);
        return "redirect:list";
    }

    @PostMapping("/jenjang/delete")
    public String deleteJenjang(@RequestParam Jenjang jenjang){
        jenjang.setStatus(StatusRecord.HAPUS);
        jenjangDao.save(jenjang);

        return "redirect:list";
    }
}

