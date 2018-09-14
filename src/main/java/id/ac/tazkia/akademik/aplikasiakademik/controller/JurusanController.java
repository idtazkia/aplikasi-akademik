package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.FakultasDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.JurusanDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Fakultas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jurusan;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;

import static id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants.Aktif;

@Controller
public class JurusanController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JurusanController.class);

    @Autowired
    JurusanDao jurusanDao;

    @Autowired
    UserDao userDao;

    @Autowired
    FakultasDao fakultasDao;

    @ModelAttribute("daftarFakultas")
    public Iterable<Fakultas> daftarFakultas(){
        return fakultasDao.findByStatus(StatusRecord.AKTIF);
    }

    @GetMapping("/jurusan/list")
    public void list(Model model, @PageableDefault(size = 10) Pageable page, String search){

        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", jurusanDao.findByStatusAndNamaJurusanContainingIgnoreCaseOrderByNamaJurusan(StatusConstants.Aktif, search, page));
        } else {
            model.addAttribute("list",jurusanDao.findByStatus(StatusConstants.Aktif,page));

        }
    }

    @GetMapping("/jurusan/form")
    public String  formJurusan(Model model, Authentication currentUser, @RequestParam(required = false)String id) {

        model.addAttribute("jurusan", new Jurusan());

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
            Jurusan jurusan = jurusanDao.findById(id).get();
            if (jurusan != null) {
                jurusan.setUserEdit(u);
                jurusan.setTglEdit(LocalDateTime.now());
                model.addAttribute("jurusan", jurusan);
            }
        }
        return "/jurusan/form";
    }

    @PostMapping(value = "/jurusan/form")
    public String uploadBukti(@Valid Jurusan jurusan,
                              BindingResult error, @RequestParam(required = false) Jurusan id,
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
            jurusan.setTglEdit(LocalDateTime.now());
            jurusan.setUserEdit(u);
            jurusan.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            jurusan.setUserInsert(u);
            jurusan.setTglInsert(LocalDateTime.now());
        }

        jurusanDao.save(jurusan);


        return "redirect:/jurusan/list";

    }

    @PostMapping("/delete/jurusan")
    public String delete(@RequestParam Jurusan jurusan,
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

        jurusan.setTglEdit(LocalDateTime.now());
        jurusan.setUserEdit(u);
        jurusan.setStatus(StatusConstants.Nonaktif);
        jurusanDao.save(jurusan);

        return "redirect:/jurusan/list";
    }
}
