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

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);


    @Autowired
    private UserDao userDao;

    @Autowired
    private UserPasswordDao userPasswordDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private WaliDao waliDao;



    @GetMapping("/user/data")
    public void dataUser(Model model, Authentication currentUser) {
        LOGGER.debug("Authentication class : {}", currentUser.getClass().getName());

        if (currentUser == null) {
            LOGGER.warn("Current user is null");
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);
        LOGGER.debug("User ID : {}", u.getId());
        if (u == null) {
            LOGGER.warn("Username {} not found in database ", username);
        }

        Mahasiswa mahasiswa = mahasiswaDao.findByUser(u);
        model.addAttribute("mahasiswa",mahasiswa);

    }

    @GetMapping("/user/form")
    public void formUser(Model model, Authentication currentUser) {

    }

    @PostMapping("/user/form")
    public String prosesUpdateUser() {


        return "redirect:/index";

    }


}
