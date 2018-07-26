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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
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
    public void  formLembaga(Model model, Authentication currentUser){
        Lembaga lembaga = new Lembaga();
        model.addAttribute("provinsi", provinsiDao.findAll());
        model.addAttribute("kokab", kokabDao.findAll());

        LOGGER.debug("username" + currentUser.getClass().getName());

        if (currentUser == null) {
            LOGGER.warn("Current user is null");
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);

        if (u == null) {
            LOGGER.warn("Username {} not found in database " + username);
        }

        model.addAttribute("user",u);
        model.addAttribute("lembaga",lembaga);

    }

    @PostMapping("/lembaga/form")
    public void uploadBukti(){

    }


}
