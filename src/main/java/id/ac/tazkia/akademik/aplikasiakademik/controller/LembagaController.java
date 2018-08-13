package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.KokabDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.LembagaDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProvinsiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Lembaga;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class LembagaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LembagaController.class);

    @Autowired
    LembagaDao lembagaDao;

    @Autowired
    UserDao userDao;

    @Autowired
    ProvinsiDao provinsiDao;

    @Autowired
    KokabDao kokabDao;

    @Value("${upload.folder}")
    private String uploadFolder;


    @GetMapping("/lembaga/list")
    public ModelMap list(@PageableDefault(direction = Sort.Direction.ASC) Pageable page){
        return new ModelMap()
                .addAttribute("list",lembagaDao.findByStatus(StatusConstants.Aktif,page));
    }

    @GetMapping("/lembaga/form")
    public String  formlembaga(Model model,Authentication currentUser,
                               @RequestParam(required = false)String id){

        model.addAttribute("lembaga", new Lembaga());
        model.addAttribute("provinsi", provinsiDao.findAll());
        model.addAttribute("kokab", kokabDao.findAll());

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
            Lembaga lembaga = lembagaDao.findById(id).get();
            if (lembaga != null) {
                lembaga.setUserEdit(u);
                lembaga.setTglEdit(LocalDateTime.now());
                model.addAttribute("lembaga", lembaga);
            }
        }
        return "/lembaga/form";
    }

    @PostMapping("/lembaga/form")
    public String uploadBukti(@Valid Lembaga lembaga,
                              BindingResult error, @RequestParam(required = false) Lembaga id, MultipartFile logo,
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


        String namaFile = logo.getName();
        String jenisFile = logo.getContentType();
        String namaAsli = logo.getOriginalFilename();
        Long ukuran = logo.getSize();

        LOGGER.debug("Nama File : {}", namaFile);
        LOGGER.debug("Jenis File : {}", jenisFile);
        LOGGER.debug("Nama Asli File : {}", namaAsli);
        LOGGER.debug("Ukuran File : {}", ukuran);
        System.out.println(jenisFile);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder;
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        logo.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());


        lembaga.setLogo(idFile + "." + extension);

        if (id != null){
            lembaga.setTglEdit(LocalDateTime.now());
            lembaga.setUserEdit(u);
            lembaga.setTglInsert(id.getTglInsert());
        }

        if (id == null) {
            lembaga.setUserInsert(u);
            lembaga.setTglInsert(LocalDateTime.now());
        }


        lembagaDao.save(lembaga);


        return "redirect:/lembaga/list";

    }

    @PostMapping("/delete/lembaga")
    public String delete(@RequestParam Lembaga lembaga){

        lembaga.setStatus(StatusConstants.Nonaktif);
        lembagaDao.save(lembaga);

        return "redirect:/lembaga/list";
    }


}
