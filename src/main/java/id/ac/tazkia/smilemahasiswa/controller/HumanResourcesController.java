package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.KaryawanDao;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
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
public class HumanResourcesController {
    @Autowired
    private KaryawanDao karyawanDao;

//Employee

    @GetMapping("/humanResources/employee/list")
    public void daftarKaryawan(Model model, @PageableDefault(size = 10) Pageable page, String search){
        if (StringUtils.hasText(search)){
            model.addAttribute("search", search);
            model.addAttribute("listkaryawan", karyawanDao.findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord.AKTIF, search, page));
        }else{
            model.addAttribute("listkaryawan", karyawanDao.findByStatusOrderByNamaKaryawan(StatusRecord.AKTIF, page));
        }
    }

    @GetMapping("/humanResources/employee/form")
    public void  formKaryawan(Model model, @RequestParam(required = false) String id){
        model.addAttribute("karyawan", new Karyawan());

        if (id != null && !id.isEmpty()) {
            Karyawan karyawan = karyawanDao.findById(id).get();
            model.addAttribute("karyawan" , karyawan);
        }


    }

    @PostMapping("/humanResources/employee/save")
    public String saveKaryawan(@Valid Karyawan karyawan){
        karyawanDao.save(karyawan);
        return "redirect:list";
    }

    @PostMapping("/humanResources/employee/delete")
    public String deleteKaryawan(@Valid Karyawan karyawan){
        karyawan.setStatus(StatusRecord.HAPUS);
        karyawan.setIdAbsen(karyawan.getIdAbsen());
        karyawanDao.save(karyawan);
        return "redirect:list";
    }
}
