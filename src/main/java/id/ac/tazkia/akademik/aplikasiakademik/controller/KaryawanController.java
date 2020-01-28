package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KaryawanDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.RoleDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jws.soap.SOAPBinding;
import javax.validation.Valid;

@Controller
public class KaryawanController {

    @Autowired
    private KaryawanDao karyawanDao;

    @GetMapping("/karyawan/list")
    public String daftarKaryawan(Model model,
                                 @PageableDefault(size = 10)Pageable page,
                                 @RequestParam (required = false) String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listKaryawan", karyawanDao.findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord.AKTIF, search, page));
        }else {
            model.addAttribute("listKaryawan", karyawanDao.findByStatusOrderByNamaKaryawan(StatusRecord.AKTIF, page));
        }

        return "karyawan/list";
    }

    @GetMapping("/karyawan/baru")
    public String karyawanBaru(Model model,@RequestParam(required = false) String id){

        model.addAttribute("karyawan", new Karyawan());

        if (id != null && !id.isEmpty()) {
            Karyawan karyawan = karyawanDao.findById(id).get();
            if (karyawan != null) {
                model.addAttribute("karyawan", karyawan);
            }
        }

        return "karyawan/form";
    }


    @PostMapping("/karyawan/save")
    public String saveKaryawan(@Valid @ModelAttribute Karyawan karyawan){
        karyawanDao.save(karyawan);
        return"redirect:list";

    }

    @PostMapping("/karyawan/delete")
    public String deleteKaryawan(@RequestParam Karyawan karyawan){
        karyawan.setStatus(StatusRecord.HAPUS);
        karyawanDao.save(karyawan);

        return "redirect:list";

    }


}
