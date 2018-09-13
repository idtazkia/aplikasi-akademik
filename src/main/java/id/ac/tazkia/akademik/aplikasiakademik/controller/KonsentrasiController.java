package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;

import static id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants.Aktif;

@Controller
public class KonsentrasiController {
    private static final Logger LOGGER = LoggerFactory.getLogger(KonsentrasiController.class);

    @Autowired
    KonsentrasiDao konsentrasiDao;

    @Autowired
    UserDao userDao;

    @Autowired
    ProdiDao prodiDao;

    @ModelAttribute("daftarProdi")
    public Iterable<Prodi> daftarProdi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }

    @GetMapping("/konsentrasi/list")
    public void RuangList(@PageableDefault(direction = Sort.Direction.ASC) Pageable page,String search,Model model){
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", konsentrasiDao.findByStatusNotInAndAndNamaKonsentrasiContainingIgnoreCaseOrderByNamaKonsentrasi(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list",konsentrasiDao.findByStatusNotIn(StatusRecord.HAPUS,page));

        }
    }

    @GetMapping("/konsentrasi/form")
    public String  formFakultas(Model model, Authentication currentUser, @RequestParam(required = false)String id){

        model.addAttribute("konsentrasi", new Konsentrasi());

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
            Konsentrasi konsentrasi = konsentrasiDao.findById(id).get();
            if (konsentrasi != null) {
                konsentrasi.setUserEdit(u);
                konsentrasi.setTglEdit(LocalDateTime.now());
                model.addAttribute("konsentrasi", konsentrasi);
            }
        }
        return "/konsentrasi/form";
    }

    @PostMapping(value = "/konsentrasi/form")
    public String uploadBukti(@Valid Konsentrasi konsentrasi,
                              BindingResult error, @RequestParam(required = false) Konsentrasi id,
                              Authentication currentUser){

        LOGGER.debug("Authentication class : {}", currentUser.getClass().getName());

        if (currentUser == null) {
            LOGGER.warn("Current user is null");
        }

        if (konsentrasi.getStatus() == null){
            konsentrasi.setStatus(StatusRecord.NONAKTIF);
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);
        LOGGER.debug("User ID : {}", u.getId());
        if (u == null) {
            LOGGER.warn("Username {} not found in database ", username);
        }

        if (id != null){
            konsentrasi.setTglEdit(LocalDateTime.now());
            konsentrasi.setUserEdit(u);
            konsentrasi.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            konsentrasi.setUserInsert(u);
            konsentrasi.setTglInsert(LocalDateTime.now());
        }

        konsentrasiDao.save(konsentrasi);


        return "redirect:/konsentrasi/list";

    }

    @PostMapping(value = "/delete/konsentrasi")
    public String deleteKonsentrasi(@RequestParam Konsentrasi konsentrasi,Authentication currentUser){
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

        konsentrasi.setStatus(StatusRecord.HAPUS);
        konsentrasi.setTglEdit(LocalDateTime.now());
        konsentrasi.setUserEdit(u);
        konsentrasiDao.save(konsentrasi);

        return "redirect:/konsentrasi/list";
    }
}
