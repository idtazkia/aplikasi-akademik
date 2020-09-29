package id.ac.tazkia.smilemahasiswa.controller.setting;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.MahasiswaDosenWaliDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.cariDosen(StatusRecord.HAPUS);
    }

    @GetMapping("/setting/mahasiswa/dosen_wali")
    public String settingMahasiswaDosenWali(Model model,
                                            @PageableDefault(size = 20) Pageable page,
                                            @RequestParam(required = false) Prodi prodi,
                                            @RequestParam(required = false) String angkatan){

        model.addAttribute("prodi", prodiDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("angkatan", mahasiswaDao.cariAngkatan());

        if(prodi == null){

            model.addAttribute("listMahasiswaDosenWali", mahasiswaDosenWaliDao.listMahasiswaDosenWali(page));

        }else{
            model.addAttribute("listMahasiswaDosenWali", mahasiswaDosenWaliDao.listMahasiswaDosenWaliProdi(prodi.getId(),page));
            model.addAttribute("selectedProdi",prodi);
            model.addAttribute("selectedAngkatan",angkatan);
            model.addAttribute("selectedMahasiswa",mahasiswaDosenWaliDao.listMahasiswa(angkatan,prodi));
        }

        return "setting/mahasiswadosenwali/list";

    }

    @PostMapping("/setting/mahasiswa/save")
    public String saveDosenWali(String[] mahasiswas,String dosens){
        Dosen dosen = dosenDao.findById(dosens).get();
        for (String m : mahasiswas){
            Mahasiswa mahasiswa = mahasiswaDao.findById(m).get();
            mahasiswa.setDosen(dosen);
            mahasiswaDao.save(mahasiswa);

            MahasiswaDosenWali mahasiswaDosenWali = mahasiswaDosenWaliDao.findByMahasiswaAndStatus(mahasiswa,StatusRecord.AKTIF);
            if (mahasiswaDosenWali != null){
                mahasiswaDosenWali.setStatus(StatusRecord.NONAKTIF);
                mahasiswaDosenWaliDao.save(mahasiswaDosenWali);
                MahasiswaDosenWali mhd = new MahasiswaDosenWali();
                mhd.setDosen(dosen);
                mhd.setStatus(StatusRecord.AKTIF);
                mhd.setMahasiswa(mahasiswa);
                mahasiswaDosenWaliDao.save(mhd);
            }else {
                MahasiswaDosenWali mhd = new MahasiswaDosenWali();
                mhd.setDosen(dosen);
                mhd.setStatus(StatusRecord.AKTIF);
                mhd.setMahasiswa(mahasiswa);
                mahasiswaDosenWaliDao.save(mhd);
            }
        }

        return "redirect:dosen_wali";

    }


}
