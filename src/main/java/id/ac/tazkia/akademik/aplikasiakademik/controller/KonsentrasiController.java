package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KonsentrasiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Konsentrasi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class KonsentrasiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KonsentrasiController.class);

    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private ProdiDao prodiDao;


    @GetMapping("/konsentrasi/list")
    public void daftarKonsentrasi(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("daftarKonsentrasi", konsentrasiDao.findByStatusNotInAndAndNamaKonsentrasiContainingIgnoreCaseOrderByNamaKonsentrasi(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("daftarKonsentrasi",konsentrasiDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/konsentrasi/form")
    public void  formKonsentrasi(Model model,@RequestParam(required = false) String id){
        model.addAttribute("konsentrasi", new Konsentrasi());
        model.addAttribute("prodi",prodiDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Konsentrasi konsentrasi = konsentrasiDao.findById(id).get();
            if (konsentrasi != null) {
                model.addAttribute("konsentrasi", konsentrasi);
                if (konsentrasi.getStatus() == null){
                    konsentrasi.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/konsentrasi/form")
    public String prosesForm(@Valid Konsentrasi konsentrasi){
        if (konsentrasi.getStatus() == null){
            konsentrasi.setStatus(StatusRecord.NONAKTIF);
        }
        konsentrasiDao.save(konsentrasi);
        return "redirect:list";
    }

    @PostMapping("/konsentrasi/delete")
    public String deletekonsentrasi(@RequestParam Konsentrasi konsentrasi){
        konsentrasi.setStatus(StatusRecord.HAPUS);
        konsentrasiDao.save(konsentrasi);

        return "redirect:list";
    }
}
