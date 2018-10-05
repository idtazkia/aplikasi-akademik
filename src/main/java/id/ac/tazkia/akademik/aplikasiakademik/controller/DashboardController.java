package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Objects;

@Controller
public class DashboardController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private JurusanDao jurusanDao;

    @Autowired
    private FakultasDao fakultasDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @GetMapping("/dashboardmahasiswa")
    public void dashboardMahasiswa(Model model, Authentication currentUser){
        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u=userDao.findByUsername(username);
        model.addAttribute("datamahasiswa", mahasiswaDao.findByStatusNotInAndIdUser(StatusRecord.HAPUS,u));
        model.addAttribute("prodi",prodiDao.findByStatusNotIn(StatusRecord.HAPUS));
        model.addAttribute("jurusan",jurusanDao.findByStatusNotIn(StatusRecord.HAPUS));
        model.addAttribute("fakultas",fakultasDao.findByStatusNotIn(StatusRecord.HAPUS));
    }

    @GetMapping("/dashboard")
    public String daftarDashboard(Model model, Authentication currentUser){
        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u=userDao.findByUsername(username);
        if (Objects.equals(u.getRole().getId(), "mahasiswa")){
            return "redirect:dashboardmahasiswa";
        }else{
            return "dashboard";
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

    @GetMapping("/")
    public String formAwal(){
        return "redirect:/login";
    }

}
