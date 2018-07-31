package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.JenjangDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jenjang;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
public class JenjangController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JenjangController.class);


    @Autowired
    JenjangDao jenjangDao;

    @Autowired
    UserDao userDao;

    @GetMapping("/jenjang/list")
    public ModelMap list(@PageableDefault(direction = Sort.Direction.ASC) Pageable page){
        return new ModelMap()
                .addAttribute("list",jenjangDao.findByStatus(StatusConstants.Aktif,page));
    }

    @GetMapping("/jenjang/form")
    public String  formFakultas(Model model,Authentication currentUser, @RequestParam(required = false)String id){

        model.addAttribute("jenjang", new Jenjang());

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

        if (id != null && !id.isEmpty()) {
            Jenjang jenjang = jenjangDao.findById(id).get();
            if (jenjang != null) {
                jenjang.setUserEdit(u);
                jenjang.setTglEdit(LocalDateTime.now());
                model.addAttribute("jenjang", jenjang);
            }
        }
        return "/jenjang/form";
    }


    @PostMapping(value = "/jenjang/form")
    public String uploadBukti(@Valid Jenjang jenjang,
                              BindingResult error,@RequestParam(required = false) Jenjang id,
                              Authentication currentUser){

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

        if (id != null){
            jenjang.setTglEdit(LocalDateTime.now());
            jenjang.setUserEdit(u);
            jenjang.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            jenjang.setUserInsert(u);
            jenjang.setTglInsert(LocalDateTime.now());
        }

        jenjangDao.save(jenjang);


        return "redirect:/jenjang/list";

    }
}
