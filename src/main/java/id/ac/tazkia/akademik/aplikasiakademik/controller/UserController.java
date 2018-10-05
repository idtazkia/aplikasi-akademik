package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;

import javax.validation.Valid;
import java.util.Set;

@Controller
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserPasswordDao userPasswordDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private DosenDao dosenDao;



    @GetMapping("/user/list")
    public void dataUser(Model model, Authentication currentUser) {





    }

    @GetMapping("/user/form")
    public void formUser(Model model, Authentication currentUser) {

    }

    @PostMapping("/user/form")
    public String prosesUpdateUser() {


        return "redirect:/index";

    }


}
