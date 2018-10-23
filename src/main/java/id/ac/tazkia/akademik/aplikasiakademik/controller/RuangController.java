package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.GedungDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.RuanganDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.RuanganJenisDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Ruangan;
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
public class RuangController {

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private RuanganJenisDao ruanganJenisDao;

    @Autowired
    private GedungDao gedungDao;

    @GetMapping("/ruang/list")
    public void daftarRuang(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listRuang", ruanganDao.findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("listRuang",ruanganDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/ruang/form")
    public void  formRuang(Model model,@RequestParam(required = false) String id){
        model.addAttribute("ruangan", new Ruangan());
        model.addAttribute("ruangJenis", ruanganJenisDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("gedung", gedungDao.findByStatus(StatusRecord.AKTIF));


        if (id != null && !id.isEmpty()) {
            Ruangan ruangan = ruanganDao.findById(id).get();
            if (ruangan != null) {
                model.addAttribute("ruangan", ruangan);
                if (ruangan.getStatus() == null){
                    ruangan.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/ruang/form")
    public String prosesForm(@Valid Ruangan ruangan){
        if (ruangan.getStatus() == null){
            ruangan.setStatus(StatusRecord.NONAKTIF);
        }
        ruanganDao.save(ruangan);
        return "redirect:list";
    }

    @PostMapping("/ruang/delete")
    public String deleteRuangan(@RequestParam Ruangan ruangan){
        ruangan.setStatus(StatusRecord.HAPUS);
        ruanganDao.save(ruangan);

        return "redirect:list";
    }

}
