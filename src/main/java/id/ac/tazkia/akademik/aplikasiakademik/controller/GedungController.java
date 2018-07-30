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

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
public class GedungController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GedungController.class);

    @Autowired
    ProvinsiDao provinsiDao;
    @Autowired
    KokabDao kokabDao;
    @Autowired
    KampusDao kampusDao;
    @Autowired
    UserDao userDao;
    @Autowired
    GedungDao gedungDao;



    @GetMapping("/gedung/kampus/form")
    public void  formKampus(){
    }

    @GetMapping("/gedung/list")
    public ModelMap GedungList(@PageableDefault(direction = Sort.Direction.ASC) Pageable page){
        return new ModelMap()
                .addAttribute("list",kampusDao.findByStatus(StatusConstants.Aktif,page))
                .addAttribute("gedung",gedungDao.findByStatus(StatusConstants.Aktif,page));
    }


    @ModelAttribute("daftarProp")
    public Iterable<Provinsi> daftaProp() {
        return provinsiDao.findAll();
    }

    @ModelAttribute("daftarKokab")
    public Iterable<KabupatenKota> daftaKokab() {
        return kokabDao.findAll();
    }

    @GetMapping("/gedung/form")
    private void gedungForm(Model model){
        model.addAttribute("kampus",kampusDao.findByStatus(StatusConstants.Aktif));
    }

    @PostMapping(value = "/gedung/kampus/form")
    public String uploadBukti(@Valid Kampus kampus,
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



        kampus.setTglEdit(LocalDateTime.now());
        kampus.setTglInsert(LocalDateTime.now());
        kampus.setUserEdit(u);
        kampus.setUserInsert(u);
        kampus.setStatus(StatusConstants.Aktif);
        kampusDao.save(kampus);

        return "redirect:/gedung/list";

    }


    @PostMapping(value = "/gedung/form")
    public String simpanData(@Valid Gedung gedung,
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



        gedung.setTglEdit(LocalDateTime.now());
        gedung.setTglInsert(LocalDateTime.now());
        gedung.setUserEdit(u);
        gedung.setUserInsert(u);
        gedung.setStatus(StatusConstants.Aktif);
        gedungDao.save(gedung);

        return "redirect:/gedung/list";

    }
}
