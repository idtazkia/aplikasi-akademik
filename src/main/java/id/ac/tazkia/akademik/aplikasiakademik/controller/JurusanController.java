package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.FakultasDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.JurusanDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jurusan;
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
public class JurusanController {

    @Autowired
    private JurusanDao jurusanDao;

    @Autowired
    private FakultasDao fakultasDao;

    @GetMapping("/jurusan/list")
    public void daftarJurusan(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listJurusan", jurusanDao.findByStatusNotInAndNamaJurusanContainingIgnoreCaseOrderByNamaJurusan(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("listJurusan",jurusanDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/jurusan/form")
    public void  formJurusan(Model model,@RequestParam(required = false) String id){
        model.addAttribute("jurusan", new Jurusan());

        if (id != null && !id.isEmpty()) {
            Jurusan jurusan = jurusanDao.findById(id).get();
            if (jurusan != null) {
                model.addAttribute("jurusan", jurusan);
                if (jurusan.getStatus() == null){
                    jurusan.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }

        model.addAttribute("listFakultas",fakultasDao.findByStatus(StatusRecord.AKTIF));


    }

    @PostMapping("/jurusan/form")
    public String prosesFormJurusan(@Valid Jurusan jurusan){
        if (jurusan.getStatus() == null){
            jurusan.setStatus(StatusRecord.NONAKTIF);
        }
        jurusanDao.save(jurusan);
        return "redirect:list";
    }

    @PostMapping("/jurusan/delete")
    public String deleteJurusan(@RequestParam Jurusan jurusan){
        jurusan.setStatus(StatusRecord.HAPUS);
        jurusanDao.save(jurusan);

        return "redirect:list";
    }




}
