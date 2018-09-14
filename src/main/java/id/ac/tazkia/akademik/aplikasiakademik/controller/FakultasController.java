package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.FakultasDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
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

    @GetMapping("/fakultas/list")
    public void list(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", fakultasDao.findByStatusNotInAndAndNamaFakultasContainingIgnoreCaseOrderByNamaFakultas(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",fakultasDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }
    
    @GetMapping("/fakultas/form")
    public String  formFakultas(Model model,Authentication currentUser, @RequestParam(required = false)String id) {

        model.addAttribute("fakultas", new Fakultas());

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
            Fakultas fakultas = fakultasDao.findById(id).get();
            if (fakultas != null) {
                fakultas.setUserEdit(u);
                fakultas.setTglEdit(LocalDateTime.now());
                model.addAttribute("fakultas", fakultas);
            }
        }
        return "/fakultas/form";
    }

    @PostMapping(value = "/fakultas/form")
    public String uploadBukti(@Valid Fakultas fakultas,
                              BindingResult error,@RequestParam(required = false) Fakultas id,
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
            fakultas.setTglEdit(LocalDateTime.now());
            fakultas.setUserEdit(u);
            fakultas.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            fakultas.setUserInsert(u);
            fakultas.setTglInsert(LocalDateTime.now());
        }

        if (fakultas.getStatus() == null){
            fakultas.setStatus(StatusRecord.NONAKTIF);
        }

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
        fakultas.setStatus(StatusRecord.HAPUS);
        fakultasDao.save(fakultas);

        return "redirect:/fakultas/list";
    }
}