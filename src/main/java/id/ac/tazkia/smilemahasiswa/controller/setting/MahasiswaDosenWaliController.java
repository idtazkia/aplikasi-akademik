package id.ac.tazkia.smilemahasiswa.controller.setting;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MahasiswaDosenWaliController {

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private MahasiswaDosenWaliDao mahasiswaDosenWaliDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ProdiDao prodiDao;

    @GetMapping("/setting/mahasiswa/dosen_wali")
    public String settingMahasiswaDosenWali(Model model,
                                            @PageableDefault(size = 20) Pageable page,
                                            @RequestParam(required = false) Prodi prodi){

        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("angkatan");

        if(prodi == null){

            model.addAttribute("listMahasiswaDosenWali", mahasiswaDosenWaliDao.listMahasiswaDosenWali(page));

        }else{

            model.addAttribute("listMahasiswaDosenWali", mahasiswaDosenWaliDao.listMahasiswaDosenWaliProdi(prodi.getId(),page));

        }

        return "setting/mahasiswadosenwali/list";

    }


}
