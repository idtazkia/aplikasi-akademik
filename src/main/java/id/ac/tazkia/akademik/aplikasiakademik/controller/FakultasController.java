package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.FakultasDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
public class FakultasController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakultasController.class);

    @Autowired
    FakultasDao fakultasDao;

    @Autowired
    UserDao userDao;

    //    syntaxtampil
    @GetMapping("/fakultas/list")
    public void daftarFakultas(Model model, Pageable pagel) {
        model.addAttribute("list", fakultasDao.findByStatus(StatusConstants.Aktif, pagel));
    }

    @GetMapping("/fakultas/form")
    public void formFakultas(){
    }

    @PostMapping(value = "/fakultas/form")
    public String uploadBukti(@Valid Fakultas fakultas,
                              BindingResult error,
                              Authentication currentUser) throws Exception {

//        mengambil data user yang login
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

        fakultas.setTglEdit(LocalDateTime.now());
        fakultas.setTglInsert(LocalDateTime.now());
        fakultas.setUserEdit(u);
        fakultas.setUserInsert(u);
        fakultas.setStatus(StatusConstants.Aktif);
        fakultasDao.save(fakultas);

        return "redirect:/fakultas/list";
    }

    @PostMapping("/delete/fakultas")
    public String delete(@RequestParam Fakultas fakultas,
                         Authentication currentUser) {

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

        fakultas.setTglEdit(LocalDateTime.now());
        fakultas.setUserEdit(u);
        fakultas.setStatus(StatusConstants.Nonaktif);
        fakultasDao.save(fakultas);

        return "redirect:/fakultas/list";
    }
}
