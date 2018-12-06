package id.ac.tazkia.akademik.aplikasiakademik.controller;


import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.print.Pageable;

@Controller
public class AbsensiMahasiswaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbsensiMahasiswaController.class);


    @Autowired
    private UserDao userDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private RekapKehadiranMahasiswaDao rekapKehadiranMahasiswaDao;

    @GetMapping("/menumahasiswa/absensi/list")
    public void daftarAttendance(Model model, Authentication authentication) {
        LOGGER.debug("Authentication class : {}", authentication.getClass().getName());

        if (authentication == null) {
            LOGGER.warn("Current user is null");
        }

        User user = currentUserService.currentUser(authentication);

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa, ta);
        model.addAttribute("presensimahasiswa",rekapKehadiranMahasiswaDao.rekapKehadiranMahasiswa(ta,mahasiswa));

    }


    @GetMapping("/menumahasiswa/absensi/form")
    public void  formAbsensi(){

    }
}
