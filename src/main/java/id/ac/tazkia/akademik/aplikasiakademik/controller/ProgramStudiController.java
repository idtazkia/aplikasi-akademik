package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.JenjangDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.JurusanDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProgramDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jenjang;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
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
public class ProgramStudiController {
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private JurusanDao jurusanDao;
    @Autowired
    private JenjangDao jenjangDao;
    @Autowired
    private ProgramDao programDao;

    @GetMapping("/programstudi/list")
    public void daftarProgramStudi(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", prodiDao.findByStatusNotInAndAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",prodiDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/programstudi/form")
    public void  formProgramStudi(Model model,@RequestParam(required = false) String id){
        model.addAttribute("programStudy", new Prodi());
        model.addAttribute("jurusan",jurusanDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("jenjang",jenjangDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("program", programDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Prodi prodi = prodiDao.findById(id).get();
            if (prodi != null) {
                model.addAttribute("programStudy", prodi);
                if (prodi.getStatus() == null){
                    prodi.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/programstudi/form")
    public String prosesForm(@Valid Prodi prodi){
        if (prodi.getStatus() == null){
            prodi.setStatus(StatusRecord.NONAKTIF);
        }
        prodiDao.save(prodi);
        return "redirect:list";
    }

    @PostMapping("/programstudi/delete")
    public String deleteJenjang(@RequestParam Prodi prodi){
        prodi.setStatus(StatusRecord.HAPUS);
        prodiDao.save(prodi);

        return "redirect:list";
    }
}
