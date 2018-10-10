package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import sun.security.util.Password;

import java.util.List;

@Controller
public class KrsMahasiswaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(KrsMahasiswaController.class);

    @Autowired
    private UserDao userDao;
    @Autowired
    private MahasiswaDao mahasiswaDao;
    @Autowired
    private KrsDao krsDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private KaryawanDao karyawanDao;
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private UserPasswordDao userPasswordDao;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/menumahasiswa/krs/list")
    public void daftarKRS(Model model, Authentication currentUser){
        LOGGER.debug("Authentication class : {}", currentUser.getClass().getName());

        if (currentUser == null) {
            LOGGER.warn("Current user is null");
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User user = userDao.findByUsername(username);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        model.addAttribute("mahasiswa",mahasiswa);

        Krs krs = krsDao.findByTahunAkademikStatusAndMahasiswa(StatusRecord.AKTIF,mahasiswa);
        model.addAttribute("krs",krs);

        model.addAttribute("minggu",krsDetailDao.findByKrsAndAndMahasiswaAndJadwalIdHariId(krs,mahasiswa,"00"));
        model.addAttribute("senin",krsDetailDao.findByKrsAndAndMahasiswaAndJadwalIdHariId(krs,mahasiswa,"01"));
        model.addAttribute("selasa",krsDetailDao.findByKrsAndAndMahasiswaAndJadwalIdHariId(krs,mahasiswa,"02"));
        model.addAttribute("rabu",krsDetailDao.findByKrsAndAndMahasiswaAndJadwalIdHariId(krs,mahasiswa,"03"));
        model.addAttribute("kamis",krsDetailDao.findByKrsAndAndMahasiswaAndJadwalIdHariId(krs,mahasiswa,"04"));
        model.addAttribute("jumat",krsDetailDao.findByKrsAndAndMahasiswaAndJadwalIdHariId(krs,mahasiswa,"05"));
        model.addAttribute("sabtu",krsDetailDao.findByKrsAndAndMahasiswaAndJadwalIdHariId(krs,mahasiswa,"06"));



    }

    @GetMapping("/menumahasiswa/krs/form")
    public void  formKRS(){

    }

}
