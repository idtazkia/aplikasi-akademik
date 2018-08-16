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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
public class ProgramStudiController {


    private static final Logger LOGGER = LoggerFactory.getLogger(JenjangController.class);


    @Autowired
    ProdiDao prodiDao;

    @Autowired
    UserDao userDao;

    @Autowired
    FakultasDao fakultasDao;

    @Autowired
    JurusanDao jurusanDao;

    @Autowired
    JenjangDao jenjangDao;

    @ModelAttribute("daftarJenjang")
    public Iterable<Jenjang> daftarJenjang() {
        return jenjangDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif);
    }

    @GetMapping("/programstudi/list")
    public ModelMap list(@PageableDefault(direction = Sort.Direction.ASC) Pageable page){
        return new ModelMap()
                .addAttribute("prodi",prodiDao.findByStatus(StatusRecord.AKTIF,page))
                .addAttribute("jurusan",jurusanDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif))
                .addAttribute("jenjang",jenjangDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif))
                .addAttribute("fakultas",fakultasDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif));
    }

    @GetMapping("/programstudi/form")
    public String  formFakultas(Model model, Authentication currentUser, @RequestParam(required = false)String id){
        model.addAttribute("fakultas",fakultasDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif));
        model.addAttribute("jurusan",jurusanDao.findByStatusAndNa(StatusConstants.Aktif,StatusConstants.Aktif));
        model.addAttribute("prodi", new Prodi());

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
            Prodi prodi = prodiDao.findById(id).get();
            if (prodi != null) {
                prodi.setUserEdit(u);
                prodi.setTglEdit(LocalDateTime.now());
                model.addAttribute("prodi", prodi);
            }
        }
        return "/programstudi/form";
    }

    @PostMapping(value = "/programstudi/form")
    public String uploadBukti(@Valid Prodi prodi,
                              BindingResult error, @RequestParam(required = false) Prodi id,
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
            prodi.setTglEdit(LocalDateTime.now());
            prodi.setUserEdit(u);
            prodi.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            prodi.setUserInsert(u);
            prodi.setTglInsert(LocalDateTime.now());
        }

        prodiDao.save(prodi);


        return "redirect:/programstudi/list";

    }

    @PostMapping("/delete/programstudi")
    public String hapus(@RequestParam Prodi prodi,Authentication currentUser){

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

        prodi.setUserEdit(u);
        prodi.setTglEdit(LocalDateTime.now());
        prodi.setStatus(StatusRecord.NONAKTIF);
        prodiDao.save(prodi);

        return "redirect:/programstudi/list";
    }
}
