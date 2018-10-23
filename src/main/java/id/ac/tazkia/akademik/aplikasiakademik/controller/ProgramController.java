package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KonsentrasiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProgramDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Konsentrasi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Program;
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
public class ProgramController {
    private static final Logger LOGGER = LoggerFactory.getLogger(KonsentrasiController.class);

    @Autowired
    private ProgramDao programDao;


    @GetMapping("/program/list")
    public void daftarProgram(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("daftarProgram", programDao.findByStatusNotInAndNamaProgramContainingIgnoreCaseOrderByNamaProgram(StatusRecord.HAPUS,search,page));
        } else {
            model.addAttribute("daftarProgram",programDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/program/form")
    public void formProgram(Model model,@RequestParam(required = false) String id){
        model.addAttribute("program", new Program());

        if (id != null && !id.isEmpty()) {
            Program program = programDao.findById(id).get();
            if (program != null) {
                model.addAttribute("program", program);
                if (program.getStatus() == null){
                    program.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/program/form")
    public String prosesForm(@Valid Program program){
        if (program.getStatus() == null){
            program.setStatus(StatusRecord.NONAKTIF);
        }
        programDao.save(program);
        return "redirect:list";
    }

    @PostMapping("/program/delete")
    public String deleteProgram(@RequestParam Program program){
        program.setStatus(StatusRecord.HAPUS);
        programDao.save(program);

        return "redirect:list";
    }
    }

