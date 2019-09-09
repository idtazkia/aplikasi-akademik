package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.DosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class DosenController {

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserDao userDao;

    @GetMapping("/dosen/list")
    public void gedungList(Model model, Pageable page, String search) {

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listDosen", dosenDao.findByStatusNotInAndKaryawanNamaKaryawanContainingIgnoreCaseOrKaryawanNikContainingIgnoreCase(StatusRecord.HAPUS, search,search,page));
        } else {
            model.addAttribute("listDosen", dosenDao.findByStatusNotIn(StatusRecord.HAPUS,page));
        }
    }

    @GetMapping("/dosen/form")
    public void   formDosen(Model model,@RequestParam(required = false) String id){
        model.addAttribute("prodi", prodiDao.findByStatusNotIn(StatusRecord.HAPUS));
        model.addAttribute("karyawan", new DosenDto());

        if (id != null && !id.isEmpty()) {
            Karyawan karyawan = karyawanDao.findById(id).get();
            if (karyawan != null) {
                DosenDto dosenDto = new DosenDto();
                dosenDto.setEmail(karyawan.getEmail());
                dosenDto.setGelar(karyawan.getGelar());
                dosenDto.setId(karyawan.getId());
                dosenDto.setJenisKelamin(karyawan.getJenisKelamin());
                dosenDto.setNama(karyawan.getNamaKaryawan());
                dosenDto.setNidn(karyawan.getNidn());
                dosenDto.setNik(karyawan.getNik());
                if (karyawan.getIdUser() != null) {
                    dosenDto.setIdUser(karyawan.getIdUser());
                }
                dosenDto.setProdi(dosenDao.findByKaryawan(karyawan).getProdi().getId());
                dosenDto.setTanggalLahir(karyawan.getTanggalLahir());
                model.addAttribute("karyawan", dosenDto);
                model.addAttribute("dosen", dosenDao.findByKaryawan(karyawan));
            }
        }

    }


    @PostMapping("/dosen/form")
    public String prosesForm(@Valid DosenDto dosenDto){
        if (dosenDto.getId() == null || dosenDto.getId().isEmpty()) {
            User user = new User();
            user.setActive(Boolean.TRUE);
            user.setRole(roleDao.findById("dosen").get());
            user.setUsername(dosenDto.getEmail());
            userDao.save(user);

            Karyawan karyawan = new Karyawan();
            karyawan.setEmail(dosenDto.getEmail());
            karyawan.setGelar(dosenDto.getGelar());
            karyawan.setJenisKelamin(dosenDto.getJenisKelamin());
            karyawan.setNamaKaryawan(dosenDto.getNama());
            karyawan.setNidn(dosenDto.getNidn());
            karyawan.setNik(dosenDto.getNik());
            karyawan.setIdUser(user);
            karyawan.setTanggalLahir(dosenDto.getTanggalLahir());
            karyawanDao.save(karyawan);

            Dosen d = new Dosen();
            d.setProdi(prodiDao.findById(dosenDto.getProdi()).get());
            d.setKaryawan(karyawan);
            d.setStatus(StatusRecord.AKTIF);
            dosenDao.save(d);
        }else {
            Karyawan karyawan = karyawanDao.findById(dosenDto.getId()).get();
            if (dosenDto.getIdUser() == null){
                User user = new User();
                user.setActive(Boolean.TRUE);
                user.setRole(roleDao.findById("dosen").get());
                user.setUsername(dosenDto.getEmail());
                userDao.save(user);
                karyawan.setIdUser(user);
            }
            if (dosenDto.getIdUser() != null){
                karyawan.setIdUser(dosenDto.getIdUser());
                User user = userDao.findById(dosenDto.getIdUser().getId()).get();
                user.setUsername(dosenDto.getEmail());
                userDao.save(user);
            }
            karyawan.setEmail(dosenDto.getEmail());
            karyawan.setGelar(dosenDto.getGelar());
            karyawan.setJenisKelamin(dosenDto.getJenisKelamin());
            karyawan.setNamaKaryawan(dosenDto.getNama());
            karyawan.setNidn(dosenDto.getNidn());
            karyawan.setNik(dosenDto.getNik());
            karyawan.setTanggalLahir(dosenDto.getTanggalLahir());

            karyawanDao.save(karyawan);

            Dosen dosen = dosenDao.findByKaryawan(karyawan);
            dosen.setProdi(prodiDao.findById(dosenDto.getProdi()).get());
            dosenDao.save(dosen);
        }


        return "redirect:list";
    }

    @PostMapping("/dosen/delete")
    public String delete(@RequestParam Dosen dosen){
        dosen.setStatus(StatusRecord.HAPUS);
        dosenDao.save(dosen);

        return "redirect:list";
    }

}
