package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.GedungDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.RuanganDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Gedung;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Ruangan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
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
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
public class RuangController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuangController.class);

    @Autowired
    RuanganDao ruanganDao;

    @Autowired
    UserDao userDao;

    @Autowired
    GedungDao gedungDao;

    @ModelAttribute("daftarGedung")
    public Iterable<Gedung> daftarGedung() {
        return gedungDao.findByStatus(StatusRecord.AKTIF);
    }

    @GetMapping("/ruang/list")
    public void RuangList(@PageableDefault(direction = Sort.Direction.ASC) Pageable page, Model model, String search) {
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", ruanganDao.findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(StatusRecord.HAPUS, search, page));
        } else {
            model.addAttribute("list", ruanganDao.findByStatusNotIn(StatusRecord.HAPUS, page));

        }
    }

    @GetMapping("/ruang/form")
    public String formFakultas(Model model, Authentication currentUser, @RequestParam(required = false) String id) {

        model.addAttribute("ruangan", new Ruangan());

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
            Ruangan ruangan = ruanganDao.findById(id).get();
            if (ruangan != null) {
                ruangan.setUserEdit(u);
                ruangan.setTglEdit(LocalDateTime.now());
                model.addAttribute("ruangan", ruangan);
            }
        }
        return "/ruang/form";
    }

    @PostMapping(value = "/ruang/form")
    public String uploadBukti(@Valid Ruangan ruangan,
                              BindingResult error, @RequestParam(required = false) Ruangan id,
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

        if (ruangan.getStatus() == null) {
            ruangan.setStatus(StatusRecord.NONAKTIF);
        }
        if (id != null) {
            ruangan.setTglEdit(LocalDateTime.now());
            ruangan.setUserEdit(u);
            ruangan.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            ruangan.setUserInsert(u);
            ruangan.setTglInsert(LocalDateTime.now());
        }

        ruanganDao.save(ruangan);


        return "redirect:/ruang/list";

    }

    @PostMapping(value = "/delete/ruangan")
    public String deleteRuangan(@RequestParam Ruangan ruangan, Authentication currentUser) {
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

        ruangan.setStatus(StatusRecord.HAPUS);
        ruangan.setTglEdit(LocalDateTime.now());
        ruangan.setUserEdit(u);
        ruanganDao.save(ruangan);

        return "redirect:/ruang/list";
    }
}