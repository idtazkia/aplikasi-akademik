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

@Controller
public class GedungController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GedungController.class);

    @ModelAttribute("daftarProp")
    public Iterable<Provinsi> daftaProp() {
        return provinsiDao.findAll();
    }

    @ModelAttribute("daftarKokab")
    public Iterable<KabupatenKota> daftaKokab() {
        return kokabDao.findAll();
    }

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


    @GetMapping("/gedung/list")
    public void GedungList(@PageableDefault(direction = Sort.Direction.ASC) Pageable page,Model model,@RequestParam(required = false) String search,@RequestParam(required = false) String searchGedung) {

        model.addAttribute("gedung",gedungDao.findByStatusNotIn(StatusRecord.HAPUS,page));
        model.addAttribute("list",kampusDao.findByStatusNotIn(StatusRecord.HAPUS,page));


    }


    //Controller Kampus

    @GetMapping("/gedung/kampus/form")
    public String formGedung(Model model, Authentication currentUser, @RequestParam(required = false) String id) {

        model.addAttribute("kampus", new Kampus());

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
            Kampus kampus = kampusDao.findById(id).get();
            if (kampus != null) {
                kampus.setUserEdit(u);
                kampus.setTglEdit(LocalDateTime.now());
                model.addAttribute("kampus", kampus);
            }
        }
        return "/gedung/kampus/form";
    }


    @PostMapping(value = "/gedung/kampus/form")
    public String proses(@Valid Kampus kampus,
                         BindingResult error, @RequestParam(required = false) Kampus id,
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

        if (id != null) {
            kampus.setTglEdit(LocalDateTime.now());
            kampus.setUserEdit(u);
            kampus.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            kampus.setUserInsert(u);
            kampus.setTglInsert(LocalDateTime.now());
        }

        kampusDao.save(kampus);


        return "redirect:/gedung/list";

    }

    @PostMapping("/delete/gedung/kampus")
    public String delete(@RequestParam Kampus kampus, Authentication currentUser) {

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

        kampus.setStatus(StatusRecord.HAPUS);
        kampus.setUserEdit(u);
        kampus.setTglEdit(LocalDateTime.now());
        kampusDao.save(kampus);

        return "redirect:/gedung/list";
    }


///////

    //Controller Gedung
    @GetMapping("/gedung/form")
    public String formFakultas(Model model, Authentication currentUser, @RequestParam(required = false) String id) {

        model.addAttribute("gedung", new Gedung());
        model.addAttribute("kampus", kampusDao.findByStatus(StatusRecord.AKTIF));


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
            Gedung gedung = gedungDao.findById(id).get();
            if (gedung != null) {
                gedung.setUserEdit(u);
                gedung.setTglEdit(LocalDateTime.now());
                model.addAttribute("gedung", gedung);
            }
        }
        return "/gedung/form";
    }

    @PostMapping(value = "/gedung/form")
    public String uploadBukti(@Valid Gedung gedung,
                              BindingResult error, @RequestParam(required = false) Gedung id,
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

        if (id != null) {
            gedung.setTglEdit(LocalDateTime.now());
            gedung.setUserEdit(u);
            gedung.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            gedung.setUserInsert(u);
            gedung.setTglInsert(LocalDateTime.now());
        }

        gedungDao.save(gedung);


        return "redirect:/gedung/list";

    }

    @PostMapping("/delete/gedung")
    public String delete(@RequestParam Gedung gedung, Authentication currentUser) {

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

        gedung.setStatus(StatusRecord.HAPUS);
        gedung.setUserEdit(u);
        gedung.setTglEdit(LocalDateTime.now());
        gedungDao.save(gedung);

        return "redirect:/gedung/list";
    }


}