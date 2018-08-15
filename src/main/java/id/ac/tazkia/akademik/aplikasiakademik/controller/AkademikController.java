package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProgramDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.TahunAkademikDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class AkademikController {

    @Autowired
    TahunAkademikDao tahunAkademikDao;

    @Autowired
    ProdiDao prodiDao;

    @Autowired
    ProgramDao programDao;

    @GetMapping("/akademik/list")
    public ModelMap list(@PageableDefault(direction = Sort.Direction.ASC) Pageable page){
        return new ModelMap()
                .addAttribute("tahunAkademik",tahunAkademikDao.findByStatus(StatusConstants.Aktif,page))
                .addAttribute("prodi",prodiDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif))
                .addAttribute("program",programDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif));
    }


    @GetMapping(value = "/akademik/form")
    public String tampilkanForms(@RequestParam(value = "id", required = false) String id, Model model) {
        TahunAkademik tahunAkademik = new TahunAkademik();
        if (id != null && !id.isEmpty()) {
            tahunAkademik = tahunAkademikDao.findById(id).get();
        } else {

        }

        model.addAttribute("tahunAkademik", tahunAkademik);
        model.addAttribute("listProdi", prodiDao.findByStatusAndNa("1","1"));
        model.addAttribute("listProgram", programDao.findByStatusAndNa("1","1"));
        return "akademik/form";
    }

    @RequestMapping(value = "/akademik/form", method = RequestMethod.POST)
    public String simpan(@Valid TahunAkademik tahunAkademik, BindingResult errors) {
//        if (errors.hasErrors()) {
//            return "akademik/form";
//        }

        tahunAkademik.setStatus("1");
        tahunAkademikDao.save(tahunAkademik);
        return "redirect:/akademik/list";
    }
}
