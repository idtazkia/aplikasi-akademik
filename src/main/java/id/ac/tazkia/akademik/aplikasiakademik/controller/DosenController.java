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
public class DosenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DosenController.class);

    @Autowired
    private UserDao userDao;
    @Autowired
    private DosenDao dosenDao;
    @Autowired
    private ProvinsiDao provinsiDao;
    @Autowired
    private KokabDao kokabDao;
    @Autowired
    private TempatTinggalDao tempatTinggalDao;
    @Autowired
    private AgamaDao agamaDao;
    @Autowired
    private ProdiDao prodiDao;
    @Autowired
    private DosenProdiDao dosenProdiDao;


    @ModelAttribute("daftarProp")
    public Iterable<Provinsi> daftaProp() {
        return provinsiDao.findAll();
    }

    @ModelAttribute("daftarKokab")
    public Iterable<KabupatenKota> daftaKokab() {
        return kokabDao.findAll();
    }

    @ModelAttribute("daftarTempat")
    public Iterable<TempatTinggal> daftarTempat() {
        return tempatTinggalDao.findAll();
    }

    @ModelAttribute("daftarAgama")
    public Iterable<Agama> daftaAgama() {
        return agamaDao.findAll();
    }

    @ModelAttribute("daftarProdi")
    public Iterable<Prodi> daftaProdi() {
        return prodiDao.findByStatus(StatusRecord.AKTIF);
    }


    @GetMapping("/dosen/list")
    public String gedungList(@RequestParam(required = false) String search, Model m, Pageable page) {
        if (StringUtils.hasText(search)) {
            m.addAttribute("search", search);
            m.addAttribute("listDosen", dosenDao.findByStatusAndNamaContainingIgnoreCaseOrderByNama( StatusConstants.Aktif,search, page));
        } else {
            m.addAttribute("listDosen", dosenDao.findByStatus(StatusConstants.Aktif, page));
        }
        return "dosen/list";

    }

    @GetMapping("/dosen/form")
    public String  formDosen(Model model, Authentication currentUser, @RequestParam(required = false)String id){

        model.addAttribute("dosen", new Dosen());

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
            Dosen dosen = dosenDao.findById(id).get();
            if (dosen != null) {
                dosen.setUserEdit(u);
                dosen.setTglEdit(LocalDateTime.now());
                model.addAttribute("dosen", dosen);
            }
        }
        return "/dosen/form";
    }

    @PostMapping(value = "/dosen/form")
    public String prosesDosen(@Valid Dosen dosen,
                         BindingResult error,@RequestParam(required = false) Dosen id,
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
            dosen.setTglEdit(LocalDateTime.now());
            dosen.setUserEdit(u);
            dosen.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            dosen.setUserInsert(u);
            dosen.setTglInsert(LocalDateTime.now());
        }

        dosenDao.save(dosen);

        return "redirect:/dosen/list";

    }

    @PostMapping("/delete/dosen")
    public String delete(@RequestParam Dosen dosen,Authentication currentUser){

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

        dosen.setStatus(StatusConstants.Nonaktif);
        dosen.setUserEdit(u);
        dosen.setTglEdit(LocalDateTime.now());
        dosenDao.save(dosen);

        return "redirect:/dosen/list";
    }

    @GetMapping("/dosen/jurusan/list")
    public void  listJurusanDosen(@PageableDefault(direction = Sort.Direction.ASC) Pageable page,
                                  @RequestParam(value = "id", required = true) String id,
                                  @RequestParam(required = false) String error,
                                  Model m){
        Dosen d = dosenDao.findById(id).get();

        m.addAttribute("listDosenProdi",dosenProdiDao.findByIdDosenAndStatus(d, StatusConstants.Aktif, page));


        m.addAttribute("dosen", d);
        if (d != null){
            LOGGER.debug("Dosen :" + d.getNama());
        }


    }

    @PostMapping("/dosen/jurusan/list")
    public String prosesJurusanDosen(@Valid DosenProdi dosenProdi, Authentication currentUser){

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

            dosenProdi.setUserInsert(u);
            dosenProdi.setTglInsert(LocalDateTime.now());

        dosenProdiDao.save(dosenProdi);

        return "redirect:/dosen/jurusan/list?id="+dosenProdi.getIdDosen().getIdDosen();
    }

    @PostMapping("/delete/jurusan/dosenProdi")
    public String deleteJurusanDosen(@RequestParam DosenProdi dosenProdi,Authentication currentUser){

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

        dosenProdi.setStatus(StatusConstants.Nonaktif);
        dosenProdi.setUserEdit(u);
        dosenProdi.setTglEdit(LocalDateTime.now());
        dosenProdiDao.save(dosenProdi);

        return "redirect:/dosen/jurusan/list?id="+dosenProdi.getIdDosen().getIdDosen();
    }
}
