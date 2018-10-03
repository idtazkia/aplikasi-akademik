package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KrsDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MahasiswaDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KrsMahasiswaController {

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @GetMapping("/menumahasiswa/krs/list")
    public void role(Model model, Authentication currentUser) throws Exception{

        System.out.println("username" + currentUser.getClass().getName());


        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User user = userDao.findByUsername(username);

        Mahasiswa mhsw = mahasiswaDao.findByUser(user);

        model.addAttribute("mahasiswaDetail", mhsw);




    }

    @GetMapping("/menumahasiswa/krs/form")
    public void  formKRS(){

    }

}
