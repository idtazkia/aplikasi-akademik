package id.ac.tazkia.akademik.aplikasiakademik.controller;


import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
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
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private RekapKehadiranMahasiswaDao rekapKehadiranMahasiswaDao;

    @GetMapping("/menumahasiswa/absensi/list")
    public void daftarAttendance(Model model, Authentication currentUser) {
        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(u);
        TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Krs k = krsDao.findByMahasiswaAndTahunAkademik(mahasiswa, ta);
        model.addAttribute("presensimahasiswa",rekapKehadiranMahasiswaDao.rekapKehadiranMahasiswa(ta,mahasiswa));
    }


    @GetMapping("/menumahasiswa/absensi/form")
    public void  formAbsensi(){

    }
}
